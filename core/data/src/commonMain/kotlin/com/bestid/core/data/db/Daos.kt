package com.bestid.core.data.db

import androidx.room.Dao
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
    @Query("SELECT * FROM vaccine_records WHERE petId = :petId AND deletedAt IS NULL ORDER BY applicationDate DESC, createdAt DESC")
    fun observeForPet(petId: String): Flow<List<VaccineRecordEntity>>

    @Query("SELECT * FROM vaccine_records WHERE id = :id AND deletedAt IS NULL")
    fun observe(id: String): Flow<VaccineRecordEntity?>

    @Query("SELECT * FROM vaccine_records WHERE id = :id")
    suspend fun get(id: String): VaccineRecordEntity?

    @Upsert
    suspend fun upsert(record: VaccineRecordEntity)
}
