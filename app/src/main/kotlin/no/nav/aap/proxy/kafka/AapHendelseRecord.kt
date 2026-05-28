package no.nav.aap.proxy.kafka

data class AapHendelseRecord(
    val ident: String,
    val hendelse: Hendelse,
)

enum class Hendelse {
    VEDTAK,
    SOKNAD,
}
