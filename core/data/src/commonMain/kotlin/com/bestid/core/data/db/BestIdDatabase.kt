package com.bestid.core.data.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

/**
 * Base de datos local. Cada cambio de esquema sube [version] y trae su migración (regla 8);
 * los esquemas se exportan a `core/data/schemas/`.
 */
@Database(
    entities = [PetEntity::class, VetEntity::class, VaccineTypeEntity::class, VaccineRecordEntity::class],
    version = 1,
    exportSchema = true,
)
@ConstructedBy(BestIdDatabaseConstructor::class)
abstract class BestIdDatabase : RoomDatabase() {
    abstract fun petDao(): PetDao
    abstract fun vetDao(): VetDao
    abstract fun vaccineTypeDao(): VaccineTypeDao
    abstract fun vaccineRecordDao(): VaccineRecordDao

    companion object {
        const val FILE_NAME = "bestid.db"
    }
}

// Room genera la implementación en cada plataforma.
@Suppress("NO_ACTUAL_FOR_EXPECT", "KotlinNoActualForExpect")
expect object BestIdDatabaseConstructor : RoomDatabaseConstructor<BestIdDatabase> {
    override fun initialize(): BestIdDatabase
}

/** Configuración común: driver SQLite empaquetado y consultas fuera del hilo principal. */
fun RoomDatabase.Builder<BestIdDatabase>.configured(): BestIdDatabase =
    setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
