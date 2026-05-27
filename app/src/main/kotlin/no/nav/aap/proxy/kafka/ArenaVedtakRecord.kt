package no.nav.aap.proxy.kafka

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class ArenaVedtakRecord(
    @JsonProperty("op_type") val opType: String,
    val before: AapVedtakData?,
    val after: AapVedtakData?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AapVedtakData(
    @JsonProperty("PERSONIDENT") val personident: String,
    @JsonProperty("VEDTAK_ID") val vedtakId: Long,
)
