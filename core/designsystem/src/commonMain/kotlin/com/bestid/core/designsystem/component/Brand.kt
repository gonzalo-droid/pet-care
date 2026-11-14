package com.bestid.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.Preview
import com.bestid.core.designsystem.preview.PreviewSurface
import com.bestid.core.designsystem.resources.Res
import com.bestid.core.designsystem.resources.ds_brand_best
import com.bestid.core.designsystem.resources.ds_brand_id
import com.bestid.core.designsystem.resources.ds_brand_name
import com.bestid.core.designsystem.theme.BestIdTheme
import org.jetbrains.compose.resources.stringResource

/** Marca "Best ID" del prototipo: "Best" en tinta e "ID" en una pastilla verde (Bienvenida). */
@Composable
fun BrandLogo(modifier: Modifier = Modifier) {
    val colors = BestIdTheme.colors
    val name = stringResource(Res.string.ds_brand_name)
    Row(
        modifier = modifier.clearAndSetSemantics { contentDescription = name },
        horizontalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(stringResource(Res.string.ds_brand_best), style = BestIdTheme.typography.display, color = colors.ink)
        Text(
            stringResource(Res.string.ds_brand_id),
            style = BestIdTheme.typography.display,
            color = colors.onPrimary,
            modifier = Modifier
                .background(colors.primary, BestIdTheme.shapes.field)
                .padding(horizontal = BestIdTheme.spacing.xs),
        )
    }
}

@Preview
@Composable
private fun BrandLogoPreview() = PreviewSurface { BrandLogo() }
