package com.bestid.core.domain.validation

import com.bestid.core.model.CareKind
import kotlinx.datetime.LocalDate

enum class CareError { TITLE_REQUIRED, LAST_DATE_REQUIRED, LAST_DATE_IN_FUTURE, INTERVAL_REQUIRED }

enum class WeightError { REQUIRED, OUT_OF_RANGE, DATE_IN_FUTURE }

/** Validaciones de Salud (sección 9). */
object HealthValidation {
    /** 0,05 kg a 150 kg. */
    const val MIN_GRAMS = 50
    const val MAX_GRAMS = 150_000

    fun validateWeight(grams: Int?, date: LocalDate, today: LocalDate): WeightError? = when {
        grams == null -> WeightError.REQUIRED
        grams < MIN_GRAMS || grams > MAX_GRAMS -> WeightError.OUT_OF_RANGE
        date > today -> WeightError.DATE_IN_FUTURE
        else -> null
    }

    fun validateCare(
        kind: CareKind,
        title: String?,
        lastDate: LocalDate?,
        intervalValue: Int?,
        reminderEnabled: Boolean,
        today: LocalDate,
    ): List<CareError> = buildList {
        if (kind == CareKind.OTHER && title.isNullOrBlank()) add(CareError.TITLE_REQUIRED)
        if (lastDate != null && lastDate > today) add(CareError.LAST_DATE_IN_FUTURE)
        // Sin última fecha e intervalo no hay próxima fecha: solo se exigen si hay aviso.
        if (reminderEnabled && lastDate == null) add(CareError.LAST_DATE_REQUIRED)
        if (intervalValue != null && intervalValue <= 0 || reminderEnabled && intervalValue == null) {
            add(CareError.INTERVAL_REQUIRED)
        }
    }

    /** "4,2" o "4.25" → gramos (4200, 4250). Null si no es un número. */
    fun parseKilograms(text: String): Int? {
        val normalized = text.trim().replace(',', '.')
        if (normalized.isEmpty()) return null
        val kg = normalized.toDoubleOrNull() ?: return null
        return kotlin.math.round(kg * 1000).toInt()
    }
}
