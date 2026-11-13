package com.bestid.app.navigation

import androidx.compose.runtime.Composable
import com.bestid.app.resources.Res
import com.bestid.app.resources.photo_viewer_description
import com.bestid.core.designsystem.component.PhotoViewer
import org.jetbrains.compose.resources.stringResource

/** Visor de foto a pantalla completa, para cualquier foto guardada (cartilla, sticker, mascota). */
@Composable
fun PhotoViewerScreen(path: String, onClose: () -> Unit) {
    PhotoViewer(image = path, contentDescription = stringResource(Res.string.photo_viewer_description), onClose = onClose)
}
