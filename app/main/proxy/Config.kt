package proxy

import libs.kafka.KafkaConfig

private fun getEnvVar(envar: String) = System.getenv(envar) ?: error("missing envvar $envar")

data class AzureConfig(
    val tokenEndpoint: String = getEnvVar("AZURE_OPENID_CONFIG_TOKEN_ENDPOINT"),
    val clientId: String = getEnvVar("AZURE_APP_CLIENT_ID"),
    val clientSecret: String = getEnvVar("AZURE_APP_CLIENT_SECRET"),
    val jwksUri: String = getEnvVar("AZURE_OPENID_CONFIG_JWKS_URI"),
    val issuer: String = getEnvVar("AZURE_OPENID_CONFIG_ISSUER")
)


data class Config(
    val azure: AzureConfig = AzureConfig(),
    val kafka: KafkaConfig = KafkaConfig(
        brokers = getEnvVar("KAFKA_BROKERS"),
        truststorePath = getEnvVar("KAFKA_TRUSTSTORE_PATH"),
        keystorePath = getEnvVar("KAFKA_KEYSTORE_PATH"),
        credstorePsw = getEnvVar("KAFKA_CREDSTORE_PASSWORD"),
    ),
)