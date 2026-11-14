package com.bestid.core.domain

import com.bestid.core.domain.backup.BACKUP_FORMAT_VERSION
import com.bestid.core.domain.backup.BackupSummary
import com.bestid.core.domain.backup.DeleteAllData
import com.bestid.core.domain.backup.ExportBackup
import com.bestid.core.domain.backup.ImportBackup
import com.bestid.core.domain.backup.ImportResult
import com.bestid.core.domain.catalog.VaccineCatalog
import com.bestid.core.domain.port.AppInfo
import com.bestid.core.domain.reminder.SyncReminders
import com.bestid.core.domain.usecase.DeleteVaccineRecord
import com.bestid.core.domain.usecase.ImportPhotos
import com.bestid.core.domain.usecase.PetDraft
import com.bestid.core.domain.usecase.SavePet
import com.bestid.core.domain.usecase.SavePetResult
import com.bestid.core.domain.usecase.SaveVaccineRecord
import com.bestid.core.domain.usecase.SaveVaccineResult
import com.bestid.core.domain.usecase.VaccineDraft
import com.bestid.core.model.Photo
import com.bestid.core.model.Species
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Dos teléfonos que comparten los archivos de respaldo (como pasarlos por WhatsApp o Drive). */
class BackupTest {
    private val shared = mutableMapOf<String, FakeBackupArchive.Zip>()

    private inner class Phone(prefix: String) {
        val clock = FakeClock()
        val ids = IdGenerator(prefix)
        val pets = FakePetRepository()
        val vets = FakeVetRepository()
        val types = FakeTypeRepository(VaccineCatalog.seed)
        val records = FakeRecordRepository()
        val cares = FakeCareRepository()
        val weights = FakeWeightRepository()
        val photos = FakePhotoRepository()
        val files = FakePhotoFileStore()
        val selected = FakeSelectedPetStore()
        val scheduled = FakeScheduledRepository()
        val scheduler = FakeScheduler()
        val settings = FakeBackupSettings()
        val archive = FakeBackupArchive(files).also { it.zips.putAll(shared) }
        val store = FakeBackupStore(pets, vets, types, records, cares, weights, photos)
        val sync = SyncReminders(pets, records, cares, types, FakeSettingsStore(), scheduled, scheduler, clock) { TimeZone.of("America/Lima") }
        val savePet = SavePet(pets, selected, clock, ids)
        val saveVaccine = SaveVaccineRecord(records, types, vets, clock, ids)
        val importPhotos = ImportPhotos(FakeImageProcessor(), files, photos, clock, ids)
        val export = ExportBackup(store, archive, FakeTempFiles(), settings, clock, object : AppInfo { override val versionName = "0.4.0" })
        val import = ImportBackup(archive, store, photos, files, sync)

        suspend fun exportTo(name: String): String = export(name).also { shared[it] = archive.zips.getValue(it) }
        suspend fun importFrom(path: String): ImportResult {
            archive.zips.putAll(shared)
            return import(path)
        }
    }

    private class IdGenerator(private val prefix: String) : com.bestid.core.domain.port.IdGenerator {
        private var next = 1
        override fun newId() = "$prefix-${next++}"
    }

    /** Lilith con su vacuna de rabia (próxima dosis en el futuro), una página de la cartilla y un registro borrado. */
    private suspend fun Phone.lilith(): String {
        val pet = (savePet(PetDraft(name = "Lilith", species = Species.CAT, ownerName = "Tutor de Prueba")) as SavePetResult.Saved).pet
        val page = importPhotos(listOf(byteArrayOf(1, 2, 3))).photos.single()
        saveVaccine(
            VaccineDraft(
                petId = pet.id, petSpecies = Species.CAT, applicationDate = LocalDate(2025, 12, 21),
                nextDate = LocalDate(2026, 12, 21), typeIds = setOf("rabia"), sourcePhotoIds = listOf(page.id),
                customTypeLabels = listOf("Leucemia felina"), vetName = "Veterinaria San Martín",
            ),
        )
        val old = (saveVaccine(VaccineDraft(petId = pet.id, petSpecies = Species.CAT, applicationDate = LocalDate(2024, 6, 29), typeIds = setOf("rabia"))) as SaveVaccineResult.Saved).record
        DeleteVaccineRecord(records, clock)(old.id)
        return pet.id
    }

