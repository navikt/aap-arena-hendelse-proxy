package aap.arena.hendelse.kafka

import aap.arena.hendelse.proxy.logger
import libs.kafka.KafkaConfig
import libs.kafka.KafkaFactory
import org.apache.kafka.clients.producer.ProducerRecord
import proxy.kafka.hendelseInput
import java.time.LocalDateTime

class MinSideKafkaProducer(config: KafkaConfig) : KafkaProducer, AutoCloseable {
    private val producer = KafkaFactory.createProducer("arena-hendelse-api-proxy", config)
    private val topic = ""

    override fun produce(input: hendelseInput) {
        val record = createRecord(input)
        producer.send(record) { metadata, err ->
            if (err != null) {
                logger.error("Fikk ikke varslet hendelse for ${input.identifikator}", err)
                throw KafkaProducerException("Fikk ikke varslet hendelse for $${input.identifikator}")
            } else {
                logger.debug("Varslet hendelse for $personident: $metadata")
            }
        }.get() // Blocking call to ensure the message is sent
    }

    private fun createRecord(input: hendelseInput): ProducerRecord<String, String> {
        val json = SamHendelse(
            tpNr = input.tpNr,
            ytelsesType = input.ytelsesType,
            identifikator= input.identifikator,
            vedtakId = input.vedtakId,
            fom = LocalDateTime.now().toString(),
        )

        return ProducerRecord(topic, personident, json)
    }

    override fun close() = producer.close()
}
