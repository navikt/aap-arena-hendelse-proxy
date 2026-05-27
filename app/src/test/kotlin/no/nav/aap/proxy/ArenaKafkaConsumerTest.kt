package no.nav.aap.proxy.kafka

import org.apache.kafka.clients.consumer.MockConsumer
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ArenaKafkaConsumerTest {

    private val published = mutableListOf<AapHendelseRecord>()
    private val mockProducer = object : InternHendelseProducer {
        override fun produce(record: AapHendelseRecord) { published.add(record) }
        override fun close() {}
    }
    private val consumer = ArenaKafkaConsumer(
        arenaVedtakTopic = "test-topic",
        internHendelseProducer = mockProducer,
        consumer = MockConsumer(OffsetResetStrategy.EARLIEST),
    )

    @Test
    fun `INSERT produserer hendelse med riktig ident`() {
        val record = ArenaVedtakRecord(
            opType = "I",
            before = null,
            after = AapVedtakData(personident = "12345678901", vedtakId = 42),
        )
        val result = consumer.mapToHendelse(record)
        assertThat(result).isEqualTo(AapHendelseRecord(ident = "12345678901", hendelse = Hendelse.VEDTAK))
    }

    @Test
    fun `UPDATE bruker after-feltet for ident`() {
        val record = ArenaVedtakRecord(
            opType = "U",
            before = AapVedtakData(personident = "00000000000", vedtakId = 1),
            after = AapVedtakData(personident = "12345678901", vedtakId = 42),
        )
        val result = consumer.mapToHendelse(record)
        assertThat(result).isEqualTo(AapHendelseRecord(ident = "12345678901", hendelse = Hendelse.VEDTAK))
    }

    @Test
    fun `DELETE hoppes over`() {
        val record = ArenaVedtakRecord(
            opType = "D",
            before = AapVedtakData(personident = "12345678901", vedtakId = 42),
            after = null,
        )
        assertThat(consumer.mapToHendelse(record)).isNull()
    }

    @Test
    fun `manglende after hoppes over`() {
        val record = ArenaVedtakRecord(
            opType = "U",
            before = AapVedtakData(personident = "12345678901", vedtakId = 42),
            after = null,
        )
        assertThat(consumer.mapToHendelse(record)).isNull()
    }
}
