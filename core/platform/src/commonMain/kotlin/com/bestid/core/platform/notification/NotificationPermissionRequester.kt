package com.bestid.core.platform.notification

import androidx.compose.runtime.Composable

/**
 * Pedir el permiso de avisos y abrir los ajustes del sistema necesitan la pantalla, por eso viven
 * en la UI y no en el puerto `NotificationScheduler`.
 */
interface NotificationPermissionRequester {
    /** Muestra el diálogo del sistema; `onResult` recibe si quedó concedido. */
    fun request()

    /** Abre los ajustes de avisos de la app en el sistema. */
    fun openSettings()
}

@Composable
expect fun rememberNotificationPermissionRequester(onResult: (granted: Boolean) -> Unit): NotificationPermissionRequester
