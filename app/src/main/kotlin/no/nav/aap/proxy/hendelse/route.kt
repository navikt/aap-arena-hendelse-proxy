package no.nav.aap.proxy.hendelse

import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.post
import com.papsign.ktor.openapigen.route.route
import com.papsign.ktor.openapigen.route.status
import io.ktor.http.*
import no.nav.aap.proxy.kafka.HendelseInput
import no.nav.aap.proxy.kafka.HendelseInputFlereTpNr
import no.nav.aap.proxy.kafka.HendelseProducer

fun NormalOpenAPIRoute.hendelse(
    hendelseApiProducer: HendelseProducer
) {
    route("/hendelse").status(202) {
        post<Unit, String, HendelseInputFlereTpNr> { _, input ->

            input.tpNr.forEach { tpNr ->
                hendelseApiProducer.produce(
                    HendelseInput(
                        tpNr,
                        input.identifikator,
                        input.vedtakId,
                        input.fom,
                        input.tom,
                    )
                )
            }
            responder.respond(
                HttpStatusCode.Accepted, "{}", pipeline
            )
        }
    }

}