package com.bestid.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.bestid.core.designsystem.icon.BestIdIcons
import com.bestid.core.designsystem.preview.PreviewData
import com.bestid.core.designsystem.preview.PreviewSurface
import com.bestid.core.designsystem.theme.BestIdTheme
import com.bestid.core.designsystem.theme.isLargeFont
import org.jetbrains.compose.resources.painterResource

/** Fecha en mosaico (día y mes). [review] la pinta en durazno cuando la fecha es dudosa. */
@Composable
fun DateTile(
    day: String,
    month: String,
    contentDescription: String,
    review: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val description = contentDescription
    val colors = BestIdTheme.colors
    val (container, content) = if (review) colors.alert to colors.alertInk else colors.primarySoft to colors.primaryInk
    Column(
        modifier = modifier
            .widthIn(min = BestIdTheme.sizes.dateTile)
            .background(container, BestIdTheme.shapes.field)
            .padding(horizontal = BestIdTheme.spacing.xxs, vertical = BestIdTheme.spacing.xs)
            .clearAndSetSemantics { this.contentDescription = description },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(day, style = BestIdTheme.typography.subtitle.copy(fontFeatureSettings = "tnum"), color = content)
        Text(month.uppercase(), style = BestIdTheme.typography.eyebrow, color = content)
    }
}

/**
 * Anillo de cuenta regresiva. [progress] null dibuja solo la pista. Es decorativo: quien lo usa
 * aporta el texto accesible.
 */
@Composable
fun CountdownRing(value: String, unit: String, progress: Float?, modifier: Modifier = Modifier) {
    val colors = BestIdTheme.colors
    val stroke = BestIdTheme.sizes.ringStroke
    Box(
        // El anillo crece con la letra para que el valor y la unidad nunca toquen el trazo.
        modifier = modifier.size(BestIdTheme.sizes.ring * LocalDensity.current.fontScale.coerceAtLeast(1f)).clearAndSetSemantics {},
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val inset = stroke.toPx() / 2
            val arc = Size(size.width - stroke.toPx(), size.height - stroke.toPx())
            drawArc(colors.track, 0f, 360f, false, Offset(inset, inset), arc, style = Stroke(stroke.toPx()))
            if (progress != null && progress > 0f) {
                drawArc(
                    colors.primary, -90f, 360f * progress.coerceAtMost(1f), false, Offset(inset, inset), arc,
                    style = Stroke(stroke.toPx(), cap = StrokeCap.Round),
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = BestIdTheme.typography.hero, color = colors.ink)
            Text(unit, style = BestIdTheme.typography.support, color = colors.muted)
        }
    }
}

/** Franja de aviso "hay datos por revisar": toda la fila es tocable; la pastilla refuerza la acción. */
@Composable
fun ReviewStrip(text: String, actionText: String, onAction: () -> Unit, modifier: Modifier = Modifier) {
    val colors = BestIdTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(BestIdTheme.shapes.field)
            .background(colors.alert, BestIdTheme.shapes.field)
            .heightIn(min = BestIdTheme.sizes.minTouch)
            .clickable(role = Role.Button, onClick = onAction)
            .padding(horizontal = BestIdTheme.spacing.md, vertical = BestIdTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.sm),
    ) {
        Icon(painterResource(BestIdIcons.Warning), contentDescription = null, tint = colors.alertInk)
        Text(text, style = BestIdTheme.typography.bodyStrong, color = colors.alertInk, modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .heightIn(min = BestIdTheme.sizes.actionPill)
                .background(colors.surface, BestIdTheme.shapes.pill)
                .padding(horizontal = BestIdTheme.spacing.md),
            contentAlignment = Alignment.Center,
        ) {
            Text(actionText, style = BestIdTheme.typography.chip, color = colors.alertInk, textAlign = TextAlign.Center)
        }
    }
}

/** Elemento de línea de tiempo: fecha a la izquierda con riel. Con letra grande, fecha arriba y sin riel. */
@Composable
fun TimelineItem(date: @Composable () -> Unit, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val gap = Arrangement.spacedBy(BestIdTheme.spacing.sm)
    if (isLargeFont()) {
        Column(modifier = modifier.fillMaxWidth(), verticalArrangement = gap) {
            date()
            content()
        }
        return
    }
    Row(modifier = modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = gap) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            date()
            Box(Modifier.weight(1f).width(BestIdTheme.sizes.rail).background(BestIdTheme.colors.track))
        }
        Box(Modifier.weight(1f)) { content() }
    }
}

/** Fila de progreso ("3 de 5 datos") toda tocable, con la acción como texto. */
@Composable
fun ProgressRow(text: String, progress: Float, actionText: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = BestIdTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.bg)
            .heightIn(min = BestIdTheme.sizes.minTouch)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = BestIdTheme.spacing.xs),
        verticalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xxs),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.sm)) {
            Text(text, style = BestIdTheme.typography.body, color = colors.ink, modifier = Modifier.weight(1f))
            Text(actionText, style = BestIdTheme.typography.bodyStrong, color = colors.primaryInk)
        }
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth(),
            color = colors.primary,
            trackColor = colors.track,
        )
    }
}

@Preview
@Composable
private fun PolishPreview() = PreviewSurface {
    Row(horizontalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.md)) {
        DateTile("21", "dic", "21 de diciembre")
        DateTile("?", "—", "Fecha sin leer", review = true)
        CountdownRing("12", "días", 0.6f)
    }
    ReviewStrip("2 datos por revisar", "Revisar", {})
    TimelineItem({ DateTile("21", "dic", "21 de diciembre") }) {
        VaccineRecordCard(null, PreviewData.VACCINE_TYPES.take(2), {}, products = PreviewData.RECORD_PRODUCTS)
    }
    ProgressRow("3 de 5 datos completos", 0.6f, "Completar", {})
}

@Preview
@Composable
private fun PolishProgressPreview() = PreviewSurface {
    Row(horizontalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.md)) {
        CountdownRing("?", "días", null)
        CountdownRing("0", "días", 0f)
        CountdownRing("30", "días", 1f)
    }
    ProgressRow("0 de 5", 0f, "Completar", {})
    ProgressRow("5 de 5", 1f, "Ver", {})
}

@Preview(fontScale = 2f)
@Composable
private fun PolishLongTextPreview() = PreviewSurface {
    DateTile("21", "dic", "21 de diciembre", review = true)
    CountdownRing("128", "días", 0.6f)
    ReviewStrip(PreviewData.LONG_TEXT, "Revisar", {})
    TimelineItem({ DateTile("21", "dic", "21 de diciembre") }) {
        VaccineRecordCard(null, PreviewData.VACCINE_TYPES, {}, products = PreviewData.LONG_TEXT)
    }
    ProgressRow(PreviewData.LONG_TEXT, 0.5f, "Completar", {})
}
