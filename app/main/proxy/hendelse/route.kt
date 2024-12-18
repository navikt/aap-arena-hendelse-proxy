package proxy.hendelse

import aap.arena.hendelse.kafka.HendelseApiProducer
import io.ktor.server.request.*
import io.ktor.server.routing.*
import proxy.kafka.HendelseInput

fun Route.hendelse(hendelseApiProducer: HendelseApiProducer){
    post("/hendelse") {
        val input = call.receive<HendelseInput>()
        hendelseApiProducer.produce(input)
    }
}