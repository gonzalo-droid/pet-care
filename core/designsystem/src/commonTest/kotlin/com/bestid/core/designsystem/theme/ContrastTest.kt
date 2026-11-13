package com.bestid.core.designsystem.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import kotlin.math.max
import kotlin.math.min
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Regla 2 de CLAUDE.md: texto con contraste de 4,5:1 o más y bordes de controles con 3:1 o más
 * (WCAG 1.4.3 y 1.4.11). Cada par es una combinación que usan los componentes.
 */
class ContrastTest {
    private val c = LightBestIdColors

    private fun ratio(a: Color, b: Color): Double {
        val la = a.luminance().toDouble()
        val lb = b.luminance().toDouble()
        return (max(la, lb) + 0.05) / (min(la, lb) + 0.05)
    }

    private fun assertContrast(name: String, fg: Color, bg: Color, minimum: Double) {
        val r = ratio(fg, bg)
        assertTrue(r >= minimum, "$name: ${(r * 100).toInt() / 100.0}:1 < $minimum:1")
    }

    @Test
    fun textPairsReachAA() {
        val pairs = listOf(
            Triple("ink sobre bg", c.ink, c.bg),
            Triple("ink sobre surface", c.ink, c.surface),
            Triple("ink sobre track", c.ink, c.track),
            Triple("muted sobre bg", c.muted, c.bg),
            Triple("muted sobre surface", c.muted, c.surface),
            Triple("primary sobre bg", c.primary, c.bg),
            Triple("primary sobre surface", c.primary, c.surface),
            Triple("onPrimary sobre primary", c.onPrimary, c.primary),
            Triple("primaryInk sobre primarySoft", c.primaryInk, c.primarySoft),
            Triple("alertInk sobre alert", c.alertInk, c.alert),
            Triple("error sobre bg", c.error, c.bg),
            Triple("error sobre surface", c.error, c.surface),
            Triple("surface sobre ink (snackbar)", c.surface, c.ink),
        )
        pairs.forEach { (name, fg, bg) -> assertContrast(name, fg, bg, 4.5) }
    }

    @Test
    fun controlBordersReach3to1() {
        assertContrast("outlineStrong sobre surface", c.outlineStrong, c.surface, 3.0)
        assertContrast("outlineStrong sobre bg", c.outlineStrong, c.bg, 3.0)
        assertContrast("primary sobre surface (borde con foco)", c.primary, c.surface, 3.0)
    }
}
