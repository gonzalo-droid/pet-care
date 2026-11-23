package com.bestid.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bestid.core.domain.reminder.ScheduleTestReminder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext
import kotlin.time.Duration.Companion.seconds

/**
 * Solo en debug. Programa el aviso de prueba desde adb para la prueba en el emulador del CI:
 * `adb shell am broadcast -n com.nekodojo.bestid/com.bestid.app.DebugTestReminderReceiver --el seconds 60`
 */
class DebugTestReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val seconds = intent.getLongExtra(EXTRA_SECONDS, 60)
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            try {
                GlobalContext.get().get<ScheduleTestReminder>().invoke(seconds.seconds)
            } finally {
                pending.finish()
            }
        }
    }

    private companion object {
        const val EXTRA_SECONDS = "seconds"
    }
}
