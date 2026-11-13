package com.bestid.core.domain.usecase

import com.bestid.core.domain.port.AppClock
import com.bestid.core.domain.port.IdGenerator
import com.bestid.core.domain.port.PetRepository
import com.bestid.core.domain.port.SelectedPetStore
import com.bestid.core.domain.validation.PetError
import com.bestid.core.domain.validation.PetValidation
import com.bestid.core.model.DatePrecision
import com.bestid.core.model.Pet
import com.bestid.core.model.Sex
import com.bestid.core.model.Species
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate

/** Datos del formulario de mascota (M2). `id == null` crea una nueva. */
data class PetDraft(
    val id: String? = null,
    val name: String,
    val species: Species,
    val breed: String? = null,
    val sex: Sex? = null,
    val birthDate: LocalDate? = null,
    val birthDatePrecision: DatePrecision? = null,
    val distinguishingMarks: String? = null,
    val ownerName: String? = null,
    val ownerAddress: String? = null,
    /** Ruta relativa de la foto ya importada ([ImportPhotos]); `null` = sin foto. */
    val photoPath: String? = null,
)

sealed interface SavePetResult {
    data class Saved(val pet: Pet) : SavePetResult
    data class Invalid(val error: PetError) : SavePetResult
}

class ObservePets(private val pets: PetRepository) {
    operator fun invoke(): Flow<List<Pet>> = pets.observePets()
}

class ObservePet(private val pets: PetRepository) {
    operator fun invoke(id: String): Flow<Pet?> = pets.observePet(id)
}

/** La mascota seleccionada; si la guardada ya no existe, la primera activa. */
class ObserveSelectedPet(private val pets: PetRepository, private val store: SelectedPetStore) {
    operator fun invoke(): Flow<Pet?> =
        combine(pets.observePets(), store.selectedPetId) { list, id ->
            list.firstOrNull { it.id == id } ?: list.firstOrNull()
        }
}

class SelectPet(private val store: SelectedPetStore) {
    suspend operator fun invoke(petId: String) = store.select(petId)
}

/** Crea o edita una mascota. Una mascota nueva queda seleccionada. */
class SavePet(
    private val pets: PetRepository,
    private val store: SelectedPetStore,
    private val clock: AppClock,
    private val ids: IdGenerator,
) {
    suspend operator fun invoke(draft: PetDraft): SavePetResult {
        PetValidation.validateName(draft.name)?.let { return SavePetResult.Invalid(it) }
        val now = clock.now()
        val existing = draft.id?.let { pets.get(it) }
        val pet = Pet(
            id = existing?.id ?: ids.newId(),
            name = draft.name.trim(),
            species = draft.species,
            breed = draft.breed.cleaned(),
            sex = draft.sex,
            birthDate = draft.birthDate,
            birthDatePrecision = draft.birthDate?.let { draft.birthDatePrecision ?: DatePrecision.EXACT },
            distinguishingMarks = draft.distinguishingMarks.cleaned(),
            ownerName = draft.ownerName.cleaned(),
            ownerAddress = draft.ownerAddress.cleaned(),
            photoPath = draft.photoPath,
            createdAt = existing?.createdAt ?: now,
            updatedAt = now,
            deletedAt = null,
        )
        pets.upsert(pet)
        if (existing == null) store.select(pet.id)
        return SavePetResult.Saved(pet)
    }
}

/** Borrado lógico: se puede deshacer con [RestorePet]. */
class DeletePet(
    private val pets: PetRepository,
    private val store: SelectedPetStore,
    private val clock: AppClock,
) {
    suspend operator fun invoke(petId: String) {
        val pet = pets.get(petId) ?: return
        val now = clock.now()
        pets.upsert(pet.copy(deletedAt = now, updatedAt = now))
        if (store.selectedPetId.first() == petId) store.select(null)
    }
}

class RestorePet(
    private val pets: PetRepository,
    private val store: SelectedPetStore,
    private val clock: AppClock,
) {
    suspend operator fun invoke(petId: String) {
        val pet = pets.get(petId) ?: return
        pets.upsert(pet.copy(deletedAt = null, updatedAt = clock.now()))
        store.select(petId)
    }
}

internal fun String?.cleaned(): String? = this?.trim()?.takeIf { it.isNotEmpty() }
