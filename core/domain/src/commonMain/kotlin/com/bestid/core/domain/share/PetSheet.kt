package com.bestid.core.domain.share

import com.bestid.core.domain.port.AppClock
import com.bestid.core.domain.port.PetRepository
import com.bestid.core.domain.port.PhotoRepository
import com.bestid.core.domain.port.VaccineRecordRepository
import com.bestid.core.domain.port.VaccineTypeRepository
import com.bestid.core.domain.port.VetRepository
import com.bestid.core.domain.port.WeightRepository
import com.bestid.core.domain.schedule.NextDue
import com.bestid.core.domain.schedule.NextDueCalculator
import com.bestid.core.model.Pet
import com.bestid.core.model.Photo
import com.bestid.core.model.VaccineRecord
import com.bestid.core.model.Vet
import com.bestid.core.model.WeightEntry
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate

/**
 * Qué incluir en la ficha (SH1), como en el prototipo: vacunas y fotos de la cartilla marcadas,
 * peso y nombre del tutor sin marcar. La dirección nunca va en la ficha (D34).
 */
data class SheetOptions(
    val includeVaccines: Boolean = true,
    val includeCardPhotos: Boolean = true,
    val includeWeight: Boolean = false,
    val includeOwnerName: Boolean = false,
)

/** Una vacuna lista para la tabla: con los nombres de sus tipos y su veterinaria. */
data class SheetVaccine(val record: VaccineRecord, val typeLabels: List<String>, val vet: Vet?)

/** Datos de la ficha en PDF. Sin textos de interfaz: los pone la pantalla (regla 3). */
data class PetSheet(
    /** Nunca con dirección; con el nombre del tutor solo si se pidió. */
    val pet: Pet,
    /** De la más reciente a la más antigua. */
    val vaccines: List<SheetVaccine>,
    val nextDue: NextDue?,
    val weights: List<WeightEntry>,
    /** Páginas de la cartilla, sin repetir, en el orden de las vacunas. */
    val cardPhotos: List<Photo>,
    val generatedOn: LocalDate,
)

/** Arma la ficha de una mascota con lo activo (sin borrados) y según [SheetOptions]. */
class BuildPetSheet(
    private val pets: PetRepository,
    private val records: VaccineRecordRepository,
    private val types: VaccineTypeRepository,
    private val vets: VetRepository,
    private val weights: WeightRepository,
    private val photos: PhotoRepository,
    private val clock: AppClock,
) {
    suspend operator fun invoke(petId: String, options: SheetOptions): PetSheet? {
        val pet = pets.observePet(petId).first() ?: return null
        val petRecords = records.observeForPet(petId).first()
        val labels = types.getAll().associate { it.id to it.label }
        val today = clock.today()
        val vaccines = if (!options.includeVaccines) emptyList() else petRecords.map { record ->
            SheetVaccine(record, record.typeIds.mapNotNull { labels[it] }, record.vetId?.let { vets.get(it) })
        }
        val pageIds = if (options.includeCardPhotos) petRecords.flatMap { it.sourcePhotoIds }.distinct() else emptyList()
        val pages = photos.get(pageIds).associateBy { it.id }
        return PetSheet(
            pet = pet.copy(ownerName = pet.ownerName.takeIf { options.includeOwnerName }, ownerAddress = null),
            vaccines = vaccines,
            nextDue = NextDueCalculator.nextDue(petRecords, today),
            weights = if (options.includeWeight) weights.observeForPet(petId).first() else emptyList(),
            cardPhotos = pageIds.mapNotNull { pages[it] },
            generatedOn = today,
        )
    }
}
