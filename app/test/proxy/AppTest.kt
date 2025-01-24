package proxy

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Assertions.assertEquals
import proxy.kafka.HendelseInput
import proxy.kafka.HendelseProducer
import kotlin.test.Test

class AppTest {
    companion object {
        init {
            System.setProperty("KAFKA_HOST", "localhost:9092")
            System.setProperty("azure.openid.config.token.endpoint", "http://localhost:9098/token")
            System.setProperty("azure.app.client.id", "default")
            System.setProperty("azure.app.client.secret", "xxxx")
            System.setProperty("azure.openid.config.jwks.uri", "http://localhost:9098/jwks")
            System.setProperty("azure.openid.config.issuer", "default")
            System.setProperty("kafka.brokers", "...")
            System.setProperty("kafka.truststore.path", "...")
            System.setProperty("kafka.keystore.path", "...")
            System.setProperty("kafka.credstore.password", "...")
            System.setProperty("hendelse.topic", "...")
        }
    }

    @Test
    fun serverSwagger() = testApplication {
        application {
            server(Config(), object : HendelseProducer {
                override fun produce(input: HendelseInput) {
                    TODO("Not yet implemented")
                }

                override fun close() {
                    TODO("Not yet implemented")
                }
            })
        }
        val response = client.get("/openapi.json")
        assertEquals(HttpStatusCode.OK, response.status)
    }
}
