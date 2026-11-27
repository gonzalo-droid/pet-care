package com.bestid.app

import com.bestid.app.di.initKoin
import com.bestid.core.domain.reminder.SyncReminders
import com.bestid.core.platform.notification.IosNotifications
import kotlinx.cinterop.ExperimentalForeignApi
import kotlin.experimental.ExperimentalNativeApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatform
import platform.BackgroundTasks.BGAppRefreshTask
import platform.BackgroundTasks.BGAppRefreshTaskRequest
import platform.BackgroundTasks.BGTaskScheduler
import platform.Foundation.NSDate
import platform.Foundation.NSLog
import platform.Foundation.dateWithTimeIntervalSinceNow

private const val REFRESH_TASK_ID = "com.nekodojo.bestid.refresh"
private const val REFRESH_INTERVAL_SECONDS = 12.0 * 60 * 60
private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

/**
 * Arranque de iOS: lo llama iOSApp.swift al iniciar. Inicia Koin, instala el delegado de avisos
 * (para abrir el registro al tocar uno) y registra la actualización en segundo plano, que vuelve a
 * programar los 50 avisos más cercanos (sección 11).
 */
@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@Suppress("unused")
fun startApp() {
    // OBS-03: Kotlin/Native escribe el error solo en stderr, que no queda en el log del sistema. Así la
    // traza aparece en Console.app y en el informe de crash antes del abort. Solo la traza: sin datos.
    setUnhandledExceptionHook { error ->
        NSLog("BestID: excepción Kotlin no capturada: %@", error.stackTraceToString())
        terminateWithUnhandledException(error)
    }
    initKoin()
    IosNotifications.installDelegate()
    BGTaskScheduler.sharedScheduler.registerForTaskWithIdentifier(REFRESH_TASK_ID, usingQueue = null) { task ->
        scheduleRefresh()
        scope.launch {
            runCatching { KoinPlatform.getKoin().get<SyncReminders>().invoke() }
            (task as? BGAppRefreshTask)?.setTaskCompletedWithSuccess(true)
        }
    }
    scheduleRefresh()
}

@OptIn(ExperimentalForeignApi::class)
private fun scheduleRefresh() {
    val request = BGAppRefreshTaskRequest(REFRESH_TASK_ID)
    request.earliestBeginDate = NSDate.dateWithTimeIntervalSinceNow(REFRESH_INTERVAL_SECONDS)
    BGTaskScheduler.sharedScheduler.submitTaskRequest(request, error = null)
}
