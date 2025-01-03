package proxy.kafka

import libs.kafka.KafkaConfig
import libs.kafka.KafkaFactory
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

private val logger = LoggerFactory.getLogger(HendelseApiProducer::class.java)

class HendelseApiProducer(config: KafkaConfig, private val topic: String) : KafkaProducer, AutoCloseable {
    private val producer = KafkaFactory.createProducer("arena-hendelse-api-proxy", config)

    override fun produce(input: HendelseInput) {
        val record = createRecord(input)
        producer.send(record) { metadata, err ->
            if (err != null) {
                logger.error("Fikk ikke varslet hendelse for ${input.identifikator}", err)
                throw KafkaProducerException("Fikk ikke varslet hendelse for $${input.identifikator}")
            } else {
                logger.debug("Varslet hendelse for \${}: {}", input.identifikator, metadata)
            }
        }.get() // Blocking call to ensure the message is sent
    }

    private fun createRecord(input: HendelseInput): ProducerRecord<String, String> {
        val json = SamHendelse(
            tpNr = input.tpNr,
            ytelsesType = input.ytelsesType,
            identifikator= input.identifikator,
            vedtakId = input.vedtakId,
            fom = LocalDateTime.now().toString(),
        ).toString()

        return ProducerRecord(topic, input.identifikator, json)
    }

    override fun close() = producer.close()
}
