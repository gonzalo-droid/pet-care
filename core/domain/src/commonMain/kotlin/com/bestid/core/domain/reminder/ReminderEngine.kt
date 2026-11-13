package com.bestid.core.domain.reminder

import com.bestid.core.model.CareTask
import com.bestid.core.model.Pet
import com.bestid.core.model.VaccineRecord
import com.bestid.core.model.VaccineType
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlin.time.Instant

/** Datos que necesita el motor. Solo se consideran mascotas, registros y cuidados activos. */
data class ReminderInput(
    val pets: List<Pet>,
    val records: List<VaccineRecord>,
    val cares: List<CareTask>,
    val types: Map<String, VaccineType>,
    val reminderTime: LocalTime,
    val now: Instant,
    val timeZone: TimeZone,
    /** iOS permite 64 avisos pendientes por app: se programan los 50 más cercanos (sección 11). */
    val limit: Int = MAX_SCHEDULED,
) {
    companion object {
        const val MAX_SCHEDULED = 50
    }
}

/**
 * Motor de avisos (sección 11): función pura, común y sin plataforma.
 *
 * - Vacunas con próxima fecha confirmada y cuidados con aviso activo.
 * - 7 días antes, 1 día antes y el mismo día, a la hora preferida; y un seguimiento a los 3 días.
 * - Una vacuna deja de avisar si ya hay un registro posterior de la misma mascota con algún tipo
 *   en común (ya se puso la siguiente dosis). Un cuidado se actualiza al registrar su última fecha.
 * - Solo avisos futuros, ordenados y limitados a los [ReminderInput.limit] más cercanos.
 */
object ReminderEngine {
    fun plan(input: ReminderInput): List<PlannedReminder> {
        val pets = input.pets.filter { it.deletedAt == null }.associateBy { it.id }
        val records = input.records.filter { it.deletedAt == null && it.petId in pets }
        val dues = buildList {
            records.forEach { record ->
                val date = record.nextDate ?: return@forEach
                if (!record.nextDateConfirmed || record.isSuperseded(records)) return@forEach
                val label = record.typeIds.firstNotNullOfOrNull { input.types[it]?.label }
                add(Due(ReminderRefType.VACCINE, record.id, pets.getValue(record.petId), ReminderSubject.Vaccine(label), date))
            }
            input.cares.forEach { care ->
                val pet = pets[care.petId] ?: return@forEach
                val date = care.nextDate ?: return@forEach
                if (care.deletedAt != null || !care.reminderEnabled) return@forEach
                add(Due(ReminderRefType.CARE, care.id, pet, ReminderSubject.Care(care.kind, care.title), date))
            }
        }
        return dues.asSequence()
            .flatMap { due -> ReminderKind.entries.map { kind -> due.toReminder(kind, input) } }
            .filter { it.fireAt > input.now }
            .sortedWith(compareBy<PlannedReminder> { it.fireAt }.thenBy { it.id })
            .take(input.limit)
            .toList()
    }

    private data class Due(
        val refType: ReminderRefType,
        val refId: String,
        val pet: Pet,
        val subject: ReminderSubject,
        val date: LocalDate,
    )

    private fun Due.toReminder(kind: ReminderKind, input: ReminderInput): PlannedReminder {
        val day = date.plus(DatePeriod(days = kind.daysFromDue))
        val fireAt = LocalDateTime(day, input.reminderTime).toInstant(input.timeZone)
        // El ID incluye lo que se muestra (nombre, asunto) para reprogramar si cambia.
        val signature = "${pet.name}|$subject".hashCode().toUInt().toString(16)
        return PlannedReminder(
            id = "${refType.name.lowercase()}:$refId:${kind.name.lowercase()}:$date:$signature",
            refType = refType,
            refId = refId,
            petId = pet.id,
            petName = pet.name,
            subject = subject,
            kind = kind,
            dueDate = date,
            fireAt = fireAt,
        )
    }

    private fun VaccineRecord.isSuperseded(all: List<VaccineRecord>): Boolean = all.any { other ->
        other.id != id &&
            other.petId == petId &&
            other.applicationDate > applicationDate &&
            (typeIds.isEmpty() || other.typeIds.any { it in typeIds })
    }
}

/** Qué agregar y qué cancelar para pasar de lo programado a lo deseado. */
data class ReminderDiff(val toSchedule: List<PlannedReminder>, val toCancel: List<String>) {
    companion object {
        fun between(desired: List<PlannedReminder>, scheduledIds: Set<String>): ReminderDiff {
            val desiredIds = desired.map { it.id }.toSet()
            return ReminderDiff(
                toSchedule = desired.filter { it.id !in scheduledIds },
                toCancel = scheduledIds.filter { it !in desiredIds }.sorted(),
            )
        }
    }
}
