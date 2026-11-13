package com.bestid.core.platform.notification

import com.bestid.core.domain.reminder.ReminderRefType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** La persona tocó un aviso: la app abre ese registro (sección 11). */
data class ReminderTap(val refType: ReminderRefType, val refId: String)

/**
 * Puente entre el sistema (Activity en Android, delegado de UNUserNotificationCenter en iOS) y la
 * navegación de la app. Guarda el último toque hasta que la UI lo consume.
 */
object ReminderTaps {
    const val EXTRA_REF_TYPE = "bestid.refType"
    const val EXTRA_REF_ID = "bestid.refId"

    private val pending = MutableStateFlow<ReminderTap?>(null)
    val events: StateFlow<ReminderTap?> = pending.asStateFlow()

    fun emit(refType: String?, refId: String?) {
        val type = refType?.let { runCatching { ReminderRefType.valueOf(it) }.getOrNull() } ?: return
        if (refId.isNullOrEmpty()) return
        pending.value = ReminderTap(type, refId)
    }

    fun consume() {
        pending.value = null
    }
}
