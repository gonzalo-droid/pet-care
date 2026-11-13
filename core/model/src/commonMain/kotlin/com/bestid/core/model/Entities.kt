package com.bestid.core.model

import kotlinx.datetime.LocalDate
import kotlin.time.Instant

/**
 * Entidades del MVP (sección 9 de SYSTEM_DESIGN.md). Los IDs son UUID en texto y toda entidad
 * editable lleva `createdAt`, `updatedAt` y `deletedAt` (borrado lógico) para el respaldo.
 */
data class Pet(
    val id: String,
    val name: String,
    val species: Species,
    val breed: String?,
    val sex: Sex?,
    val birthDate: LocalDate?,
    val birthDatePrecision: DatePrecision?,
    /** Señas particulares, por ejemplo "mancha blanca en la cola". */
    val distinguishingMarks: String?,
    val ownerName: String?,
    val ownerAddress: String?,
    /** Ruta relativa dentro del directorio privado de la app. */
    val photoPath: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val deletedAt: Instant?,
)

/** Veterinaria, se autocompleta en formularios. */
data class Vet(
    val id: String,
    val name: String,
    /** Teléfono o WhatsApp. */
    val contact: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val deletedAt: Instant?,
)

/** Tipo de vacuna: catálogo semilla o escrito a mano. */
data class VaccineType(
    /** "calicivirus" para el catálogo o "custom:<uuid>" para los escritos a mano. */
    val id: String,
    val label: String,
    val species: Set<Species>,
    val isCustom: Boolean,
)

data class VaccineRecord(
    val id: String,
    val petId: String,
    val applicationDate: LocalDate,
    val nextDate: LocalDate?,
    /** Sin confirmar no se programa aviso. */
    val nextDateConfirmed: Boolean,
    /** Por ejemplo "Próx. leucemia". */
    val nextDateLabel: String?,
    val typeIds: List<String>,
    /** Tipos que la persona debe confirmar (sticker con casilla sin marcar). */
    val typeIdsToConfirm: List<String>,
    val items: List<VaccineItem>,
    val vetId: String?,
    val source: RecordSource,
    /** Páginas de la cartilla. */
    val sourcePhotoIds: List<String>,
    val stickerPhotoId: String?,
    val extraction: ExtractionMeta?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val deletedAt: Instant?,
)

/** Un sticker de frasco. */
data class VaccineItem(
    val productName: String?,
    val lot: String?,
    val manufactureDate: LocalDate?,
    val expiryDate: LocalDate?,
)

/** Metadatos de la lectura: confianza por campo y versión del modelo. */
data class ExtractionMeta(
    val modelVersion: String,
    val fieldConfidence: Map<String, Double>,
    /** Cuántos campos corrigió la persona (sin contenido), para medir exactitud. */
    val correctedFields: Int,
)

/**
 * Desparasitación u otro cuidado periódico. El intervalo lo pone el dueño (la app no sugiere).
 * Decisión: `intervalValue` + `intervalUnit` en lugar de `intervalDays` (ver docs/DECISIONS.md).
 */
data class CareTask(
    val id: String,
    val petId: String,
    val kind: CareKind,
    /** Obligatorio solo si [kind] es OTHER. */
    val title: String?,
    val lastDate: LocalDate?,
    val intervalValue: Int?,
    val intervalUnit: IntervalUnit?,
    val nextDate: LocalDate?,
    val reminderEnabled: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
    val deletedAt: Instant?,
)

data class WeightEntry(
    val id: String,
    val petId: String,
    val date: LocalDate,
    /** En gramos para evitar decimales. */
    val grams: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
    val deletedAt: Instant?,
)

data class Photo(
    val id: String,
    /** Ruta relativa dentro del directorio privado de la app. */
    val path: String,
    val width: Int,
    val height: Int,
    val createdAt: Instant,
)

/** Cola local de fotos pendientes de leer. */
data class ScanJob(
    val id: String,
    val petId: String?,
    val photoIds: List<String>,
    val status: ScanStatus,
    val error: String?,
    val createdAt: Instant,
)

/** Lo programado, para comparar y reprogramar solo la diferencia. */
data class ScheduledNotification(
    val id: String,
    val refType: String,
    val refId: String,
    val fireAt: Instant,
)
