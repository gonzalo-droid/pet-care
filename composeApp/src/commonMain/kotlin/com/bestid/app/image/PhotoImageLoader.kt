package com.bestid.app.image

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.map.Mapper
import coil3.request.Options
import com.bestid.core.domain.port.PhotoFileStore
import com.bestid.core.domain.usecase.PHOTOS_DIR
import okio.Path
import okio.Path.Companion.toPath

/**
 * La base guarda rutas relativas ("photos/<id>.jpg") porque en iOS el directorio de la app cambia
 * entre instalaciones. Las pantallas pasan esa ruta a Coil y aquí se resuelve a la absoluta.
 */
class RelativePhotoMapper(private val files: PhotoFileStore) : Mapper<String, Path> {
    override fun map(data: String, options: Options): Path? =
        if (data.startsWith("$PHOTOS_DIR/")) files.absolutePath(data).toPath() else null
}

fun photoImageLoader(context: PlatformContext, files: PhotoFileStore): ImageLoader =
    ImageLoader.Builder(context)
        .components { add(RelativePhotoMapper(files)) }
        .build()
