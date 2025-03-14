package no.nav.aap.proxy.kafka

import no.nav.aap.komponenter.json.DefaultJsonMapper
import no.nav.aap.proxy.hendelseAvgitt
import no.nav.aap.proxy.prometheus
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import java.time.format.DateTimeFormatter

private val logger = LoggerFactory.getLogger(HendelseApiKafkaProducer::class.java)

interface HendelseProducer : KafkaProducer, AutoCloseable {
    override fun produce(input: HendelseInput)

    override fun close()
}

class HendelseApiKafkaProducer(config: KafkaConfig, private val topic: String) : HendelseProducer {
    private val producer = KafkaFactory.createProducer("arena-hendelse-api-proxy", config)
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    override fun produce(input: HendelseInput) {
        val record = createRecord(input)
        producer.send(record) { metadata, err ->
            if (err != null) {
                logger.error("Fikk ikke varslet hendelse for ${input.identifikator}.", err)
                prometheus.hendelseAvgitt(sendStatus = "feilet").increment()
                throw KafkaProducerException("Fikk ikke varslet hendelse for ${input.identifikator}")
            } else {
                prometheus.hendelseAvgitt(sendStatus = "sendt").increment()
                logger.debug("Varslet hendelse for {}: {}", input.identifikator, metadata)
            }
        }.get() // Blocking call to ensure the message is sent
    }

    private fun createRecord(input: HendelseInput): ProducerRecord<String, String> {
        val samHendelse = SamHendelse(
            tpNr = input.tpNr,
            identifikator = input.identifikator,
            vedtakId = input.vedtakId,
            fom = input.fom.format(dateFormatter),
            tom = input.tom?.format(dateFormatter)
        )
        val jsonSomString = DefaultJsonMapper.toJson(samHendelse)

        return ProducerRecord(topic, input.identifikator, jsonSomString)
    }

    override fun close() = producer.close()
}
