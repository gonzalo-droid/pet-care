package com.bestid.core.data.db

import androidx.room.AutoMigration
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
    entities = [
        PetEntity::class,
        VetEntity::class,
        VaccineTypeEntity::class,
        VaccineRecordEntity::class,
        CareTaskEntity::class,
        WeightEntryEntity::class,
        ScheduledNotificationEntity::class,
        PhotoEntity::class,
    ],
    version = 3,
    exportSchema = true,
    autoMigrations = [
        // v2 (M2): cuidados, pesos y avisos programados. Solo agrega tablas.
        AutoMigration(from = 1, to = 2),
        // v3 (M3): fotos. Solo agrega una tabla.
        AutoMigration(from = 2, to = 3),
    ],
)
@ConstructedBy(BestIdDatabaseConstructor::class)
abstract class BestIdDatabase : RoomDatabase() {
    abstract fun petDao(): PetDao
    abstract fun vetDao(): VetDao
    abstract fun vaccineTypeDao(): VaccineTypeDao
    abstract fun vaccineRecordDao(): VaccineRecordDao
    abstract fun careTaskDao(): CareTaskDao
    abstract fun weightEntryDao(): WeightEntryDao
    abstract fun scheduledNotificationDao(): ScheduledNotificationDao
    abstract fun photoDao(): PhotoDao

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
