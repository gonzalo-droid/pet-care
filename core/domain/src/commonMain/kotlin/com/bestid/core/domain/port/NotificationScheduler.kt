package com.bestid.core.domain.port

import com.bestid.core.domain.reminder.PlannedReminder

enum class NotificationPermission { GRANTED, DENIED, NOT_DETERMINED }

/**
 * Puerto de avisos locales (sección 8). Adaptadores: AlarmManager + NotificationCompat en Android,
 * UNUserNotificationCenter en iOS. Pedir el permiso necesita la pantalla, así que vive en la UI
 * (`rememberNotificationPermissionRequester` en `core/platform`).
 */
interface NotificationScheduler {
    suspend fun permission(): NotificationPermission
    suspend fun schedule(reminders: List<PlannedReminder>)
    suspend fun cancel(ids: List<String>)
}
