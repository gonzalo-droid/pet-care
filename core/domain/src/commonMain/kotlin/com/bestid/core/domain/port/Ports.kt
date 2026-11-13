package com.bestid.core.domain.port

import kotlinx.datetime.LocalDate
import kotlin.time.Instant

/** Reloj de la app. Se inyecta para poder fijar "hoy" en las pruebas. */
interface AppClock {
    fun now(): Instant
    fun today(): LocalDate
}

/** Genera IDs únicos (UUID en texto). */
fun interface IdGenerator {
    fun newId(): String
}
