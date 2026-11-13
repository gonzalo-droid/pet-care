package com.bestid.feature.cartilla

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bestid.core.domain.catalog.VaccineCatalog
import com.bestid.core.domain.reminder.ShouldAskNotificationPermission
import com.bestid.core.domain.usecase.GetPhotos
import com.bestid.core.domain.usecase.GetVet
import com.bestid.core.domain.usecase.ImportPhotos
import com.bestid.core.domain.usecase.ObservePet
import com.bestid.core.domain.usecase.ObserveSelectedPet
import com.bestid.core.domain.usecase.ObserveVaccineRecord
import com.bestid.core.domain.usecase.ObserveVaccineTypes
import com.bestid.core.domain.usecase.ObserveVets
import com.bestid.core.domain.usecase.SaveVaccineRecord
import com.bestid.core.domain.usecase.SaveVaccineResult
import com.bestid.core.domain.usecase.VaccineDraft
import com.bestid.core.domain.validation.VaccineError
import com.bestid.core.domain.validation.VaccineWarning
import com.bestid.core.model.Pet
import com.bestid.core.model.Photo
import com.bestid.core.platform.image.ImagePickerResult
import com.bestid.core.platform.image.MAX_CARD_PAGES
import com.bestid.core.model.VaccineItem
import com.bestid.core.model.VaccineType
import com.bestid.core.model.Vet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

/** Un frasco (sticker) en el formulario. */
data class StickerForm(
    val product: String = "",
    val lot: String = "",
    val manufactureDate: LocalDate? = null,
    val expiryDate: LocalDate? = null,
)

/** Campo de fecha que el selector nativo va a completar. */
sealed interface DateTarget {
    data object Application : DateTarget
    data object Next : DateTarget
    data class Manufacture(val index: Int) : DateTarget
    data class Expiry(val index: Int) : DateTarget
}

data class VaccineFormState(
    val loaded: Boolean = false,
    val isEdit: Boolean = false,
    val pet: Pet? = null,
    val types: List<VaccineType> = emptyList(),
    val showAllTypes: Boolean = false,
    val selectedTypeIds: Set<String> = emptySet(),
    val customLabels: List<String> = emptyList(),
    val customInputVisible: Boolean = false,
    val customInput: String = "",
    val applicationDate: LocalDate? = null,
    val nextDate: LocalDate? = null,
    val nextDateLabel: String? = null,
    val stickers: List<StickerForm> = listOf(StickerForm()),
    val vetName: String = "",
    val vetContact: String = "",
    /** Páginas de la cartilla, ya importadas (O2 o "Agregar foto"). */
    val pages: List<Photo> = emptyList(),
    val stickerPhoto: Photo? = null,
    val importing: Boolean = false,
    val photoFailed: Int = 0,
    val cameraDenied: Boolean = false,
    val errors: List<VaccineError> = emptyList(),
    val warnings: List<VaccineWarning> = emptyList(),
    val saving: Boolean = false,
    val done: Boolean = false,
    /** Primer logro: mostrar la pantalla de permiso de avisos (O5) al cerrar. */
    val askPermission: Boolean = false,
) {
    /** Tipos sugeridos para la especie más los ya elegidos; todos si se pidió o no hay sugeridos. */
    val visibleTypes: List<VaccineType>
        get() {
            val species = pet?.species ?: return types
            val suggested = VaccineCatalog.suggestedFor(types, species)
            if (showAllTypes || suggested.isEmpty()) return types
            return suggested + types.filter { it.id in selectedTypeIds && it !in suggested }
        }

    /** Frascos que vencían antes de la aplicación: aviso en durazno, se puede guardar. */
    val expiredStickers: Set<Int>
        get() {
            val applied = applicationDate ?: return emptySet()
            return stickers.indices.filter { i -> stickers[i].expiryDate?.let { it < applied } == true }.toSet()
        }

    val hasSuggestions: Boolean
        get() = pet?.let { VaccineCatalog.suggestedFor(types, it.species).isNotEmpty() } ?: false
}

/** Adónde va la próxima foto elegida. */
enum class PhotoTarget { PAGE, STICKER }

