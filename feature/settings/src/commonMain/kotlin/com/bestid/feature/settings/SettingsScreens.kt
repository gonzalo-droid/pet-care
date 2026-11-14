package com.bestid.feature.settings

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.bestid.core.designsystem.component.ConfirmSheet
import com.bestid.core.designsystem.component.Eyebrow
import com.bestid.core.designsystem.component.FlowScaffold
import com.bestid.core.designsystem.component.InfoCard
import com.bestid.core.designsystem.component.ListRow
import com.bestid.core.designsystem.component.PlainCard
import com.bestid.core.designsystem.component.SecondaryButton
import com.bestid.core.designsystem.component.TextAction
import com.bestid.core.designsystem.component.TimeDialog
import com.bestid.core.designsystem.icon.BestIdIcons
import com.bestid.core.designsystem.theme.BestIdTheme
import com.bestid.core.domain.backup.DeleteAllData
import com.bestid.core.domain.reminder.ScheduleTestReminder
import com.bestid.core.domain.usecase.GetAppVersion
import com.bestid.core.domain.usecase.ObserveLastBackup
import com.bestid.core.domain.usecase.ObserveReminderTime
import com.bestid.core.domain.usecase.SetReminderTime
import com.bestid.core.platform.notification.rememberNotificationPermissionRequester
import com.bestid.core.ui.formatLong
import com.bestid.feature.settings.resources.Res
import com.bestid.feature.settings.resources.settings_about
import com.bestid.feature.settings.resources.settings_about_body
import com.bestid.feature.settings.resources.settings_am
import com.bestid.feature.settings.resources.settings_back
import com.bestid.feature.settings.resources.settings_backup
import com.bestid.feature.settings.resources.settings_backup_last
import com.bestid.feature.settings.resources.settings_backup_never
import com.bestid.feature.settings.resources.settings_delete_all
import com.bestid.feature.settings.resources.settings_delete_body
import com.bestid.feature.settings.resources.settings_delete_confirm
import com.bestid.feature.settings.resources.settings_delete_title
import com.bestid.feature.settings.resources.settings_open_catalog
import com.bestid.feature.settings.resources.settings_pm
import com.bestid.feature.settings.resources.settings_privacy
import com.bestid.feature.settings.resources.settings_privacy_body
import com.bestid.feature.settings.resources.settings_reminder_time
import com.bestid.feature.settings.resources.settings_reminder_time_dialog
import com.bestid.feature.settings.resources.settings_reminder_time_hint
import com.bestid.feature.settings.resources.settings_reminder_time_value
import com.bestid.feature.settings.resources.settings_reminders
import com.bestid.feature.settings.resources.settings_test_reminder
import com.bestid.feature.settings.resources.settings_test_reminder_done
import com.bestid.feature.settings.resources.settings_title
import com.bestid.feature.settings.resources.settings_tools
import com.bestid.feature.settings.resources.settings_version
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

data class SettingsState(
    val reminderTime: LocalTime? = null,
    val lastBackup: LocalDate? = null,
    val version: String = "",
    val deleted: Boolean = false,
)

/** Ajustes (A1). */
class SettingsViewModel(
    observeTime: ObserveReminderTime,
    observeLastBackup: ObserveLastBackup,
    getVersion: GetAppVersion,
    private val setTime: SetReminderTime,
    private val deleteAll: DeleteAllData,
) : ViewModel() {
    private val deleted = MutableStateFlow(false)

    val state: StateFlow<SettingsState> = combine(observeTime(), observeLastBackup(), deleted) { time, last, gone ->
        SettingsState(time, last?.toLocalDateTime(TimeZone.currentSystemDefault())?.date, getVersion(), gone)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsState(version = getVersion()))

    fun setReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch { setTime(LocalTime(hour, minute)) }
    }

    fun deleteEverything() {
        viewModelScope.launch {
            deleteAll()
            deleted.value = true
        }
    }
}

/**
 * Ajustes (A1): respaldo, hora de los avisos, privacidad, acerca de, versión y "Borrar todos los
 * datos" (sección 14). Las herramientas de prueba se quitan en M5 (D26).
 */
