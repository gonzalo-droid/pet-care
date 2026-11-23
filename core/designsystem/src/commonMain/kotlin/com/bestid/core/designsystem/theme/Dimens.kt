package com.bestid.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Espaciado base de 4 (sección 7). */
@Immutable
data class BestIdSpacing(
    val xxs: Dp = 4.dp,
    val xs: Dp = 8.dp,
    val sm: Dp = 12.dp,
    val md: Dp = 16.dp,
    val lg: Dp = 20.dp,
    val xl: Dp = 24.dp,
    val xxl: Dp = 32.dp,
    /** Margen lateral de pantalla. */
    val screen: Dp = 20.dp,
)

/** Elevación de las superficies: sombra suave en lugar de borde. */
@Immutable
data class BestIdElevation(val card: Dp = 6.dp)

/** Tamaños de la sección 7: alto táctil mínimo 48, botón principal 58, secundario 52, íconos 24. */
@Immutable
data class BestIdSizes(
    val minTouch: Dp = 48.dp,
    val primaryButton: Dp = 58.dp,
    /** Alto de campos y opciones de selección (prototipo: 52 a 54). */
    val field: Dp = 54.dp,
    val secondaryButton: Dp = 52.dp,
    val icon: Dp = 24.dp,
    val iconSmall: Dp = 18.dp,
    val avatarSmall: Dp = 40.dp,
    val avatarMedium: Dp = 56.dp,
    val avatarLarge: Dp = 96.dp,
    val thumb: Dp = 72.dp,
    /** Ilustraciones de gato y perro de la Bienvenida. */
    val illustration: Dp = 120.dp,
    /** Íconos grandes de la introducción y su círculo de fondo. */
    val introIcon: Dp = 32.dp,
    val introIconCircle: Dp = 72.dp,
    /** Puntos del indicador de páginas: el activo es más ancho (no solo cambia el color). */
    val pageDot: Dp = 8.dp,
    val pageDotActive: Dp = 24.dp,
    val border: Dp = 2.dp,
    val chartHeight: Dp = 200.dp,
    val ring: Dp = 112.dp,
    val ringStroke: Dp = 10.dp,
    val dateTile: Dp = 52.dp,
    /** Cuadro con el ícono de una StatCard. */
    val statIcon: Dp = 34.dp,
    val actionPill: Dp = 40.dp,
    val rail: Dp = 2.dp,
    /** Carnet: alto del encabezado verde, cuánto lo monta el avatar, aro del avatar, paso de la trama y muescas. */
    val idCardHeader: Dp = 96.dp,
    val idCardAvatarLift: Dp = 44.dp,
    val idCardRing: Dp = 5.dp,
    val idCardDots: Dp = 44.dp,
    /** Radio de cada punto de la trama y su distancia al borde de la celda. */
    val idCardDotRadius: Dp = 7.dp,
    val idCardDotOffset: Dp = 20.dp,
    val idCardNotch: Dp = 20.dp,
)
