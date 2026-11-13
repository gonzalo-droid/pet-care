package com.bestid.core.data.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.bestid.core.data.db.BestIdDatabase
import com.bestid.core.data.db.configured
import com.bestid.core.data.repository.DataStoreReminderSettings
import com.bestid.core.data.repository.DataStoreSelectedPetStore
import com.bestid.core.data.repository.RoomCareTaskRepository
import com.bestid.core.data.repository.RoomScheduledNotificationRepository
import com.bestid.core.data.repository.RoomWeightRepository
import com.bestid.core.data.repository.RoomPetRepository
import com.bestid.core.data.repository.RoomVaccineRecordRepository
import com.bestid.core.data.repository.RoomVaccineTypeRepository
import com.bestid.core.data.repository.RoomVetRepository
import com.bestid.core.data.repository.SystemAppClock
import com.bestid.core.data.repository.UuidGenerator
import com.bestid.core.domain.port.AppClock
import com.bestid.core.domain.port.CareTaskRepository
import com.bestid.core.domain.port.ReminderSettingsStore
import com.bestid.core.domain.port.ScheduledNotificationRepository
import com.bestid.core.domain.port.WeightRepository
import com.bestid.core.domain.port.IdGenerator
import com.bestid.core.domain.port.PetRepository
import com.bestid.core.domain.port.SelectedPetStore
import com.bestid.core.domain.port.VaccineRecordRepository
import com.bestid.core.domain.port.VaccineTypeRepository
import com.bestid.core.domain.port.VetRepository
import okio.Path.Companion.toPath
import org.koin.core.module.Module
import org.koin.dsl.module

/** Rutas y constructor de Room propios de cada plataforma (directorio privado de la app). */
internal expect fun platformDataModule(): Module

internal const val PREFERENCES_FILE = "bestid.preferences_pb"

internal fun createPreferences(path: String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(produceFile = { path.toPath() })

val dataModule: Module = module {
    includes(platformDataModule())
    single<BestIdDatabase> { get<androidx.room.RoomDatabase.Builder<BestIdDatabase>>().configured() }
    single<PetRepository> { RoomPetRepository(get()) }
    single<VetRepository> { RoomVetRepository(get()) }
    single<VaccineTypeRepository> { RoomVaccineTypeRepository(get()) }
    single<VaccineRecordRepository> { RoomVaccineRecordRepository(get()) }
    single<SelectedPetStore> { DataStoreSelectedPetStore(get()) }
    single<CareTaskRepository> { RoomCareTaskRepository(get()) }
    single<WeightRepository> { RoomWeightRepository(get()) }
    single<ScheduledNotificationRepository> {
        RoomScheduledNotificationRepository(get())
    }
    single<ReminderSettingsStore> { DataStoreReminderSettings(get()) }
    single<AppClock> { SystemAppClock() }
    single<IdGenerator> { UuidGenerator() }
}
