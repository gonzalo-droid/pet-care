package com.bestid.core.domain

import com.bestid.core.domain.reminder.MarkNotificationPromptShown
import com.bestid.core.domain.reminder.ShouldAskNotificationPermission
import com.bestid.core.domain.reminder.SyncReminders
import com.bestid.core.domain.schedule.CareSchedule
import com.bestid.core.domain.usecase.CareDraft
import com.bestid.core.domain.usecase.PetDraft
import com.bestid.core.domain.usecase.SaveCareResult
import com.bestid.core.domain.usecase.SaveCareTask
import com.bestid.core.domain.usecase.SavePet
import com.bestid.core.domain.usecase.SavePetResult
import com.bestid.core.domain.usecase.SaveVaccineRecord
import com.bestid.core.domain.usecase.SaveWeight
import com.bestid.core.domain.usecase.SaveWeightResult
import com.bestid.core.domain.usecase.VaccineDraft
import com.bestid.core.domain.usecase.DeleteVaccineRecord
import com.bestid.core.domain.validation.CareError
import com.bestid.core.domain.validation.HealthValidation
import com.bestid.core.domain.validation.WeightError
import com.bestid.core.domain.catalog.VaccineCatalog
import com.bestid.core.domain.port.NotificationPermission
import com.bestid.core.model.CareKind
import com.bestid.core.model.IntervalUnit
import com.bestid.core.model.Species
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CareScheduleTest {
    @Test
    fun addsDaysOrCalendarMonths() {
        assertEquals(LocalDate(2026, 2, 28), CareSchedule.nextDate(LocalDate(2026, 1, 31), 1, IntervalUnit.MONTH))
        assertEquals(LocalDate(2028, 2, 29), CareSchedule.nextDate(LocalDate(2028, 1, 31), 1, IntervalUnit.MONTH))
        assertEquals(LocalDate(2026, 4, 30), CareSchedule.nextDate(LocalDate(2026, 1, 30), 3, IntervalUnit.MONTH))
        assertEquals(LocalDate(2026, 3, 2), CareSchedule.nextDate(LocalDate(2026, 1, 31), 30, IntervalUnit.DAY))
        assertNull(CareSchedule.nextDate(null, 3, IntervalUnit.MONTH))
        assertNull(CareSchedule.nextDate(LocalDate(2026, 1, 1), 0, IntervalUnit.DAY))
    }
}

class HealthValidationTest {
    private val today = LocalDate(2026, 11, 13)

    @Test
    fun weightBetween50GramsAnd150Kilos() {
        assertEquals(WeightError.REQUIRED, HealthValidation.validateWeight(null, today, today))
        assertEquals(WeightError.OUT_OF_RANGE, HealthValidation.validateWeight(49, today, today))
        assertEquals(WeightError.OUT_OF_RANGE, HealthValidation.validateWeight(150_001, today, today))
        assertNull(HealthValidation.validateWeight(50, today, today))
        assertNull(HealthValidation.validateWeight(150_000, today, today))
        assertEquals(WeightError.DATE_IN_FUTURE, HealthValidation.validateWeight(4200, LocalDate(2026, 11, 14), today))
    }

    @Test
    fun parsesKilogramsWithDecimalComma() {
        assertEquals(4200, HealthValidation.parseKilograms("4,2"))
        assertEquals(4250, HealthValidation.parseKilograms(" 4.25 "))
        assertEquals(50, HealthValidation.parseKilograms("0,05"))
        assertNull(HealthValidation.parseKilograms("abc"))
        assertNull(HealthValidation.parseKilograms(""))
    }

    @Test
    fun careNeedsTitleForOtherAndDatesOnlyWithReminder() {
        assertEquals(
            listOf(CareError.TITLE_REQUIRED, CareError.LAST_DATE_REQUIRED, CareError.INTERVAL_REQUIRED),
            HealthValidation.validateCare(CareKind.OTHER, " ", null, null, reminderEnabled = true, today = today),
        )
        assertTrue(HealthValidation.validateCare(CareKind.DEWORM_EXTERNAL, null, null, null, reminderEnabled = false, today = today).isEmpty())
        assertEquals(
            listOf(CareError.LAST_DATE_IN_FUTURE),
            HealthValidation.validateCare(CareKind.DEWORM_INTERNAL, null, LocalDate(2026, 12, 1), 3, true, today),
        )
    }
}

class HealthUseCasesTest {
    private val clock = FakeClock(LocalDate(2026, 11, 13))
    private val ids = SequentialIds()

