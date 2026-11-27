package com.bestid.core.designsystem.component

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

/**
 * Cierra la hoja con su animación y después corre la acción. Sacar la hoja de la composición
 * directamente (al tocar un botón) la hace desaparecer de golpe. Un segundo toque mientras se
 * cierra se ignora, para que "Eliminar" no corra dos veces.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun rememberHideThen(state: SheetState): (() -> Unit) -> Unit {
    val scope = rememberCoroutineScope()
    return remember(state, scope) {
        var closing = false
        { action ->
            if (!closing) {
                closing = true
                scope.launch { state.hide() }.invokeOnCompletion { action() }
            }
        }
    }
}
