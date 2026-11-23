package com.bestid.core.designsystem.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.Preview
import com.bestid.core.designsystem.preview.PreviewSurface
import com.bestid.core.designsystem.theme.BestIdTheme

/**
 * Puntos de un carrusel. El actual es más ancho y verde: la posición no depende solo del color.
 * [description] es lo que lee el lector de pantalla ("Página 2 de 3").
 */
@Composable
fun PageIndicator(count: Int, current: Int, description: String, modifier: Modifier = Modifier) {
    val colors = BestIdTheme.colors
    val sizes = BestIdTheme.sizes
    Row(
        modifier = modifier.clearAndSetSemantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(count) { index ->
            val selected = index == current
            val width by animateDpAsState(if (selected) sizes.pageDotActive else sizes.pageDot)
            Box(
                Modifier
                    .width(width)
                    .height(sizes.pageDot)
                    .background(if (selected) colors.primary else colors.outlineStrong, BestIdTheme.shapes.pill),
            )
        }
    }
}

@Preview
@Composable
private fun PageIndicatorPreview() = PreviewSurface {
    PageIndicator(count = 3, current = 0, description = "")
    PageIndicator(count = 3, current = 1, description = "")
    PageIndicator(count = 3, current = 2, description = "")
}
