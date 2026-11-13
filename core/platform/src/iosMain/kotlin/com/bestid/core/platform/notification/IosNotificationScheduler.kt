package com.bestid.core.platform.notification

import com.bestid.core.domain.port.NotificationPermission
import com.bestid.core.domain.port.NotificationScheduler
import com.bestid.core.domain.reminder.PlannedReminder
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import platform.Foundation.NSDateComponents
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusDenied
import platform.UserNotifications.UNAuthorizationStatusEphemeral
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume

/**
 * Adaptador iOS de `NotificationScheduler`: `UNCalendarNotificationTrigger` en la hora local.
 * iOS permite 64 avisos pendientes; el motor entrega como máximo 50 (sección 11).
 */
@OptIn(ExperimentalForeignApi::class)
class IosNotificationScheduler : NotificationScheduler {
    private val center get() = UNUserNotificationCenter.currentNotificationCenter()

    override suspend fun permission(): NotificationPermission = suspendCancellableCoroutine { cont ->
        center.getNotificationSettingsWithCompletionHandler { settings ->
            val status = when (settings?.authorizationStatus) {
                UNAuthorizationStatusAuthorized, UNAuthorizationStatusProvisional, UNAuthorizationStatusEphemeral ->
                    NotificationPermission.GRANTED
                UNAuthorizationStatusDenied -> NotificationPermission.DENIED
                else -> NotificationPermission.NOT_DETERMINED
            }
            cont.resume(status)
        }
    }

    override suspend fun schedule(reminders: List<PlannedReminder>) {
        reminders.forEach { reminder ->
            val text = reminder.text()
            val content = UNMutableNotificationContent().apply {
                setTitle(text.title)
                setBody(text.body)
                setSound(UNNotificationSound.defaultSound)
                setUserInfo(
                    mapOf<Any?, Any?>(
                        ReminderTaps.EXTRA_REF_TYPE to reminder.refType.name,
                        ReminderTaps.EXTRA_REF_ID to reminder.refId,
                    ),
                )
            }
            val local = reminder.fireAt.toLocalDateTime(TimeZone.currentSystemDefault())
            val components = NSDateComponents().apply {
                setYear(local.year.toLong())
                setMonth(local.month.number.toLong())
                setDay(local.day.toLong())
                setHour(local.hour.toLong())
                setMinute(local.minute.toLong())
                setSecond(local.second.toLong())
            }
            val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(components, repeats = false)
            val request = UNNotificationRequest.requestWithIdentifier(reminder.id, content, trigger)
            center.addNotificationRequest(request, withCompletionHandler = null)
        }
    }

    override suspend fun cancel(ids: List<String>) {
        if (ids.isNotEmpty()) center.removePendingNotificationRequestsWithIdentifiers(ids)
    }
}
