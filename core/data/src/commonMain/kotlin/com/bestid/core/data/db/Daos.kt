package com.bestid.core.data.db

import androidx.room.Dao
import androidx.room.Transaction
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface PetDao {
    @Query("SELECT * FROM pets WHERE deletedAt IS NULL ORDER BY createdAt")
    fun observeActive(): Flow<List<PetEntity>>

    @Query("SELECT * FROM pets WHERE id = :id AND deletedAt IS NULL")
    fun observe(id: String): Flow<PetEntity?>

    @Query("SELECT * FROM pets WHERE id = :id")
    suspend fun get(id: String): PetEntity?

    @Query("SELECT * FROM pets")
    suspend fun getAll(): List<PetEntity>

    @Upsert
    suspend fun upsert(pet: PetEntity)
}

@Dao
interface VetDao {
    @Query("SELECT * FROM vets WHERE deletedAt IS NULL ORDER BY name")
    fun observeActive(): Flow<List<VetEntity>>

    @Query("SELECT * FROM vets WHERE id = :id")
    suspend fun get(id: String): VetEntity?

    @Upsert
    suspend fun upsert(vet: VetEntity)
}

@Dao
interface VaccineTypeDao {
    @Query("SELECT * FROM vaccine_types ORDER BY isCustom, label")
    fun observeAll(): Flow<List<VaccineTypeEntity>>

    @Query("SELECT * FROM vaccine_types ORDER BY isCustom, label")
    suspend fun getAll(): List<VaccineTypeEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(types: List<VaccineTypeEntity>)
}

@Dao
interface VaccineRecordDao {
    @Query("SELECT * FROM vaccine_records WHERE deletedAt IS NULL")
    fun observeAll(): Flow<List<VaccineRecordEntity>>

    @Query("SELECT * FROM vaccine_records WHERE petId = :petId AND deletedAt IS NULL ORDER BY applicationDate DESC, createdAt DESC")
    fun observeForPet(petId: String): Flow<List<VaccineRecordEntity>>

    @Query("SELECT * FROM vaccine_records WHERE id = :id AND deletedAt IS NULL")
    fun observe(id: String): Flow<VaccineRecordEntity?>

    @Query("SELECT * FROM vaccine_records WHERE id = :id")
    suspend fun get(id: String): VaccineRecordEntity?

    @Query("SELECT * FROM vaccine_records")
    suspend fun getAll(): List<VaccineRecordEntity>

    @Upsert
    suspend fun upsert(record: VaccineRecordEntity)
}

@Dao
interface CareTaskDao {
    @Query("SELECT * FROM care_tasks WHERE petId = :petId AND deletedAt IS NULL ORDER BY nextDate IS NULL, nextDate, createdAt")
    fun observeForPet(petId: String): Flow<List<CareTaskEntity>>

    @Query("SELECT * FROM care_tasks WHERE deletedAt IS NULL")
    fun observeAll(): Flow<List<CareTaskEntity>>

    @Query("SELECT * FROM care_tasks WHERE id = :id AND deletedAt IS NULL")
    fun observe(id: String): Flow<CareTaskEntity?>

    @Query("SELECT * FROM care_tasks WHERE id = :id")
    suspend fun get(id: String): CareTaskEntity?

    @Upsert
    suspend fun upsert(task: CareTaskEntity)
}

@Dao
interface WeightEntryDao {
    @Query("SELECT * FROM weight_entries WHERE petId = :petId AND deletedAt IS NULL ORDER BY date, createdAt")
    fun observeForPet(petId: String): Flow<List<WeightEntryEntity>>

    @Query("SELECT * FROM weight_entries WHERE id = :id")
    suspend fun get(id: String): WeightEntryEntity?

    @Upsert
    suspend fun upsert(entry: WeightEntryEntity)
}

@Dao
interface ScheduledNotificationDao {
    @Query("SELECT * FROM scheduled_notifications")
    suspend fun getAll(): List<ScheduledNotificationEntity>

    @Upsert
    suspend fun upsertAll(items: List<ScheduledNotificationEntity>)

    @Query("DELETE FROM scheduled_notifications WHERE id IN (:ids)")
    suspend fun deleteIds(ids: List<String>)

    @Query("DELETE FROM scheduled_notifications")
    suspend fun clear()

    @Transaction
    suspend fun replace(added: List<ScheduledNotificationEntity>, removedIds: List<String>) {
        if (removedIds.isNotEmpty()) deleteIds(removedIds)
        if (added.isNotEmpty()) upsertAll(added)
    }
}

@Dao
interface PhotoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(photo: PhotoEntity)

    @Query("SELECT * FROM photos WHERE id IN (:ids)")
    suspend fun get(ids: List<String>): List<PhotoEntity>

    @Query("SELECT * FROM photos")
    suspend fun getAll(): List<PhotoEntity>

    @Query("DELETE FROM photos WHERE id IN (:ids)")
    suspend fun delete(ids: List<String>)
}
