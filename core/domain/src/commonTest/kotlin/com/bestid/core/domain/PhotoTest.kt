package com.bestid.core.domain

import com.bestid.core.domain.catalog.VaccineCatalog
import com.bestid.core.domain.usecase.CleanUpPhotos
import com.bestid.core.domain.usecase.DeleteVaccineRecord
import com.bestid.core.domain.usecase.GetPhotoFiles
import com.bestid.core.domain.usecase.ImportPhotos
import com.bestid.core.domain.usecase.PetDraft
import com.bestid.core.domain.usecase.PhotoFile
import com.bestid.core.domain.usecase.SavePet
import com.bestid.core.domain.usecase.SavePetResult
import com.bestid.core.domain.usecase.SaveVaccineRecord
import com.bestid.core.domain.usecase.SaveVaccineResult
import com.bestid.core.domain.usecase.VaccineDraft
import com.bestid.core.model.Species
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlinx.datetime.DatePeriod
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PhotoTest {
    private val clock = FakeClock()
    private val ids = SequentialIds()
    private val pets = FakePetRepository()
    private val records = FakeRecordRepository()
    private val photos = FakePhotoRepository()
    private val files = FakePhotoFileStore()

    private val import = ImportPhotos(FakeImageProcessor(), files, photos, clock, ids)
    private val savePet = SavePet(pets, FakeSelectedPetStore(), clock, ids)
    private val saveVaccine = SaveVaccineRecord(records, FakeTypeRepository(VaccineCatalog.seed), FakeVetRepository(), clock, ids)
    private val cleanUp = CleanUpPhotos(photos, files, pets, records, clock)

    private fun page(n: Int) = byteArrayOf(1, n.toByte())

    @Test
    fun importsPagesIntoThePhotosDirectory() = runTest {
        val result = import(listOf(page(1), page(2)))
        assertEquals(0, result.failed)
        assertEquals(listOf("photos/id-1.jpg", "photos/id-2.jpg"), result.photos.map { it.path })
        assertEquals(setOf("photos/id-1.jpg", "photos/id-2.jpg"), files.files.keys)
        assertEquals(2000, result.photos.first().width)
    }

    @Test
    fun unreadableImageIsCountedAndNotSaved() = runTest {
        val result = import(listOf(page(1), byteArrayOf(0, 0)))
        assertEquals(1, result.photos.size)
        assertEquals(1, result.failed)
        assertEquals(1, files.files.size)
    }

    @Test
    fun vaccineKeepsItsPagesAndStickerInOrder() = runTest {
        val lilith = (savePet(PetDraft(name = "Lilith", species = Species.CAT)) as SavePetResult.Saved).pet
        val (p1, p2, sticker) = import(listOf(page(1), page(2), page(3))).photos
        val saved = saveVaccine(
            VaccineDraft(
                petId = lilith.id, petSpecies = Species.CAT,
                applicationDate = LocalDate(2025, 12, 21),
                typeIds = setOf("rabia"),
                sourcePhotoIds = listOf(p2.id, p1.id, p2.id),
                stickerPhotoId = sticker.id,
            ),
        ) as SaveVaccineResult.Saved
        assertEquals(listOf(p2.id, p1.id), saved.record.sourcePhotoIds)
        assertEquals(sticker.id, saved.record.stickerPhotoId)

        val shown = GetPhotoFiles(photos, files)(saved.record.sourcePhotoIds + "missing")
        assertEquals(listOf(PhotoFile(p2.id, "/app/${p2.path}"), PhotoFile(p1.id, "/app/${p1.path}")), shown)
    }

    @Test
    fun petPhotoIsSavedAndCanBeRemoved() = runTest {
        val photo = import(listOf(page(1))).photos.single()
        val pet = (savePet(PetDraft(name = "Lilith", species = Species.CAT, photoPath = photo.path)) as SavePetResult.Saved).pet
        assertEquals(photo.path, pet.photoPath)
        val edited = (savePet(PetDraft(id = pet.id, name = "Lilith", species = Species.CAT, photoPath = null)) as SavePetResult.Saved).pet
        assertEquals(null, edited.photoPath)
    }

    @Test
    fun cleanUpKeepsUsedAndRecentPhotosAndDeletesTheRest() = runTest {
        val (used, removed, abandoned, petPhoto, deletedRecordPhoto) = import((1..5).map(::page)).photos
        val lilith = (savePet(PetDraft(name = "Lilith", species = Species.CAT, photoPath = petPhoto.path)) as SavePetResult.Saved).pet
        val draft = VaccineDraft(petId = lilith.id, petSpecies = Species.CAT, applicationDate = LocalDate(2025, 12, 21), typeIds = setOf("rabia"))
        saveVaccine(draft.copy(sourcePhotoIds = listOf(used.id, removed.id)))
        val record = records.all.value.single()
        // Se quita una página del registro.
        saveVaccine(draft.copy(id = record.id, sourcePhotoIds = listOf(used.id)))
        // Un registro borrado conserva su foto (se puede restaurar).
        val other = (saveVaccine(draft.copy(sourcePhotoIds = listOf(deletedRecordPhoto.id))) as SaveVaccineResult.Saved).record
        DeleteVaccineRecord(records, clock)(other.id)
        files.files["photos/suelta.jpg"] = byteArrayOf(1)

        // Dentro del margen no se borra nada guardado en la base, solo el archivo suelto.
        assertEquals(1, cleanUp())
        assertEquals(5, photos.all.value.size)

        clock.today = clock.today.plus(DatePeriod(days = 2))
        val fresh = import(listOf(page(9))).photos.single()
        assertEquals(2, cleanUp())
        val left = photos.all.value.map { it.id }.toSet()
        assertEquals(setOf(used.id, petPhoto.id, deletedRecordPhoto.id, fresh.id), left)
        assertTrue(removed.path !in files.files)
        assertTrue(abandoned.path !in files.files) // tomada en un formulario que no se guardó
        assertEquals(left.size, files.files.size)
    }
}
