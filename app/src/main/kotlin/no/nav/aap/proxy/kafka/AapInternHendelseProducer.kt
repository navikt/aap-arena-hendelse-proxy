package no.nav.aap.proxy.kafka

import no.nav.aap.komponenter.json.DefaultJsonMapper
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger(AapInternHendelseProducer::class.java)

interface InternHendelseProducer : AutoCloseable {
    fun produce(record: AapHendelseRecord)
}

class AapInternHendelseProducer(config: KafkaConfig, private val topic: String) : InternHendelseProducer {
    private val producer = KafkaFactory.createProducer("aap-intern-hendelse-producer", config)

    override fun produce(record: AapHendelseRecord) {
        val json = DefaultJsonMapper.toJson(record)
        producer.send(ProducerRecord(topic, record.ident, json)) { metadata, err ->
            if (err != null) {
                logger.error("Feil ved publisering av intern hendelse for ${record.ident}", err)
                throw KafkaProducerException("Feil ved publisering av intern hendelse for ${record.ident}")
            } else {
                logger.debug("Publiserte intern hendelse for {}: {}", record.ident, metadata)
            }
        }.get()
    }

    override fun close() = producer.close()
}
