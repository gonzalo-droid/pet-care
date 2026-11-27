package com.bestid.feature.health

import com.bestid.feature.health.resources.health_weight_delete_row
import com.bestid.core.designsystem.component.IconAction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import com.bestid.core.designsystem.component.ConfirmSheet
import com.bestid.core.designsystem.component.EmptyState
import com.bestid.core.designsystem.component.InfoCard
import com.bestid.core.designsystem.component.PlainCard
import com.bestid.core.designsystem.component.WeightChart
import com.bestid.core.designsystem.component.WeightPoint
import com.bestid.core.designsystem.icon.BestIdIcons
import com.bestid.core.designsystem.theme.BestIdTheme
import com.bestid.core.model.WeightEntry
import com.bestid.core.ui.formatDayMonth
import com.bestid.core.ui.formatKgNumber
import com.bestid.core.ui.formatLong
import com.bestid.core.ui.formatWeight
import com.bestid.feature.health.resources.Res
import com.bestid.feature.health.resources.health_delete
import com.bestid.feature.health.resources.health_last_weight
import com.bestid.feature.health.resources.health_weight_chart
import com.bestid.feature.health.resources.health_weight_delete_body
import com.bestid.feature.health.resources.health_weight_delete_title
import com.bestid.feature.health.resources.health_weight_down
import com.bestid.feature.health.resources.health_weight_empty_body
import com.bestid.feature.health.resources.health_weight_empty_title
import com.bestid.feature.health.resources.health_weight_same
import com.bestid.feature.health.resources.health_weight_up
import org.jetbrains.compose.resources.stringResource

// Sección de peso de Salud, separada de HealthScreen.kt para que no pase de ~300 líneas (COD-05).

@Composable
internal fun WeightSection(petName: String, weights: List<WeightEntry>, onDelete: (String) -> Unit) {
    if (weights.isEmpty()) {
        EmptyState(
            illustration = BestIdIcons.Health,
            title = stringResource(Res.string.health_weight_empty_title),
            body = stringResource(Res.string.health_weight_empty_body),
        )
        return
    }
    var toDelete by remember { mutableStateOf<WeightEntry?>(null) }
    val last = weights.last()
    val previous = weights.getOrNull(weights.size - 2)
    InfoCard(icon = null) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(Res.string.health_last_weight, last.date.formatDayMonth()), style = BestIdTheme.typography.support)
                Text(formatWeight(last.grams), style = BestIdTheme.typography.display, color = BestIdTheme.colors.ink)
            }
            if (previous != null) {
                val diff = last.grams - previous.grams
                Text(
                    when {
                        diff > 0 -> stringResource(Res.string.health_weight_up, formatWeight(diff), previous.date.formatDayMonth())
                        diff < 0 -> stringResource(Res.string.health_weight_down, formatWeight(-diff), previous.date.formatDayMonth())
                        else -> stringResource(Res.string.health_weight_same)
                    },
                    style = BestIdTheme.typography.bodyStrong,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
    PlainCard {
        WeightChart(
            points = weights.takeLast(12).map { WeightPoint(it.date.formatDayMonth(), it.grams / 1000.0, formatKgNumber(it.grams)) },
            description = stringResource(Res.string.health_weight_chart, petName, weights.joinToString("; ") { formatKgNumber(it.grams) }),
        )
    }
    weights.asReversed().forEach { entry ->
        val weight = formatWeight(entry.grams)
        val date = entry.date.formatLong()
        PlainCard {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.sm)) {
                Row(modifier = Modifier.weight(1f).semantics(mergeDescendants = true) {}) {
                    Text(date, style = BestIdTheme.typography.body, color = BestIdTheme.colors.muted, modifier = Modifier.weight(1f))
                    Text(weight, style = BestIdTheme.typography.bodyStrong)
                }
                IconAction(
                    icon = BestIdIcons.Delete,
                    contentDescription = stringResource(Res.string.health_weight_delete_row, date, weight),
                    onClick = { toDelete = entry },
                )
            }
        }
    }
    toDelete?.let { entry ->
        ConfirmSheet(
            title = stringResource(Res.string.health_weight_delete_title),
            body = stringResource(Res.string.health_weight_delete_body, formatWeight(entry.grams), entry.date.formatLong()),
            confirmText = stringResource(Res.string.health_delete),
            destructive = true,
            onConfirm = {
                toDelete = null
                onDelete(entry.id)
            },
            onDismiss = { toDelete = null },
        )
    }
}
