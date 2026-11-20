package com.bestid.feature.health

import com.bestid.feature.health.resources.health_weight_delete_row
import com.bestid.feature.health.resources.health_care_edit
import com.bestid.core.ui.SaveFailedCard
import com.bestid.core.designsystem.component.IconAction
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bestid.core.designsystem.component.ConfirmSheet
import com.bestid.core.designsystem.component.EmptyState
import com.bestid.core.designsystem.component.InfoCard
import com.bestid.core.designsystem.component.ErrorState
import com.bestid.core.designsystem.component.LoadingState
import com.bestid.core.designsystem.component.LocalUndoController
import com.bestid.core.designsystem.component.PermissionCard
import com.bestid.core.designsystem.component.PlainCard
import com.bestid.core.designsystem.component.PrimaryButton
import com.bestid.core.designsystem.component.SegmentedControl
import com.bestid.core.designsystem.component.SwitchRow
import com.bestid.core.designsystem.component.WeightChart
import com.bestid.core.designsystem.component.WeightPoint
import com.bestid.core.designsystem.icon.BestIdIcons
import com.bestid.core.designsystem.theme.BestIdTheme
import com.bestid.core.ui.resources.Res as UiRes
import com.bestid.core.ui.resources.state_load_error
import com.bestid.core.domain.port.NotificationPermission
import com.bestid.core.domain.usecase.RestoreWeight
import com.bestid.core.model.CareTask
import com.bestid.core.model.WeightEntry
import com.bestid.core.platform.notification.rememberNotificationPermissionRequester
import com.bestid.core.ui.daysText
import com.bestid.core.ui.displayName
import com.bestid.core.ui.formatDayMonth
import com.bestid.core.ui.formatKgNumber
import com.bestid.core.ui.formatLong
import com.bestid.core.ui.formatWeight
import com.bestid.feature.health.resources.Res
import com.bestid.feature.health.resources.health_add_care
import com.bestid.feature.health.resources.health_add_pet
import com.bestid.feature.health.resources.health_add_weight
import com.bestid.feature.health.resources.health_care_empty_body
import com.bestid.feature.health.resources.health_care_empty_title
import com.bestid.feature.health.resources.health_care_in_days
import com.bestid.feature.health.resources.health_care_last
import com.bestid.feature.health.resources.health_care_next
import com.bestid.feature.health.resources.health_care_no_date
import com.bestid.feature.health.resources.health_care_overdue
import com.bestid.feature.health.resources.health_care_reminder
import com.bestid.feature.health.resources.health_care_today
import com.bestid.feature.health.resources.health_delete
import com.bestid.feature.health.resources.health_last_weight
import com.bestid.feature.health.resources.health_no_pet_body
import com.bestid.feature.health.resources.health_no_pet_title
import com.bestid.feature.health.resources.health_offline_note
import com.bestid.feature.health.resources.health_permission_body
import com.bestid.feature.health.resources.health_permission_title
import com.bestid.feature.health.resources.health_tab_care
import com.bestid.feature.health.resources.health_tab_weight
import com.bestid.feature.health.resources.health_title
import com.bestid.feature.health.resources.health_undo
import com.bestid.feature.health.resources.health_weight_chart
import com.bestid.feature.health.resources.health_weight_delete_body
import com.bestid.feature.health.resources.health_weight_delete_title
import com.bestid.feature.health.resources.health_weight_deleted
import com.bestid.feature.health.resources.health_weight_down
import com.bestid.feature.health.resources.health_weight_empty_body
import com.bestid.feature.health.resources.health_weight_empty_title
import com.bestid.feature.health.resources.health_weight_same
import com.bestid.feature.health.resources.health_weight_up
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/** Salud (S1) con el segmento Cuidados | Peso. Las vacunas siguen en Cartilla. */
@Composable
fun HealthScreen(
    onAddCare: () -> Unit,
    onOpenCare: (String) -> Unit,
    onAddWeight: () -> Unit,
    onAddPet: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HealthViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val actionFailed by viewModel.actionFailed.collectAsStateWithLifecycle()
    var segment by rememberSaveable { mutableIntStateOf(0) }
    val undo = LocalUndoController.current
    val restoreWeight = koinInject<RestoreWeight>()
    val deletedMessage = stringResource(Res.string.health_weight_deleted)
    val undoLabel = stringResource(Res.string.health_undo)
    val requester = rememberNotificationPermissionRequester { viewModel.refreshPermission() }

    LifecycleResumeEffect(Unit) {
        viewModel.refreshPermission()
        onPauseOrDispose { }
    }
    LaunchedEffect(state.deletedWeightId) {
        state.deletedWeightId?.let { id ->
            undo.show(deletedMessage, undoLabel) { restoreWeight(id) }
            viewModel.undoShown()
        }
    }

    val pet = state.pet
    when {
        state.loading -> LoadingState(modifier = modifier.fillMaxSize())
        state.failed -> ErrorState(stringResource(UiRes.string.state_load_error), modifier = modifier.fillMaxSize(), onRetry = viewModel::retry)
        pet == null -> Column(modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            EmptyState(
                illustration = BestIdIcons.Health,
                title = stringResource(Res.string.health_no_pet_title),
                body = stringResource(Res.string.health_no_pet_body),
                actionText = stringResource(Res.string.health_add_pet),
                onAction = onAddPet,
            )
        }
        else -> Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = BestIdTheme.spacing.screen, vertical = BestIdTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.sm),
        ) {
            Text(stringResource(Res.string.health_title), style = BestIdTheme.typography.title, modifier = Modifier.semantics { heading() })
            SegmentedControl(
                options = listOf(stringResource(Res.string.health_tab_care), stringResource(Res.string.health_tab_weight)),
                selectedIndex = segment,
                onSelect = { segment = it },
            )
            if (actionFailed) SaveFailedCard()
            if (segment == 0) {
                if (state.permission == NotificationPermission.DENIED && state.cares.any { it.reminderEnabled }) {
                    PermissionCard(
                        title = stringResource(Res.string.health_permission_title),
                        body = stringResource(Res.string.health_permission_body),
                        onOpenSettings = requester::openSettings,
                    )
                }
                CaresSection(state.cares, state.today, onOpenCare, viewModel::setReminder)
                PrimaryButton(stringResource(Res.string.health_add_care), onClick = onAddCare, icon = BestIdIcons.Add)
                if (state.cares.isNotEmpty()) {
                    InfoCard(icon = BestIdIcons.Notifications) {
                        Text(stringResource(Res.string.health_offline_note), style = BestIdTheme.typography.body)
                    }
                }
            } else {
                WeightSection(pet.name, state.weights, onDelete = viewModel::deleteWeight)
                PrimaryButton(stringResource(Res.string.health_add_weight), onClick = onAddWeight, icon = BestIdIcons.Add)
            }
        }
    }
}

