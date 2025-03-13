package no.nav.aap.proxy.kafka

interface KafkaProducer: AutoCloseable {
    fun produce(input: HendelseInput)
}

class SamHendelse(
    val tpNr: String,
    val ytelsesType: String = "AAP",
    val identifikator: String,
    val vedtakId: String,
    var samId: String? = null,
    val fom: String,
    var tom: String? = null
)

class KafkaProducerException(msg: String): RuntimeException(msg)