    @Test
    fun savingACareComputesItsNextDate() = runTest {
        val repo = FakeCareRepository()
        val result = SaveCareTask(repo, clock, ids)(
            CareDraft(petId = "p", kind = CareKind.DEWORM_INTERNAL, lastDate = LocalDate(2026, 10, 1), intervalValue = 3, intervalUnit = IntervalUnit.MONTH, reminderEnabled = true),
        )
        assertIs<SaveCareResult.Saved>(result)
        assertEquals(LocalDate(2027, 1, 1), result.task.nextDate)
        assertNull(result.task.title) // el título solo se guarda para "Otro"
    }

    @Test
    fun weightIsSavedInGrams() = runTest {
        val repo = FakeWeightRepository()
        val save = SaveWeight(repo, clock, ids)
        assertIs<SaveWeightResult.Saved>(save("p", LocalDate(2026, 11, 5), 3800))
        assertEquals(SaveWeightResult.Invalid(WeightError.OUT_OF_RANGE), save("p", LocalDate(2026, 11, 5), 20))
        assertEquals(1, repo.all.value.size)
    }
}

class SyncRemindersTest {
    private val clock = FakeClock(LocalDate(2026, 11, 13))
    private val ids = SequentialIds()
    private val pets = FakePetRepository()
    private val records = FakeRecordRepository()
    private val cares = FakeCareRepository()
    private val types = FakeTypeRepository(VaccineCatalog.seed)
    private val settings = FakeSettingsStore()
    private val scheduled = FakeScheduledRepository()
    private val scheduler = FakeScheduler()
    private val sync = SyncReminders(pets, records, cares, types, settings, scheduled, scheduler, clock) { TimeZone.of("America/Lima") }

    private suspend fun lilithWithVaccine(): String {
        val pet = (SavePet(pets, FakeSelectedPetStore(), clock, ids)(PetDraft(name = "Lilith", species = Species.CAT)) as SavePetResult.Saved).pet
        val saved = SaveVaccineRecord(records, types, FakeVetRepository(), clock, ids)(
            VaccineDraft(petId = pet.id, petSpecies = Species.CAT, applicationDate = LocalDate(2025, 12, 21), nextDate = LocalDate(2026, 12, 31), typeIds = setOf("leucemia")),
        ) as com.bestid.core.domain.usecase.SaveVaccineResult.Saved
        return saved.record.id
    }

    /** Como tras restaurar un respaldo del sistema: la tabla dice que hay avisos, el teléfono no los tiene. */
    @Test
    fun firstSyncAfterLaunchReschedulesEverything() = runTest {
        lilithWithVaccine()
        sync()
        val restoredTable = scheduled.all.value
        val newPhone = FakeScheduler()
        val coordinatorSync = SyncReminders(pets, records, cares, types, settings, FakeScheduledRepository().also { it.all.value = restoredTable }, newPhone, clock) { TimeZone.of("America/Lima") }
        val job = launch { com.bestid.core.domain.reminder.ReminderCoordinator(pets, records, cares, settings, coordinatorSync).run() }
        advanceTimeBy(1_000)
        job.cancel()
        assertEquals(4, newPhone.scheduled.size)
    }

    @Test
    fun schedulesOnlyTheDifference() = runTest {
        val recordId = lilithWithVaccine()
        sync()
        assertEquals(4, scheduler.scheduled.size)
        assertEquals(4, scheduled.all.value.size)

        sync() // sin cambios: no agrega ni cancela
        assertEquals(1, scheduler.scheduleCalls.size)
        assertTrue(scheduler.cancelCalls.isEmpty())

        DeleteVaccineRecord(records, clock)(recordId)
        sync()
        assertTrue(scheduler.scheduled.isEmpty())
        assertTrue(scheduled.all.value.isEmpty())
    }

    @Test
    fun forceReschedulesEverything() = runTest {
        lilithWithVaccine()
        sync()
        scheduler.scheduled.clear() // el teléfono se reinició y Android borró las alarmas
        sync(force = true)
        assertEquals(4, scheduler.scheduled.size)
        assertEquals(4, scheduled.all.value.size)
    }

    @Test
    fun testReminderGoesThroughSyncAndSurvivesForce() = runTest {
        com.bestid.core.domain.reminder.ScheduleTestReminder(settings, sync, clock)()
        assertEquals(1, scheduler.scheduled.size)
        assertTrue(scheduler.scheduled.keys.single().startsWith("test:"))
        scheduler.scheduled.clear() // reinicio en Android
        sync(force = true)
        assertEquals(1, scheduler.scheduled.size)
    }

    @Test
    fun permissionPromptOnlyOnceAndOnlyIfUndetermined() = runTest {
        val ask = ShouldAskNotificationPermission(settings, scheduler)
        assertTrue(ask())
        MarkNotificationPromptShown(settings)()
        assertFalse(ask())
        settings.shown.value = false
        scheduler.permission = NotificationPermission.DENIED
        assertFalse(ask())
    }
}
