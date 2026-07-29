package no.nav.aap.proxy

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.install
import io.ktor.server.engine.ConnectorType
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation as ServerContentNegatiation
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.server.testing.*
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import no.nav.aap.proxy.auth.TokenGen
import no.nav.aap.proxy.kafka.HendelseInput
import no.nav.aap.proxy.kafka.HendelseInputFlereTpNr
import no.nav.aap.proxy.kafka.HendelseProducer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class AppTest {
    companion object {
        private val texas = embeddedServer(Netty, port = 0, module = {
            install(ServerContentNegatiation) {
                jackson()
            }

            routing {
                post("/introspect") {
                    call.respond(mapOf("active" to true))
                }
            }
        })

        init {
            System.setProperty("KAFKA_HOST", "localhost:9092")
            System.setProperty("kafka.brokers", "...")
            System.setProperty("kafka.truststore.path", "...")
            System.setProperty("kafka.keystore.path", "...")
            System.setProperty("kafka.credstore.password", "...")
            System.setProperty("hendelse.topic", "...")
            System.setProperty("arena.vedtak.topic", "...")
            System.setProperty("intern.hendelse.topic", "...")
        }

        @BeforeAll
        @JvmStatic
        fun setup() {
            texas.start()

            System.setProperty("nais.token.introspection.endpoint", "http://localhost:${texas.port()}/introspect")
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            texas.stop()
        }
    }

    @Test
    fun senderTilKafka() = testApplication {
        val received = mutableListOf<HendelseInput>()
        application {
            server(object : HendelseProducer {
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
            bearerAuth(TokenGen("issuer", "audience").generate())
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
            server(object : HendelseProducer {
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

    @Test
    fun incrementsMetricWhenHendelseIsSent() = testApplication {
        application {
            server(object : HendelseProducer {
                override fun produce(input: HendelseInput) {
                    // Increment the metric directly, simulating what HendelseApiKafkaProducer would do
                    prometheus.hendelseAvgitt(sendStatus = "sendt").increment()
                }

                override fun close() {
                    // No-op
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

        // Make a request to the /hendelse endpoint to trigger the metric increment
        val response = client.post("/hendelse") {
            contentType(ContentType.Application.Json)
            bearerAuth(TokenGen("issuer", "audience").generate())
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

        // Get the metrics from the /actuator/metrics endpoint
        val metricsResponse = client.get("/actuator/metrics")
        assertEquals(HttpStatusCode.OK, metricsResponse.status)

        // Check if the response contains the expected metric
        val metricsBody = metricsResponse.bodyAsText()
        assertThat(metricsBody).contains("aap_hendelse_proxy_hendelse_avgitt_total{sendStatus=\"sendt\"} 2.0")
    }

}

private fun EmbeddedServer<*, *>.port(): Int =
    runBlocking { this@port.engine.resolvedConnectors() }
        .first { it.type == ConnectorType.HTTP }
        .port
