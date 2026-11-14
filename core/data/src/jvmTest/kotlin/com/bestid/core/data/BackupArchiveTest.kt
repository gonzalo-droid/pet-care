package com.bestid.core.data

import androidx.room.Room
import com.bestid.core.data.backup.OkioBackupArchive
import com.bestid.core.data.backup.RoomBackupStore
import com.bestid.core.data.db.BestIdDatabase
import com.bestid.core.data.db.configured
import com.bestid.core.data.repository.OkioPhotoFileStore
import com.bestid.core.data.repository.RoomPetRepository
import com.bestid.core.data.repository.RoomPhotoRepository
import com.bestid.core.data.repository.RoomVaccineRecordRepository
import com.bestid.core.data.repository.RoomVaccineTypeRepository
import com.bestid.core.domain.backup.BACKUP_FORMAT_VERSION
import com.bestid.core.domain.backup.BackupManifest
import com.bestid.core.domain.port.ArchiveContent
import com.bestid.core.model.CareKind
import com.bestid.core.model.CareTask
import com.bestid.core.model.ExtractionMeta
import com.bestid.core.model.IntervalUnit
import com.bestid.core.model.Pet
import com.bestid.core.model.Photo
import com.bestid.core.model.RecordSource
import com.bestid.core.model.Sex
import com.bestid.core.model.Species
import com.bestid.core.model.VaccineItem
import com.bestid.core.model.VaccineRecord
import com.bestid.core.model.VaccineType
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import okio.FileSystem
import okio.Path.Companion.toPath
import java.nio.file.Files
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.time.Instant

class BackupArchiveTest {
    private val t0 = Instant.fromEpochMilliseconds(1_700_000_000_000)
    private val root = Files.createTempDirectory("bestid-backup").toString()
    private val fs = FileSystem.SYSTEM

    private val phoneA = Room.inMemoryDatabaseBuilder<BestIdDatabase>().configured()
    private val phoneB = Room.inMemoryDatabaseBuilder<BestIdDatabase>().configured()
    private val filesA = OkioPhotoFileStore("$root/a", fs)
    private val filesB = OkioPhotoFileStore("$root/b", fs)
    private val archiveA = OkioBackupArchive(fs, filesA)
    private val archiveB = OkioBackupArchive(fs, filesB)

    @AfterTest
    fun close() {
        phoneA.close()
        phoneB.close()
        fs.deleteRecursively(root.toPath())
    }

    private val lilith = Pet(
        "lilith", "Lilith", Species.CAT, "Mestiza", Sex.FEMALE, LocalDate(2023, 3, 1), null, "Mancha blanca en la cola",
        "Tutor de Prueba", null, "photos/foto-lilith.jpg", t0, t0, null,
    )
    private val record = VaccineRecord(
        id = "v1", petId = "lilith", applicationDate = LocalDate(2025, 12, 21), nextDate = LocalDate(2026, 12, 21),
        nextDateConfirmed = true, nextDateLabel = "Próx. rabia", typeIds = listOf("rabia", "custom:leucemia"), typeIdsToConfirm = emptyList(),
        items = listOf(VaccineItem("Defensor 3 (antirrábica)", "766517", LocalDate(2024, 9, 19), LocalDate(2026, 3, 18))),
        vetId = null, source = RecordSource.MANUAL, sourcePhotoIds = listOf("pagina-1"), stickerPhotoId = null,
        extraction = ExtractionMeta("prueba", mapOf("nextDate" to 0.4), 1), createdAt = t0, updatedAt = t0, deletedAt = null,
    )
    private val deletedRecord = record.copy(id = "v0", applicationDate = LocalDate(2024, 6, 29), deletedAt = t0)
    private val care = CareTask("c1", "lilith", CareKind.DEWORM_INTERNAL, null, LocalDate(2026, 10, 1), 3, IntervalUnit.MONTH, LocalDate(2027, 1, 1), true, t0, t0, null)
    private val photos = listOf(
        Photo("pagina-1", "photos/pagina-1.jpg", 2000, 1500, t0),
        Photo("foto-lilith", "photos/foto-lilith.jpg", 800, 800, t0),
        // Una fila sin archivo: no debe ir en el respaldo.
        Photo("sin-archivo", "photos/sin-archivo.jpg", 10, 10, t0),
    )