/** Agregar o editar vacuna a mano (C3), con las fotos de la cartilla a la vista (D29). */
class VaccineFormViewModel(
    private val recordId: String?,
    private val photoIds: List<String>,
    private val observeSelectedPet: ObserveSelectedPet,
    private val observePet: ObservePet,
    private val observeRecord: ObserveVaccineRecord,
    private val observeTypes: ObserveVaccineTypes,
    observeVets: ObserveVets,
    private val getVet: GetVet,
    private val saveRecord: SaveVaccineRecord,
    private val shouldAskPermission: ShouldAskNotificationPermission,
    private val getPhotos: GetPhotos,
    private val importPhotos: ImportPhotos,
) : ViewModel() {
    private val _state = MutableStateFlow(VaccineFormState())
    val state: StateFlow<VaccineFormState> = _state.asStateFlow()

    /** Para autocompletar la veterinaria. */
    val vets: StateFlow<List<Vet>> = observeVets().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        viewModelScope.launch {
            val types = observeTypes().first()
            val record = recordId?.let { observeRecord(it).first() }
            if (record == null) {
                _state.value = VaccineFormState(loaded = true, pet = observeSelectedPet().first(), types = types, pages = getPhotos(photoIds))
                return@launch
            }
            val vet = record.vetId?.let { getVet(it) }
            _state.value = VaccineFormState(
                loaded = true,
                isEdit = true,
                pet = observePet(record.petId).first(),
                types = types,
                selectedTypeIds = record.typeIds.toSet(),
                applicationDate = record.applicationDate,
                nextDate = record.nextDate,
                nextDateLabel = record.nextDateLabel,
                stickers = record.items.map { StickerForm(it.productName.orEmpty(), it.lot.orEmpty(), it.manufactureDate, it.expiryDate) }
                    .ifEmpty { listOf(StickerForm()) },
                vetName = vet?.name.orEmpty(),
                vetContact = vet?.contact.orEmpty(),
                pages = getPhotos(record.sourcePhotoIds),
                stickerPhoto = record.stickerPhotoId?.let { getPhotos(listOf(it)).firstOrNull() },
            )
        }
    }

    fun onPhotoPicked(target: PhotoTarget, result: ImagePickerResult) {
        when (result) {
            ImagePickerResult.CameraDenied -> _state.update { it.copy(cameraDenied = true) }
            is ImagePickerResult.Picked -> {
                val max = if (target == PhotoTarget.PAGE) MAX_CARD_PAGES - _state.value.pages.size else 1
                val images = result.images.take(max)
                if (images.isEmpty()) return
                _state.update { it.copy(importing = true, photoFailed = 0, cameraDenied = false) }
                viewModelScope.launch {
                    val imported = importPhotos(images)
                    _state.update { s ->
                        when (target) {
                            PhotoTarget.PAGE -> s.copy(pages = s.pages + imported.photos)
                            PhotoTarget.STICKER -> s.copy(stickerPhoto = imported.photos.firstOrNull() ?: s.stickerPhoto)
                        }.copy(importing = false, photoFailed = imported.failed)
                    }
                }
            }
        }
    }

    fun removePage(id: String) = _state.update { s -> s.copy(pages = s.pages.filterNot { it.id == id }) }
    fun removeStickerPhoto() = _state.update { it.copy(stickerPhoto = null) }

    fun toggleType(id: String, selected: Boolean) = _state.update {
        it.copy(selectedTypeIds = if (selected) it.selectedTypeIds + id else it.selectedTypeIds - id, errors = it.errors - VaccineError.NoType)
    }

    fun toggleShowAll() = _state.update { it.copy(showAllTypes = !it.showAllTypes) }
    fun showCustomInput() = _state.update { it.copy(customInputVisible = true) }
    fun onCustomInput(value: String) = _state.update { it.copy(customInput = value) }

    fun addCustomLabel() = _state.update {
        val label = it.customInput.trim()
        if (label.isEmpty()) it
        else it.copy(customLabels = it.customLabels + label, customInput = "", customInputVisible = false, errors = it.errors - VaccineError.NoType)
    }

    fun removeCustomLabel(label: String) = _state.update { it.copy(customLabels = it.customLabels - label) }

    fun onDate(target: DateTarget, date: LocalDate) = _state.update { s ->
        when (target) {
            DateTarget.Application -> s.copy(applicationDate = date)
            DateTarget.Next -> s.copy(nextDate = date)
            is DateTarget.Manufacture -> s.copy(stickers = s.stickers.updated(target.index) { it.copy(manufactureDate = date) })
            is DateTarget.Expiry -> s.copy(stickers = s.stickers.updated(target.index) { it.copy(expiryDate = date) })
        }.copy(errors = emptyList(), warnings = emptyList())
    }

    fun clearNextDate() = _state.update { it.copy(nextDate = null) }

    fun onSticker(index: Int, sticker: StickerForm) = _state.update { it.copy(stickers = it.stickers.updated(index) { sticker }) }
    fun addSticker() = _state.update { it.copy(stickers = it.stickers + StickerForm()) }
    fun removeSticker(index: Int) = _state.update {
        it.copy(stickers = it.stickers.filterIndexed { i, _ -> i != index }.ifEmpty { listOf(StickerForm()) })
    }

    fun onVetName(value: String) = _state.update { it.copy(vetName = value) }
    fun onVetContact(value: String) = _state.update { it.copy(vetContact = value) }
    fun pickVet(vet: Vet) = _state.update { it.copy(vetName = vet.name, vetContact = vet.contact.orEmpty()) }

    fun save() {
        val s = _state.value
        val pet = s.pet ?: return
        if (s.saving || s.importing) return
        _state.update { it.copy(saving = true) }
        viewModelScope.launch {
            val result = saveRecord(
                VaccineDraft(
                    id = recordId,
                    petId = pet.id,
                    petSpecies = pet.species,
                    applicationDate = s.applicationDate,
                    nextDate = s.nextDate,
                    nextDateLabel = s.nextDateLabel,
                    typeIds = s.selectedTypeIds,
                    customTypeLabels = s.customLabels + listOfNotNull(s.customInput.trim().takeIf { it.isNotEmpty() }),
                    items = s.stickers.map { VaccineItem(it.product, it.lot, it.manufactureDate, it.expiryDate) },
                    vetName = s.vetName,
                    vetContact = s.vetContact,
                    sourcePhotoIds = s.pages.map { it.id },
                    stickerPhotoId = s.stickerPhoto?.id,
                ),
            )
            when (result) {
                is SaveVaccineResult.Saved -> {
                    val ask = result.record.nextDate != null && shouldAskPermission()
                    _state.update { it.copy(saving = false, done = true, askPermission = ask) }
                }
                is SaveVaccineResult.Invalid -> _state.update {
                    it.copy(saving = false, errors = result.validation.errors, warnings = result.validation.warnings)
                }
            }
        }
    }
}

private fun <T> List<T>.updated(index: Int, transform: (T) -> T): List<T> =
    mapIndexed { i, item -> if (i == index) transform(item) else item }
