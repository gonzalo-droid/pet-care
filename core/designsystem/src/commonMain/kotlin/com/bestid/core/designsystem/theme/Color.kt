package com.bestid.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Tokens de color de la sección 7 de SYSTEM_DESIGN.md.
 * Es la única fuente de colores de la app: ninguna pantalla define colores sueltos.
 */
@Immutable
data class BestIdColors(
    val bg: Color,
    val surface: Color,
    val ink: Color,
    val muted: Color,
    val line: Color,
    val track: Color,
    /** Borde decorativo y punteado (no delimita controles: 1,82:1 sobre surface). */
    val outline: Color,
    /** Borde de controles interactivos. Cumple 3:1 sobre surface y bg (WCAG 1.4.11). */
    val outlineStrong: Color,
    val primary: Color,
    val onPrimary: Color,
    val primarySoft: Color,
    val primaryInk: Color,
    val alert: Color,
    val alertInk: Color,
    val error: Color,
    /** Color de la sombra de las superficies. */
    val shadow: Color,
)

val LightBestIdColors = BestIdColors(
    bg = Color(0xFFFBF6EC),
    surface = Color(0xFFFFFFFF),
    ink = Color(0xFF2B3A36),
    muted = Color(0xFF5A6B65),
    line = Color(0xFFEADFCB),
    track = Color(0xFFEFE6D3),
    outline = Color(0xFFCDBF9F),
    outlineStrong = Color(0xFFA48C55),
    primary = Color(0xFF2F7D68),
    onPrimary = Color(0xFFFFFFFF),
    primarySoft = Color(0xFFDDF1E8),
    primaryInk = Color(0xFF1F5E4D),
    alert = Color(0xFFFFE0CC),
    alertInk = Color(0xFF7A3B1B),
    error = Color(0xFFB3261E),
    shadow = Color(0xFF2B3A36).copy(alpha = 0.18f),
)

/** Traducción de los tokens al esquema de Material 3, para que sus componentes hereden los colores. */
internal fun BestIdColors.toMaterialColorScheme(): ColorScheme = lightColorScheme(
    primary = primary,
    onPrimary = onPrimary,
    primaryContainer = primarySoft,
    onPrimaryContainer = primaryInk,
    secondary = primary,
    onSecondary = onPrimary,
    secondaryContainer = primarySoft,
    onSecondaryContainer = primaryInk,
    tertiary = alertInk,
    onTertiary = onPrimary,
    tertiaryContainer = alert,
    onTertiaryContainer = alertInk,
    background = bg,
    onBackground = ink,
    surface = surface,
    onSurface = ink,
    surfaceVariant = track,
    onSurfaceVariant = muted,
    surfaceContainerLowest = surface,
    surfaceContainerLow = surface,
    surfaceContainer = surface,
    surfaceContainerHigh = surface,
    surfaceContainerHighest = track,
    inverseSurface = ink,
    inverseOnSurface = surface,
    inversePrimary = primarySoft,
    outline = outlineStrong,
    outlineVariant = line,
    error = error,
    onError = onPrimary,
    errorContainer = alert,
    onErrorContainer = alertInk,
    scrim = ink,
)
