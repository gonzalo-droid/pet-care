package com.bestid.feature.settings

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.bestid.core.designsystem.component.AlertCard
import com.bestid.core.designsystem.component.FlowScaffold
import com.bestid.core.designsystem.component.InfoCard
import com.bestid.core.designsystem.component.LoadingState
import com.bestid.core.designsystem.component.PrimaryButton
import com.bestid.core.designsystem.component.SecondaryButton
import com.bestid.core.designsystem.icon.BestIdIcons
import com.bestid.core.designsystem.theme.BestIdTheme
import com.bestid.core.domain.backup.BackupSummary
import com.bestid.core.domain.backup.ExportBackup
import com.bestid.core.domain.backup.ImportBackup
import com.bestid.core.domain.backup.ImportResult
import com.bestid.core.domain.port.AppClock
import com.bestid.core.domain.usecase.ObserveLastBackup
import com.bestid.core.navigation.BackupRoute
import com.bestid.core.navigation.SettingsRoute
import com.bestid.core.platform.share.MimeTypes
import com.bestid.core.platform.share.rememberFilePicker
import com.bestid.core.platform.share.rememberShareSheet
import com.bestid.core.ui.formatLong
import com.bestid.feature.settings.resources.Res
import com.bestid.feature.settings.resources.backup_body
import com.bestid.feature.settings.resources.backup_done_and
import com.bestid.feature.settings.resources.backup_done_body
import com.bestid.feature.settings.resources.backup_done_cares
import com.bestid.feature.settings.resources.backup_done_nothing
import com.bestid.feature.settings.resources.backup_done_pets
import com.bestid.feature.settings.resources.backup_done_photos
import com.bestid.feature.settings.resources.backup_done_title
import com.bestid.feature.settings.resources.backup_done_vaccines
import com.bestid.feature.settings.resources.backup_done_weights
import com.bestid.feature.settings.resources.backup_error_export
import com.bestid.feature.settings.resources.backup_error_invalid
import com.bestid.feature.settings.resources.backup_error_too_new
import com.bestid.feature.settings.resources.backup_export
import com.bestid.feature.settings.resources.backup_exporting
import com.bestid.feature.settings.resources.backup_file_name
import com.bestid.feature.settings.resources.backup_import
import com.bestid.feature.settings.resources.backup_import_hint
import com.bestid.feature.settings.resources.backup_importing
import com.bestid.feature.settings.resources.backup_last
import com.bestid.feature.settings.resources.backup_never
import com.bestid.feature.settings.resources.backup_share_title
import com.bestid.feature.settings.resources.backup_continue
import com.bestid.feature.settings.resources.backup_restore_body
import com.bestid.feature.settings.resources.backup_restore_title
import com.bestid.feature.settings.resources.backup_title
import com.bestid.feature.settings.resources.settings_back
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

sealed interface BackupMessage {
    data class Imported(val summary: BackupSummary) : BackupMessage
    data object Invalid : BackupMessage
    data object TooNew : BackupMessage
    data object ExportFailed : BackupMessage
}

data class BackupState(
    val lastExport: LocalDate? = null,
    val exporting: Boolean = false,
    val importing: Boolean = false,
    val message: BackupMessage? = null,
    /** Respaldo listo para la hoja de compartir; la pantalla lo consume una vez. */
    val readyPath: String? = null,
)

/** Respaldo (A2): exportar a la hoja de compartir e importar desde un archivo. */
class BackupViewModel(
    observeLast: ObserveLastBackup,
    private val export: ExportBackup,
    private val import: ImportBackup,
    private val clock: AppClock,
) : ViewModel() {
    private val local = MutableStateFlow(BackupState())

    val state: StateFlow<BackupState> = combine(observeLast(), local) { last, s ->
        s.copy(lastExport = last?.toLocalDateTime(TimeZone.currentSystemDefault())?.date)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BackupState())

    fun exportBackup() {
        if (local.value.exporting || local.value.importing) return
        local.update { it.copy(exporting = true, message = null) }
        viewModelScope.launch {
            val result = runCatching { export(getString(Res.string.backup_file_name, clock.today().toString())) }
            local.update {
                it.copy(exporting = false, readyPath = result.getOrNull(), message = if (result.isFailure) BackupMessage.ExportFailed else null)
            }
        }
    }

    fun importBackup(path: String) {
        if (local.value.exporting || local.value.importing) return
        local.update { it.copy(importing = true, message = null) }
        viewModelScope.launch {
            val message = when (val result = runCatching { import(path) }.getOrElse { ImportResult.Invalid }) {
                is ImportResult.Imported -> BackupMessage.Imported(result.summary)
                ImportResult.Invalid -> BackupMessage.Invalid
                ImportResult.TooNew -> BackupMessage.TooNew
            }
            local.update { it.copy(importing = false, message = message) }
        }
    }

    fun consumeReady() = local.update { it.copy(readyPath = null) }
}

