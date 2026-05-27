package no.nav.aap.proxy.kafka

import no.nav.aap.komponenter.json.DefaultJsonMapper
import org.apache.kafka.clients.consumer.Consumer
import org.slf4j.LoggerFactory
import java.time.Duration

private val logger = LoggerFactory.getLogger(ArenaKafkaConsumer::class.java)

class ArenaKafkaConsumer(
    config: KafkaConfig,
    private val arenaVedtakTopic: String,
    private val internHendelseProducer: AapInternHendelseProducer,
) : AutoCloseable {
    private val consumer: Consumer<String, String> =
        KafkaFactory.createConsumer("aap-arena-vedtak-consumer", config)

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
                    if (arenaRecord.opType == "D") continue
                    val vedtakData = arenaRecord.after ?: continue
                    val hendelseRecord = AapHendelseRecord(
                        ident = vedtakData.personident,
                        hendelse = Hendelse.VEDTAK,
                    )
                    internHendelseProducer.produce(hendelseRecord)
                } catch (e: Exception) {
                    logger.error("Feil ved prosessering av arena-vedtak-record offset={}", record.offset(), e)
                }
            }
            if (!records.isEmpty) {
                consumer.commitSync()
            }
        }
    }

    override fun close() {
        running = false
        consumer.wakeup()
    }
}