    @Test
    fun exportKeepsEverythingIncludingDeletedAndRemembersTheDate() = runTest {
        val android = Phone("a")
        android.lilith()
        val path = android.exportTo("respaldo.zip")
        val zip = shared.getValue(path)
        assertEquals(BACKUP_FORMAT_VERSION, zip.manifest.formatVersion)
        assertEquals("0.4.0", zip.manifest.appVersion)
        assertEquals(2, zip.data.records.size)
        assertEquals(1, zip.data.records.count { it.deletedAt != null })
        assertEquals(listOf("Leucemia felina"), zip.data.vaccineTypes.map { it.label })
        assertEquals(1, zip.photos.size)
        assertEquals(android.clock.now(), android.settings.last.value)
    }

    @Test
    fun androidBackupRestoresOnANewIphone() = runTest {
        val android = Phone("a")
        val petId = android.lilith()
        val path = android.exportTo("respaldo.zip")

        val iphone = Phone("i")
        val result = assertIs<ImportResult.Imported>(iphone.importFrom(path))
        assertEquals(BackupSummary(pets = 1, records = 1, cares = 0, weights = 0, photos = 1), result.summary)
        assertEquals("Lilith", iphone.pets.get(petId)?.name)
        assertEquals(1, iphone.records.observeForPet(petId).first().size)
        assertNotNull(iphone.records.all.value.firstOrNull { it.deletedAt != null }) // el borrado también viaja
        assertTrue(iphone.types.all.value.any { it.label == "Leucemia felina" })
        assertEquals(android.files.files.values.single().toList(), iphone.files.files.values.single().toList())
        // Los avisos de la próxima dosis quedan programados en el teléfono nuevo.
        assertTrue(iphone.scheduler.scheduled.isNotEmpty())
    }

    @Test
    fun theMostRecentChangeWinsInBothDirections() = runTest {
        val android = Phone("a")
        val petId = android.lilith()
        val iphone = Phone("i")
        val first = android.exportTo("1.zip")
        iphone.importFrom(first)

        // En el iPhone se edita después: gana al volver a importar el respaldo viejo de Android.
        iphone.clock.today = iphone.clock.today.plus(DatePeriod(days = 1))
        iphone.savePet(PetDraft(id = petId, name = "Lilith Editada", species = Species.CAT))
        val again = assertIs<ImportResult.Imported>(iphone.importFrom(first))
        assertEquals(0, again.summary.pets)
        assertEquals("Lilith Editada", iphone.pets.get(petId)?.name)

        // Y al revés: el respaldo del iPhone actualiza Android.
        val back = assertIs<ImportResult.Imported>(android.importFrom(iphone.exportTo("2.zip")))
        assertEquals(1, back.summary.pets)
        assertEquals("Lilith Editada", android.pets.get(petId)?.name)
    }

    @Test
    fun backupFromANewerAppIsRejectedWithoutChanges() = runTest {
        val android = Phone("a")
        android.lilith()
        val path = android.exportTo("nuevo.zip")
        val zip = shared.getValue(path)
        shared[path] = FakeBackupArchive.Zip(zip.manifest.copy(formatVersion = BACKUP_FORMAT_VERSION + 1), zip.data, zip.photos)

        val iphone = Phone("i")
        assertEquals(ImportResult.TooNew, iphone.importFrom(path))
        assertTrue(iphone.pets.all.value.isEmpty())
        assertEquals(ImportResult.Invalid, iphone.importFrom("no-existe.zip"))
    }

    @Test
    fun photoWithAnUnsafePathIsSkipped() = runTest {
        val android = Phone("a")
        android.lilith()
        val path = android.exportTo("raro.zip")
        val zip = shared.getValue(path)
        val evil = Photo("evil", "../../bestid.db", 1, 1, android.clock.now())
        shared[path] = FakeBackupArchive.Zip(zip.manifest, zip.data.copy(photos = zip.data.photos + evil), zip.photos + ("../../bestid.db" to byteArrayOf(9)))

        val iphone = Phone("i")
        val result = assertIs<ImportResult.Imported>(iphone.importFrom(path))
        assertEquals(1, result.summary.photos)
        assertTrue(iphone.files.files.keys.all { it.startsWith("photos/") })
    }

    @Test
    fun deleteAllDataClearsEverythingAndCancelsReminders() = runTest {
        val phone = Phone("a")
        phone.lilith()
        phone.sync()
        assertTrue(phone.scheduler.scheduled.isNotEmpty())

        DeleteAllData(phone.store, phone.files, phone.selected, phone.sync)()
        assertTrue(phone.store.snapshot().isEmpty)
        assertTrue(phone.files.files.isEmpty())
        assertNull(phone.selected.value.value)
        assertTrue(phone.scheduler.scheduled.isEmpty())
        assertTrue(phone.types.all.value.isNotEmpty()) // el catálogo semilla se queda
    }
}
