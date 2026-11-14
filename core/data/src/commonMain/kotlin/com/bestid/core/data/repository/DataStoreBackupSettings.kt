package com.bestid.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import com.bestid.core.domain.port.BackupSettingsStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Instant

class DataStoreBackupSettings(private val prefs: DataStore<Preferences>) : BackupSettingsStore {
    private val lastExportKey = longPreferencesKey("last_backup_export_at")

    override val lastExportAt: Flow<Instant?> = prefs.data.map { p -> p[lastExportKey]?.let(Instant::fromEpochMilliseconds) }

    override suspend fun setLastExportAt(at: Instant) {
        prefs.edit { it[lastExportKey] = at.toEpochMilliseconds() }
    }
}
