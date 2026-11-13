package com.bestid.app

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.lifecycle.SavedStateHandle
import androidx.room.RoomDatabase
import com.bestid.app.di.appModules
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.test.verify.verify
import kotlin.test.Test

/**
 * Verifica que cada definición de Koin encuentra sus dependencias. Un fallo aquí haría que la app
 * se cierre al abrir una pantalla. Context, Room y DataStore los aporta la plataforma en ejecución.
 */
class KoinGraphTest {
    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun appGraphResolves() {
        module { includes(appModules) }.verify(
            extraTypes = listOf(
                Context::class,
                RoomDatabase.Builder::class,
                DataStore::class,
                SavedStateHandle::class,
            ),
        )
    }
}
