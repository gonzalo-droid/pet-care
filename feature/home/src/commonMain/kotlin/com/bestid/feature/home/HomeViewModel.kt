package com.bestid.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bestid.core.domain.port.AppClock
import com.bestid.core.domain.schedule.NextDue
import com.bestid.core.domain.schedule.NextDueCalculator
import com.bestid.core.domain.usecase.ObserveSelectedPet
import com.bestid.core.domain.usecase.ObserveVaccineRecords
import com.bestid.core.domain.usecase.ObserveVaccineTypes
import com.bestid.core.model.Pet
import com.bestid.core.model.VaccineRecord
import com.bestid.core.model.VaccineType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

data class HomeState(
    val loading: Boolean = true,
    val pet: Pet? = null,
    val vaccineCount: Int = 0,
    val nextDue: NextDue? = null,
    val lastRecord: VaccineRecord? = null,
    val types: Map<String, VaccineType> = emptyMap(),
)

/** Inicio (H1): tarjeta de la mascota, próximo con días restantes y última vacuna. */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    observeSelectedPet: ObserveSelectedPet,
    observeRecords: ObserveVaccineRecords,
    observeTypes: ObserveVaccineTypes,
    clock: AppClock,
) : ViewModel() {
    val state: StateFlow<HomeState> = observeSelectedPet()
        .flatMapLatest { pet ->
            if (pet == null) {
                flowOf(HomeState(loading = false))
            } else {
                combine(observeRecords(pet.id), observeTypes()) { records, types ->
                    HomeState(
                        loading = false,
                        pet = pet,
                        vaccineCount = records.size,
                        nextDue = NextDueCalculator.nextDue(records, clock.today()),
                        lastRecord = records.firstOrNull(),
                        types = types.associateBy { it.id },
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeState())
}
