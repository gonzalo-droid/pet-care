package com.bestid.app.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.bestid.app.resources.settings_test_reminder
import com.bestid.app.resources.settings_test_reminder_done
import com.bestid.app.resources.settings_test_reminder_subject
import com.bestid.core.designsystem.component.InfoCard
import com.bestid.core.designsystem.theme.BestIdTheme
import com.bestid.core.domain.reminder.ScheduleTestReminder
import com.bestid.core.platform.notification.rememberNotificationPermissionRequester
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import com.bestid.app.resources.Res
import com.bestid.app.resources.back
import com.bestid.app.resources.catalog_title
import com.bestid.app.resources.settings
import com.bestid.app.resources.settings_coming_body
import com.bestid.app.resources.settings_coming_title
import com.bestid.app.resources.settings_open_catalog
import com.bestid.core.designsystem.catalog.ComponentCatalog
import com.bestid.core.designsystem.component.EmptyState
import com.bestid.core.designsystem.component.FlowScaffold
import com.bestid.core.designsystem.component.SecondaryButton
import com.bestid.core.designsystem.icon.BestIdIcons
import org.jetbrains.compose.resources.stringResource

/** Ajustes llega en M4. En M0 sirve de acceso al catálogo de componentes para revisar en teléfono. */
@Composable
internal fun SettingsPlaceholder(onClose: () -> Unit, onOpenCatalog: () -> Unit) {
    FlowScaffold(
        title = stringResource(Res.string.settings),
        onClose = onClose,
        closeIcon = BestIdIcons.ArrowBack,
        closeDescription = stringResource(Res.string.back),
    ) {
        EmptyState(
            illustration = BestIdIcons.Settings,
            title = stringResource(Res.string.settings_coming_title),
            body = stringResource(Res.string.settings_coming_body),
        )
        SecondaryButton(text = stringResource(Res.string.settings_open_catalog), onClick = onOpenCatalog)
        TestReminderButton()
    }
}

@Composable
internal fun CatalogScreen(onClose: () -> Unit) {
    ComponentCatalog(title = stringResource(Res.string.catalog_title), onClose = onClose)
}

/**
 * Herramienta de desarrollo para la prueba de M2: programa un aviso en 1 minuto (pide el permiso si
 * falta). Sirve para comprobar que llega con la app cerrada y tras reiniciar el teléfono.
 */
@Composable
private fun TestReminderButton() {
    val scheduleTest = koinInject<ScheduleTestReminder>()
    val scope = rememberCoroutineScope()
    var scheduled by remember { mutableStateOf(false) }
    val label = stringResource(Res.string.settings_test_reminder_subject)
    val requester = rememberNotificationPermissionRequester { granted ->
        if (granted) {
            scope.launch {
                scheduleTest(label)
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
