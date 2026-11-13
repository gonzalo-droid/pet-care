package com.bestid.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.bestid.app.navigation.BestIdNavHost
import com.bestid.core.designsystem.component.LocalUndoController
import com.bestid.core.designsystem.component.rememberUndoController
import com.bestid.core.designsystem.theme.AppTheme

/** Raíz de la UI compartida. Android (MainActivity) e iOS (MainViewController) solo la montan. */
@Composable
fun App() {
    AppTheme {
        CompositionLocalProvider(LocalUndoController provides rememberUndoController()) {
            BestIdNavHost()
        }
    }
}
