package com.bestid.core.domain.port

import com.bestid.core.model.Photo

/** Imagen lista para guardar: JPEG ya rotado, reducido y sin metadatos (sin ubicación). */
class ProcessedImage(val jpeg: ByteArray, val width: Int, val height: Int)

/**
 * Puerto de plataforma (sección 8): rota según EXIF, reduce a ~2000 px de lado largo, JPEG 85 y
 * quita metadatos. Devuelve `null` si los bytes no son una imagen que se pueda leer.
 */
interface ImageProcessor {
    suspend fun process(original: ByteArray): ProcessedImage?
}

/** Archivos de fotos dentro del directorio privado de la app. Las rutas son relativas a ese directorio. */
interface PhotoFileStore {
    suspend fun write(relativePath: String, bytes: ByteArray)
    suspend fun delete(relativePath: String)

    /** Rutas relativas de todos los archivos del directorio de fotos. */
    suspend fun list(): List<String>

    /** Ruta absoluta para mostrar la foto. En iOS cambia entre instalaciones: nunca se guarda. */
    fun absolutePath(relativePath: String): String
}

interface PhotoRepository {
    suspend fun insert(photo: Photo)
    suspend fun get(ids: List<String>): List<Photo>
    suspend fun getAll(): List<Photo>
    suspend fun delete(ids: List<String>)
}
