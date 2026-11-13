package com.bestid.core.domain.schedule

import com.bestid.core.model.VaccineRecord
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

/** Próxima dosis de una mascota y los días que faltan (negativos si está atrasada). */
data class NextDue(
    val record: VaccineRecord,
    val date: LocalDate,
    val daysLeft: Int,
    val confirmed: Boolean,
)

/**
 * Elige la próxima dosis entre los registros: la fecha futura (o de hoy) más cercana; si no hay,
 * la atrasada más reciente. Solo considera registros con próxima fecha. El motor de avisos (M2)
 * reutiliza esta regla.
 */
object NextDueCalculator {
    fun nextDue(records: List<VaccineRecord>, today: LocalDate): NextDue? {
        val dated = records.mapNotNull { r -> r.nextDate?.let { r to it } }
        val upcoming = dated.filter { (_, date) -> date >= today }.minByOrNull { (_, date) -> date }
        val chosen = upcoming ?: dated.maxByOrNull { (_, date) -> date } ?: return null
        val (record, date) = chosen
        return NextDue(record, date, today.daysUntil(date), record.nextDateConfirmed)
    }
}
