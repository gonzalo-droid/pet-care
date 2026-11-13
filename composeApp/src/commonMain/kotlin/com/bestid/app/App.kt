package com.bestid.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import com.bestid.core.domain.reminder.ReminderCoordinator
import org.koin.compose.koinInject
import com.bestid.app.navigation.BestIdNavHost
import com.bestid.core.designsystem.component.LocalUndoController
import com.bestid.core.designsystem.component.rememberUndoController
import com.bestid.core.designsystem.theme.AppTheme

/** Raíz de la UI compartida. Android (MainActivity) e iOS (MainViewController) solo la montan. */
@Composable
fun App() {
    // Mantiene los avisos al día mientras la app está abierta (incluye "al abrir la app").
    val reminders = koinInject<ReminderCoordinator>()
    LaunchedEffect(reminders) { reminders.run() }
    AppTheme {
        CompositionLocalProvider(LocalUndoController provides rememberUndoController()) {
            BestIdNavHost()
        }
    }
}
