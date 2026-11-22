package com.bestid.core.designsystem.component

import com.bestid.core.designsystem.theme.isLargeFont
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.bestid.core.designsystem.icon.BestIdIcons
import com.bestid.core.designsystem.preview.PreviewData
import com.bestid.core.designsystem.preview.PreviewSurface
import com.bestid.core.designsystem.theme.BestIdTheme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/** Tarjeta base de superficie blanca con sombra suave. Tocable si recibe [onClick]. */
@Composable
fun PlainCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onClickLabel: String? = null,
    /** Chevron a la derecha: distingue una tarjeta que abre otra pantalla de una informativa. */
    showChevron: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = BestIdTheme.colors
    BaseCard(
        container = colors.surface,
        contentColor = colors.ink,
        elevated = true,
        modifier = modifier,
        onClick = onClick,
        onClickLabel = onClickLabel,
        showChevron = showChevron,
        content = content,
    )
}

/** Tarjeta informativa o de estado correcto: fondo primarySoft. */
@Composable
fun InfoCard(
    modifier: Modifier = Modifier,
    icon: DrawableResource? = BestIdIcons.Info,
    onClick: (() -> Unit)? = null,
    onClickLabel: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = BestIdTheme.colors
    BaseCard(colors.primarySoft, colors.primaryInk, false, modifier, onClick, onClickLabel, BestIdTheme.shapes.cardLarge) {
        IconRow(icon = icon, tint = colors.primaryInk, content = content)
    }
}

/** Tarjeta de aviso o dato por confirmar: fondo alert (durazno). Siempre lleva ícono y texto. */
@Composable
fun AlertCard(
    modifier: Modifier = Modifier,
    icon: DrawableResource = BestIdIcons.Warning,
    onClick: (() -> Unit)? = null,
    onClickLabel: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = BestIdTheme.colors
    BaseCard(colors.alert, colors.alertInk, false, modifier, onClick, onClickLabel) {
        IconRow(icon = icon, tint = colors.alertInk, content = content)
    }
}

/** Dato destacado (última vacuna, último peso). La acción opcional es un botón + en la esquina inferior derecha; [actionText] es su nombre para lectores de pantalla. */
@Composable
fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    detail: String? = null,
    onClick: (() -> Unit)? = null,
    onClickLabel: String? = null,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    icon: DrawableResource? = null,
) {
    PlainCard(modifier = modifier, onClick = onClick, onClickLabel = onClickLabel, showChevron = onClick != null) {
        if (icon != null) {
            Box(
                Modifier.size(BestIdTheme.sizes.statIcon).background(BestIdTheme.colors.primarySoft, BestIdTheme.shapes.field),
                contentAlignment = Alignment.Center,
            ) {
                Icon(painterResource(icon), contentDescription = null, tint = BestIdTheme.colors.primaryInk, modifier = Modifier.size(BestIdTheme.sizes.iconSmall))
            }
        }
        Text(label, style = BestIdTheme.typography.support, color = BestIdTheme.colors.muted)
        Text(value, style = BestIdTheme.typography.card, color = BestIdTheme.colors.ink)
        if (detail != null) Text(detail, style = BestIdTheme.typography.support, color = BestIdTheme.colors.muted)
        if (actionText != null && onAction != null) {
            // Empuja el + a la esquina inferior derecha cuando la tarjeta es más alta que su contenido.
            Spacer(Modifier.weight(1f))
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomEnd) {
                IconButton(
                    onClick = onAction,
                    modifier = Modifier.size(BestIdTheme.sizes.minTouch).background(BestIdTheme.colors.primary, BestIdTheme.shapes.pill),
                ) {
                    Icon(painterResource(BestIdIcons.Add), contentDescription = actionText, tint = BestIdTheme.colors.onPrimary)
                }
            }
        }
    }
}

/** Rótulo en mayúsculas sobre una sección o tarjeta ("PRÓXIMO", "VETERINARIA"). */
@Composable
fun Eyebrow(text: String, modifier: Modifier = Modifier, color: Color = BestIdTheme.colors.muted) {
    Text(text.uppercase(), style = BestIdTheme.typography.eyebrow, color = color, modifier = modifier)
}

/**
 * Fila de dos columnas: etiqueta y valor. El valor crece en varias líneas si hace falta; con letra
 * grande la etiqueta va encima del valor para no partir palabras. Se lee como una sola unidad.
 */
@Composable
fun ListRow(label: String, value: String, modifier: Modifier = Modifier) {
    val rowModifier = modifier.fillMaxWidth().padding(vertical = BestIdTheme.spacing.xxs).semantics(mergeDescendants = true) {}
    if (isLargeFont()) {
        Column(modifier = rowModifier) {
            Text(label, style = BestIdTheme.typography.body, color = BestIdTheme.colors.muted)
            Text(value, style = BestIdTheme.typography.bodyStrong, color = BestIdTheme.colors.ink)
        }
        return
    }
    Row(modifier = rowModifier, horizontalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.sm)) {
        Text(label, style = BestIdTheme.typography.body, color = BestIdTheme.colors.muted, modifier = Modifier.weight(1f))
        Text(
            value,
            style = BestIdTheme.typography.bodyStrong,
            color = BestIdTheme.colors.ink,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.4f),
        )
    }
}