@Composable
fun SettingsScreen(
    onClose: () -> Unit,
    onOpenBackup: () -> Unit,
    onOpenCatalog: () -> Unit,
    onDeletedAll: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var editTime by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    LaunchedEffect(state.deleted) { if (state.deleted) onDeletedAll() }

    FlowScaffold(
        title = stringResource(Res.string.settings_title),
        onClose = onClose,
        closeIcon = BestIdIcons.ArrowBack,
        closeDescription = stringResource(Res.string.settings_back),
    ) {
        PlainCard(onClick = onOpenBackup, onClickLabel = stringResource(Res.string.settings_backup)) {
            Eyebrow(stringResource(Res.string.settings_backup))
            Text(
                state.lastBackup?.let { stringResource(Res.string.settings_backup_last, it.formatLong()) }
                    ?: stringResource(Res.string.settings_backup_never),
                style = BestIdTheme.typography.body,
            )
        }

        val time = state.reminderTime
        PlainCard(onClick = { editTime = true }, onClickLabel = stringResource(Res.string.settings_reminder_time)) {
            Eyebrow(stringResource(Res.string.settings_reminders))
            ListRow(stringResource(Res.string.settings_reminder_time), time?.let { formatTime(it) }.orEmpty())
            Text(stringResource(Res.string.settings_reminder_time_hint), style = BestIdTheme.typography.support, color = BestIdTheme.colors.muted)
        }

        PlainCard {
            Eyebrow(stringResource(Res.string.settings_privacy))
            Text(stringResource(Res.string.settings_privacy_body), style = BestIdTheme.typography.body)
        }

        PlainCard {
            Eyebrow(stringResource(Res.string.settings_about))
            Text(stringResource(Res.string.settings_about_body), style = BestIdTheme.typography.body)
            Text(stringResource(Res.string.settings_version, state.version), style = BestIdTheme.typography.support, color = BestIdTheme.colors.muted)
        }

        TextAction(
            text = stringResource(Res.string.settings_delete_all),
            onClick = { confirmDelete = true },
            icon = BestIdIcons.Delete,
            destructive = true,
        )

        Eyebrow(stringResource(Res.string.settings_tools))
        SecondaryButton(text = stringResource(Res.string.settings_open_catalog), onClick = onOpenCatalog)
        TestReminderButton()
    }

    val time = state.reminderTime
    if (editTime && time != null) {
        TimeDialog(
            title = stringResource(Res.string.settings_reminder_time_dialog),
            initialHour = time.hour,
            initialMinute = time.minute,
            onConfirm = { h, m ->
                editTime = false
                viewModel.setReminderTime(h, m)
            },
            onDismiss = { editTime = false },
        )
    }

    if (confirmDelete) {
        ConfirmSheet(
            title = stringResource(Res.string.settings_delete_title),
            body = stringResource(Res.string.settings_delete_body),
            confirmText = stringResource(Res.string.settings_delete_confirm),
            destructive = true,
            onConfirm = {
                confirmDelete = false
                viewModel.deleteEverything()
            },
            onDismiss = { confirmDelete = false },
        )
    }
}

/** "9:00 a. m." */
@Composable
private fun formatTime(time: LocalTime): String {
    val hour12 = (time.hour % 12).let { if (it == 0) 12 else it }
    val period = stringResource(if (time.hour < 12) Res.string.settings_am else Res.string.settings_pm)
    return stringResource(Res.string.settings_reminder_time_value, hour12, time.minute.toString().padStart(2, '0'), period)
}

/**
 * Herramienta de desarrollo para la prueba de M2 (D26): programa un aviso en 1 minuto (pide el
 * permiso si falta) para comprobar que llega con la app cerrada y tras reiniciar el teléfono.
 */
@Composable
private fun TestReminderButton() {
    val scheduleTest = koinInject<ScheduleTestReminder>()
    val scope = rememberCoroutineScope()
    var scheduled by remember { mutableStateOf(false) }
    val requester = rememberNotificationPermissionRequester { granted ->
        if (granted) {
            scope.launch {
                scheduleTest()
                scheduled = true
            }
        }
    }
    SecondaryButton(
        text = stringResource(Res.string.settings_test_reminder),
        onClick = requester::request,
        icon = BestIdIcons.Notifications,
    )
    if (scheduled) {
        InfoCard(icon = BestIdIcons.CheckCircle) {
            Text(stringResource(Res.string.settings_test_reminder_done), style = BestIdTheme.typography.body)
        }
    }
}
