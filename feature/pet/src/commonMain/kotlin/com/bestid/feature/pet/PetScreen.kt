package com.bestid.feature.pet

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bestid.core.designsystem.component.EmptyState
import com.bestid.core.designsystem.icon.BestIdIcons
import com.bestid.feature.pet.resources.Res
import com.bestid.feature.pet.resources.pet_empty_body
import com.bestid.feature.pet.resources.pet_empty_title
import org.jetbrains.compose.resources.stringResource

/** Mascota (M1). En M0 solo el estado vacío; el carnet BestID llega en M1. */
@Composable
fun PetScreen(modifier: Modifier = Modifier) {
    EmptyState(
        illustration = BestIdIcons.PetsFilled,
        title = stringResource(Res.string.pet_empty_title),
        body = stringResource(Res.string.pet_empty_body),
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()),
    )
}
