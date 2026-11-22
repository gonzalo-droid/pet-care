package com.bestid.core.domain.schedule

import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

/** Fracción transcurrida entre [from] y [to] en [0, 1]; null si no hay rango válido. */
fun countdownProgress(from: LocalDate?, to: LocalDate, today: LocalDate): Float? {
    if (from == null || from >= to) return null
    return (from.daysUntil(today).toFloat() / from.daysUntil(to)).coerceIn(0f, 1f)
}
