package proxy.kafka

data class HendelseInput(
    val tpNr: String,
    val identifikator: String,
    val vedtakId: String,
    val ytelsesType: String = "AAP"
)