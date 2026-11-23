package com.bestid.core.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity

/** Desde esta escala de fuente del sistema, lo que va lado a lado se apila (regla 2: texto al 200 % sin cortes). */
const val LARGE_FONT_SCALE = 1.3f

/** La persona agrandó la letra lo suficiente como para que dos columnas partan palabras. */
@Composable
fun isLargeFont(): Boolean = LocalDensity.current.fontScale >= LARGE_FONT_SCALE
