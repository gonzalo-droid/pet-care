package com.bestid.core.domain.usecase

import com.bestid.core.domain.port.AppClock
import com.bestid.core.domain.port.IdGenerator
import com.bestid.core.domain.port.ImageProcessor
import com.bestid.core.domain.port.PetRepository
import com.bestid.core.domain.port.PhotoFileStore
import com.bestid.core.domain.port.PhotoRepository
import com.bestid.core.domain.port.VaccineRecordRepository
import com.bestid.core.model.Photo
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

/** Directorio de fotos dentro del directorio privado de la app. */
const val PHOTOS_DIR = "photos"

/** Resultado de [ImportPhotos]: las fotos guardadas y cuántas no se pudieron leer. */
data class ImportedPhotos(val photos: List<Photo>, val failed: Int)

/**
 * Procesa (rota, reduce, quita metadatos) y guarda fotos en el directorio privado.
 * La fila se inserta antes de escribir el archivo para que [CleanUpPhotos] nunca vea un archivo
 * sin fila mientras se importa.
 */
class ImportPhotos(
    private val processor: ImageProcessor,
    private val files: PhotoFileStore,
    private val photos: PhotoRepository,
    private val clock: AppClock,
    private val ids: IdGenerator,
) {
    suspend operator fun invoke(originals: List<ByteArray>): ImportedPhotos {
        val saved = mutableListOf<Photo>()
        var failed = 0
        for (original in originals) {
            val image = processor.process(original)
            if (image == null) {
                failed++
                continue
            }
            val id = ids.newId()
            val photo = Photo(id, "$PHOTOS_DIR/$id.jpg", image.width, image.height, clock.now())
            photos.insert(photo)
            files.write(photo.path, image.jpeg)
            saved += photo
        }
        return ImportedPhotos(saved, failed)
    }
}

/** Foto lista para mostrar: [path] es absoluta y solo vale mientras la app corre. */
data class PhotoFile(val id: String, val path: String)

/** Fotos por ID, en el mismo orden, para mostrarlas. Las que no existen se omiten. */
class GetPhotoFiles(private val photos: PhotoRepository, private val files: PhotoFileStore) {
    suspend operator fun invoke(ids: List<String>): List<PhotoFile> {
        if (ids.isEmpty()) return emptyList()
        val byId = photos.get(ids).associateBy { it.id }
        return ids.mapNotNull { id -> byId[id]?.let { PhotoFile(it.id, files.absolutePath(it.path)) } }
    }

    /** Ruta absoluta de una ruta relativa guardada (por ejemplo, la foto de la mascota). */
    fun absolute(relativePath: String?): String? = relativePath?.let(files::absolutePath)
}

/**
 * Borra las fotos que nadie usa: ni una vacuna (páginas o sticker) ni una mascota, aunque estén
 * borradas (se conservan para poder restaurarlas y para el respaldo). Deja un margen de [grace]
 * para las fotos recién tomadas de un formulario que todavía no se guardó.
 * También borra archivos sueltos sin fila (por ejemplo, si la app se cerró a mitad de un borrado).
 */
class CleanUpPhotos(
    private val photos: PhotoRepository,
    private val files: PhotoFileStore,
    private val pets: PetRepository,
    private val records: VaccineRecordRepository,
    private val clock: AppClock,
    private val grace: Duration = 24.hours,
) {
    /** Devuelve cuántos archivos borró. */
    suspend operator fun invoke(): Int {
        // Primero los archivos y después las filas: un archivo nuevo siempre tiene su fila antes.
        val onDisk = files.list()
        val all = photos.getAll()
        val usedIds = records.getAllIncludingDeleted().flatMap { it.sourcePhotoIds + listOfNotNull(it.stickerPhotoId) }.toSet()
        val usedPaths = pets.getAllIncludingDeleted().mapNotNull { it.photoPath }.toSet()
        val limit = clock.now() - grace
        val unused = all.filter { it.id !in usedIds && it.path !in usedPaths && it.createdAt < limit }
        if (unused.isNotEmpty()) photos.delete(unused.map { it.id })
        val rowPaths = all.map { it.path }.toSet()
        val toDelete = (unused.map { it.path } + onDisk.filter { it !in rowPaths }).distinct()
        toDelete.forEach { files.delete(it) }
        return toDelete.size
    }
}
