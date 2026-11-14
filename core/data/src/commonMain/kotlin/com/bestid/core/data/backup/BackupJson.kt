package com.bestid.core.data.backup

import com.bestid.core.domain.backup.BackupData
import com.bestid.core.domain.backup.BackupManifest
import com.bestid.core.model.CareKind
import com.bestid.core.model.CareTask
import com.bestid.core.model.DatePrecision
import com.bestid.core.model.ExtractionMeta
import com.bestid.core.model.IntervalUnit
import com.bestid.core.model.Pet
import com.bestid.core.model.Photo
import com.bestid.core.model.RecordSource
import com.bestid.core.model.Sex
import com.bestid.core.model.Species
import com.bestid.core.model.VaccineItem
import com.bestid.core.model.VaccineRecord
import com.bestid.core.model.VaccineType
import com.bestid.core.model.Vet
import com.bestid.core.model.WeightEntry
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.time.Instant

/*
 * Formato de `manifest.json` y `data.json` (sección 13). Es propio del respaldo, no de la base:
 * fechas en ISO (aaaa-mm-dd), instantes en milisegundos y enums por nombre. Un campo nuevo se
 * agrega con valor por defecto para que los respaldos viejos se sigan leyendo.
 */

internal val backupJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    prettyPrint = false
}

@Serializable
internal data class ManifestJson(val formatVersion: Int, val createdAt: Long, val appVersion: String, val app: String = "BestID")

@Serializable
internal data class DataJson(
    val pets: List<PetJson> = emptyList(),
    val vets: List<VetJson> = emptyList(),
    val vaccineTypes: List<VaccineTypeJson> = emptyList(),
    val records: List<RecordJson> = emptyList(),
    val cares: List<CareJson> = emptyList(),
    val weights: List<WeightJson> = emptyList(),
    val photos: List<PhotoJson> = emptyList(),
)

@Serializable
internal data class PetJson(
    val id: String, val name: String, val species: String, val breed: String? = null, val sex: String? = null,
    val birthDate: String? = null, val birthDatePrecision: String? = null, val distinguishingMarks: String? = null,
    val ownerName: String? = null, val ownerAddress: String? = null, val photoPath: String? = null,
    val createdAt: Long, val updatedAt: Long, val deletedAt: Long? = null,
)

@Serializable
internal data class VetJson(val id: String, val name: String, val contact: String? = null, val createdAt: Long, val updatedAt: Long, val deletedAt: Long? = null)

@Serializable
internal data class VaccineTypeJson(val id: String, val label: String, val species: List<String>, val isCustom: Boolean)

@Serializable
internal data class ItemJson(val productName: String? = null, val lot: String? = null, val manufactureDate: String? = null, val expiryDate: String? = null)

@Serializable
internal data class ExtractionJson(val modelVersion: String, val fieldConfidence: Map<String, Double> = emptyMap(), val correctedFields: Int = 0)

@Serializable
internal data class RecordJson(
    val id: String, val petId: String, val applicationDate: String, val nextDate: String? = null,
    val nextDateConfirmed: Boolean = false, val nextDateLabel: String? = null, val typeIds: List<String> = emptyList(),
    val typeIdsToConfirm: List<String> = emptyList(), val items: List<ItemJson> = emptyList(), val vetId: String? = null,
    val source: String, val sourcePhotoIds: List<String> = emptyList(), val stickerPhotoId: String? = null,
    val extraction: ExtractionJson? = null, val createdAt: Long, val updatedAt: Long, val deletedAt: Long? = null,
)

@Serializable
internal data class CareJson(
    val id: String, val petId: String, val kind: String, val title: String? = null, val lastDate: String? = null,
    val intervalValue: Int? = null, val intervalUnit: String? = null, val nextDate: String? = null,
    val reminderEnabled: Boolean = false, val createdAt: Long, val updatedAt: Long, val deletedAt: Long? = null,
)

@Serializable
internal data class WeightJson(val id: String, val petId: String, val date: String, val grams: Int, val createdAt: Long, val updatedAt: Long, val deletedAt: Long? = null)

@Serializable
internal data class PhotoJson(val id: String, val path: String, val width: Int, val height: Int, val createdAt: Long)

private fun Instant.ms() = toEpochMilliseconds()
private fun Long.instant() = Instant.fromEpochMilliseconds(this)
private fun String.date() = LocalDate.parse(this)

internal fun BackupManifest.toJson() = ManifestJson(formatVersion, createdAt.ms(), appVersion)
internal fun ManifestJson.toModel() = BackupManifest(formatVersion, createdAt.instant(), appVersion)

