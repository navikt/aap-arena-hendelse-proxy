package proxy

import com.papsign.ktor.openapigen.model.info.InfoModel
import com.papsign.ktor.openapigen.route.apiRouting
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import no.nav.aap.komponenter.server.commonKtorModule
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import proxy.hendelse.hendelse
import proxy.kafka.HendelseApiProducer

val logger: Logger = LoggerFactory.getLogger("App")

fun main() {
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        logger.error(
            "Uhåndtert feil",
            throwable
        )
    }
    embeddedServer(Netty, port = 8080, module = Application::server).start(wait = true)
}

fun Application.server(
    config: Config = Config(),
    hendelseProducer: HendelseApiProducer = HendelseApiProducer(config.kafka, config.topicConfig),
) {
    val prometheus = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    commonKtorModule(
        prometheus = prometheus,
        azureConfig = null,
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
        apiRouting {
            hendelse(hendelseProducer)
        }
        actuator(prometheus = prometheus)
    }
}