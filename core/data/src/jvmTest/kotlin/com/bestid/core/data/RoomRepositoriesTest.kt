package com.bestid.core.data

import androidx.room.Room
import com.bestid.core.data.db.BestIdDatabase
import com.bestid.core.data.db.configured
import com.bestid.core.data.repository.RoomPetRepository
import com.bestid.core.data.repository.RoomVaccineRecordRepository
import com.bestid.core.data.repository.RoomVaccineTypeRepository
import com.bestid.core.model.ExtractionMeta
import com.bestid.core.model.Pet
import com.bestid.core.model.RecordSource
import com.bestid.core.model.Sex
import com.bestid.core.model.Species
import com.bestid.core.model.VaccineItem
import com.bestid.core.model.VaccineRecord
import com.bestid.core.model.VaccineType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class RoomRepositoriesTest {
    private val db = Room.inMemoryDatabaseBuilder<BestIdDatabase>().configured()
    private val pets = RoomPetRepository(db)
    private val records = RoomVaccineRecordRepository(db)
    private val types = RoomVaccineTypeRepository(db)
    private val t0 = Instant.fromEpochMilliseconds(1_700_000_000_000)

    @AfterTest
    fun close() = db.close()

    private fun pet(id: String, deleted: Boolean = false) = Pet(
        id = id, name = "Lilith", species = Species.CAT, breed = null, sex = Sex.FEMALE,
        birthDate = LocalDate(2023, 3, 1), birthDatePrecision = null, distinguishingMarks = null,
        ownerName = "Tutor de Prueba", ownerAddress = null, photoPath = null,
        createdAt = t0, updatedAt = t0, deletedAt = if (deleted) t0 else null,
    )

    private fun record(id: String, petId: String, date: LocalDate) = VaccineRecord(
        id = id, petId = petId, applicationDate = date, nextDate = null, nextDateConfirmed = false,
        nextDateLabel = "Próx. leucemia", typeIds = listOf("calicivirus", "rinotraqueitis"),
        typeIdsToConfirm = listOf("rabia"),
        items = listOf(VaccineItem("Felocell 3", "774233A", LocalDate(2024, 11, 8), LocalDate(2026, 5, 5))),
        vetId = null, source = RecordSource.MANUAL, sourcePhotoIds = listOf("photo-1"), stickerPhotoId = null,
        extraction = ExtractionMeta("test/model/2026-11-13", mapOf("nextDate" to 0.2), 1),
        createdAt = t0, updatedAt = t0, deletedAt = null,
    )

    @Test
    fun petRoundTripAndSoftDelete() = runTest {
        pets.upsert(pet("a"))
        pets.upsert(pet("b", deleted = true))
        assertEquals(listOf("a"), pets.observePets().first().map { it.id })
        assertEquals(pet("a"), pets.get("a"))
        assertNull(pets.observePet("b").first())
        assertEquals("b", pets.get("b")?.id) // borradas siguen disponibles para restaurar
    }

    @Test
    fun recordRoundTripKeepsListsAndDates() = runTest {
        val r = record("r1", "a", LocalDate(2025, 12, 21))
        records.upsert(r)
        assertEquals(r, records.get("r1"))
    }

    @Test
    fun recordsAreFilteredByPetAndSortedNewestFirst() = runTest {
        records.upsert(record("old", "a", LocalDate(2024, 6, 29)))
        records.upsert(record("new", "a", LocalDate(2025, 12, 21)))
        records.upsert(record("other", "b", LocalDate(2025, 1, 1)))
        assertEquals(listOf("new", "old"), records.observeForPet("a").first().map { it.id })

        records.upsert(records.get("new")!!.copy(deletedAt = t0))
        assertEquals(listOf("old"), records.observeForPet("a").first().map { it.id })
    }

    @Test
    fun catalogIsSeededOnceAndCustomTypesAreAdded() = runTest {
        assertEquals(12, types.getAll().size)
        types.insert(VaccineType("custom:1", "Giardia", setOf(Species.DOG), isCustom = true))
        val all = types.observeAll().first()
        assertEquals(13, all.size)
        assertTrue(all.last().isCustom)
        assertEquals(setOf(Species.DOG, Species.CAT), all.first { it.id == "rabia" }.species)
    }
}
