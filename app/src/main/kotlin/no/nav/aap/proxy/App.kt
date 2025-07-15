package no.nav.aap.proxy

import com.papsign.ktor.openapigen.model.info.InfoModel
import com.papsign.ktor.openapigen.route.apiRouting
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import no.nav.aap.komponenter.server.AZURE
import no.nav.aap.komponenter.server.commonKtorModule
import no.nav.aap.proxy.hendelse.hendelse
import no.nav.aap.proxy.kafka.HendelseApiKafkaProducer
import no.nav.aap.proxy.kafka.HendelseProducer
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
    embeddedServer(Netty, port = 8080) {
        server(
            config,
            HendelseApiKafkaProducer(config.kafka, config.topicConfig),
        )
    }.start(wait = true)
}

fun Application.server(
    config: Config,
    hendelseProducer: HendelseProducer
) {
    commonKtorModule(
        prometheus = prometheus,
        azureConfig = config.azure,
        infoModel = InfoModel(
            title = "AAP Arena HendelseProxy",
        ),
        tokenxConfig = null,
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

    monitor.subscribe(ApplicationStopping) {
        runBlocking {
            delay(50)
        }
        hendelseProducer.close()
    }

    routing {
        authenticate(AZURE) {
            apiRouting {
                hendelse(hendelseProducer)
            }
        }
        actuator(prometheus = prometheus)
    }
}
