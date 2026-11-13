package com.bestid.core.designsystem.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/** Radios de la sección 7: tarjetas 24 a 28, campos 18, botones 24, chips y badges redondos. */
@Immutable
data class BestIdShapes(
    val card: Shape = RoundedCornerShape(24.dp),
    val cardLarge: Shape = RoundedCornerShape(28.dp),
    val field: Shape = RoundedCornerShape(18.dp),
    val button: Shape = RoundedCornerShape(24.dp),
    val pill: Shape = CircleShape,
    val sheet: Shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
)

internal fun BestIdShapes.toMaterialShapes(): Shapes = Shapes(
    extraSmall = RoundedCornerShape(18.dp),
    small = RoundedCornerShape(18.dp),
    medium = RoundedCornerShape(24.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp),
)
