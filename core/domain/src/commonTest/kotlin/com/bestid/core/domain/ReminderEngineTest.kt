package com.bestid.core.domain

import com.bestid.core.domain.catalog.VaccineCatalog
import com.bestid.core.domain.reminder.ReminderEngine
import com.bestid.core.domain.reminder.ReminderInput
import com.bestid.core.domain.reminder.ReminderKind
import com.bestid.core.domain.reminder.ReminderRefType
import com.bestid.core.domain.reminder.ReminderSubject
import com.bestid.core.model.CareKind
import com.bestid.core.model.CareTask
import com.bestid.core.model.IntervalUnit
import com.bestid.core.model.Pet
import com.bestid.core.model.RecordSource
import com.bestid.core.model.Species
import com.bestid.core.model.VaccineRecord
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

class ReminderEngineTest {
    private val lima = TimeZone.of("America/Lima")
    private val t0 = Instant.fromEpochSeconds(0)
    private val lilith = pet("lilith", "Lilith")
    private val types = VaccineCatalog.seed.associateBy { it.id }

    private fun pet(id: String, name: String, deleted: Boolean = false) = Pet(
        id, name, Species.CAT, null, null, null, null, null, null, null, null, t0, t0, if (deleted) t0 else null,
    )

    private fun record(
        id: String,
        applied: LocalDate,
        next: LocalDate?,
        confirmed: Boolean = true,
        typeIds: List<String> = listOf("leucemia"),
        petId: String = "lilith",
    ) = VaccineRecord(
        id, petId, applied, next, confirmed, null, typeIds, emptyList(), emptyList(), null,
        RecordSource.MANUAL, emptyList(), null, null, t0, t0, null,
    )

    private fun care(id: String, next: LocalDate?, enabled: Boolean = true) = CareTask(
        id, "lilith", CareKind.DEWORM_INTERNAL, null, null, 3, IntervalUnit.MONTH, next, enabled, t0, t0, null,
    )

    private fun at(date: LocalDate, hour: Int = 9) = LocalDateTime(date, LocalTime(hour, 0)).toInstant(lima)

    private fun plan(
        now: Instant,
        records: List<com.bestid.core.model.VaccineRecord> = emptyList(),
        cares: List<CareTask> = emptyList(),
        pets: List<Pet> = listOf(lilith),
        limit: Int = 50,
    ) = ReminderEngine.plan(ReminderInput(pets, records, cares, types, LocalTime(9, 0), now, lima, limit))

    @Test
    fun threeRemindersAndFollowUpAtPreferredHourInLima() {
        val due = LocalDate(2026, 12, 31)
        val result = plan(now = at(LocalDate(2026, 11, 13)), records = listOf(record("r", LocalDate(2025, 12, 21), due)))
        assertEquals(
            listOf(ReminderKind.WEEK_BEFORE, ReminderKind.DAY_BEFORE, ReminderKind.SAME_DAY, ReminderKind.FOLLOW_UP),
            result.map { it.kind },
        )
        assertEquals(
            listOf(at(LocalDate(2026, 12, 24)), at(LocalDate(2026, 12, 30)), at(due), at(LocalDate(2027, 1, 3))),
            result.map { it.fireAt },
        )
        // 9:00 en Lima es 14:00 UTC
        assertEquals(Instant.parse("2026-12-31T14:00:00Z"), result[2].fireAt)
        assertEquals(ReminderSubject.Vaccine("Leucemia"), result.first().subject)
        assertEquals("Lilith", result.first().petName)
        assertTrue(result.all { it.refType == ReminderRefType.VACCINE && it.refId == "r" })
    }

    @Test
    fun pastRemindersAreDropped() {
        val due = LocalDate(2026, 11, 15)
        val result = plan(now = at(LocalDate(2026, 11, 14), hour = 10), records = listOf(record("r", LocalDate(2025, 11, 15), due)))
        assertEquals(listOf(ReminderKind.SAME_DAY, ReminderKind.FOLLOW_UP), result.map { it.kind })
    }

