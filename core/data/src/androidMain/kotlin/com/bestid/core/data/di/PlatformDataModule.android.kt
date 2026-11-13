package com.bestid.core.data.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.bestid.core.data.db.BestIdDatabase
import org.koin.dsl.module

internal actual fun platformDataModule() = module {
    single<RoomDatabase.Builder<BestIdDatabase>> {
        val context = get<Context>()
        Room.databaseBuilder<BestIdDatabase>(
            context = context,
            name = context.getDatabasePath(BestIdDatabase.FILE_NAME).absolutePath,
        )
    }
    single { createPreferences(get<Context>().filesDir.resolve(PREFERENCES_FILE).absolutePath) }
}
