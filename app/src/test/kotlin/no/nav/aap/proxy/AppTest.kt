package no.nav.aap.proxy

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.testing.*
import no.nav.aap.proxy.kafka.HendelseInput
import no.nav.aap.proxy.kafka.HendelseInputFlereTpNr
import no.nav.aap.proxy.kafka.HendelseProducer
import no.nav.security.mock.oauth2.MockOAuth2Server
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.time.LocalDate

class AppTest {
    companion object {
        private val server = MockOAuth2Server()

        init {
            System.setProperty("KAFKA_HOST", "localhost:9092")
            System.setProperty("kafka.brokers", "...")
            System.setProperty("kafka.truststore.path", "...")
            System.setProperty("kafka.keystore.path", "...")
            System.setProperty("kafka.credstore.password", "...")
            System.setProperty("hendelse.topic", "...")
        }

        @BeforeAll
        @JvmStatic
        fun setup() {
            server.start()

            val wellnowurl = server.wellKnownUrl("default").toString()
            val jwksuri = server.jwksUrl("default").toString()

            System.setProperty(
                "azure.openid.config.token.endpoint",
                server.tokenEndpointUrl("default").toString()
            )
            System.setProperty("azure.app.client.id", "default")
            System.setProperty("azure.app.client.secret", "xxxx")
            System.setProperty("azure.openid.config.jwks.uri", jwksuri)
            System.setProperty("azure.openid.config.issuer", server.issuerUrl("default").toString())
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            server.shutdown()
        }
    }

    @Test
    fun senderTilKafka() = testApplication {
        val received = mutableListOf<HendelseInput>()
        application {
            server(Config(), object : HendelseProducer {
                override fun produce(input: HendelseInput) {
                    received.add(input)
                }

                override fun close() {
                }
            })
        }
        val client = createClient {
            install(ContentNegotiation) {
                jackson {
                    registerModule(JavaTimeModule())
                    disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                }
            }
        }
        val response = client.post("/hendelse") {
            contentType(ContentType.Application.Json)
            bearerAuth(issueToken().serialize())
            setBody(
                HendelseInputFlereTpNr(
                    tpNr = listOf("1234", "34565"),
                    identifikator = "123459999",
                    vedtakId = "12321",
                    fom = LocalDate.now().minusWeeks(4),
                    tom = LocalDate.now()
                )
            )
        }
        assertEquals(HttpStatusCode.Accepted, response.status)
        assertThat(received.size).isEqualTo(2)
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


    private fun issueToken() = server.issueToken(
        issuerId = "default",
        claims = mapOf(
//            "scope" to scope,
            "consumer" to mapOf("authority" to "123")
        ),
    )
}
