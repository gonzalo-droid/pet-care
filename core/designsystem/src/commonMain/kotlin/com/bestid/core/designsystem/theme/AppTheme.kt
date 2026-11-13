package com.bestid.core.designsystem.theme

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf

private val LocalBestIdColors = staticCompositionLocalOf { LightBestIdColors }
private val LocalBestIdTypography = staticCompositionLocalOf<BestIdTypography> {
    error("BestIdTypography no está disponible: envuelve el contenido en AppTheme")
}
private val LocalBestIdShapes = staticCompositionLocalOf { BestIdShapes() }
private val LocalBestIdSpacing = staticCompositionLocalOf { BestIdSpacing() }
private val LocalBestIdSizes = staticCompositionLocalOf { BestIdSizes() }

/**
 * Aplica tokens, fuente Nunito y formas. Tema claro siempre: el tema oscuro está fuera del MVP.
 */
@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val colors = LightBestIdColors
    val family = nunitoFontFamily()
    val typography = remember(family) { bestIdTypography(family) }
    val shapes = remember { BestIdShapes() }

    CompositionLocalProvider(
        LocalBestIdColors provides colors,
        LocalBestIdTypography provides typography,
        LocalBestIdShapes provides shapes,
        LocalBestIdSpacing provides BestIdSpacing(),
        LocalBestIdSizes provides BestIdSizes(),
    ) {
        MaterialTheme(
            colorScheme = colors.toMaterialColorScheme(),
            typography = typography.toMaterialTypography(),
            shapes = shapes.toMaterialShapes(),
        ) {
            CompositionLocalProvider(LocalContentColor provides colors.ink, content = content)
        }
    }
}

/** Punto de acceso a los tokens desde cualquier componente. */
object BestIdTheme {
    val colors: BestIdColors
        @Composable @ReadOnlyComposable get() = LocalBestIdColors.current
    val typography: BestIdTypography
        @Composable @ReadOnlyComposable get() = LocalBestIdTypography.current
    val shapes: BestIdShapes
        @Composable @ReadOnlyComposable get() = LocalBestIdShapes.current
    val spacing: BestIdSpacing
        @Composable @ReadOnlyComposable get() = LocalBestIdSpacing.current
    val sizes: BestIdSizes
        @Composable @ReadOnlyComposable get() = LocalBestIdSizes.current
}
