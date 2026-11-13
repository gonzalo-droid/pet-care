package com.bestid.core.designsystem.component

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Muestra "Deshacer" durante 5 segundos aunque la pantalla que borró ya se haya cerrado
 * (por ejemplo, eliminar desde el detalle y volver a la lista). Vive a nivel de la app.
 */
class UndoController(val hostState: SnackbarHostState, private val scope: CoroutineScope) {
    fun show(message: String, undoLabel: String, onUndo: suspend () -> Unit) {
        scope.launch {
            hostState.currentSnackbarData?.dismiss()
            if (hostState.showUndo(message, undoLabel)) onUndo()
        }
    }
}

val LocalUndoController = staticCompositionLocalOf<UndoController> {
    error("UndoController no está disponible: provéelo en la raíz de la app")
}

@Composable
fun rememberUndoController(): UndoController {
    val scope = rememberCoroutineScope()
    return remember(scope) { UndoController(SnackbarHostState(), scope) }
}
