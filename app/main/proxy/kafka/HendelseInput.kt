package proxy.kafka

data class HendelseInputFlereTpNr(
    val tpNr: List<String>,
    val identifikator: String,
    val vedtakId: String
)

data class HendelseInput(
    val tpNr: String,
    val identifikator: String,
    val vedtakId: String
)