/**
 * Dos tarjetas lado a lado (por ejemplo, "Última vacuna" y "Peso" en Inicio), del mismo alto. Con
 * letra grande se apilan, para que ninguna parta palabras.
 */
@Composable
fun AdaptivePair(
    first: @Composable (Modifier) -> Unit,
    second: @Composable (Modifier) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = Arrangement.spacedBy(BestIdTheme.spacing.sm)
    if (isLargeFont()) {
        Column(modifier = modifier.fillMaxWidth(), verticalArrangement = spacing) {
            first(Modifier.fillMaxWidth())
            second(Modifier.fillMaxWidth())
        }
    } else {
        Row(modifier = modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = spacing) {
            first(Modifier.weight(1f).fillMaxHeight())
            second(Modifier.weight(1f).fillMaxHeight())
        }
    }
}

/** Tono de un [Badge]. */
enum class BadgeTone { OK, ALERT, NEUTRAL }

/** Etiqueta corta de estado. El texto describe el estado; el color solo lo refuerza. */
@Composable
fun Badge(text: String, tone: BadgeTone, modifier: Modifier = Modifier) {
    val colors = BestIdTheme.colors
    val (container, content) = when (tone) {
        BadgeTone.OK -> colors.primarySoft to colors.primaryInk
        BadgeTone.ALERT -> colors.alert to colors.alertInk
        BadgeTone.NEUTRAL -> colors.surface to colors.primaryInk
    }
    Row(
        modifier = modifier
            .background(container, BestIdTheme.shapes.pill)
            .padding(horizontal = BestIdTheme.spacing.sm, vertical = BestIdTheme.spacing.xxs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xxs),
    ) {
        Text(text, style = BestIdTheme.typography.chip, color = content)
    }
}

/** Título de sección dentro de una pantalla. Se anuncia como encabezado. */
@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = BestIdTheme.typography.card,
        color = BestIdTheme.colors.ink,
        modifier = modifier.semantics { heading() },
    )
}

@Composable
internal fun BaseCard(
    container: Color,
    contentColor: Color,
    elevated: Boolean,
    modifier: Modifier,
    onClick: (() -> Unit)?,
    onClickLabel: String?,
    shape: androidx.compose.ui.graphics.Shape = BestIdTheme.shapes.card,
    showChevron: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(if (elevated) Modifier.shadow(BestIdTheme.elevation.card, shape, ambientColor = BestIdTheme.colors.shadow, spotColor = BestIdTheme.colors.shadow) else Modifier)
            .clip(shape)
            .background(container, shape)
            .then(
                if (onClick != null) {
                    Modifier
                        .heightIn(min = BestIdTheme.sizes.minTouch)
                        .clickable(onClickLabel = onClickLabel, role = Role.Button, onClick = onClick)
                } else Modifier,
            )
            .padding(BestIdTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xs),
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            if (showChevron) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xs),
                        content = content,
                    )
                    Icon(painterResource(BestIdIcons.ChevronRight), contentDescription = null, tint = BestIdTheme.colors.muted)
                }
            } else {
                content()
            }
        }
    }
}

@Composable
private fun IconRow(icon: DrawableResource?, tint: Color, content: @Composable ColumnScope.() -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.sm)) {
        if (icon != null) Icon(painterResource(icon), contentDescription = null, tint = tint)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xxs),
            content = content,
        )
    }
}

@Preview
@Composable
private fun CardsPreview() = PreviewSurface {
    InfoCard { Text(PreviewData.INFO, style = BestIdTheme.typography.body) }
    AlertCard { Text(PreviewData.ALERT, style = BestIdTheme.typography.body) }
    PlainCard(onClick = {}) {
        ListRow(PreviewData.ROW_LABEL, PreviewData.ROW_VALUE)
        ListRow(PreviewData.ID_SEX, PreviewData.SEX.first())
    }
    StatCard(PreviewData.STAT_LABEL, PreviewData.STAT_VALUE, actionText = PreviewData.STAT_ACTION, onAction = {})
    Row(horizontalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xs)) {
        Badge(PreviewData.BADGE_OK, BadgeTone.OK)
        Badge(PreviewData.BADGE_ALERT, BadgeTone.ALERT)
        Badge(PreviewData.BADGE_NEUTRAL, BadgeTone.NEUTRAL)
    }
    SectionTitle(PreviewData.EMPTY_TITLE)
}

@Preview(fontScale = 2f)
@Composable
private fun CardsLongTextPreview() = PreviewSurface {
    AlertCard { Text(PreviewData.LONG_TEXT, style = BestIdTheme.typography.body) }
    ListRow(PreviewData.LONG_TEXT, PreviewData.LONG_TEXT)
    StatCard(PreviewData.LONG_TEXT, PreviewData.STAT_VALUE)
    Badge(PreviewData.LONG_TEXT, BadgeTone.ALERT)
}
