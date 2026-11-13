package com.bestid.core.platform.notification

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.app.NotificationManagerCompat
import com.bestid.core.domain.port.NotificationPermission
import com.bestid.core.domain.port.NotificationScheduler
import com.bestid.core.domain.reminder.PlannedReminder
import com.bestid.core.platform.resources.Res
import com.bestid.core.platform.resources.reminder_channel_description
import com.bestid.core.platform.resources.reminder_channel_name
import org.jetbrains.compose.resources.getString

/**
 * Adaptador Android de `NotificationScheduler`. Cada alarma lleva el título y el texto ya armados;
 * [ReminderAlarmReceiver] solo los muestra.
 *
 * - Android 12+: alarma no exacta con `setAndAllowWhileIdle` (sección 11; no pide el permiso de
 *   alarmas exactas). El sistema la entrega a lo más 1 hora después de la hora pedida.
 * - Android 8 a 11: ahí la ventana de una alarma no exacta es el 75 % del tiempo que falta (un aviso
 *   a 7 días podía llegar días tarde), así que se usa `setExactAndAllowWhileIdle`, que en esas
 *   versiones no necesita ningún permiso (D28).
 */
class AndroidNotificationScheduler(private val context: Context) : NotificationScheduler {
    private val alarms = context.getSystemService(AlarmManager::class.java)

    override suspend fun permission(): NotificationPermission {
        val enabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
        return when {
            enabled -> NotificationPermission.GRANTED
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED &&
                !PermissionPrefs.wasAsked(context) -> NotificationPermission.NOT_DETERMINED
            else -> NotificationPermission.DENIED
        }
    }

    override suspend fun schedule(reminders: List<PlannedReminder>) {
        ensureChannel(context)
        reminders.forEach { reminder ->
            val text = reminder.text()
            val intent = alarmIntent(context, reminder.id)
                .putExtra(ReminderAlarmReceiver.EXTRA_ID, reminder.id)
                .putExtra(ReminderAlarmReceiver.EXTRA_TITLE, text.title)
                .putExtra(ReminderAlarmReceiver.EXTRA_BODY, text.body)
                .putExtra(ReminderTaps.EXTRA_REF_TYPE, reminder.refType.name)
                .putExtra(ReminderTaps.EXTRA_REF_ID, reminder.refId)
            val pending = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            val at = reminder.fireAt.toEpochMilliseconds()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pending)
            } else {
                alarms.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pending)
            }
        }
    }

    override suspend fun cancel(ids: List<String>) {
        ids.forEach { id ->
            PendingIntent.getBroadcast(
                context,
                0,
                alarmIntent(context, id),
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
            )?.let {
                alarms.cancel(it)
                it.cancel()
            }
        }
    }

    internal companion object {
        const val CHANNEL_ID = "bestid_reminders"

        /** El URI hace única cada alarma (las extras no cuentan para distinguir PendingIntents). */
        fun alarmIntent(context: Context, id: String) =
            Intent(context, ReminderAlarmReceiver::class.java).setData("bestid://reminder/$id".toUri())

        suspend fun ensureChannel(context: Context) {
            val manager = context.getSystemService(NotificationManager::class.java)
            if (manager.getNotificationChannel(CHANNEL_ID) != null) return
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(Res.string.reminder_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply { description = getString(Res.string.reminder_channel_description) }
            manager.createNotificationChannel(channel)
        }
    }
}

/** Android no distingue "nunca preguntado" de "denegado": se recuerda si ya se pidió. */
internal object PermissionPrefs {
    private const val FILE = "bestid_notification_permission"
    private const val KEY = "asked"

    fun wasAsked(context: Context) = context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getBoolean(KEY, false)

    fun markAsked(context: Context) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putBoolean(KEY, true).apply()
    }
}