    private suspend fun seedPhoneA() {
        RoomPetRepository(phoneA).upsert(lilith)
        RoomVaccineTypeRepository(phoneA).insert(VaccineType("custom:leucemia", "Leucemia felina", setOf(Species.CAT), true))
        RoomVaccineRecordRepository(phoneA).apply { upsert(record); upsert(deletedRecord) }
        phoneA.careTaskDao().upsert(with(care) { com.bestid.core.data.db.CareTaskEntity(id, petId, kind.name, title, lastDate.toString(), intervalValue, intervalUnit?.name, nextDate.toString(), reminderEnabled, t0.toEpochMilliseconds(), t0.toEpochMilliseconds(), null) })
        photos.forEach { RoomPhotoRepository(phoneA).insert(it) }
        filesA.write("photos/pagina-1.jpg", byteArrayOf(1, 2, 3, 4))
        filesA.write("photos/foto-lilith.jpg", byteArrayOf(5, 6))
    }

    @Test
    fun backupFromOnePhoneRestoresEverythingOnAnother() = runTest {
        seedPhoneA()
        val zip = "$root/share/respaldo.zip"
        archiveA.write(zip, BackupManifest(BACKUP_FORMAT_VERSION, t0, "0.4.0"), RoomBackupStore(phoneA).snapshot())

        val read = assertIs<ArchiveContent.Read>(archiveB.read(zip))
        assertEquals(BackupManifest(BACKUP_FORMAT_VERSION, t0, "0.4.0"), read.manifest)
        assertEquals(listOf("foto-lilith", "pagina-1"), read.data.photos.map { it.id }.sorted())
        assertContentEquals(byteArrayOf(1, 2, 3, 4), archiveB.readPhoto(zip, "photos/pagina-1.jpg"))
        assertNull(archiveB.readPhoto(zip, "photos/sin-archivo.jpg"))

        RoomBackupStore(phoneB).apply(read.data)
        val restored = RoomBackupStore(phoneB).snapshot()
        assertEquals(listOf(lilith), restored.pets)
        assertEquals(setOf(record, deletedRecord), restored.records.toSet())
        assertEquals(listOf(care), restored.cares)
        assertEquals(listOf("Leucemia felina"), restored.vaccineTypes.map { it.label })
    }

    @Test
    fun theZipIsStandardAndOtherAppsCanOpenIt() = runTest {
        seedPhoneA()
        val zip = "$root/share/respaldo.zip"
        archiveA.write(zip, BackupManifest(BACKUP_FORMAT_VERSION, t0, "0.4.0"), RoomBackupStore(phoneA).snapshot())
        ZipFile(zip).use { file ->
            assertEquals(setOf("manifest.json", "data.json", "photos/pagina-1.jpg", "photos/foto-lilith.jpg"), file.entries().toList().map { it.name }.toSet())
            // ZipFile verifica el CRC al leer hasta el final.
            assertContentEquals(byteArrayOf(1, 2, 3, 4), file.getInputStream(file.getEntry("photos/pagina-1.jpg")).readBytes())
            file.getInputStream(file.getEntry("data.json")).readBytes()
        }
    }

    @Test
    fun aCompressedZipMadeByAnotherAppIsReadToo() = runTest {
        seedPhoneA()
        val original = "$root/share/respaldo.zip"
        archiveA.write(original, BackupManifest(BACKUP_FORMAT_VERSION, t0, "0.4.0"), RoomBackupStore(phoneA).snapshot())
        // Por ejemplo, alguien lo descomprime y lo vuelve a comprimir en la computadora.
        val recompressed = "$root/share/recomprimido.zip"
        ZipFile(original).use { input ->
            ZipOutputStream(Files.newOutputStream(java.nio.file.Path.of(recompressed))).use { out ->
                input.entries().toList().forEach { e ->
                    out.putNextEntry(ZipEntry(e.name).apply { method = ZipEntry.DEFLATED })
                    out.write(input.getInputStream(e).readBytes())
                    out.closeEntry()
                }
            }
        }
        val read = assertIs<ArchiveContent.Read>(archiveB.read(recompressed))
        assertEquals(listOf(lilith), read.data.pets)
        assertContentEquals(byteArrayOf(5, 6), archiveB.readPhoto(recompressed, "photos/foto-lilith.jpg"))
    }

    @Test
    fun aFileThatIsNotABackupIsInvalid() = runTest {
        val path = "$root/otro.zip"
        fs.write(path.toPath()) { writeUtf8("no soy un zip") }
        assertEquals(ArchiveContent.Invalid, archiveB.read(path))
        assertEquals(ArchiveContent.Invalid, archiveB.read("$root/no-existe.zip"))
    }

    @Test
    fun clearAllKeepsTheSeedCatalog() = runTest {
        seedPhoneA()
        RoomBackupStore(phoneA).clearAll()
        val left = RoomBackupStore(phoneA).snapshot()
        assertEquals(true, left.isEmpty)
        assertEquals(true, RoomVaccineTypeRepository(phoneA).getAll().isNotEmpty())
    }
}
