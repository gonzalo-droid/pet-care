package com.bestid.core.domain.backup

import com.bestid.core.model.CareTask
import com.bestid.core.model.Pet
import com.bestid.core.model.Photo
import com.bestid.core.model.VaccineRecord
import com.bestid.core.model.VaccineType
import com.bestid.core.model.Vet
import com.bestid.core.model.WeightEntry
import kotlin.time.Instant

/**
 * Versión del formato del respaldo (no de la base). Sube solo si cambia `data.json` de forma que
 * una versión anterior de la app no lo pueda leer.
 */
const val BACKUP_FORMAT_VERSION = 1

data class BackupManifest(
    val formatVersion: Int,
    val createdAt: Instant,
    val appVersion: String,
)

/** Todo lo que va en `data.json`. */
data class BackupData(
    val pets: List<Pet> = emptyList(),
    val vets: List<Vet> = emptyList(),
    /** Solo los tipos escritos a mano: el catálogo semilla ya viene en la app. */
    val vaccineTypes: List<VaccineType> = emptyList(),
    val records: List<VaccineRecord> = emptyList(),
    val cares: List<CareTask> = emptyList(),
    val weights: List<WeightEntry> = emptyList(),
    val photos: List<Photo> = emptyList(),
) {
    val isEmpty: Boolean
        get() = pets.isEmpty() && vets.isEmpty() && vaccineTypes.isEmpty() && records.isEmpty() &&
            cares.isEmpty() && weights.isEmpty() && photos.isEmpty()
}

/** Lo que cambió al importar: nuevos o más recientes que los del teléfono, sin contar borrados. */
data class BackupSummary(
    val pets: Int,
    val records: Int,
    val cares: Int,
    val weights: Int,
    val photos: Int,
)

/**
 * Combina por ID (sección 13): entra lo que no existe en el teléfono y lo que tiene un `updatedAt`
 * más reciente. Los tipos escritos a mano y las fotos no se editan: entran solo si faltan.
 */
object BackupMerge {
    fun changes(local: BackupData, incoming: BackupData): BackupData = BackupData(
        pets = newer(local.pets, incoming.pets, { it.id }, { it.updatedAt }),
        vets = newer(local.vets, incoming.vets, { it.id }, { it.updatedAt }),
        vaccineTypes = missing(local.vaccineTypes, incoming.vaccineTypes.filter { it.isCustom }) { it.id },
        records = newer(local.records, incoming.records, { it.id }, { it.updatedAt }),
        cares = newer(local.cares, incoming.cares, { it.id }, { it.updatedAt }),
        weights = newer(local.weights, incoming.weights, { it.id }, { it.updatedAt }),
        photos = missing(local.photos, incoming.photos) { it.id },
    )

    /** Para el mensaje final: cuenta solo lo activo (lo borrado viaja, pero no se anuncia). */
    fun summary(changes: BackupData) = BackupSummary(
        pets = changes.pets.count { it.deletedAt == null },
        records = changes.records.count { it.deletedAt == null },
        cares = changes.cares.count { it.deletedAt == null },
        weights = changes.weights.count { it.deletedAt == null },
        photos = changes.photos.size,
    )

    private fun <T> newer(local: List<T>, incoming: List<T>, id: (T) -> String, updatedAt: (T) -> Instant): List<T> {
        val byId = local.associateBy(id)
        return incoming.filter { item -> byId[id(item)]?.let { updatedAt(item) > updatedAt(it) } ?: true }
    }

    private fun <T> missing(local: List<T>, incoming: List<T>, id: (T) -> String): List<T> {
        val ids = local.map(id).toSet()
        return incoming.filter { id(it) !in ids }
    }
}
