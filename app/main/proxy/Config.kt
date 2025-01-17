package proxy

import libs.kafka.KafkaConfig
import no.nav.aap.komponenter.httpklient.httpclient.tokenprovider.azurecc.AzureConfig

private fun getEnvVar(envar: String) = System.getenv(envar) ?: error("missing envvar $envar")


data class Config(
    val azure: AzureConfig = AzureConfig(),
    val kafka: KafkaConfig = KafkaConfig(
        brokers = getEnvVar("KAFKA_BROKERS"),
        truststorePath = getEnvVar("KAFKA_TRUSTSTORE_PATH"),
        keystorePath = getEnvVar("KAFKA_KEYSTORE_PATH"),
        credstorePsw = getEnvVar("KAFKA_CREDSTORE_PASSWORD"),
    ),
    val topicConfig: String = getEnvVar("HENDELSE_TOPIC"),
)