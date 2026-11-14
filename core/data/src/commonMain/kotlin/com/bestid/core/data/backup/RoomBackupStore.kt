package com.bestid.core.data.backup

import com.bestid.core.data.db.BestIdDatabase
import com.bestid.core.data.db.toEntity
import com.bestid.core.data.db.toModel
import com.bestid.core.domain.backup.BackupData
import com.bestid.core.domain.port.BackupStore

class RoomBackupStore(db: BestIdDatabase) : BackupStore {
    private val dao = db.backupDao()

    override suspend fun snapshot() = BackupData(
        pets = dao.pets().map { it.toModel() },
        vets = dao.vets().map { it.toModel() },
        vaccineTypes = dao.customTypes().map { it.toModel() },
        records = dao.records().map { it.toModel() },
        cares = dao.cares().map { it.toModel() },
        weights = dao.weights().map { it.toModel() },
        photos = dao.photos().map { it.toModel() },
    )

    override suspend fun apply(changes: BackupData) = dao.apply(
        pets = changes.pets.map { it.toEntity() },
        vets = changes.vets.map { it.toEntity() },
        types = changes.vaccineTypes.map { it.toEntity() },
        records = changes.records.map { it.toEntity() },
        cares = changes.cares.map { it.toEntity() },
        weights = changes.weights.map { it.toEntity() },
        photos = changes.photos.map { it.toEntity() },
    )

    override suspend fun clearAll() = dao.clearAll()
}
