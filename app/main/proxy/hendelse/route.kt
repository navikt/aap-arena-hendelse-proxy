package proxy.hendelse

import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.post
import com.papsign.ktor.openapigen.route.route
import io.ktor.http.*
import proxy.kafka.HendelseApiProducer
import proxy.kafka.HendelseInput

fun NormalOpenAPIRoute.hendelse(
    hendelseApiProducer: HendelseApiProducer
) {
    route("/hendelse") {
        post<Unit, String, HendelseInput> { _, input ->

            hendelseApiProducer.produce(input)

            responder.respond(
                HttpStatusCode.Accepted, "{}", pipeline
            )
        }
    }

}