package proxy

import no.nav.aap.komponenter.config.requiredConfigForKey
import no.nav.aap.komponenter.httpklient.httpclient.tokenprovider.azurecc.AzureConfig
import proxy.kafka.KafkaConfig

data class Config(
    val azure: AzureConfig = AzureConfig(),
    val kafka: KafkaConfig = KafkaConfig(
        brokers = requiredConfigForKey("kafka.brokers"),
        truststorePath = requiredConfigForKey("kafka.truststore.path"),
        keystorePath = requiredConfigForKey("kafka.keystore.path"),
        credstorePsw = requiredConfigForKey("kafka.credstore.password"),
    ),
    val topicConfig: String = requiredConfigForKey("hendelse.topic"),
)