package com.bestid.app

import androidx.compose.runtime.Composable
import com.bestid.app.navigation.BestIdNavHost
import com.bestid.core.designsystem.theme.AppTheme

/** Raíz de la UI compartida. Android (MainActivity) e iOS (MainViewController) solo la montan. */
@Composable
fun App() {
    AppTheme {
        BestIdNavHost()
    }
}
