package no.nav.aap.proxy.kafka

import io.ktor.server.testing.*
import no.nav.aap.proxy.Config
import no.nav.aap.proxy.hendelseAvgitt
import no.nav.aap.proxy.prometheus
import no.nav.aap.proxy.server
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.kafka.KafkaContainer
import org.testcontainers.utility.DockerImageName
import java.time.Duration
import java.time.LocalDate
import java.util.*

@Testcontainers
class KafkaProducerTest {

    companion object {
        @Container
        val kafka = KafkaContainer(DockerImageName.parse("apache/kafka-native:3.8.0"))
            .withReuse(true)
    }

    private lateinit var consumer: KafkaConsumer<String, String>
    private val testTopic = "test-hendelse-topic"

    @BeforeEach
    fun setup() {
        kafka.start()

        // Configure consumer
        val consumerProps = Properties().apply {
            this[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = kafka.bootstrapServers
            this[ConsumerConfig.GROUP_ID_CONFIG] = "test-consumer-group"
            this[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
            this[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
            this[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
        }

        consumer = KafkaConsumer(consumerProps)
        consumer.subscribe(listOf(testTopic))
    }

    @AfterEach
    fun tearDown() {
        consumer.close()
    }

    @Test
    fun `test real kafka producer sends message and increments metric`() = testApplication {
        // Create a KafkaConfig that uses the test container
        val testKafkaConfig = KafkaConfig(
            brokers = kafka.bootstrapServers,
            truststorePath = "",  // Not needed for test container
            keystorePath = "",    // Not needed for test container
            credstorePsw = "",    // Not needed for test container
            useSSL = false        // Disable SSL for test container
        )

        // Create the real producer with the test config
        val producer = HendelseApiKafkaProducer(testKafkaConfig, testTopic)

        // Create test data
        val testInput = HendelseInput(
            tpNr = "1234",
            identifikator = "12345678910",
            vedtakId = "test-vedtak-id",
            fom = LocalDate.now().minusWeeks(1),
            tom = LocalDate.now()
        )

        // Send message using the real producer
        producer.produce(testInput)

        // Poll for the message
        val records = consumer.poll(Duration.ofSeconds(5))

        // Verify that the message was received
        assertThat(records.count()).isEqualTo(1)

        val record = records.iterator().next()
        assertThat(record.key()).isEqualTo(testInput.identifikator)
        assertThat(record.value()).contains(testInput.identifikator)
        assertThat(record.value()).contains(testInput.tpNr)
        assertThat(record.value()).contains(testInput.vedtakId)

        // Verify that the metric was incremented
        val metricValue = prometheus.hendelseAvgitt(sendStatus = "sendt").count()
        assertThat(metricValue).isEqualTo(1.0)
    }
}
