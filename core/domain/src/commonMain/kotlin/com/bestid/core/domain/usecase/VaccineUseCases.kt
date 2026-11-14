package com.bestid.core.domain.usecase

import com.bestid.core.domain.port.AppClock
import com.bestid.core.domain.port.IdGenerator
import com.bestid.core.domain.port.VaccineRecordRepository
import com.bestid.core.domain.port.VaccineTypeRepository
import com.bestid.core.domain.port.VetRepository
import com.bestid.core.domain.validation.VaccineValidation
import com.bestid.core.domain.validation.VaccineValidationResult
import com.bestid.core.domain.validation.VaccineWarning
import com.bestid.core.model.RecordSource
import com.bestid.core.model.Species
import com.bestid.core.model.VaccineItem
import com.bestid.core.model.VaccineRecord
import com.bestid.core.model.VaccineType
import com.bestid.core.model.Vet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate

/** Datos del formulario de vacuna a mano (C3). `id == null` crea un registro nuevo. */
data class VaccineDraft(
    val id: String? = null,
    val petId: String,
    val petSpecies: Species,
    val applicationDate: LocalDate?,
    val nextDate: LocalDate? = null,
    val nextDateLabel: String? = null,
    /** Tipos del catálogo o ya creados. */
    val typeIds: Set<String> = emptySet(),
    /** Tipos escritos a mano ("Otra"). Se reutiliza uno existente con la misma etiqueta. */
    val customTypeLabels: List<String> = emptyList(),
    val items: List<VaccineItem> = emptyList(),
    val vetName: String? = null,
    val vetContact: String? = null,
    /** Páginas de la cartilla (fotos ya importadas), en orden. */
    val sourcePhotoIds: List<String> = emptyList(),
    /** Foto del sticker del frasco. */
    val stickerPhotoId: String? = null,
)

sealed interface SaveVaccineResult {
    data class Saved(val record: VaccineRecord, val warnings: List<VaccineWarning>) : SaveVaccineResult
    data class Invalid(val validation: VaccineValidationResult) : SaveVaccineResult
}

class ObserveVaccineRecords(private val records: VaccineRecordRepository) {
    operator fun invoke(petId: String): Flow<List<VaccineRecord>> = records.observeForPet(petId)
}

class ObserveVaccineRecord(private val records: VaccineRecordRepository) {
    operator fun invoke(id: String): Flow<VaccineRecord?> = records.observe(id)
}

class ObserveVaccineTypes(private val types: VaccineTypeRepository) {
    operator fun invoke(): Flow<List<VaccineType>> = types.observeAll()
}

class ObserveVets(private val vets: VetRepository) {
    operator fun invoke(): Flow<List<Vet>> = vets.observeAll()
}

class GetVet(private val vets: VetRepository) {
    suspend operator fun invoke(id: String): Vet? = vets.get(id)
}

/**
 * Guarda una vacuna registrada a mano: valida, resuelve los tipos escritos a mano y la
 * veterinaria (reutiliza la existente por nombre), y conserva lo que venía de un escaneo al editar. Las fotos
 * llegan ya importadas ([ImportPhotos]); quitar una solo la desvincula y [CleanUpPhotos] borra el archivo.
 */
class SaveVaccineRecord(
    private val records: VaccineRecordRepository,
    private val types: VaccineTypeRepository,
    private val vets: VetRepository,
    private val clock: AppClock,
    private val ids: IdGenerator,
) {
    suspend operator fun invoke(draft: VaccineDraft): SaveVaccineResult {
        val customLabels = draft.customTypeLabels.mapNotNull { it.cleaned() }.distinctBy { it.normalized() }
        val items = draft.items.filterNot { it.isEmpty() }
        val validation = VaccineValidation.validate(
            hasType = draft.typeIds.isNotEmpty() || customLabels.isNotEmpty(),
            applicationDate = draft.applicationDate,
            nextDate = draft.nextDate,
            items = items,
            today = clock.today(),
        )
        if (!validation.isValid) return SaveVaccineResult.Invalid(validation)

        val now = clock.now()
        val typeIds = draft.typeIds.toList() + resolveCustomTypes(customLabels, draft.petSpecies)
        val existing = draft.id?.let { records.get(it) }
        val record = VaccineRecord(
            id = existing?.id ?: ids.newId(),
            petId = draft.petId,
            applicationDate = requireNotNull(draft.applicationDate),
            nextDate = draft.nextDate,
            nextDateConfirmed = draft.nextDate != null,
            nextDateLabel = draft.nextDateLabel.cleaned(),
            typeIds = typeIds.distinct(),
            typeIdsToConfirm = existing?.typeIdsToConfirm.orEmpty() - typeIds.toSet(),
            items = items,
            vetId = resolveVet(draft.vetName, draft.vetContact),
            source = existing?.source ?: RecordSource.MANUAL,
            sourcePhotoIds = draft.sourcePhotoIds.distinct(),
            stickerPhotoId = draft.stickerPhotoId,
            extraction = existing?.extraction,
            createdAt = existing?.createdAt ?: now,
            updatedAt = now,
            deletedAt = null,
        )
        records.upsert(record)
        return SaveVaccineResult.Saved(record, validation.warnings)
    }

    private suspend fun resolveCustomTypes(labels: List<String>, species: Species): List<String> {
        if (labels.isEmpty()) return emptyList()
        val known = types.getAll()
        return labels.map { label ->
            known.firstOrNull { it.label.normalized() == label.normalized() }?.id
                ?: VaccineType(
                    id = "custom:${ids.newId()}",
                    label = label,
                    species = setOf(species),
                    isCustom = true,
                ).also { types.insert(it) }.id
        }
    }

    private suspend fun resolveVet(name: String?, contact: String?): String? {
        val cleanName = name.cleaned() ?: return null
        val cleanContact = contact.cleaned()
        val now = clock.now()
        val existing = vets.observeAll().first().firstOrNull { it.name.normalized() == cleanName.normalized() }
        return when {
            existing == null -> Vet(ids.newId(), cleanName, cleanContact, now, now, null).also { vets.upsert(it) }.id
            cleanContact != null && cleanContact != existing.contact -> {
                vets.upsert(existing.copy(contact = cleanContact, updatedAt = now))
                existing.id
            }
            else -> existing.id
        }
    }
}

/** Borrado lógico: se puede deshacer con [RestoreVaccineRecord]. */
class DeleteVaccineRecord(private val records: VaccineRecordRepository, private val clock: AppClock) {
    suspend operator fun invoke(id: String) {
        val record = records.get(id) ?: return
        val now = clock.now()
        records.upsert(record.copy(deletedAt = now, updatedAt = now))
    }
}

class RestoreVaccineRecord(private val records: VaccineRecordRepository, private val clock: AppClock) {
    suspend operator fun invoke(id: String) {
        val record = records.get(id) ?: return
        records.upsert(record.copy(deletedAt = null, updatedAt = clock.now()))
    }
}

private fun VaccineItem.isEmpty() =
    productName.cleaned() == null && lot.cleaned() == null && manufactureDate == null && expiryDate == null

/** Compara etiquetas sin mayúsculas, tildes ni espacios extra. */
internal fun String.normalized(): String = trim().lowercase()
    .map { c ->
        when (c) {
            'á', 'à', 'ä' -> 'a'
            'é', 'è', 'ë' -> 'e'
            'í', 'ì', 'ï' -> 'i'
            'ó', 'ò', 'ö' -> 'o'
            'ú', 'ù', 'ü' -> 'u'
            else -> c
        }
    }
    .joinToString("")
    .replace(Regex("\\s+"), " ")
