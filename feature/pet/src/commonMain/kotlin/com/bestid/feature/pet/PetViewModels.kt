package com.bestid.feature.pet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bestid.core.domain.port.AppClock
import com.bestid.core.domain.schedule.NextDue
import com.bestid.core.domain.schedule.NextDueCalculator
import com.bestid.core.domain.usecase.DeletePet
import com.bestid.core.domain.usecase.ImportPhotos
import com.bestid.core.platform.image.ImagePickerResult
import com.bestid.core.domain.usecase.ObservePet
import com.bestid.core.domain.usecase.ObserveSelectedPet
import com.bestid.core.domain.usecase.ObserveVaccineRecords
import com.bestid.core.domain.usecase.PetDraft
import com.bestid.core.domain.usecase.SavePet
import com.bestid.core.domain.usecase.SavePetResult
import com.bestid.core.domain.validation.PetError
import com.bestid.core.domain.validation.PetValidation
import com.bestid.core.model.DatePrecision
import com.bestid.core.model.Pet
import com.bestid.core.model.Sex
import com.bestid.core.model.Species
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import com.bestid.core.ui.Retry
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.bestid.core.domain.usecase.ImportedPhotos
import com.bestid.core.ui.attempt
import kotlinx.datetime.LocalDate

data class PetTabState(
    val loading: Boolean = true,
    /** No se pudieron leer los datos: se muestra el error con "Reintentar". */
    val failed: Boolean = false,
    val pet: Pet? = null,
    val vaccineCount: Int = 0,
    val nextDue: NextDue? = null,
)

