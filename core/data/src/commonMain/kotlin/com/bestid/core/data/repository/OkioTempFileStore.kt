package com.bestid.core.data.repository

import com.bestid.core.domain.port.TempFileStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path.Companion.toPath

/**
 * Archivos para compartir o importar, en `<caché>/share/`. En Android esa carpeta es la que
 * expone el `FileProvider` a la hoja de compartir.
 */
class OkioTempFileStore(cacheDir: String, private val fs: FileSystem) : TempFileStore {
    private val dir = cacheDir.toPath() / SHARE_DIR

    override fun path(fileName: String): String {
        fs.createDirectories(dir)
        return (dir / fileName).toString()
    }

    override suspend fun delete(path: String) = withContext(Dispatchers.IO) {
        fs.delete(path.toPath(), mustExist = false)
    }

    companion object {
        const val SHARE_DIR = "share"
    }
}
