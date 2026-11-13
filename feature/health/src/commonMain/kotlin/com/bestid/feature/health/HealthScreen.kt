package com.bestid.feature.health

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.bestid.core.designsystem.component.EmptyState
import com.bestid.core.designsystem.component.SegmentedControl
import com.bestid.core.designsystem.icon.BestIdIcons
import com.bestid.core.designsystem.theme.BestIdTheme
import com.bestid.feature.health.resources.Res
import com.bestid.feature.health.resources.health_care_empty_body
import com.bestid.feature.health.resources.health_care_empty_title
import com.bestid.feature.health.resources.health_tab_care
import com.bestid.feature.health.resources.health_tab_weight
import com.bestid.feature.health.resources.health_weight_empty_body
import com.bestid.feature.health.resources.health_weight_empty_title
import org.jetbrains.compose.resources.stringResource

/** Salud (S1) con el segmento Cuidados | Peso. En M0 solo los estados vacíos; el contenido llega en M2. */
@Composable
fun HealthScreen(modifier: Modifier = Modifier) {
    var segment by rememberSaveable { mutableIntStateOf(0) }
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = BestIdTheme.spacing.screen, vertical = BestIdTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.md),
    ) {
        SegmentedControl(
            options = listOf(stringResource(Res.string.health_tab_care), stringResource(Res.string.health_tab_weight)),
            selectedIndex = segment,
            onSelect = { segment = it },
        )
        if (segment == 0) {
            EmptyState(
                illustration = BestIdIcons.Health,
                title = stringResource(Res.string.health_care_empty_title),
                body = stringResource(Res.string.health_care_empty_body),
            )
        } else {
            EmptyState(
                illustration = BestIdIcons.Health,
                title = stringResource(Res.string.health_weight_empty_title),
                body = stringResource(Res.string.health_weight_empty_body),
            )
        }
    }
}