@Composable
fun BackupScreen(onClose: () -> Unit, restore: Boolean = false, viewModel: BackupViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val shareSheet = rememberShareSheet()
    val filePicker = rememberFilePicker(viewModel::importBackup)
    val shareTitle = stringResource(Res.string.backup_share_title)

    LaunchedEffect(state.readyPath) {
        state.readyPath?.let {
            shareSheet.share(it, MimeTypes.ZIP, shareTitle)
            viewModel.consumeReady()
        }
    }

    val imported = state.message is BackupMessage.Imported
    FlowScaffold(
        title = stringResource(if (restore) Res.string.backup_restore_title else Res.string.backup_title),
        onClose = onClose,
        closeIcon = BestIdIcons.ArrowBack,
        closeDescription = stringResource(Res.string.settings_back),
        bottomAction = if (restore && imported) {
            { PrimaryButton(text = stringResource(Res.string.backup_continue), onClick = onClose) }
        } else {
            null
        },
    ) {
        if (restore) {
            // Teléfono nuevo, sin datos: solo tiene sentido importar.
            Text(stringResource(Res.string.backup_restore_body), style = BestIdTheme.typography.body)
            if (!imported) {
                PrimaryButton(
                    text = stringResource(Res.string.backup_import),
                    onClick = filePicker::pick,
                    loading = state.importing,
                )
            }
        } else {
            Text(stringResource(Res.string.backup_body), style = BestIdTheme.typography.body)
            Text(
                state.lastExport?.let { stringResource(Res.string.backup_last, it.formatLong()) } ?: stringResource(Res.string.backup_never),
                style = BestIdTheme.typography.bodyStrong,
            )
            PrimaryButton(
                text = stringResource(Res.string.backup_export),
                onClick = viewModel::exportBackup,
                loading = state.exporting,
                enabled = !state.importing,
                icon = BestIdIcons.Share,
            )
            SecondaryButton(
                text = stringResource(Res.string.backup_import),
                onClick = filePicker::pick,
                loading = state.importing,
                enabled = !state.exporting,
            )
        }
        Text(stringResource(Res.string.backup_import_hint), style = BestIdTheme.typography.support, color = BestIdTheme.colors.muted)
        if (state.exporting) LoadingState(text = stringResource(Res.string.backup_exporting))
        if (state.importing) LoadingState(text = stringResource(Res.string.backup_importing))
        when (val message = state.message) {
            null -> Unit
            is BackupMessage.Imported -> InfoCard(icon = BestIdIcons.CheckCircle) {
                Text(stringResource(Res.string.backup_done_title), style = BestIdTheme.typography.card)
                Text(summaryText(message.summary), style = BestIdTheme.typography.body)
            }
            BackupMessage.Invalid -> AlertCard { Text(stringResource(Res.string.backup_error_invalid), style = BestIdTheme.typography.body) }
            BackupMessage.TooNew -> AlertCard { Text(stringResource(Res.string.backup_error_too_new), style = BestIdTheme.typography.body) }
            BackupMessage.ExportFailed -> AlertCard { Text(stringResource(Res.string.backup_error_export), style = BestIdTheme.typography.body) }
        }
    }
}

/** "Se importaron 2 mascotas, 5 vacunas y 3 fotos." (sección 13) */
@Composable
private fun summaryText(summary: BackupSummary): String {
    val parts = listOfNotNull(
        summary.pets.takeIf { it > 0 }?.let { pluralStringResource(Res.plurals.backup_done_pets, it, it) },
        summary.records.takeIf { it > 0 }?.let { pluralStringResource(Res.plurals.backup_done_vaccines, it, it) },
        summary.cares.takeIf { it > 0 }?.let { pluralStringResource(Res.plurals.backup_done_cares, it, it) },
        summary.weights.takeIf { it > 0 }?.let { pluralStringResource(Res.plurals.backup_done_weights, it, it) },
        summary.photos.takeIf { it > 0 }?.let { pluralStringResource(Res.plurals.backup_done_photos, it, it) },
    )
    if (parts.isEmpty()) return stringResource(Res.string.backup_done_nothing)
    val and = stringResource(Res.string.backup_done_and)
    val list = if (parts.size == 1) parts.single() else parts.dropLast(1).joinToString(", ") + " $and " + parts.last()
    return stringResource(Res.string.backup_done_body, list)
}

/** Ajustes y Respaldo, sobre las pestañas. */
fun NavGraphBuilder.settingsFlows(
    onClose: () -> Unit,
    onOpenBackup: () -> Unit,
    onOpenCatalog: () -> Unit,
    onDeletedAll: () -> Unit,
) {
    composable<SettingsRoute> {
        SettingsScreen(onClose = onClose, onOpenBackup = onOpenBackup, onOpenCatalog = onOpenCatalog, onDeletedAll = onDeletedAll)
    }
    composable<BackupRoute> { entry -> BackupScreen(onClose = onClose, restore = entry.toRoute<BackupRoute>().restore) }
}

val settingsModule = module {
    viewModelOf(::SettingsViewModel)
    viewModelOf(::BackupViewModel)
}
