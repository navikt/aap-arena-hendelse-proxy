package proxy.hendelse

import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.post
import com.papsign.ktor.openapigen.route.route
import io.ktor.http.*
import proxy.kafka.HendelseApiProducer
import proxy.kafka.HendelseInput
import proxy.kafka.HendelseInputFlereTpNr

fun NormalOpenAPIRoute.hendelse(
    hendelseApiProducer: HendelseApiProducer
) {
    route("/hendelse") {
        post<Unit, String, HendelseInputFlereTpNr> { _, input ->

            input.tpNr.forEach {tpNr ->
                hendelseApiProducer.produce(HendelseInput(tpNr, input.identifikator, input.vedtakId))
            }
            responder.respond(
                HttpStatusCode.Accepted, "{}", pipeline
            )
        }
    }

}