    @Test
    fun unconfirmedOrMissingNextDateHasNoReminders() {
        val result = plan(
            now = at(LocalDate(2026, 11, 13)),
            records = listOf(
                record("a", LocalDate(2025, 12, 21), LocalDate(2026, 12, 31), confirmed = false),
                record("b", LocalDate(2025, 12, 21), null),
            ),
        )
        assertTrue(result.isEmpty())
    }

    @Test
    fun aLaterDoseOfTheSameTypeCancelsTheOldReminders() {
        val old = record("old", LocalDate(2025, 1, 10), LocalDate(2026, 1, 10), typeIds = listOf("rabia"))
        val newer = record("new", LocalDate(2026, 1, 12), LocalDate(2027, 1, 12), typeIds = listOf("rabia", "calicivirus"))
        val result = plan(now = at(LocalDate(2026, 1, 11)), records = listOf(old, newer))
        assertTrue(result.none { it.refId == "old" }, "la dosis ya registrada no debe seguir avisando")
        assertTrue(result.any { it.refId == "new" })
    }

    @Test
    fun aLaterRecordOfAnotherTypeDoesNotCancel() {
        val old = record("old", LocalDate(2025, 1, 10), LocalDate(2026, 12, 1), typeIds = listOf("leucemia"))
        val other = record("other", LocalDate(2026, 3, 1), null, typeIds = listOf("rabia"))
        val result = plan(now = at(LocalDate(2026, 11, 13)), records = listOf(old, other))
        assertTrue(result.any { it.refId == "old" })
    }

    @Test
    fun caresWithReminderEnabledOnly() {
        val result = plan(
            now = at(LocalDate(2026, 11, 13)),
            cares = listOf(care("on", LocalDate(2026, 12, 1)), care("off", LocalDate(2026, 12, 1), enabled = false), care("nodate", null)),
        )
        assertEquals(setOf("on"), result.map { it.refId }.toSet())
        assertEquals(ReminderSubject.Care(CareKind.DEWORM_INTERNAL, null), result.first().subject)
        assertTrue(result.all { it.refType == ReminderRefType.CARE })
    }

    @Test
    fun deletedPetsGetNoReminders() {
        val result = plan(
            now = at(LocalDate(2026, 11, 13)),
            pets = listOf(pet("lilith", "Lilith", deleted = true)),
            records = listOf(record("r", LocalDate(2025, 12, 21), LocalDate(2026, 12, 31))),
            cares = listOf(care("c", LocalDate(2026, 12, 1))),
        )
        assertTrue(result.isEmpty())
    }

    @Test
    fun keepsOnlyTheClosestUpToTheLimit() {
        val records = (1..30).map { i -> record("r$i", LocalDate(2025, 1, 1), LocalDate(2027, 1, 1).let { LocalDate.fromEpochDays(it.toEpochDays() + i) }, typeIds = listOf("t$i")) }
        val result = plan(now = at(LocalDate(2026, 11, 13)), records = records, limit = 50)
        assertEquals(50, result.size)
        assertEquals(result.sortedBy { it.fireAt }, result)
        assertTrue(result.none { it.refId == "r30" && it.kind == ReminderKind.FOLLOW_UP })
    }

    @Test
    fun idIsStableAndChangesWhenTheTextChanges() {
        val r = record("r", LocalDate(2025, 12, 21), LocalDate(2026, 12, 31))
        val now = at(LocalDate(2026, 11, 13))
        val a = plan(now, records = listOf(r)).map { it.id }
        val b = plan(now, records = listOf(r)).map { it.id }
        val renamed = plan(now, records = listOf(r), pets = listOf(pet("lilith", "Lili"))).map { it.id }
        assertEquals(a, b)
        assertNotEquals(a, renamed)
        assertEquals(a.size, a.toSet().size)
    }
}
