package com.bestid.core.domain

import com.bestid.core.domain.catalog.VaccineCatalog
import com.bestid.core.domain.usecase.DeletePet
import com.bestid.core.domain.usecase.DeleteVaccineRecord
import com.bestid.core.domain.usecase.ObserveSelectedPet
import com.bestid.core.domain.usecase.PetDraft
import com.bestid.core.domain.usecase.RestorePet
import com.bestid.core.domain.usecase.RestoreVaccineRecord
import com.bestid.core.domain.usecase.SavePet
import com.bestid.core.domain.usecase.SavePetResult
import com.bestid.core.domain.usecase.SaveVaccineRecord
import com.bestid.core.domain.usecase.SaveVaccineResult
import com.bestid.core.domain.usecase.SelectPet
import com.bestid.core.domain.usecase.VaccineDraft
import com.bestid.core.domain.validation.PetError
import com.bestid.core.domain.validation.VaccineError
import com.bestid.core.model.Sex
import com.bestid.core.model.Species
import com.bestid.core.model.VaccineItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UseCasesTest {
    private val clock = FakeClock()
    private val ids = SequentialIds()
    private val pets = FakePetRepository()
    private val records = FakeRecordRepository()
    private val types = FakeTypeRepository(VaccineCatalog.seed)
    private val vets = FakeVetRepository()
    private val store = FakeSelectedPetStore()

    private val savePet = SavePet(pets, store, clock, ids)
    private val saveVaccine = SaveVaccineRecord(records, types, vets, clock, ids)
    private val selected = ObserveSelectedPet(pets, store)

    private suspend fun lilith() =
        (savePet(PetDraft(name = " Lilith ", species = Species.CAT, sex = Sex.FEMALE, ownerName = "Tutor de Prueba")) as SavePetResult.Saved).pet

    @Test
    fun newPetIsTrimmedAndSelected() = runTest {
        val pet = lilith()
        assertEquals("Lilith", pet.name)
        assertEquals(pet, selected().first())
    }

    @Test
    fun invalidPetNameIsRejected() = runTest {
        val result = savePet(PetDraft(name = "", species = Species.DOG))
        assertEquals(SavePetResult.Invalid(PetError.NAME_EMPTY), result)
        assertTrue(pets.all.value.isEmpty())
    }

    @Test
    fun selectionSwitchesBetweenTwoPets() = runTest {
        val lilith = lilith()
        val toby = (savePet(PetDraft(name = "Toby", species = Species.DOG)) as SavePetResult.Saved).pet
        assertEquals(toby, selected().first())
        SelectPet(store)(lilith.id)
        assertEquals(lilith, selected().first())
    }

    @Test
    fun deletingAPetCanBeUndone() = runTest {
        val lilith = lilith()
        val toby = (savePet(PetDraft(name = "Toby", species = Species.DOG)) as SavePetResult.Saved).pet
        DeletePet(pets, store, clock)(toby.id)
        assertEquals(lilith, selected().first())
        assertNotNull(pets.get(toby.id)?.deletedAt)

        RestorePet(pets, store, clock)(toby.id)
        assertEquals(toby.id, selected().first()?.id)
        assertNull(pets.get(toby.id)?.deletedAt)
    }

    @Test
    fun registersLilithsTwoVaccinesByHand() = runTest {
        val lilith = lilith()
        val first = saveVaccine(
            VaccineDraft(
                petId = lilith.id, petSpecies = Species.CAT,
                applicationDate = LocalDate(2024, 6, 29),
                typeIds = setOf("rinotraqueitis", "panleucopenia", "calicivirus"),
                items = listOf(
                    VaccineItem("Felocell 3", "669022", LocalDate(2023, 5, 11), LocalDate(2024, 11, 5)),
                    VaccineItem("Diluente estéril Felocell 3", "675043", LocalDate(2023, 4, 24), LocalDate(2024, 11, 5)),
                ),
            ),
        )
        val second = saveVaccine(
            VaccineDraft(
                petId = lilith.id, petSpecies = Species.CAT,
                applicationDate = LocalDate(2025, 12, 21),
                typeIds = setOf("rinotraqueitis", "panleucopenia", "calicivirus", "rabia"),
                items = listOf(VaccineItem("Defensor 3 (antirrábica)", "766517", LocalDate(2024, 9, 19), LocalDate(2026, 3, 18))),
                vetName = "Veterinaria San Martín",
                vetContact = "987654321",
            ),
        )
        assertIs<SaveVaccineResult.Saved>(first)
        assertIs<SaveVaccineResult.Saved>(second)
        val saved = records.observeForPet(lilith.id).first()
        assertEquals(listOf(LocalDate(2025, 12, 21), LocalDate(2024, 6, 29)), saved.map { it.applicationDate })
        assertEquals(1, vets.all.value.size)
        assertEquals(vets.all.value.single().id, saved.first().vetId)
    }

    @Test
    fun invalidVaccineIsNotSaved() = runTest {
        val result = saveVaccine(VaccineDraft(petId = "p", petSpecies = Species.CAT, applicationDate = null))
        assertIs<SaveVaccineResult.Invalid>(result)
        assertEquals(listOf(VaccineError.NoType, VaccineError.NoApplicationDate), result.validation.errors)
        assertTrue(records.all.value.isEmpty())
    }

    @Test
    fun customTypeIsCreatedOnceAndReused() = runTest {
        repeat(2) {
            saveVaccine(
                VaccineDraft(
                    petId = "p", petSpecies = Species.DOG, applicationDate = LocalDate(2026, 1, 10),
                    customTypeLabels = listOf(if (it == 0) "Giardia" else " giardia "),
                ),
            )
        }
        val custom = types.all.value.filter { it.isCustom }
        assertEquals(1, custom.size)
        assertTrue(custom.single().id.startsWith("custom:"))
        assertEquals(setOf(Species.DOG), custom.single().species)
    }

    @Test
    fun customLabelMatchingTheCatalogUsesTheCatalogType() = runTest {
        val result = saveVaccine(
            VaccineDraft(petId = "p", petSpecies = Species.CAT, applicationDate = LocalDate(2026, 1, 10), customTypeLabels = listOf("rinotraqueitis")),
        ) as SaveVaccineResult.Saved
        assertEquals(listOf("rinotraqueitis"), result.record.typeIds)
        assertTrue(types.all.value.none { it.isCustom })
    }

    @Test
    fun deletingAVaccineCanBeUndone() = runTest {
        val saved = saveVaccine(
            VaccineDraft(petId = "p", petSpecies = Species.CAT, applicationDate = LocalDate(2026, 1, 10), typeIds = setOf("rabia")),
        ) as SaveVaccineResult.Saved
        DeleteVaccineRecord(records, clock)(saved.record.id)
        assertTrue(records.observeForPet("p").first().isEmpty())
        RestoreVaccineRecord(records, clock)(saved.record.id)
        assertEquals(1, records.observeForPet("p").first().size)
    }

    @Test
    fun editingKeepsCreationDateAndMarksNextDateConfirmed() = runTest {
        val saved = saveVaccine(
            VaccineDraft(petId = "p", petSpecies = Species.CAT, applicationDate = LocalDate(2025, 12, 21), typeIds = setOf("rabia")),
        ) as SaveVaccineResult.Saved
        clock.today = LocalDate(2026, 11, 20)
        val edited = saveVaccine(
            VaccineDraft(
                id = saved.record.id, petId = "p", petSpecies = Species.CAT, applicationDate = LocalDate(2025, 12, 21),
                nextDate = LocalDate(2026, 12, 31), typeIds = setOf("rabia"),
            ),
        ) as SaveVaccineResult.Saved
        assertEquals(saved.record.createdAt, edited.record.createdAt)
        assertTrue(edited.record.updatedAt > saved.record.updatedAt)
        assertTrue(edited.record.nextDateConfirmed)
        assertEquals(1, records.all.value.size)
    }
}
