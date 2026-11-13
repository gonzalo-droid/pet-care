package com.bestid.core.platform.notification

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.bestid.core.domain.reminder.SyncReminders
import com.bestid.core.platform.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext

/** Llega la alarma: muestra el aviso. Tocarlo abre la app en el registro (ReminderTaps). */
class ReminderAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getStringExtra(EXTRA_ID) ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            try {
                AndroidNotificationScheduler.ensureChannel(context)
                val open = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    putExtra(ReminderTaps.EXTRA_REF_TYPE, intent.getStringExtra(ReminderTaps.EXTRA_REF_TYPE))
                    putExtra(ReminderTaps.EXTRA_REF_ID, intent.getStringExtra(ReminderTaps.EXTRA_REF_ID))
                }
                val body = intent.getStringExtra(EXTRA_BODY).orEmpty()
                val notification = NotificationCompat.Builder(context, AndroidNotificationScheduler.CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_stat_bestid)
                    .setContentTitle(intent.getStringExtra(EXTRA_TITLE))
                    .setContentText(body)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                    .setAutoCancel(true)
                    .setContentIntent(
                        open?.let {
                            PendingIntent.getActivity(context, id.hashCode(), it, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                        },
                    )
                    .build()
                NotificationManagerCompat.from(context).notify(id, 0, notification)
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val EXTRA_ID = "bestid.id"
        const val EXTRA_TITLE = "bestid.title"
        const val EXTRA_BODY = "bestid.body"
    }
}

/**
 * Android borra las alarmas al reiniciar y puede moverlas al cambiar la hora o la zona: se vuelven
 * a programar todas desde la base (sección 11).
 */
class RescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val koin = GlobalContext.getOrNull() ?: return
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            try {
                koin.get<SyncReminders>().invoke(force = true)
            } finally {
                pending.finish()
            }
        }
    }
}

/** Lleva a [ReminderTaps] el toque de un aviso que abrió la Activity. */
fun ReminderTaps.handle(intent: Intent?) {
    intent ?: return
    emit(intent.getStringExtra(ReminderTaps.EXTRA_REF_TYPE), intent.getStringExtra(ReminderTaps.EXTRA_REF_ID))
    intent.removeExtra(ReminderTaps.EXTRA_REF_TYPE)
    intent.removeExtra(ReminderTaps.EXTRA_REF_ID)
}