@Composable
private fun CaresSection(
    cares: List<CareTask>,
    today: LocalDate?,
    onOpen: (String) -> Unit,
    onReminder: (String, Boolean) -> Unit,
) {
    if (cares.isEmpty()) {
        EmptyState(
            illustration = BestIdIcons.Health,
            title = stringResource(Res.string.health_care_empty_title),
            body = stringResource(Res.string.health_care_empty_body),
        )
        return
    }
    val editLabel = stringResource(Res.string.health_care_edit)
    cares.forEach { care ->
        // Solo la parte de arriba abre el cuidado: el interruptor queda aparte, sin toques cruzados.
        PlainCard {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = BestIdTheme.sizes.minTouch)
                    .clickable(onClickLabel = editLabel, role = Role.Button) { onOpen(care.id) }
                    .semantics(mergeDescendants = true) {},
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xxs)) {
                    Text(care.displayName(), style = BestIdTheme.typography.card)
                    Text(careLine(care, today), style = BestIdTheme.typography.body, color = BestIdTheme.colors.muted)
                }
                Icon(painterResource(BestIdIcons.ChevronRight), contentDescription = null, tint = BestIdTheme.colors.muted)
            }
            SwitchRow(
                title = stringResource(Res.string.health_care_reminder),
                checked = care.reminderEnabled,
                onCheckedChange = { onReminder(care.id, it) },
            )
        }
    }
}

@Composable
private fun careLine(care: CareTask, today: LocalDate?): String {
    val next = care.nextDate
    if (next != null && today != null) {
        val days = today.daysUntil(next)
        val whenText = when {
            days == 0 -> stringResource(Res.string.health_care_today)
            days > 0 -> stringResource(Res.string.health_care_in_days, daysText(days))
            else -> stringResource(Res.string.health_care_overdue, daysText(-days))
        }
        return stringResource(Res.string.health_care_next, next.formatDayMonth(), whenText)
    }
    return care.lastDate?.let { stringResource(Res.string.health_care_last, it.formatLong()) }
        ?: stringResource(Res.string.health_care_no_date)
}

@Composable
private fun WeightSection(petName: String, weights: List<WeightEntry>, onDelete: (String) -> Unit) {
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
