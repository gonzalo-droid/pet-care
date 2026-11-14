package com.bestid.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import coil3.compose.AsyncImage
import com.bestid.core.designsystem.icon.BestIdIcons
import com.bestid.core.designsystem.preview.PreviewData
import com.bestid.core.designsystem.preview.PreviewSurface
import com.bestid.core.designsystem.resources.Res
import com.bestid.core.designsystem.resources.ds_close
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

/**
 * Página de la cartilla en la captura (O2): foto en proporción 3:4 con su etiqueta ("Pág. 1").
 * Al tocarla se abre el visor.
 */
@Composable
fun PageTile(
    image: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    val colors = BestIdTheme.colors
    val shape = BestIdTheme.shapes.card
    val description = contentDescription ?: label
    Box(
        modifier = modifier
            .aspectRatio(3f / 4f)
            .clip(shape)
            .background(colors.track, shape)
            .clickable(role = Role.Image, onClick = onClick)
            .semantics { this.contentDescription = description },
    ) {
        AsyncImage(model = image, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        Text(
            label,
            style = BestIdTheme.typography.chip,
            color = colors.ink,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(BestIdTheme.spacing.xs)
                .background(colors.surface, BestIdTheme.shapes.pill)
                .padding(horizontal = BestIdTheme.spacing.sm, vertical = BestIdTheme.spacing.xxs),
        )
    }
}

/** Espacio para agregar otra página, con borde punteado, del mismo tamaño que [PageTile]. */
@Composable
fun AddPageTile(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val colors = BestIdTheme.colors
    val strokeWidth = BestIdTheme.sizes.border
    val color = if (enabled) colors.outlineStrong else colors.line
    Column(
        modifier = modifier
            .aspectRatio(3f / 4f)
            .clip(BestIdTheme.shapes.card)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .drawWithContent {
                drawContent()
                val radius = 24.dp.toPx()
                drawRoundRect(
                    color = color,
                    cornerRadius = CornerRadius(radius, radius),
                    style = Stroke(strokeWidth.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10.dp.toPx(), 6.dp.toPx()))),
                )
            }
            .padding(BestIdTheme.spacing.xs),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(painterResource(BestIdIcons.Add), contentDescription = null, tint = if (enabled) colors.primary else colors.muted)
        Text(text, style = BestIdTheme.typography.bodyStrong, color = if (enabled) colors.primary else colors.muted, textAlign = TextAlign.Center)
    }
}

/**
 * Visor de foto a pantalla completa: pellizcar para acercar, arrastrar para moverse y doble toque
 * para acercar o volver al tamaño original.
 */
@Composable
fun PhotoViewer(
    image: String,
    contentDescription: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable () -> Unit = {},
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BestIdTheme.colors.ink),
    ) {
        AsyncImage(
            model = image,
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 5f)
                        offset = if (scale == 1f) Offset.Zero else offset + pan
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(onDoubleTap = {
                        if (scale > 1f) {
                            scale = 1f
                            offset = Offset.Zero
                        } else {
                            scale = 2.5f
                        }
                    })
                }
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                },
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(BestIdTheme.spacing.xs),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconAction(BestIdIcons.Close, stringResource(Res.string.ds_close), onClose)
            Box(Modifier.navigationBarsPadding()) { actions() }
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

@Preview
@Composable
private fun PageTilesPreview() = PreviewSurface {
    Row(horizontalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.sm)) {
        AddPageTile(PreviewData.ADD_PAGE, onClick = {}, modifier = Modifier.weight(1f))
        AddPageTile(PreviewData.ADD_PAGE, onClick = {}, modifier = Modifier.weight(1f), enabled = false)
        Box(Modifier.weight(1f))
    }
}
