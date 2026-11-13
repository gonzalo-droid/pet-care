package com.bestid.core.data.repository

import com.bestid.core.domain.port.PhotoFileStore
import com.bestid.core.domain.usecase.PHOTOS_DIR
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath

/**
 * Fotos en el directorio privado de la app ([baseDir]): `filesDir` en Android y Application Support
 * en iOS (sección 9). Se escribe en un archivo temporal y se renombra para no dejar fotos a medias.
 */
class OkioPhotoFileStore(baseDir: String, private val fs: FileSystem) : PhotoFileStore {
    private val base: Path = baseDir.toPath()

    override suspend fun write(relativePath: String, bytes: ByteArray) = withContext(Dispatchers.IO) {
        val target = base / relativePath
        val dir = requireNotNull(target.parent)
        fs.createDirectories(dir)
        val temp = dir / "${target.name}.tmp"
        fs.write(temp) { write(bytes) }
        fs.atomicMove(temp, target)
    }

    override suspend fun delete(relativePath: String) = withContext(Dispatchers.IO) {
        fs.delete(base / relativePath, mustExist = false)
    }

    override suspend fun list(): List<String> = withContext(Dispatchers.IO) {
        val dir = base / PHOTOS_DIR
        fs.listOrNull(dir).orEmpty().map { "$PHOTOS_DIR/${it.name}" }
    }

    override fun absolutePath(relativePath: String): String = (base / relativePath).toString()
}
