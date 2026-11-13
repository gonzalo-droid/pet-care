package com.bestid.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.bestid.core.domain.port.ReminderSettingsStore
import com.bestid.core.domain.reminder.DEFAULT_REMINDER_TIME
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalTime
import kotlin.time.Instant

class DataStoreReminderSettings(private val prefs: DataStore<Preferences>) : ReminderSettingsStore {
    private val timeKey = stringPreferencesKey("reminder_time")
    private val promptKey = booleanPreferencesKey("notification_prompt_shown")
    private val testKey = longPreferencesKey("test_reminder_at")

    override val reminderTime: Flow<LocalTime> = prefs.data
        .map { p -> p[timeKey]?.let { runCatching { LocalTime.parse(it) }.getOrNull() } ?: DEFAULT_REMINDER_TIME }
        .distinctUntilChanged()

    override val permissionPromptShown: Flow<Boolean> = prefs.data.map { it[promptKey] ?: false }

    override suspend fun setReminderTime(time: LocalTime) {
        prefs.edit { it[timeKey] = time.toString() }
    }

    override suspend fun markPermissionPromptShown() {
        prefs.edit { it[promptKey] = true }
    }

    override val testReminderAt: Flow<Instant?> = prefs.data.map { p -> p[testKey]?.let(Instant::fromEpochMilliseconds) }

    override suspend fun setTestReminderAt(at: Instant?) {
        prefs.edit { if (at == null) it.remove(testKey) else it[testKey] = at.toEpochMilliseconds() }
    }
}
