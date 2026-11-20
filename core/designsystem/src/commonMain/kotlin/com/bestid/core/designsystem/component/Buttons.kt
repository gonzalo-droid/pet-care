package com.bestid.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bestid.core.designsystem.icon.BestIdIcons
import com.bestid.core.designsystem.preview.PreviewData
import com.bestid.core.designsystem.preview.PreviewSurface
import com.bestid.core.designsystem.theme.BestIdTheme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * Acción principal de una pantalla. Alto 58, crece con el texto grande.
 * [destructive] la pinta en `error` (confirmar un borrado; contraste 6,5:1 con `onPrimary`).
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: DrawableResource? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
    destructive: Boolean = false,
) {
    val colors = BestIdTheme.colors
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier.fillMaxWidth().heightIn(min = BestIdTheme.sizes.primaryButton),
        shape = BestIdTheme.shapes.button,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (destructive) colors.error else colors.primary,
            contentColor = colors.onPrimary,
            disabledContainerColor = colors.track,
            disabledContentColor = colors.muted,
        ),
        contentPadding = buttonPadding(),
    ) {
        ButtonContent(text, icon, loading, colors.muted, BestIdTheme.typography.button)
    }
}

/** Acción secundaria. Alto 52, fondo surface y borde outlineStrong (3:1). */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: DrawableResource? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    val colors = BestIdTheme.colors
    OutlinedButton(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier.fillMaxWidth().heightIn(min = BestIdTheme.sizes.secondaryButton),
        shape = BestIdTheme.shapes.button,
        border = BorderStroke(
            BestIdTheme.sizes.border,
            if (enabled) colors.outlineStrong else colors.line,
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = colors.surface,
            contentColor = colors.ink,
            disabledContainerColor = colors.track,
            disabledContentColor = colors.muted,
        ),
        contentPadding = buttonPadding(),
    ) {
        ButtonContent(text, icon, loading, colors.muted, BestIdTheme.typography.bodyStrong)
    }
}

/** Acción de agregar con borde punteado (por ejemplo "+ Agregar otra mascota"). */
@Composable
fun DashedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: DrawableResource? = BestIdIcons.Add,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    val colors = BestIdTheme.colors
    val borderColor = if (enabled) colors.outlineStrong else colors.line
    val strokeWidth = BestIdTheme.sizes.border
    TextButton(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = BestIdTheme.sizes.secondaryButton)
            .drawWithContent {
                drawContent()
                val radius = 24.dp.toPx()
                drawRoundRect(
                    color = borderColor,
                    cornerRadius = CornerRadius(radius, radius),
                    style = Stroke(
                        width = strokeWidth.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10.dp.toPx(), 6.dp.toPx())),
                    ),
                )
            },
        shape = BestIdTheme.shapes.button,
        colors = ButtonDefaults.textButtonColors(
            containerColor = Color.Transparent,
            contentColor = colors.primary,
            disabledContentColor = colors.muted,
        ),
        contentPadding = buttonPadding(),
    ) {
        ButtonContent(text, icon, loading, colors.muted, BestIdTheme.typography.bodyStrong)
    }
}

/** Acción de texto, sin fondo. Mantiene el alto táctil de 48. [destructive] la pinta en `error` (eliminar). */
@Composable
fun TextAction(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: DrawableResource? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
    destructive: Boolean = false,
) {
    val colors = BestIdTheme.colors
    TextButton(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier.heightIn(min = BestIdTheme.sizes.minTouch),
        shape = BestIdTheme.shapes.button,
        colors = ButtonDefaults.textButtonColors(
            contentColor = if (destructive) colors.error else colors.primary,
            disabledContentColor = colors.muted,
        ),
    ) {
        ButtonContent(text, icon, loading, colors.muted, BestIdTheme.typography.bodyStrong)
    }
}

@Composable
private fun buttonPadding() = PaddingValues(
    horizontal = BestIdTheme.spacing.lg,
    vertical = BestIdTheme.spacing.sm,
)

@Composable
private fun RowScope.ButtonContent(
    text: String,
    icon: DrawableResource?,
    loading: Boolean,
    progressColor: Color,
    style: androidx.compose.ui.text.TextStyle,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xs, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when {
            loading -> CircularProgressIndicator(
                modifier = Modifier.size(BestIdTheme.sizes.iconSmall),
                color = progressColor,
                strokeWidth = 2.dp,
            )
            icon != null -> Icon(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(BestIdTheme.sizes.icon),
            )
        }
        Text(text = text, style = style, textAlign = TextAlign.Center)
    }
}

@Preview
@Composable
private fun ButtonsPreview() = PreviewSurface {
    PrimaryButton(PreviewData.BUTTON, onClick = {})
    SecondaryButton(PreviewData.BUTTON_SECONDARY, onClick = {}, icon = BestIdIcons.Image)
    DashedButton(PreviewData.BUTTON_DASHED, onClick = {})
    TextAction(PreviewData.TEXT_ACTION, onClick = {})
    TextAction(PreviewData.CONFIRM_ACTION, onClick = {}, icon = BestIdIcons.Delete, destructive = true)
}

@Preview
@Composable
private fun ButtonsDisabledPreview() = PreviewSurface {
    PrimaryButton(PreviewData.BUTTON, onClick = {}, enabled = false)
    SecondaryButton(PreviewData.BUTTON_SECONDARY, onClick = {}, enabled = false)
    DashedButton(PreviewData.BUTTON_DASHED, onClick = {}, enabled = false)
    TextAction(PreviewData.TEXT_ACTION, onClick = {}, enabled = false)
}

@Preview
@Composable
private fun ButtonsLoadingPreview() = PreviewSurface {
    PrimaryButton(PreviewData.BUTTON, onClick = {}, loading = true)
    SecondaryButton(PreviewData.BUTTON_SECONDARY, onClick = {}, loading = true)
}

@Preview(fontScale = 2f)
@Composable
private fun ButtonsLongTextPreview() = PreviewSurface {
    PrimaryButton(PreviewData.LONG_TEXT, onClick = {}, icon = BestIdIcons.PhotoCamera)
    SecondaryButton(PreviewData.LONG_TEXT, onClick = {})
    DashedButton(PreviewData.LONG_TEXT, onClick = {})
}