/** Pestaña Mascota (M1): carnet de la mascota seleccionada. */
@OptIn(ExperimentalCoroutinesApi::class)
class PetTabViewModel(
    observeSelectedPet: ObserveSelectedPet,
    observeRecords: ObserveVaccineRecords,
    clock: AppClock,
) : ViewModel() {
    private val retryPetTabState = Retry()

    val state: StateFlow<PetTabState> = retryPetTabState.wrap(PetTabState(loading = false, failed = true)) {
        observeSelectedPet()
            .flatMapLatest { pet ->
                if (pet == null) {
                    flowOf(PetTabState(loading = false))
                } else {
                    observeRecords(pet.id).map { records ->
                        PetTabState(loading = false, pet = pet, vaccineCount = records.size, nextDue = NextDueCalculator.nextDue(records, clock.today()))
                    }
                }
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PetTabState())

    fun retry() = retryPetTabState.retry()
}

data class PetFormState(
    /** El guardado falló (por ejemplo, sin espacio): se muestra el aviso y el formulario sigue abierto. */
    val saveFailed: Boolean = false,
    val loaded: Boolean = false,
    val isEdit: Boolean = false,
    val name: String = "",
    val species: Species? = null,
    val sex: Sex? = null,
    val birthDate: LocalDate? = null,
    val birthApproximate: Boolean = false,
    val breed: String = "",
    val marks: String = "",
    val owner: String = "",
    val address: String = "",
    /** Ruta relativa de la foto ("photos/…"), ya importada. */
    val photoPath: String? = null,
    val importingPhoto: Boolean = false,
    val photoFailed: Int = 0,
    val cameraDenied: Boolean = false,
    val nameError: PetError? = null,
    val speciesMissing: Boolean = false,
    val saving: Boolean = false,
    /** Hay cambios sin guardar: cerrar pregunta antes de descartar. */
    val dirty: Boolean = false,
    /** No se pudo leer la mascota a editar. */
    val loadFailed: Boolean = false,
    /** No se pudo eliminar. */
    val deleteFailed: Boolean = false,
    /** Se cerró el formulario: guardado o eliminado. */
    val done: Boolean = false,
    val deletedPet: Pet? = null,
)

/** Agregar o editar mascota (M2). */
class PetFormViewModel(
    private val petId: String?,
    private val observePet: ObservePet,
    private val savePet: SavePet,
    private val deletePet: DeletePet,
    private val importPhotos: ImportPhotos,
) : ViewModel() {
    private val _state = MutableStateFlow(PetFormState(loaded = petId == null))
    val state: StateFlow<PetFormState> = _state.asStateFlow()

    init {
        load()
    }

    fun retryLoad() {
        _state.update { it.copy(loadFailed = false) }
        load()
    }

    private fun load() {
        if (petId != null) {
            viewModelScope.launch {
                val pet = attempt { observePet(petId).first() }.getOrElse {
                    _state.update { it.copy(loadFailed = true) }
                    return@launch
                }
                _state.value = if (pet == null) {
                    PetFormState(loaded = true, done = true)
                } else {
                    PetFormState(
                        loaded = true,
                        isEdit = true,
                        name = pet.name,
                        species = pet.species,
                        sex = pet.sex,
                        birthDate = pet.birthDate,
                        birthApproximate = pet.birthDatePrecision != null && pet.birthDatePrecision != DatePrecision.EXACT,
                        breed = pet.breed.orEmpty(),
                        marks = pet.distinguishingMarks.orEmpty(),
                        owner = pet.ownerName.orEmpty(),
                        address = pet.ownerAddress.orEmpty(),
                        photoPath = pet.photoPath,
                    )
                }
            }
        }
    }

    fun onName(value: String) = _state.update { it.copy(name = value, nameError = null, dirty = true) }
    fun onSpecies(value: Species) = _state.update { it.copy(species = value, speciesMissing = false, dirty = true) }
    fun onSex(value: Sex) = _state.update { it.copy(sex = value, dirty = true) }
    fun onBirthDate(value: LocalDate) = _state.update { it.copy(birthDate = value, dirty = true) }
    fun onBirthApproximate(value: Boolean) = _state.update { it.copy(birthApproximate = value, dirty = true) }
    fun onBreed(value: String) = _state.update { it.copy(breed = value, dirty = true) }
    fun onMarks(value: String) = _state.update { it.copy(marks = value, dirty = true) }
    fun onOwner(value: String) = _state.update { it.copy(owner = value, dirty = true) }
    fun onAddress(value: String) = _state.update { it.copy(address = value, dirty = true) }

    /** Foto elegida en la cámara o la galería: se procesa y se guarda ya; se asocia al guardar. */
    fun onPhotoPicked(result: ImagePickerResult) {
        when (result) {
            ImagePickerResult.CameraDenied -> _state.update { it.copy(cameraDenied = true) }
            is ImagePickerResult.Picked -> {
                _state.update { it.copy(importingPhoto = true, photoFailed = 0, cameraDenied = false) }
                viewModelScope.launch {
                    val picked = result.images.take(1)
                    val imported = attempt { importPhotos(picked) }.getOrElse { ImportedPhotos(emptyList(), picked.size) }
                    _state.update { s ->
                        s.copy(
                            importingPhoto = false,
                            photoPath = imported.photos.firstOrNull()?.path ?: s.photoPath,
                            photoFailed = imported.failed,
                            dirty = s.dirty || imported.photos.isNotEmpty(),
                        )
                    }
                }
            }
        }
    }

    fun removePhoto() = _state.update { it.copy(photoPath = null, photoFailed = 0, dirty = true) }

    fun save() {
        val s = _state.value
        if (s.saving || s.importingPhoto) return
        // Todos los errores juntos, no de a uno.
        val nameError = PetValidation.validateName(s.name)
        val species = s.species
        if (nameError != null || species == null) {
            _state.update { it.copy(nameError = nameError, speciesMissing = species == null) }
            return
        }
        _state.update { it.copy(saving = true, saveFailed = false) }
        viewModelScope.launch {
            val result = attempt { savePet(
                PetDraft(
                    id = petId,
                    name = s.name,
                    species = species,
                    breed = s.breed,
                    sex = s.sex,
                    birthDate = s.birthDate,
                    birthDatePrecision = if (s.birthApproximate) DatePrecision.MONTH else DatePrecision.EXACT,
                    distinguishingMarks = s.marks,
                    ownerName = s.owner,
                    ownerAddress = s.address,
                    photoPath = s.photoPath,
                ),
            ) }.getOrElse {
                _state.update { it.copy(saving = false, saveFailed = true) }
                return@launch
            }
            _state.update {
                when (result) {
                    is SavePetResult.Saved -> it.copy(saving = false, done = true)
                    is SavePetResult.Invalid -> it.copy(saving = false, nameError = result.error)
                }
            }
        }
    }

    fun delete() {
        val id = petId ?: return
        viewModelScope.launch {
            _state.update { it.copy(deleteFailed = false) }
            attempt {
                val pet = observePet(id).first()
                deletePet(id)
                pet
            }.onSuccess { pet -> _state.update { it.copy(done = true, deletedPet = pet) } }
                .onFailure { _state.update { it.copy(deleteFailed = true) } }
        }
    }
}