internal fun BackupData.toJson() = DataJson(
    pets = pets.map {
        PetJson(
            it.id, it.name, it.species.name, it.breed, it.sex?.name, it.birthDate?.toString(), it.birthDatePrecision?.name,
            it.distinguishingMarks, it.ownerName, it.ownerAddress, it.photoPath, it.createdAt.ms(), it.updatedAt.ms(), it.deletedAt?.ms(),
        )
    },
    vets = vets.map { VetJson(it.id, it.name, it.contact, it.createdAt.ms(), it.updatedAt.ms(), it.deletedAt?.ms()) },
    vaccineTypes = vaccineTypes.map { VaccineTypeJson(it.id, it.label, it.species.map(Species::name).sorted(), it.isCustom) },
    records = records.map { r ->
        RecordJson(
            r.id, r.petId, r.applicationDate.toString(), r.nextDate?.toString(), r.nextDateConfirmed, r.nextDateLabel,
            r.typeIds, r.typeIdsToConfirm,
            r.items.map { ItemJson(it.productName, it.lot, it.manufactureDate?.toString(), it.expiryDate?.toString()) },
            r.vetId, r.source.name, r.sourcePhotoIds, r.stickerPhotoId,
            r.extraction?.let { ExtractionJson(it.modelVersion, it.fieldConfidence, it.correctedFields) },
            r.createdAt.ms(), r.updatedAt.ms(), r.deletedAt?.ms(),
        )
    },
    cares = cares.map {
        CareJson(
            it.id, it.petId, it.kind.name, it.title, it.lastDate?.toString(), it.intervalValue, it.intervalUnit?.name,
            it.nextDate?.toString(), it.reminderEnabled, it.createdAt.ms(), it.updatedAt.ms(), it.deletedAt?.ms(),
        )
    },
    weights = weights.map { WeightJson(it.id, it.petId, it.date.toString(), it.grams, it.createdAt.ms(), it.updatedAt.ms(), it.deletedAt?.ms()) },
    photos = photos.map { PhotoJson(it.id, it.path, it.width, it.height, it.createdAt.ms()) },
)

/** Lanza una excepción si un valor no se reconoce: el respaldo se trata como inválido. */
internal fun DataJson.toModel() = BackupData(
    pets = pets.map {
        Pet(
            it.id, it.name, Species.valueOf(it.species), it.breed, it.sex?.let(Sex::valueOf), it.birthDate?.date(),
            it.birthDatePrecision?.let(DatePrecision::valueOf), it.distinguishingMarks, it.ownerName, it.ownerAddress,
            it.photoPath, it.createdAt.instant(), it.updatedAt.instant(), it.deletedAt?.instant(),
        )
    },
    vets = vets.map { Vet(it.id, it.name, it.contact, it.createdAt.instant(), it.updatedAt.instant(), it.deletedAt?.instant()) },
    vaccineTypes = vaccineTypes.map { VaccineType(it.id, it.label, it.species.map(Species::valueOf).toSet(), it.isCustom) },
    records = records.map { r ->
        VaccineRecord(
            r.id, r.petId, r.applicationDate.date(), r.nextDate?.date(), r.nextDateConfirmed, r.nextDateLabel,
            r.typeIds, r.typeIdsToConfirm,
            r.items.map { VaccineItem(it.productName, it.lot, it.manufactureDate?.date(), it.expiryDate?.date()) },
            r.vetId, RecordSource.valueOf(r.source), r.sourcePhotoIds, r.stickerPhotoId,
            r.extraction?.let { ExtractionMeta(it.modelVersion, it.fieldConfidence, it.correctedFields) },
            r.createdAt.instant(), r.updatedAt.instant(), r.deletedAt?.instant(),
        )
    },
    cares = cares.map {
        CareTask(
            it.id, it.petId, CareKind.valueOf(it.kind), it.title, it.lastDate?.date(), it.intervalValue,
            it.intervalUnit?.let(IntervalUnit::valueOf), it.nextDate?.date(), it.reminderEnabled,
            it.createdAt.instant(), it.updatedAt.instant(), it.deletedAt?.instant(),
        )
    },
    weights = weights.map { WeightEntry(it.id, it.petId, it.date.date(), it.grams, it.createdAt.instant(), it.updatedAt.instant(), it.deletedAt?.instant()) },
    photos = photos.map { Photo(it.id, it.path, it.width, it.height, it.createdAt.instant()) },
)
