package com.bestid.core.platform.share

import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/** `ACTION_SEND` con el `FileProvider` de la app (carpeta `share/` de la caché). */
@Composable
internal actual fun rememberPlatformShareSheet(): ShareSheet {
    val context = LocalContext.current
    return remember(context) {
        object : ShareSheet {
            override fun share(path: String, mimeType: String, title: String) {
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.bestid.photos", File(path))
                val send = Intent(Intent.ACTION_SEND)
                    .setType(mimeType)
                    .putExtra(Intent.EXTRA_STREAM, uri)
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                // Sin ClipData, algunas apps de destino no reciben el permiso de lectura.
                send.clipData = ClipData.newRawUri(title, uri)
                context.startActivity(Intent.createChooser(send, title).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
        }
    }
}

/** Storage Access Framework: copia el archivo elegido a la caché para leerlo con calma. */
@Composable
internal actual fun rememberPlatformFilePicker(onPicked: (String) -> Unit): FilePicker {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val callback by rememberUpdatedState(onPicked)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch {
            val copy = withContext(Dispatchers.IO) { copyToCache(context, uri) }
            copy?.let(callback)
        }
    }
    return remember(launcher) {
        object : FilePicker {
            // WhatsApp y Drive a veces marcan el zip como binario genérico.
            override fun pick() = launcher.launch(arrayOf("application/zip", "application/x-zip-compressed", "application/octet-stream"))
        }
    }
}

private fun copyToCache(context: Context, uri: android.net.Uri): String? = runCatching {
    val target = File(File(context.cacheDir, "share").apply { mkdirs() }, "importar.zip")
    context.contentResolver.openInputStream(uri)?.use { input -> target.outputStream().use { input.copyTo(it) } } ?: return null
    target.absolutePath
}.getOrNull()
