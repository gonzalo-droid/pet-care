package com.bestid.core.data.di

import androidx.room.Room
import androidx.room.RoomDatabase
import com.bestid.core.data.db.BestIdDatabase
import com.bestid.core.data.repository.OkioPhotoFileStore
import com.bestid.core.domain.port.PhotoFileStore
import com.bestid.core.data.backup.OkioBackupArchive
import com.bestid.core.data.repository.OkioTempFileStore
import com.bestid.core.domain.port.BackupArchive
import com.bestid.core.domain.port.TempFileStore
import okio.FileSystem
import org.koin.dsl.module
import java.io.File

/** Solo para pruebas en la JVM. */
internal actual fun platformDataModule() = module {
    val dir = File(System.getProperty("java.io.tmpdir"), "bestid").apply { mkdirs() }
    single<RoomDatabase.Builder<BestIdDatabase>> {
        Room.databaseBuilder<BestIdDatabase>(name = File(dir, BestIdDatabase.FILE_NAME).absolutePath)
    }
    single { createPreferences(File(dir, PREFERENCES_FILE).absolutePath) }
    single<PhotoFileStore> { OkioPhotoFileStore(dir.absolutePath, FileSystem.SYSTEM) }
    single<TempFileStore> { OkioTempFileStore(File(dir, "cache").absolutePath, FileSystem.SYSTEM) }
    single<BackupArchive> { OkioBackupArchive(FileSystem.SYSTEM, get()) }
}
