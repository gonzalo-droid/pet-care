package com.bestid.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import coil3.compose.AsyncImage
import com.bestid.core.designsystem.icon.BestIdIcons
import com.bestid.core.designsystem.preview.PreviewSurface
import com.bestid.core.designsystem.resources.Res
import com.bestid.core.designsystem.resources.ds_open_photo
import com.bestid.core.designsystem.theme.BestIdTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Miniatura de una foto (página de la cartilla o sticker). Al tocarla se abre el visor.
 * [image] es una ruta o URI que entiende Coil; `null` muestra un placeholder.
 */
@Composable
fun PhotoThumb(
    image: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    enabled: Boolean = true,
) {
    val colors = BestIdTheme.colors
    val shape = BestIdTheme.shapes.field
    val description = contentDescription ?: stringResource(Res.string.ds_open_photo)
    Box(
        modifier = modifier
            .size(BestIdTheme.sizes.thumb)
            .clip(shape)
            .background(colors.track, shape)
            .border(BorderStroke(BestIdTheme.sizes.border, colors.line), shape)
            .clickable(enabled = enabled, role = Role.Image, onClick = onClick)
            .semantics { this.contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        if (image != null) {
            AsyncImage(model = image, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(BestIdTheme.sizes.thumb))
        } else {
            Icon(painterResource(BestIdIcons.Image), contentDescription = null, tint = colors.muted)
        }
    }
}

@Preview
@Composable
private fun PhotoThumbPreview() = PreviewSurface {
    Row(horizontalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xs)) {
        PhotoThumb(null, onClick = {})
        PhotoThumb(null, onClick = {}, enabled = false)
    }
}
