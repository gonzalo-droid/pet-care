package com.bestid.core.data.backup

import com.bestid.core.domain.backup.BackupData
import com.bestid.core.domain.backup.BackupManifest
import com.bestid.core.domain.port.ArchiveContent
import com.bestid.core.domain.port.BackupArchive
import com.bestid.core.domain.port.PhotoFileStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.buffer
import okio.openZip

/**
 * Respaldo en `.zip` (sección 13): `manifest.json`, `data.json` y `photos/<id>.jpg`.
 * Se escribe sin compresión y se lee con okio, que también abre zips comprimidos por otra app.
 */
class OkioBackupArchive(private val fs: FileSystem, private val photoFiles: PhotoFileStore) : BackupArchive {

    override suspend fun write(target: String, manifest: BackupManifest, data: BackupData) = withContext(Dispatchers.IO) {
        val path = target.toPath()
        path.parent?.let { fs.createDirectories(it) }
        fs.sink(path).buffer().use { sink ->
            val zip = ZipWriter(sink, manifest.createdAt.toLocalDateTime(TimeZone.UTC))
            // Solo las fotos cuyo archivo existe: así el respaldo nunca promete una foto que no trae.
            val photos = data.photos.filter { photo ->
                val bytes = runCatching { fs.read(photoFiles.absolutePath(photo.path).toPath()) { readByteArray() } }.getOrNull()
                    ?: return@filter false
                zip.add(photo.path, bytes)
                true
            }
            zip.add(DATA, backupJson.encodeToString(DataJson.serializer(), data.copy(photos = photos).toJson()).encodeToByteArray())
            zip.add(MANIFEST, backupJson.encodeToString(ManifestJson.serializer(), manifest.toJson()).encodeToByteArray())
            zip.finish()
        }
    }

    override suspend fun read(source: String): ArchiveContent = withContext(Dispatchers.IO) {
        runCatching {
            val zip = fs.openZip(source.toPath())
            val manifest = zip.read("/$MANIFEST".toPath()) { backupJson.decodeFromString(ManifestJson.serializer(), readUtf8()) }
            val data = zip.read("/$DATA".toPath()) { backupJson.decodeFromString(DataJson.serializer(), readUtf8()) }
            ArchiveContent.Read(manifest.toModel(), data.toModel())
        }.getOrElse { ArchiveContent.Invalid }
    }

    override suspend fun readPhoto(source: String, relativePath: String): ByteArray? = withContext(Dispatchers.IO) {
        runCatching { fs.openZip(source.toPath()).read("/$relativePath".toPath()) { readByteArray() } }.getOrNull()
    }

    private companion object {
        const val MANIFEST = "manifest.json"
        const val DATA = "data.json"
    }
}
