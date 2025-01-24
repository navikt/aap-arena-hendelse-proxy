package proxy.kafka

import com.papsign.ktor.openapigen.annotations.properties.description.Description
import java.time.LocalDate

data class HendelseInputFlereTpNr(
    @Description("Liste av TP-nummer.")
    val tpNr: List<String>,
    @Description("Fødselsnummer.")
    val identifikator: String,
    @Description("Vedtak-ID.")
    val vedtakId: String,
    @Description("Fra-dato for vedtakets virkningsperiode.")
    val fom: LocalDate,
    @Description("Til-dato for vedtakets virkningsperiode. Null om vedtaket fortsatt er aktivt.")
    val tom: LocalDate? = null
)

data class HendelseInput(
    val tpNr: String,
    val identifikator: String,
    val vedtakId: String
)