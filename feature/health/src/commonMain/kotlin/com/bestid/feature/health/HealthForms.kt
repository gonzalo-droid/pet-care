package com.bestid.feature.health

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bestid.core.designsystem.component.ChipChoice
import com.bestid.core.designsystem.component.ConfirmSheet
import com.bestid.core.designsystem.component.DateField
import com.bestid.core.designsystem.component.FieldKind
import com.bestid.core.designsystem.component.FieldLabelText
import com.bestid.core.designsystem.component.AlertCard
import com.bestid.core.designsystem.component.ErrorState
import com.bestid.core.designsystem.component.FlowScaffold
import com.bestid.core.designsystem.component.FormField
import com.bestid.core.designsystem.component.InfoCard
import com.bestid.core.designsystem.component.LoadingState
import com.bestid.core.designsystem.component.LocalUndoController
import com.bestid.core.designsystem.component.PrimaryButton
import com.bestid.core.designsystem.component.SwitchRow
import com.bestid.core.designsystem.component.TextAction
import com.bestid.core.designsystem.icon.BestIdIcons
import com.bestid.core.designsystem.theme.BestIdTheme
import com.bestid.core.ui.DeleteFailedCard
import com.bestid.core.ui.LoadFailedState
import com.bestid.core.ui.SaveFailedCard
import com.bestid.core.domain.schedule.CareSchedule
import com.bestid.core.domain.usecase.RestoreCareTask
import com.bestid.core.domain.validation.CareError
import com.bestid.core.domain.validation.WeightError
import com.bestid.core.model.CareKind
import com.bestid.core.model.IntervalUnit
import com.bestid.core.platform.date.rememberDatePickerLauncher
import com.bestid.core.ui.formatLong
import com.bestid.core.ui.label
import com.bestid.feature.health.resources.Res
import com.bestid.feature.health.resources.care_deleted
import com.bestid.feature.health.resources.care_delete_body
import com.bestid.feature.health.resources.care_delete_title
import com.bestid.feature.health.resources.care_error_future
import com.bestid.feature.health.resources.care_error_interval
import com.bestid.feature.health.resources.care_error_last_date
import com.bestid.feature.health.resources.care_error_title
import com.bestid.feature.health.resources.care_form_close
import com.bestid.feature.health.resources.care_form_days
import com.bestid.feature.health.resources.care_form_delete
import com.bestid.feature.health.resources.care_form_edit
import com.bestid.feature.health.resources.care_form_interval
import com.bestid.feature.health.resources.care_form_interval_help
import com.bestid.feature.health.resources.care_form_interval_placeholder
import com.bestid.feature.health.resources.care_form_kind
import com.bestid.feature.health.resources.care_form_last_date
import com.bestid.feature.health.resources.care_form_months
import com.bestid.feature.health.resources.care_form_new
import com.bestid.feature.health.resources.care_form_next
import com.bestid.feature.health.resources.care_form_reminder
import com.bestid.feature.health.resources.care_form_reminder_help
import com.bestid.feature.health.resources.care_form_save
import com.bestid.feature.health.resources.care_form_title
import com.bestid.feature.health.resources.care_form_title_placeholder
import com.bestid.feature.health.resources.care_not_found
import com.bestid.feature.health.resources.health_delete
import com.bestid.feature.health.resources.health_no_pet
import com.bestid.feature.health.resources.health_undo
import com.bestid.feature.health.resources.weight_error_future
import com.bestid.feature.health.resources.weight_error_range
import com.bestid.feature.health.resources.weight_error_required
import com.bestid.feature.health.resources.weight_form_close
import com.bestid.feature.health.resources.weight_form_date
import com.bestid.feature.health.resources.weight_form_kg
import com.bestid.feature.health.resources.weight_form_kg_placeholder
import com.bestid.feature.health.resources.weight_form_save
import com.bestid.feature.health.resources.weight_form_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/** Agregar o editar cuidado (S2): tipo, nombre si es "Otro", última fecha, intervalo y aviso. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CareFormScreen(
    careId: String?,
    onClose: () -> Unit,
    onAskPermission: () -> Unit,
    viewModel: CareFormViewModel = koinViewModel(key = "care-form-$careId") { parametersOf(careId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val undo = LocalUndoController.current
    val restore = koinInject<RestoreCareTask>()
    val deletedMessage = stringResource(Res.string.care_deleted)
    val undoLabel = stringResource(Res.string.health_undo)
    var confirmDelete by remember { mutableStateOf(false) }
    val picker = rememberDatePickerLauncher(viewModel::onLastDate)

    LaunchedEffect(state.done) {
        if (!state.done) return@LaunchedEffect
        state.deletedId?.let { id -> undo.show(deletedMessage, undoLabel) { restore(id) } }
        if (state.askPermission) onAskPermission() else onClose()
    }

    FlowScaffold(
        title = stringResource(if (state.isEdit) Res.string.care_form_edit else Res.string.care_form_new),
        onClose = onClose,
        closeDescription = stringResource(Res.string.care_form_close),
        confirmDiscard = state.dirty && !state.done,
        bottomAction = {
            if (state.saveFailed) SaveFailedCard()
            PrimaryButton(
                stringResource(Res.string.care_form_save),
                onClick = viewModel::save,
                loading = state.saving,
                enabled = state.loaded && state.petId != null,
            )
        },
    ) {
        when {
            state.loadFailed -> LoadFailedState(onRetry = viewModel::retryLoad)
            state.notFound -> ErrorState(stringResource(Res.string.care_not_found))
            !state.loaded -> LoadingState()
            state.petId == null -> NoPetCard()
        }
        if (state.loadFailed || state.notFound || !state.loaded || state.petId == null) return@FlowScaffold
        Column(verticalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xs)) {
            FieldLabelText(stringResource(Res.string.care_form_kind))
            FlowRow(
                modifier = Modifier.selectableGroup(),
                horizontalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xs),
                verticalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xs),
            ) {
                CareKind.entries.forEach { kind ->
                    ChipChoice(stringResource(kind.label()), state.kind == kind, onSelect = { viewModel.onKind(kind) })
                }
            }
        }
        if (state.kind == CareKind.OTHER) {
            FormField(
                label = stringResource(Res.string.care_form_title),
                value = state.title,
                onValueChange = viewModel::onTitle,
                placeholder = stringResource(Res.string.care_form_title_placeholder),
                error = if (CareError.TITLE_REQUIRED in state.errors) stringResource(Res.string.care_error_title) else null,
            )
        }
        DateField(
            label = stringResource(Res.string.care_form_last_date),
            value = state.lastDate?.formatLong(),
            onClick = { picker.launch(state.lastDate) },
            optional = !state.reminderEnabled,
            error = when {
                CareError.LAST_DATE_REQUIRED in state.errors -> stringResource(Res.string.care_error_last_date)
                CareError.LAST_DATE_IN_FUTURE in state.errors -> stringResource(Res.string.care_error_future)
                else -> null
            },
        )
        Column(verticalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xs)) {
            FormField(
                label = stringResource(Res.string.care_form_interval),
                value = state.intervalText,
                onValueChange = viewModel::onInterval,
                placeholder = stringResource(Res.string.care_form_interval_placeholder),
                helper = stringResource(Res.string.care_form_interval_help),
                error = if (CareError.INTERVAL_REQUIRED in state.errors) stringResource(Res.string.care_error_interval) else null,
                optional = !state.reminderEnabled,
                kind = FieldKind.NUMBER,
            )
            FlowRow(modifier = Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(BestIdTheme.spacing.xs)) {
                ChipChoice(stringResource(Res.string.care_form_months), state.unit == IntervalUnit.MONTH, onSelect = { viewModel.onUnit(IntervalUnit.MONTH) })
                ChipChoice(stringResource(Res.string.care_form_days), state.unit == IntervalUnit.DAY, onSelect = { viewModel.onUnit(IntervalUnit.DAY) })
            }
        }
        SwitchRow(
            title = stringResource(Res.string.care_form_reminder),
            subtitle = stringResource(Res.string.care_form_reminder_help),
            checked = state.reminderEnabled,
            onCheckedChange = viewModel::onReminder,
        )
        CareSchedule.nextDate(state.lastDate, state.intervalText.toIntOrNull(), state.unit)?.let { next ->
            InfoCard(icon = BestIdIcons.Calendar) {
                Text(stringResource(Res.string.care_form_next, next.formatLong()), style = BestIdTheme.typography.bodyStrong)
            }
        }
        if (state.deleteFailed) DeleteFailedCard()
        if (state.isEdit) {
            TextAction(stringResource(Res.string.care_form_delete), onClick = { confirmDelete = true }, icon = BestIdIcons.Delete, destructive = true)
        }
    }

    if (confirmDelete) {
        ConfirmSheet(
            title = stringResource(Res.string.care_delete_title),
            body = stringResource(Res.string.care_delete_body),
            confirmText = stringResource(Res.string.health_delete),
            destructive = true,
            onConfirm = {
                confirmDelete = false
                viewModel.delete()
            },
            onDismiss = { confirmDelete = false },
        )
    }
}

/** Agregar peso (S3). */
@Composable
fun WeightFormScreen(onClose: () -> Unit, viewModel: WeightFormViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val picker = rememberDatePickerLauncher(viewModel::onDate)
    LaunchedEffect(state.done) { if (state.done) onClose() }

    FlowScaffold(
        title = stringResource(Res.string.weight_form_title),
        onClose = onClose,
        closeDescription = stringResource(Res.string.weight_form_close),
        confirmDiscard = state.dirty && !state.done,
        bottomAction = {
            if (state.saveFailed) SaveFailedCard()
            PrimaryButton(
                stringResource(Res.string.weight_form_save),
                onClick = viewModel::save,
                loading = state.saving,
                enabled = state.loaded && state.petId != null,
            )
        },
    ) {
        if (state.loaded && state.petId == null) {
            NoPetCard()
            return@FlowScaffold
        }
        DateField(
            label = stringResource(Res.string.weight_form_date),
            value = state.date?.formatLong(),
            onClick = { picker.launch(state.date) },
            error = if (state.error == WeightError.DATE_IN_FUTURE) stringResource(Res.string.weight_error_future) else null,
        )
        FormField(
            label = stringResource(Res.string.weight_form_kg),
            value = state.kgText,
            onValueChange = viewModel::onKg,
            placeholder = stringResource(Res.string.weight_form_kg_placeholder),
            kind = FieldKind.DECIMAL,
            error = when (state.error) {
                WeightError.REQUIRED -> stringResource(Res.string.weight_error_required)
                WeightError.OUT_OF_RANGE -> stringResource(Res.string.weight_error_range)
                else -> null
            },
        )
    }
}

/** Sin mascota seleccionada (por ejemplo, se borró mientras el formulario estaba abierto). */
@Composable
private fun NoPetCard() {
    AlertCard { Text(stringResource(Res.string.health_no_pet), style = BestIdTheme.typography.body) }
}
