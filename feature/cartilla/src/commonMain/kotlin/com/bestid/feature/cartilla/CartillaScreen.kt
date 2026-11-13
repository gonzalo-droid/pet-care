package com.bestid.feature.cartilla

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bestid.core.designsystem.component.EmptyState
import com.bestid.core.designsystem.icon.BestIdIcons
import com.bestid.feature.cartilla.resources.Res
import com.bestid.feature.cartilla.resources.cartilla_empty_body
import com.bestid.feature.cartilla.resources.cartilla_empty_title
import org.jetbrains.compose.resources.stringResource

/** Cartilla (C1). En M0 solo el estado vacío; la lista de registros llega en M1. */
@Composable
fun CartillaScreen(modifier: Modifier = Modifier) {
    EmptyState(
        illustration = BestIdIcons.Vaccines,
        title = stringResource(Res.string.cartilla_empty_title),
        body = stringResource(Res.string.cartilla_empty_body),
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()),
    )
}
