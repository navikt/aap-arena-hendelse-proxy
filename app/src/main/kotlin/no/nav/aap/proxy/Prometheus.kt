package no.nav.aap.proxy

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry

val prometheus = PrometheusMeterRegistry(
    PrometheusConfig.DEFAULT
)

fun MeterRegistry.hendelseAvgitt(sendStatus: String): Counter = this.counter(
    "aap_hendelse_proxy_hendelse_avgitt_total", listOf(Tag.of("sendStatus", sendStatus))
)