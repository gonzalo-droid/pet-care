package com.bestid.core.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// Fechas como texto ISO (LocalDate) e instantes como milisegundos (Instant): ver Converters.
// Las listas se guardan como JSON (sección 9).

@Entity(tableName = "pets")
data class PetEntity(
    @PrimaryKey val id: String,
    val name: String,
    val species: String,
    val breed: String?,
    val sex: String?,
    val birthDate: String?,
    val birthDatePrecision: String?,
    val distinguishingMarks: String?,
    val ownerName: String?,
    val ownerAddress: String?,
    val photoPath: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long?,
)

@Entity(tableName = "vets")
data class VetEntity(
    @PrimaryKey val id: String,
    val name: String,
    val contact: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long?,
)

@Entity(tableName = "vaccine_types")
data class VaccineTypeEntity(
    @PrimaryKey val id: String,
    val label: String,
    /** JSON: lista de nombres de Species. */
    val species: String,
    val isCustom: Boolean,
)

@Entity(
    tableName = "vaccine_records",
    indices = [Index("petId")],
)
data class VaccineRecordEntity(
    @PrimaryKey val id: String,
    val petId: String,
    val applicationDate: String,
    val nextDate: String?,
    val nextDateConfirmed: Boolean,
    val nextDateLabel: String?,
    /** JSON */
    val typeIds: String,
    /** JSON */
    val typeIdsToConfirm: String,
    /** JSON: lista de VaccineItemJson */
    val items: String,
    val vetId: String?,
    val source: String,
    /** JSON */
    val sourcePhotoIds: String,
    val stickerPhotoId: String?,
    /** JSON: ExtractionJson o null */
    val extraction: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long?,
)

@Entity(tableName = "care_tasks", indices = [Index("petId")])
data class CareTaskEntity(
    @PrimaryKey val id: String,
    val petId: String,
    val kind: String,
    val title: String?,
    val lastDate: String?,
    val intervalValue: Int?,
    val intervalUnit: String?,
    val nextDate: String?,
    val reminderEnabled: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long?,
)

@Entity(tableName = "weight_entries", indices = [Index("petId")])
data class WeightEntryEntity(
    @PrimaryKey val id: String,
    val petId: String,
    val date: String,
    val grams: Int,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long?,
)

/** Avisos programados en el sistema operativo (no van al respaldo: se recalculan). */
@Entity(tableName = "scheduled_notifications")
data class ScheduledNotificationEntity(
    @PrimaryKey val id: String,
    val refType: String,
    val refId: String,
    val fireAt: Long,
)

/**
 * Fotos de la cartilla, del sticker y de la mascota (v3, M3). El archivo vive en el directorio
 * privado de la app y aquí va solo la ruta relativa. Una foto no se edita: se crea o se borra.
 */
@Entity(tableName = "photos")
data class PhotoEntity(
    @PrimaryKey val id: String,
    val path: String,
    val width: Int,
    val height: Int,
    val createdAt: Long,
)
