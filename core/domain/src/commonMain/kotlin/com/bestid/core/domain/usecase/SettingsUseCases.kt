package com.bestid.core.domain.usecase

import com.bestid.core.domain.port.AppInfo
import com.bestid.core.domain.port.BackupSettingsStore
import com.bestid.core.domain.port.ReminderSettingsStore
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalTime
import kotlin.time.Instant

class ObserveReminderTime(private val settings: ReminderSettingsStore) {
    operator fun invoke(): Flow<LocalTime> = settings.reminderTime
}

/** Cambia la hora de los avisos. `ReminderCoordinator` observa la hora y reprograma solo. */
class SetReminderTime(private val settings: ReminderSettingsStore) {
    suspend operator fun invoke(time: LocalTime) = settings.setReminderTime(LocalTime(time.hour, time.minute))
}

class ObserveLastBackup(private val settings: BackupSettingsStore) {
    operator fun invoke(): Flow<Instant?> = settings.lastExportAt
}

class GetAppVersion(private val info: AppInfo) {
    operator fun invoke(): String = info.versionName
}

/** Las herramientas de prueba (catálogo, aviso de prueba) solo existen en debug, no en la beta. */
class ShowDeveloperTools(private val info: AppInfo) {
    operator fun invoke(): Boolean = info.isDebugBuild
}
