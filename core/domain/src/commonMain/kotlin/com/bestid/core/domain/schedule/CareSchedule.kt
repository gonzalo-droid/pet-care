package com.bestid.core.domain.schedule

import com.bestid.core.model.CareTask
import com.bestid.core.model.IntervalUnit
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus

/** Próximo cuidado y los días que faltan (negativos si está atrasado). */
data class CareDue(val task: CareTask, val date: LocalDate, val daysLeft: Int)

/**
 * Próxima fecha de un cuidado: última fecha + intervalo en días o en meses de calendario
 * (sección 11). Un mes después del 31 de enero es el último día de febrero.
 */
object CareSchedule {
    fun nextDate(lastDate: LocalDate?, intervalValue: Int?, unit: IntervalUnit?): LocalDate? {
        if (lastDate == null || intervalValue == null || unit == null || intervalValue <= 0) return null
        return when (unit) {
            IntervalUnit.DAY -> lastDate.plus(DatePeriod(days = intervalValue))
            IntervalUnit.MONTH -> lastDate.plus(DatePeriod(months = intervalValue))
        }
    }

    /** El cuidado con aviso activo más próximo; si no hay futuros, el atrasado más reciente. */
    fun nextDue(tasks: List<CareTask>, today: LocalDate): CareDue? {
        val dated = tasks.filter { it.reminderEnabled }.mapNotNull { t -> t.nextDate?.let { t to it } }
        val chosen = dated.filter { (_, d) -> d >= today }.minByOrNull { (_, d) -> d }
            ?: dated.maxByOrNull { (_, d) -> d }
            ?: return null
        return CareDue(chosen.first, chosen.second, today.daysUntil(chosen.second))
    }
}
