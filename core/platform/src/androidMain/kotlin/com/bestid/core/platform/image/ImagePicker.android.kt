package com.bestid.core.platform.image

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Cámara del sistema con `TakePicture` (escribe en la caché privada vía `FileProvider`) y selector de
 * fotos del sistema (`PickVisualMedia`), que no pide permisos. La app no declara el permiso de cámara:
 * así `TakePicture` no lo necesita.
 */
@Composable
internal actual fun rememberPlatformImagePickerLauncher(onResult: (ImagePickerResult) -> Unit): ImagePickerLauncher {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val callback by rememberUpdatedState(onResult)
    // Sobrevive a que el sistema cierre la app mientras la cámara está abierta.
    var pendingCapture by rememberSaveable { mutableStateOf<String?>(null) }

    fun deliver(uris: List<Uri>, deleteAfter: File? = null) {
        scope.launch {
            val images = withContext(Dispatchers.IO) {
                val bytes = uris.mapNotNull { uri -> runCatching { context.contentResolver.openInputStream(uri)?.use { it.readBytes() } }.getOrNull() }
                deleteAfter?.delete()
                bytes
            }
            if (images.isNotEmpty()) callback(ImagePickerResult.Picked(images))
        }
    }

    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
        val path = pendingCapture ?: return@rememberLauncherForActivityResult
        pendingCapture = null
        val file = File(path)
        if (saved) deliver(listOf(captureUri(context, file)), deleteAfter = file) else file.delete()
    }
    val gallerySingle = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { deliver(listOf(it)) }
    }
    val galleryMultiple = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(MAX_CARD_PAGES)) { uris ->
        if (uris.isNotEmpty()) deliver(uris)
    }

    return remember(context) {
        object : ImagePickerLauncher {
            override val canTakePhoto = context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)

            override fun takePhoto() {
                val dir = File(context.cacheDir, CAPTURE_DIR).apply { mkdirs() }
                val file = File(dir, "captura-${System.currentTimeMillis()}.jpg")
                pendingCapture = file.absolutePath
                try {
                    camera.launch(captureUri(context, file))
                } catch (_: ActivityNotFoundException) {
                    pendingCapture = null
                }
            }

            override fun pickFromGallery(max: Int) {
                val request = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                if (max <= 1) gallerySingle.launch(request) else galleryMultiple.launch(request)
            }
        }
    }
}

private const val CAPTURE_DIR = "camera"

private fun captureUri(context: Context, file: File): Uri =
    FileProvider.getUriForFile(context, "${context.packageName}.bestid.photos", file)

@Composable
actual fun rememberOpenAppSettings(): () -> Unit {
    val context = LocalContext.current
    return remember(context) {
        {
            context.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, "package:${context.packageName}".toUri())
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }
    }
}
