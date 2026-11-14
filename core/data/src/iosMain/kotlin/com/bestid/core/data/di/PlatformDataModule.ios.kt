package com.bestid.core.data.di

import androidx.room.Room
import androidx.room.RoomDatabase
import com.bestid.core.data.db.BestIdDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import com.bestid.core.data.repository.OkioPhotoFileStore
import com.bestid.core.domain.port.PhotoFileStore
import com.bestid.core.data.backup.OkioBackupArchive
import com.bestid.core.data.repository.OkioTempFileStore
import com.bestid.core.domain.port.BackupArchive
import com.bestid.core.domain.port.TempFileStore
import okio.FileSystem
import org.koin.dsl.module
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

/** En iOS los datos van en Application Support (sección 9), que entra en el respaldo del dispositivo. */
@OptIn(ExperimentalForeignApi::class)
private fun appSupportDirectory(): String {
    val url: NSURL? = NSFileManager.defaultManager.URLForDirectory(
        directory = NSApplicationSupportDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null,
    )
    return requireNotNull(url?.path)
}

/** Caché de la app: el sistema puede vaciarla, sirve para archivos que se comparten y se olvidan. */
@OptIn(ExperimentalForeignApi::class)
private fun cachesDirectory(): String {
    val url: NSURL? = NSFileManager.defaultManager.URLForDirectory(
        directory = NSCachesDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null,
    )
    return requireNotNull(url?.path)
}

internal actual fun platformDataModule() = module {
    single<RoomDatabase.Builder<BestIdDatabase>> {
        Room.databaseBuilder<BestIdDatabase>(name = appSupportDirectory() + "/" + BestIdDatabase.FILE_NAME)
    }
    single { createPreferences(appSupportDirectory() + "/" + PREFERENCES_FILE) }
    single<PhotoFileStore> { OkioPhotoFileStore(appSupportDirectory(), FileSystem.SYSTEM) }
    single<TempFileStore> { OkioTempFileStore(cachesDirectory(), FileSystem.SYSTEM) }
    single<BackupArchive> { OkioBackupArchive(FileSystem.SYSTEM, get()) }
}
