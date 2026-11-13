package com.bestid.core.data.db

import com.bestid.core.model.CareKind
import com.bestid.core.model.CareTask
import com.bestid.core.model.DatePrecision
import com.bestid.core.model.IntervalUnit
import com.bestid.core.model.ScheduledNotification
import com.bestid.core.model.WeightEntry
import com.bestid.core.model.ExtractionMeta
import com.bestid.core.model.Pet
import com.bestid.core.model.RecordSource
import com.bestid.core.model.Sex
import com.bestid.core.model.Species
import com.bestid.core.model.VaccineItem
import com.bestid.core.model.VaccineRecord
import com.bestid.core.model.VaccineType
import com.bestid.core.model.Vet
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlin.time.Instant

private val json = Json { ignoreUnknownKeys = true }
private val stringList = ListSerializer(String.serializer())

@Serializable
private data class VaccineItemJson(
    val productName: String? = null,
    val lot: String? = null,
    val manufactureDate: String? = null,
    val expiryDate: String? = null,
)

@Serializable
private data class ExtractionJson(
    val modelVersion: String,
    val fieldConfidence: Map<String, Double> = emptyMap(),
    val correctedFields: Int = 0,
)

private fun Instant.ms() = toEpochMilliseconds()
private fun Long.instant() = Instant.fromEpochMilliseconds(this)
private fun List<String>.toJson() = json.encodeToString(stringList, this)
private fun String.toStringList() = json.decodeFromString(stringList, this)

internal fun PetEntity.toModel() = Pet(
    id = id,
    name = name,
    species = Species.valueOf(species),
    breed = breed,
    sex = sex?.let(Sex::valueOf),
    birthDate = birthDate?.let(LocalDate::parse),
    birthDatePrecision = birthDatePrecision?.let(DatePrecision::valueOf),
    distinguishingMarks = distinguishingMarks,
    ownerName = ownerName,
    ownerAddress = ownerAddress,
    photoPath = photoPath,
    createdAt = createdAt.instant(),
    updatedAt = updatedAt.instant(),
    deletedAt = deletedAt?.instant(),
)

internal fun Pet.toEntity() = PetEntity(
    id = id,
    name = name,
    species = species.name,
    breed = breed,
    sex = sex?.name,
    birthDate = birthDate?.toString(),
    birthDatePrecision = birthDatePrecision?.name,
    distinguishingMarks = distinguishingMarks,
    ownerName = ownerName,
    ownerAddress = ownerAddress,
    photoPath = photoPath,
    createdAt = createdAt.ms(),
    updatedAt = updatedAt.ms(),
    deletedAt = deletedAt?.ms(),
)

internal fun VetEntity.toModel() = Vet(id, name, contact, createdAt.instant(), updatedAt.instant(), deletedAt?.instant())

internal fun Vet.toEntity() = VetEntity(id, name, contact, createdAt.ms(), updatedAt.ms(), deletedAt?.ms())

internal fun VaccineTypeEntity.toModel() = VaccineType(
    id = id,
    label = label,
    species = species.toStringList().map(Species::valueOf).toSet(),
    isCustom = isCustom,
)

internal fun VaccineType.toEntity() = VaccineTypeEntity(
    id = id,
    label = label,
    species = species.map { it.name }.sorted().toJson(),
    isCustom = isCustom,
)

internal fun VaccineRecordEntity.toModel() = VaccineRecord(
    id = id,
    petId = petId,
    applicationDate = LocalDate.parse(applicationDate),
    nextDate = nextDate?.let(LocalDate::parse),
    nextDateConfirmed = nextDateConfirmed,
    nextDateLabel = nextDateLabel,
    typeIds = typeIds.toStringList(),
    typeIdsToConfirm = typeIdsToConfirm.toStringList(),
    items = json.decodeFromString(ListSerializer(VaccineItemJson.serializer()), items).map {
        VaccineItem(it.productName, it.lot, it.manufactureDate?.let(LocalDate::parse), it.expiryDate?.let(LocalDate::parse))
    },
    vetId = vetId,
    source = RecordSource.valueOf(source),
    sourcePhotoIds = sourcePhotoIds.toStringList(),
    stickerPhotoId = stickerPhotoId,
    extraction = extraction?.let { json.decodeFromString(ExtractionJson.serializer(), it) }
        ?.let { ExtractionMeta(it.modelVersion, it.fieldConfidence, it.correctedFields) },
    createdAt = createdAt.instant(),
    updatedAt = updatedAt.instant(),
    deletedAt = deletedAt?.instant(),
)

internal fun VaccineRecord.toEntity() = VaccineRecordEntity(
    id = id,
    petId = petId,
    applicationDate = applicationDate.toString(),
    nextDate = nextDate?.toString(),
    nextDateConfirmed = nextDateConfirmed,
    nextDateLabel = nextDateLabel,
    typeIds = typeIds.toJson(),
    typeIdsToConfirm = typeIdsToConfirm.toJson(),
    items = json.encodeToString(
        ListSerializer(VaccineItemJson.serializer()),
        items.map { VaccineItemJson(it.productName, it.lot, it.manufactureDate?.toString(), it.expiryDate?.toString()) },
    ),
    vetId = vetId,
    source = source.name,
    sourcePhotoIds = sourcePhotoIds.toJson(),
    stickerPhotoId = stickerPhotoId,
    extraction = extraction?.let {
        json.encodeToString(ExtractionJson.serializer(), ExtractionJson(it.modelVersion, it.fieldConfidence, it.correctedFields))
    },
    createdAt = createdAt.ms(),
    updatedAt = updatedAt.ms(),
    deletedAt = deletedAt?.ms(),
)

internal fun CareTaskEntity.toModel() = CareTask(
    id = id,
    petId = petId,
    kind = CareKind.valueOf(kind),
    title = title,
    lastDate = lastDate?.let(LocalDate::parse),
    intervalValue = intervalValue,
    intervalUnit = intervalUnit?.let(IntervalUnit::valueOf),
    nextDate = nextDate?.let(LocalDate::parse),
    reminderEnabled = reminderEnabled,
    createdAt = createdAt.instant(),
    updatedAt = updatedAt.instant(),
    deletedAt = deletedAt?.instant(),
)

internal fun CareTask.toEntity() = CareTaskEntity(
    id = id,
    petId = petId,
    kind = kind.name,
    title = title,
    lastDate = lastDate?.toString(),
    intervalValue = intervalValue,
    intervalUnit = intervalUnit?.name,
    nextDate = nextDate?.toString(),
    reminderEnabled = reminderEnabled,
    createdAt = createdAt.ms(),
    updatedAt = updatedAt.ms(),
    deletedAt = deletedAt?.ms(),
)

internal fun WeightEntryEntity.toModel() =
    WeightEntry(id, petId, LocalDate.parse(date), grams, createdAt.instant(), updatedAt.instant(), deletedAt?.instant())

internal fun WeightEntry.toEntity() =
    WeightEntryEntity(id, petId, date.toString(), grams, createdAt.ms(), updatedAt.ms(), deletedAt?.ms())

internal fun ScheduledNotificationEntity.toModel() = ScheduledNotification(id, refType, refId, fireAt.instant())

internal fun ScheduledNotification.toEntity() = ScheduledNotificationEntity(id, refType, refId, fireAt.ms())
