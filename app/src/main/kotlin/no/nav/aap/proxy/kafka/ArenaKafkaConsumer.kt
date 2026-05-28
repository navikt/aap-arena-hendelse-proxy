package no.nav.aap.proxy.kafka

import no.nav.aap.komponenter.json.DefaultJsonMapper
import org.apache.kafka.clients.consumer.Consumer
import org.slf4j.LoggerFactory
import java.time.Duration

private val logger = LoggerFactory.getLogger(ArenaKafkaConsumer::class.java)

class ArenaKafkaConsumer(
    private val arenaVedtakTopic: String,
    private val internHendelseProducer: InternHendelseProducer,
    private val consumer: Consumer<String, String>,
) : AutoCloseable {
    constructor(
        config: KafkaConfig,
        arenaVedtakTopic: String,
        internHendelseProducer: InternHendelseProducer,
    ) : this(
        arenaVedtakTopic,
        internHendelseProducer,
        KafkaFactory.createConsumer("aap-arena-vedtak-consumer", config),
    )

    @Volatile
    private var running = true

    fun start() {
        consumer.subscribe(listOf(arenaVedtakTopic))
        logger.info("Starter konsumering fra {}", arenaVedtakTopic)
        while (running) {
            val records = consumer.poll(Duration.ofSeconds(5))
            for (record in records) {
                try {
                    val arenaRecord = DefaultJsonMapper.fromJson<ArenaVedtakRecord>(record.value())
                    mapToHendelse(arenaRecord)?.let { internHendelseProducer.produce(it) }
                } catch (e: Exception) {
                    logger.error("Feil ved prosessering av arena-vedtak-record offset={}", record.offset(), e)
                }
            }
            if (!records.isEmpty) {
                consumer.commitSync()
            }
        }
    }

    internal fun mapToHendelse(record: ArenaVedtakRecord): AapHendelseRecord? {
        if (record.opType == "D") return null
        val vedtakData = record.after ?: return null
        return AapHendelseRecord(
            ident = vedtakData.personident,
            hendelse = Hendelse.VEDTAK,
        )
    }

    override fun close() {
        running = false
        consumer.wakeup()
    }
}
