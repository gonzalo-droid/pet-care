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
    fun byDefaultTheSheetHasNoOwnerAddressWeightOrPhotos() = runTest {
        val sheet = assertNotNull(build(lilith(), SheetOptions()))
        assertNull(sheet.pet.ownerName)
        assertNull(sheet.pet.ownerAddress)
        assertTrue(sheet.weights.isEmpty())
        assertTrue(sheet.cardPhotos.isEmpty())
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
    fun ownerWeightAndPhotosAreAddedWhenChosen() = runTest {
        val sheet = assertNotNull(build(lilith(), SheetOptions(includeOwner = true, includeWeight = true, includeCardPhotos = true)))
        assertEquals("Tutor de Prueba", sheet.pet.ownerName)
        assertEquals("Av. Siempre Viva 123", sheet.pet.ownerAddress)
        assertEquals(listOf(3800), sheet.weights.map { it.grams })
        // Sin repetir la página que comparten las dos vacunas.
        assertEquals(2, sheet.cardPhotos.size)
    }

    @Test
    fun unknownPetHasNoSheet() = runTest {
        assertNull(build("no-existe", SheetOptions()))
    }
}
