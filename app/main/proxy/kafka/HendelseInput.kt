package proxy.kafka

import com.papsign.ktor.openapigen.annotations.properties.description.Description
import java.time.LocalDate

data class HendelseInputFlereTpNr(
    @property:Description("Liste av TP-nummer.")
    val tpNr: List<String>,
    @property:Description("Fødselsnummer.")
    val identifikator: String,
    @property:Description("Vedtak-ID.")
    val vedtakId: String,
    @property:Description("Fra-dato for vedtakets virkningsperiode.")
    val fom: LocalDate,
    @property:Description("Til-dato for vedtakets virkningsperiode. Kan være null fra Arena.")
    val tom: LocalDate? = null
)

data class HendelseInput(
    val tpNr: String,
    val identifikator: String,
    val vedtakId: String,
    val fom: LocalDate,
    val tom: LocalDate? = null
)