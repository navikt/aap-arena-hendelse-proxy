package no.nav.aap.proxy

import com.papsign.ktor.openapigen.model.info.InfoModel
import com.papsign.ktor.openapigen.route.apiRouting
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStarted
import io.ktor.server.application.ApplicationStopping
import io.ktor.server.application.install
import io.ktor.server.auth.authenticate
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.routing.routing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import no.nav.aap.komponenter.server.auth.IdentityProvider
import no.nav.aap.komponenter.server.commonKtorModule
import no.nav.aap.proxy.hendelse.hendelse
import no.nav.aap.proxy.kafka.AapInternHendelseProducer
import no.nav.aap.proxy.kafka.ArenaKafkaConsumer
import no.nav.aap.proxy.kafka.HendelseApiKafkaProducer
import no.nav.aap.proxy.kafka.HendelseProducer
import no.nav.aap.proxy.kafka.InternHendelseProducer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val logger: Logger = LoggerFactory.getLogger("App")

fun main() {
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        logger.error(
            "Uhåndtert feil av type $throwable.class i thread ${thread.name}",
            throwable
        )
    }
    val config = Config()
    val internHendelseProducer = AapInternHendelseProducer(config.kafka, config.internHendelseTopic)
    val arenaKafkaConsumer = ArenaKafkaConsumer(config.kafka, config.arenaVedtakTopic, internHendelseProducer)
    embeddedServer(Netty, port = 8080) {
        server(
            HendelseApiKafkaProducer(config.kafka, config.topicConfig),
            arenaKafkaConsumer,
            internHendelseProducer,
        )
    }.start(wait = true)
}

fun Application.server(
    hendelseProducer: HendelseProducer,
    arenaKafkaConsumer: ArenaKafkaConsumer? = null,
    internHendelseProducer: InternHendelseProducer? = null,
) {
    commonKtorModule(
        prometheus = prometheus,
        infoModel = InfoModel(
            title = "AAP Arena HendelseProxy",
        ),
        identityProvider = IdentityProvider.ENTRA_ID
    )

    install(StatusPages) {
        data class Error(val message: String)
        exception<Throwable> { call, cause ->
            logger.error("Uhåndtert feil ved kall til '{}'", call.request.local.uri, cause)
            call.respond(
                message = Error("Feil i tjeneste: ${cause.message}"),
                status = HttpStatusCode.InternalServerError
            )
        }
    }

    monitor.subscribe(ApplicationStarted) {
        arenaKafkaConsumer?.let { consumer ->
            launch(Dispatchers.IO) {
                consumer.start()
            }
        }
    }

    monitor.subscribe(ApplicationStopping) {
        runBlocking {
            delay(50)
        }
        arenaKafkaConsumer?.close()
        internHendelseProducer?.close()
        hendelseProducer.close()
    }

    routing {
        authenticate(IdentityProvider.ENTRA_ID.value) {
            apiRouting {
                hendelse(hendelseProducer)
            }
        }
        actuator(prometheus = prometheus)
    }
}
