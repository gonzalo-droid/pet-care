package com.bestid.feature.home

import app.cash.turbine.test
import com.bestid.core.domain.FakeCareRepository
import com.bestid.core.domain.FakeClock
import com.bestid.core.domain.FakePetRepository
import com.bestid.core.domain.FakeRecordRepository
import com.bestid.core.domain.FakeScheduler
import com.bestid.core.domain.FakeSelectedPetStore
import com.bestid.core.domain.FakeTypeRepository
import com.bestid.core.domain.FakeVetRepository
import com.bestid.core.domain.FakeWeightRepository
import com.bestid.core.domain.SequentialIds
import com.bestid.core.domain.catalog.VaccineCatalog
import com.bestid.core.domain.reminder.ObserveNotificationPermission
import com.bestid.core.domain.usecase.ObserveCareTasks
import com.bestid.core.domain.usecase.ObserveSelectedPet
import com.bestid.core.domain.usecase.ObserveVaccineRecords
import com.bestid.core.domain.usecase.ObserveVaccineTypes
import com.bestid.core.domain.usecase.ObserveWeights
import com.bestid.core.domain.usecase.PetDraft
import com.bestid.core.domain.usecase.SavePet
import com.bestid.core.domain.usecase.SavePetResult
import com.bestid.core.domain.usecase.SaveVaccineRecord
import com.bestid.core.domain.usecase.VaccineDraft
import com.bestid.core.domain.port.NotificationPermission
import com.bestid.core.model.Species
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** TST-05: Inicio (H1) con los casos de uso reales sobre fakes. Hoy es 13/11/2026 (FakeClock). */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val clock = FakeClock()
    private val ids = SequentialIds()
    private val pets = FakePetRepository()
    private val store = FakeSelectedPetStore()
    private val records = FakeRecordRepository()
    private val types = FakeTypeRepository(VaccineCatalog.seed)
    private val scheduler = FakeScheduler()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = HomeViewModel(
        observeSelectedPet = ObserveSelectedPet(pets, store),
        observeRecords = ObserveVaccineRecords(records),
        observeTypes = ObserveVaccineTypes(types),
        observeCares = ObserveCareTasks(FakeCareRepository()),
        observeWeights = ObserveWeights(FakeWeightRepository()),
        observePermission = ObserveNotificationPermission(scheduler),
        clock = clock,
    )

    private suspend fun addCatWithVaccineDue(next: LocalDate) {
        val pet = (SavePet(pets, store, clock, ids)(PetDraft(name = "Lilith", species = Species.CAT)) as SavePetResult.Saved).pet
        SaveVaccineRecord(records, types, FakeVetRepository(), clock, ids)(
            VaccineDraft(
                petId = pet.id,
                petSpecies = Species.CAT,
                applicationDate = LocalDate(2025, 11, 23),
                nextDate = next,
                typeIds = setOf(VaccineCatalog.suggestedFor(types.all.value, Species.CAT).first().id),
            ),
        )
    }

    @Test
    fun withoutPetsThereIsNothingToShow() = runTest {
        viewModel().state.test {
            val state = expectMostRecentItem()
            assertFalse(state.loading)
            assertNull(state.pet)
        }
    }

    @Test
    fun nextVaccineShowsTheDaysLeft() = runTest {
        addCatWithVaccineDue(LocalDate(2026, 11, 23))
        viewModel().state.test {
            val state = expectMostRecentItem()
            assertEquals(1, state.vaccineCount)
            val upcoming = assertIs<Upcoming.Vaccine>(state.upcoming)
            assertEquals(10, upcoming.daysLeft)
            assertNull(state.overdue)
        }
    }

    @Test
    fun blockedNotificationsAreFlaggedWhenSomethingHasAReminder() = runTest {
        addCatWithVaccineDue(LocalDate(2026, 11, 23))
        scheduler.permission = NotificationPermission.DENIED
        val vm = viewModel()
        vm.state.test {
            vm.refreshPermission()
            assertTrue(expectMostRecentItem().remindersBlocked)
        }
    }
}
