package com.bestid.core.domain.backup

import com.bestid.core.domain.port.AppClock
import com.bestid.core.domain.port.AppInfo
import com.bestid.core.domain.port.ArchiveContent
import com.bestid.core.domain.port.BackupArchive
import com.bestid.core.domain.port.BackupSettingsStore
import com.bestid.core.domain.port.BackupStore
import com.bestid.core.domain.port.PhotoFileStore
import com.bestid.core.domain.port.PhotoRepository
import com.bestid.core.domain.port.SelectedPetStore
import com.bestid.core.domain.port.TempFileStore
import com.bestid.core.domain.reminder.SyncReminders

/** Nombre de foto aceptado al importar: evita rutas fuera de `photos/` en un zip manipulado. */
private val SAFE_PHOTO_PATH = Regex("^photos/[A-Za-z0-9_-]+\\.jpg$")

/** Exporta el respaldo a un archivo temporal y devuelve su ruta, lista para compartir. */
class ExportBackup(
    private val store: BackupStore,
    private val archive: BackupArchive,
    private val temp: TempFileStore,
    private val settings: BackupSettingsStore,
    private val clock: AppClock,
    private val appInfo: AppInfo,
) {
    suspend operator fun invoke(fileName: String): String {
        val now = clock.now()
        val target = temp.path(fileName)
        archive.write(target, BackupManifest(BACKUP_FORMAT_VERSION, now, appInfo.versionName), store.snapshot())
        settings.setLastExportAt(now)
        return target
    }
}

sealed interface ImportResult {
    data class Imported(val summary: BackupSummary) : ImportResult
    /** El respaldo es de una versión más nueva de la app: hay que actualizarla. */
    data object TooNew : ImportResult
    /** No es un respaldo de BestID o está dañado. */
    data object Invalid : ImportResult
}

/**
 * Importa un respaldo (sección 13): valida la versión, combina por ID (gana el `updatedAt` más
 * reciente) y copia las fotos que faltan. Los avisos se reprograman con los datos nuevos.
 */
class ImportBackup(
    private val archive: BackupArchive,
    private val store: BackupStore,
    private val photos: PhotoRepository,
    private val files: PhotoFileStore,
    private val sync: SyncReminders,
) {
    suspend operator fun invoke(source: String): ImportResult {
        val content = archive.read(source) as? ArchiveContent.Read ?: return ImportResult.Invalid
        if (content.manifest.formatVersion > BACKUP_FORMAT_VERSION) return ImportResult.TooNew
        val changes = BackupMerge.changes(store.snapshot(), content.data)

        // Cada foto con su archivo; una foto sin archivo en el zip no se agrega.
        val copied = changes.photos.filter { photo ->
            if (!SAFE_PHOTO_PATH.matches(photo.path)) return@filter false
            val bytes = archive.readPhoto(source, photo.path) ?: return@filter false
            photos.insert(photo)
            files.write(photo.path, bytes)
            true
        }
        val applied = changes.copy(photos = copied)
        store.apply(applied.copy(photos = emptyList()))
        sync()
        return ImportResult.Imported(BackupMerge.summary(applied))
    }
}

/** "Borrar todos los datos" (sección 14): base, fotos, mascota seleccionada y avisos. */
class DeleteAllData(
    private val store: BackupStore,
    private val files: PhotoFileStore,
    private val selected: SelectedPetStore,
    private val sync: SyncReminders,
) {
    suspend operator fun invoke() {
        store.clearAll()
        files.list().forEach { files.delete(it) }
        selected.select(null)
        // Sin datos no hay avisos deseados: cancela todos los programados.
        sync()
    }
}
