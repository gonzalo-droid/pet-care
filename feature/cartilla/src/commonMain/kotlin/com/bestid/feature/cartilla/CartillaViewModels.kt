package com.bestid.feature.cartilla

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bestid.core.domain.port.AppClock
import com.bestid.core.domain.schedule.NextDue
import com.bestid.core.domain.schedule.NextDueCalculator
import com.bestid.core.domain.usecase.DeleteVaccineRecord
import com.bestid.core.domain.usecase.GetPhotos
import com.bestid.core.domain.usecase.GetVet
import com.bestid.core.domain.usecase.ObservePet
import com.bestid.core.domain.usecase.ObserveSelectedPet
import com.bestid.core.domain.usecase.ObserveVaccineRecord
import com.bestid.core.domain.usecase.ObserveVaccineRecords
import com.bestid.core.domain.usecase.ObserveVaccineTypes
import com.bestid.core.model.Pet
import com.bestid.core.model.Photo
import com.bestid.core.model.VaccineRecord
import com.bestid.core.model.VaccineType
import com.bestid.core.model.Vet
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Etiqueta de cada tipo de un registro, con el catálogo cargado. */
internal fun List<String>.labels(types: Map<String, VaccineType>): List<String> =
    mapNotNull { id -> types[id]?.label }

data class CartillaState(
    val loading: Boolean = true,
    val pet: Pet? = null,
    val records: List<VaccineRecord> = emptyList(),
    val types: Map<String, VaccineType> = emptyMap(),
    val nextDue: NextDue? = null,
)

/** Cartilla (C1): registros de la mascota seleccionada, del más reciente al más antiguo. */
@OptIn(ExperimentalCoroutinesApi::class)
class CartillaViewModel(
    observeSelectedPet: ObserveSelectedPet,
    observeRecords: ObserveVaccineRecords,
    observeTypes: ObserveVaccineTypes,
    clock: AppClock,
) : ViewModel() {
    val state: StateFlow<CartillaState> = observeSelectedPet()
        .flatMapLatest { pet ->
            if (pet == null) {
                flowOf(CartillaState(loading = false))
            } else {
                combine(observeRecords(pet.id), observeTypes()) { records, types ->
                    CartillaState(
                        loading = false,
                        pet = pet,
                        records = records,
                        types = types.associateBy { it.id },
                        nextDue = NextDueCalculator.nextDue(records, clock.today()),
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CartillaState())
}

data class DetailState(
    val loading: Boolean = true,
    val record: VaccineRecord? = null,
    val pet: Pet? = null,
    val vet: Vet? = null,
    val types: Map<String, VaccineType> = emptyMap(),
    val daysLeft: Int? = null,
    val pages: List<Photo> = emptyList(),
    val stickerPhoto: Photo? = null,
    /** Se eliminó: la pantalla se cierra y ofrece "Deshacer". */
    val deletedId: String? = null,
)

/** Detalle de vacuna (C2). */
@OptIn(ExperimentalCoroutinesApi::class)
class VaccineDetailViewModel(
    private val recordId: String,
    observeRecord: ObserveVaccineRecord,
    observePet: ObservePet,
    observeTypes: ObserveVaccineTypes,
    getVet: GetVet,
    clock: AppClock,
    private val deleteRecord: DeleteVaccineRecord,
    getPhotos: GetPhotos,
) : ViewModel() {
    private val deleted = MutableStateFlow<String?>(null)

    val state: StateFlow<DetailState> = combine(
        observeRecord(recordId).flatMapLatest { record ->
            if (record == null) {
                flowOf(DetailParts(null, null, null, emptyList()))
            } else {
                combine(
                    observePet(record.petId),
                    flow { emit(record.vetId?.let { getVet(it) }) },
                    flow { emit(getPhotos(record.sourcePhotoIds + listOfNotNull(record.stickerPhotoId))) },
                ) { pet, vet, photos -> DetailParts(record, pet, vet, photos) }
            }
        },
        observeTypes(),
        deleted,
    ) { (record, pet, vet, photos), types, deletedId ->
        DetailState(
            loading = false,
            record = record,
            pet = pet,
            vet = vet,
            pages = photos.filter { it.id != record?.stickerPhotoId },
            stickerPhoto = photos.firstOrNull { it.id == record?.stickerPhotoId },
            types = types.associateBy { it.id },
            daysLeft = record?.nextDate?.let { clock.today().daysUntilDate(it) },
            deletedId = deletedId,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetailState())

    fun delete() {
        viewModelScope.launch {
            deleteRecord(recordId)
            deleted.update { recordId }
        }
    }
}

private data class DetailParts(val record: VaccineRecord?, val pet: Pet?, val vet: Vet?, val photos: List<Photo>)

private fun kotlinx.datetime.LocalDate.daysUntilDate(other: kotlinx.datetime.LocalDate): Int =
    (other.toEpochDays() - toEpochDays()).toInt()
