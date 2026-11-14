package com.bestid.core.domain

import com.bestid.core.domain.catalog.VaccineCatalog
import com.bestid.core.domain.share.BuildPetSheet
import com.bestid.core.domain.share.SheetOptions
import com.bestid.core.domain.usecase.DeleteVaccineRecord
import com.bestid.core.domain.usecase.ImportPhotos
import com.bestid.core.domain.usecase.PetDraft
import com.bestid.core.domain.usecase.SavePet
import com.bestid.core.domain.usecase.SavePetResult
import com.bestid.core.domain.usecase.SaveVaccineRecord
import com.bestid.core.domain.usecase.SaveVaccineResult
import com.bestid.core.domain.usecase.SaveWeight
import com.bestid.core.domain.usecase.VaccineDraft
import com.bestid.core.model.Species
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PetSheetTest {
    private val clock = FakeClock()
    private val ids = SequentialIds()
    private val pets = FakePetRepository()
    private val records = FakeRecordRepository()
    private val types = FakeTypeRepository(VaccineCatalog.seed)
    private val vets = FakeVetRepository()
    private val weights = FakeWeightRepository()
    private val photos = FakePhotoRepository()
    private val saveVaccine = SaveVaccineRecord(records, types, vets, clock, ids)
    private val importPhotos = ImportPhotos(FakeImageProcessor(), FakePhotoFileStore(), photos, clock, ids)
    private val build = BuildPetSheet(pets, records, types, vets, weights, photos, clock)

    private suspend fun lilith(): String {
        val pet = (
            SavePet(pets, FakeSelectedPetStore(), clock, ids)(
                PetDraft(name = "Lilith", species = Species.CAT, ownerName = "Tutor de Prueba", ownerAddress = "Av. Siempre Viva 123"),
            ) as SavePetResult.Saved
            ).pet
        val (p1, p2) = importPhotos(listOf(byteArrayOf(1), byteArrayOf(2))).photos
        saveVaccine(VaccineDraft(petId = pet.id, petSpecies = Species.CAT, applicationDate = LocalDate(2024, 6, 29), typeIds = setOf("calicivirus"), sourcePhotoIds = listOf(p1.id, p2.id)))
        saveVaccine(
            VaccineDraft(
                petId = pet.id, petSpecies = Species.CAT, applicationDate = LocalDate(2025, 12, 21), nextDate = LocalDate(2026, 12, 21),
                typeIds = setOf("rabia", "calicivirus"), sourcePhotoIds = listOf(p2.id), vetName = "Veterinaria San Martín",
            ),
        )
        val deleted = (saveVaccine(VaccineDraft(petId = pet.id, petSpecies = Species.CAT, applicationDate = LocalDate(2023, 1, 1), typeIds = setOf("rabia"))) as SaveVaccineResult.Saved).record
        DeleteVaccineRecord(records, clock)(deleted.id)
        SaveWeight(weights, clock, ids)(pet.id, clock.today(), 3800)
        return pet.id
    }

    @Test
    fun byDefaultTheSheetHasVaccinesAndPhotosButNoOwnerAddressOrWeight() = runTest {
        val sheet = assertNotNull(build(lilith(), SheetOptions()))
        assertNull(sheet.pet.ownerName)
        assertNull(sheet.pet.ownerAddress)
        assertTrue(sheet.weights.isEmpty())
        assertEquals(2, sheet.vaccines.size)
        // Sin repetir la página que comparten las dos vacunas.
        assertEquals(2, sheet.cardPhotos.size)
        assertEquals(clock.today(), sheet.generatedOn)
    }

    @Test
    fun vaccinesAreActiveNewestFirstWithLabelsAndVet() = runTest {
        val sheet = assertNotNull(build(lilith(), SheetOptions()))
        assertEquals(listOf(LocalDate(2025, 12, 21), LocalDate(2024, 6, 29)), sheet.vaccines.map { it.record.applicationDate })
        assertEquals(setOf("Rabia", "Calicivirus"), sheet.vaccines.first().typeLabels.toSet())
        assertEquals("Veterinaria San Martín", sheet.vaccines.first().vet?.name)
        assertEquals(LocalDate(2026, 12, 21), sheet.nextDue?.date)
    }

    @Test
    fun ownerNameAndWeightAreAddedWhenChosenButNeverTheAddress() = runTest {
        val sheet = assertNotNull(build(lilith(), SheetOptions(includeOwnerName = true, includeWeight = true)))
        assertEquals("Tutor de Prueba", sheet.pet.ownerName)
        assertNull(sheet.pet.ownerAddress)
        assertEquals(listOf(3800), sheet.weights.map { it.grams })
    }

    @Test
    fun vaccinesAndPhotosCanBeLeftOut() = runTest {
        val sheet = assertNotNull(build(lilith(), SheetOptions(includeVaccines = false, includeCardPhotos = false)))
        assertTrue(sheet.vaccines.isEmpty())
        assertTrue(sheet.cardPhotos.isEmpty())
        assertEquals(LocalDate(2026, 12, 21), sheet.nextDue?.date) // la próxima dosis sigue en la ficha
    }

    @Test
    fun unknownPetHasNoSheet() = runTest {
        assertNull(build("no-existe", SheetOptions()))
    }
}
