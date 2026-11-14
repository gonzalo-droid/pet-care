package com.bestid.core.platform.share

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

/** Puerto `ShareSheet` (sección 8): la hoja de compartir del sistema (WhatsApp, Drive, Archivos…). */
interface ShareSheet {
    /** [path]: archivo en la caché de la app (`TempFileStore`). */
    fun share(path: String, mimeType: String, title: String)
}

/** Puerto `FilePicker` (sección 8): elegir un archivo para importar. */
interface FilePicker {
    fun pick()
}

/** Reemplazos para pruebas de UI (las hojas del sistema no se pueden tocar desde Compose). */
val LocalShareSheetOverride = staticCompositionLocalOf<ShareSheet?> { null }
fun interface FilePickerOverride {
    fun pick(): String?
}
val LocalFilePickerOverride = staticCompositionLocalOf<FilePickerOverride?> { null }

@Composable
fun rememberShareSheet(): ShareSheet = LocalShareSheetOverride.current ?: rememberPlatformShareSheet()

/** [onPicked] recibe la ruta de una copia del archivo en la caché de la app. Cancelar no lo llama. */
@Composable
fun rememberFilePicker(onPicked: (String) -> Unit): FilePicker {
    val override = LocalFilePickerOverride.current ?: return rememberPlatformFilePicker(onPicked)
    return object : FilePicker {
        override fun pick() {
            override.pick()?.let(onPicked)
        }
    }
}

@Composable
internal expect fun rememberPlatformShareSheet(): ShareSheet

@Composable
internal expect fun rememberPlatformFilePicker(onPicked: (String) -> Unit): FilePicker

/** Tipos de archivo. */
object MimeTypes {
    const val PDF = "application/pdf"
    const val ZIP = "application/zip"
}
