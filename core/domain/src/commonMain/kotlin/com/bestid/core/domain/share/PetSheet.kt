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

/** Qué incluir en la ficha (SH1). Propietario y dirección van solo si se marcan (sección 12). */
data class SheetOptions(
    val includeOwner: Boolean = false,
    val includeWeight: Boolean = false,
    val includeCardPhotos: Boolean = false,
)

/** Una vacuna lista para la tabla: con los nombres de sus tipos y su veterinaria. */
data class SheetVaccine(val record: VaccineRecord, val typeLabels: List<String>, val vet: Vet?)

/** Datos de la ficha en PDF. Sin textos de interfaz: los pone la pantalla (regla 3). */
data class PetSheet(
    /** Sin propietario ni dirección si no se pidieron. */
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
        val vaccines = petRecords.map { record ->
            SheetVaccine(record, record.typeIds.mapNotNull { labels[it] }, record.vetId?.let { vets.get(it) })
        }
        val pageIds = if (options.includeCardPhotos) petRecords.flatMap { it.sourcePhotoIds }.distinct() else emptyList()
        val pages = photos.get(pageIds).associateBy { it.id }
        return PetSheet(
            pet = if (options.includeOwner) pet else pet.copy(ownerName = null, ownerAddress = null),
            vaccines = vaccines,
            nextDue = NextDueCalculator.nextDue(petRecords, today),
            weights = if (options.includeWeight) weights.observeForPet(petId).first() else emptyList(),
            cardPhotos = pageIds.mapNotNull { pages[it] },
            generatedOn = today,
        )
    }
}
