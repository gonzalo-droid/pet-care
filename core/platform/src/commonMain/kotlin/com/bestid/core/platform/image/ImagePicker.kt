package com.bestid.core.platform.image

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.staticCompositionLocalOf

/** Lado largo máximo de las fotos guardadas (sección 8). */
const val MAX_PHOTO_SIDE = 2000

/** Calidad JPEG de las fotos guardadas (sección 8). */
const val JPEG_QUALITY = 85

/** Páginas de la cartilla por lectura (sección 8). */
const val MAX_CARD_PAGES = 4

enum class ImageSource { CAMERA, GALLERY }

sealed interface ImagePickerResult {
    /** Bytes originales tal como vienen de la cámara o la galería; falta pasarlos por `ImageProcessor`. */
    class Picked(val images: List<ByteArray>) : ImagePickerResult

    /** La persona negó el permiso de cámara (solo iOS lo pide): mostrar la tarjeta con enlace a Ajustes. */
    data object CameraDenied : ImagePickerResult
}

/**
 * Puerto `ImagePicker` (sección 8): cámara del sistema y selector de fotos del sistema.
 * Si la persona cancela, no se llama a `onResult`.
 */
interface ImagePickerLauncher {
    /** `false` si el dispositivo no tiene cámara (por ejemplo, el simulador de iOS). */
    val canTakePhoto: Boolean
    fun takePhoto()

    /** Elegir hasta [max] fotos de la galería. */
    fun pickFromGallery(max: Int)
}

/** Reemplazo del selector, solo para pruebas de UI (la cámara y la galería no se pueden tocar desde Compose). */
fun interface ImagePickerOverride {
    fun pick(source: ImageSource, max: Int): ImagePickerResult?
}

val LocalImagePickerOverride = staticCompositionLocalOf<ImagePickerOverride?> { null }

@Composable
fun rememberImagePickerLauncher(onResult: (ImagePickerResult) -> Unit): ImagePickerLauncher {
    val override = LocalImagePickerOverride.current ?: return rememberPlatformImagePickerLauncher(onResult)
    val callback by rememberUpdatedState(onResult)
    return remember(override) {
        object : ImagePickerLauncher {
            override val canTakePhoto = true
            override fun takePhoto() {
                override.pick(ImageSource.CAMERA, 1)?.let(callback)
            }
            override fun pickFromGallery(max: Int) {
                override.pick(ImageSource.GALLERY, max)?.let(callback)
            }
        }
    }
}

@Composable
internal expect fun rememberPlatformImagePickerLauncher(onResult: (ImagePickerResult) -> Unit): ImagePickerLauncher

/** Abre los ajustes de la app en el sistema (para dar el permiso de cámara después de negarlo). */
@Composable
expect fun rememberOpenAppSettings(): () -> Unit
