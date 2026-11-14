package com.bestid.core.domain.port

import com.bestid.core.domain.backup.BackupData
import com.bestid.core.domain.backup.BackupManifest
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

/** Lectura y escritura de todo lo que va en un respaldo (sección 13). */
interface BackupStore {
    /** Todas las entidades, incluidas las borradas lógicamente. Sin avisos programados (se recalculan). */
    suspend fun snapshot(): BackupData

    /** Inserta o reemplaza lo que trae [changes] (ya combinado). Las fotos van aparte con su archivo. */
    suspend fun apply(changes: BackupData)

    /** Borra todos los datos de la base (deja el catálogo semilla de vacunas). */
    suspend fun clearAll()
}

sealed interface ArchiveContent {
    data class Read(val manifest: BackupManifest, val data: BackupData) : ArchiveContent
    /** No es un respaldo de la app o está dañado. */
    data object Invalid : ArchiveContent
}

/** Archivo `.zip` del respaldo: `manifest.json`, `data.json` y `photos/`. */
interface BackupArchive {
    /** Escribe el respaldo en [target] (ruta absoluta), con los archivos de [BackupData.photos]. */
    suspend fun write(target: String, manifest: BackupManifest, data: BackupData)
    suspend fun read(source: String): ArchiveContent

    /** Bytes de una foto del respaldo (ruta relativa "photos/…"), o `null` si no está. */
    suspend fun readPhoto(source: String, relativePath: String): ByteArray?
}

/** Archivos temporales para compartir o importar (caché de la app). */
interface TempFileStore {
    /** Ruta absoluta de un archivo temporal con ese nombre; el directorio ya existe. */
    fun path(fileName: String): String
    suspend fun delete(path: String)
}

/** Datos de la app instalada. */
interface AppInfo {
    val versionName: String
}

/** Preferencias del respaldo (DataStore). */
interface BackupSettingsStore {
    val lastExportAt: Flow<Instant?>
    suspend fun setLastExportAt(at: Instant)
}
