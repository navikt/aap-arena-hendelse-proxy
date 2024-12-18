package aap.arena.hendelse.kafka

import proxy.kafka.HendelseInput

interface KafkaProducer: AutoCloseable {
    fun produce(input: HendelseInput)
}

class SamHendelse(
    val tpNr: String,
    val ytelsesType: String,
    val identifikator: String,
    val vedtakId: String,
    var samId: String? = null,
    val fom: String,
    var tom: String? = null
)

class KafkaProducerException(msg: String): RuntimeException(msg)