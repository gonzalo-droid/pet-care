package com.bestid.feature.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bestid.core.designsystem.component.EmptyState
import com.bestid.core.designsystem.icon.BestIdIcons
import com.bestid.feature.home.resources.Res
import com.bestid.feature.home.resources.home_empty_body
import com.bestid.feature.home.resources.home_empty_title
import org.jetbrains.compose.resources.stringResource

/** Inicio (H1). En M0 solo el estado vacío; la tarjeta de mascota llega en M1. */
@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    EmptyState(
        illustration = BestIdIcons.Pets,
        title = stringResource(Res.string.home_empty_title),
        body = stringResource(Res.string.home_empty_body),
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()),
    )
}
