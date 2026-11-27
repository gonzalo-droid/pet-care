package com.bestid.feature.cartilla

import app.cash.turbine.test
import com.bestid.core.domain.FakeClock
import com.bestid.core.domain.FakeImageProcessor
import com.bestid.core.domain.FakePetRepository
import com.bestid.core.domain.FakePhotoFileStore
import com.bestid.core.domain.FakePhotoRepository
import com.bestid.core.domain.FakeRecordRepository
import com.bestid.core.domain.FakeScheduler
import com.bestid.core.domain.FakeSelectedPetStore
import com.bestid.core.domain.FakeSettingsStore
import com.bestid.core.domain.FakeTypeRepository
import com.bestid.core.domain.FakeVetRepository
import com.bestid.core.domain.SequentialIds
import com.bestid.core.domain.catalog.VaccineCatalog
import com.bestid.core.domain.reminder.ShouldAskNotificationPermission
import com.bestid.core.domain.usecase.GetPhotos
import com.bestid.core.domain.usecase.GetVet
import com.bestid.core.domain.usecase.ImportPhotos
import com.bestid.core.domain.usecase.ObservePet
import com.bestid.core.domain.usecase.ObserveSelectedPet
import com.bestid.core.domain.usecase.ObserveVaccineRecord
import com.bestid.core.domain.usecase.ObserveVaccineTypes
import com.bestid.core.domain.usecase.ObserveVets
import com.bestid.core.domain.usecase.PetDraft
import com.bestid.core.domain.usecase.SavePet
import com.bestid.core.domain.usecase.SaveVaccineRecord
import com.bestid.core.domain.validation.VaccineError
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
import kotlin.test.assertTrue

/** TST-05: el formulario de vacuna (C3), con los casos de uso reales sobre fakes. */
@OptIn(ExperimentalCoroutinesApi::class)
class VaccineFormViewModelTest {
    private val clock = FakeClock()
    private val ids = SequentialIds()
    private val pets = FakePetRepository()
    private val store = FakeSelectedPetStore()
    private val records = FakeRecordRepository()
    private val types = FakeTypeRepository(VaccineCatalog.seed)
    private val vets = FakeVetRepository()
    private val photos = FakePhotoRepository()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(recordId: String? = null) = VaccineFormViewModel(
        recordId = recordId,
        photoIds = emptyList(),
        observeSelectedPet = ObserveSelectedPet(pets, store),
        observePet = ObservePet(pets),
        observeRecord = ObserveVaccineRecord(records),
        observeTypes = ObserveVaccineTypes(types),
        observeVets = ObserveVets(vets),
        getVet = GetVet(vets),
        saveRecord = SaveVaccineRecord(records, types, vets, clock, ids),
        shouldAskPermission = ShouldAskNotificationPermission(FakeSettingsStore(), FakeScheduler()),
        getPhotos = GetPhotos(photos),
        importPhotos = ImportPhotos(FakeImageProcessor(), FakePhotoFileStore(), photos, clock, ids),
    )

    private suspend fun addCat() = SavePet(pets, store, clock, ids)(PetDraft(name = "Lilith", species = Species.CAT))

    @Test
    fun savingWithoutTypeAndDateShowsBothErrorsAndKeepsTheForm() = runTest {
        addCat()
        val vm = viewModel()
        vm.save()
        val state = vm.state.value
        assertTrue(VaccineError.NoType in state.errors)
        assertTrue(VaccineError.NoApplicationDate in state.errors)
        assertFalse(state.done)
        assertTrue(records.all.value.isEmpty())
    }

    @Test
    fun validVaccineIsSavedAndClosesTheForm() = runTest {
        addCat()
        val vm = viewModel()
        val type = VaccineCatalog.suggestedFor(types.all.value, Species.CAT).first()
        vm.state.test {
            assertTrue(awaitItem().loaded)
            vm.toggleType(type.id, true)
            vm.onDate(DateTarget.Application, LocalDate(2026, 11, 1))
            vm.save()
            assertTrue(expectMostRecentItem().done)
        }
        assertEquals(setOf(type.id), records.all.value.single().typeIds.toSet())
    }

    @Test
    fun editingAMissingRecordShowsNotFound() = runTest {
        val state = viewModel(recordId = "no-existe").state.value
        assertTrue(state.loaded)
        assertTrue(state.notFound)
    }

    @Test
    fun anyEditMarksTheFormDirty() = runTest {
        addCat()
        val vm = viewModel()
        assertFalse(vm.state.value.dirty)
        vm.onVetName("Vet Sur")
        assertTrue(vm.state.value.dirty)
    }
}
