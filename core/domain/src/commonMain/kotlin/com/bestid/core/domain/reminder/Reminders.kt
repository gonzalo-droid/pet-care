package com.bestid.core.domain.reminder

import com.bestid.core.model.CareKind
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

/** A qué apunta un aviso; al tocarlo se abre ese registro. */
enum class ReminderRefType { VACCINE, CARE }

/** Momento del aviso respecto de la fecha (sección 11). */
enum class ReminderKind(val daysFromDue: Int) {
    /** "A {nombre} le toca su {…} en 7 días" */
    WEEK_BEFORE(-7),

    /** "Mañana le toca su {…} a {nombre}" */
    DAY_BEFORE(-1),

    /** "Hoy le toca su {…} a {nombre}" */
    SAME_DAY(0),

    /** "¿Ya le pusiste su {…} a {nombre}?" */
    FOLLOW_UP(3),
}

/** De qué trata el aviso. Los textos los arma la plataforma con Compose Resources. */
sealed interface ReminderSubject {
    /** [typeLabel] es el nombre del tipo de vacuna (dato de dominio) o null si no se conoce. */
    data class Vaccine(val typeLabel: String?) : ReminderSubject

    data class Care(val kind: CareKind, val title: String?) : ReminderSubject
}

/** Un aviso que debería estar programado. */
data class PlannedReminder(
    /** Estable: cambia solo si cambia algo de lo que se muestra o cuándo se muestra. */
    val id: String,
    val refType: ReminderRefType,
    val refId: String,
    val petId: String,
    val petName: String,
    val subject: ReminderSubject,
    val kind: ReminderKind,
    val dueDate: LocalDate,
    val fireAt: Instant,
)
