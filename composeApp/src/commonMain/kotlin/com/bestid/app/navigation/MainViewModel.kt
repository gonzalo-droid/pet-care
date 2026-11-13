package com.bestid.app.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bestid.core.domain.usecase.ObservePets
import com.bestid.core.domain.usecase.ObserveSelectedPet
import com.bestid.core.domain.usecase.SelectPet
import com.bestid.core.model.Pet
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MainState(val pets: List<Pet> = emptyList(), val selectedId: String? = null)

/** Selector de mascota de la barra superior (U7): lista y selección recordada entre sesiones. */
class MainViewModel(
    observePets: ObservePets,
    observeSelectedPet: ObserveSelectedPet,
    private val selectPet: SelectPet,
) : ViewModel() {
    val state: StateFlow<MainState> = combine(observePets(), observeSelectedPet()) { pets, selected ->
        MainState(pets, selected?.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MainState())

    fun select(petId: String) {
        viewModelScope.launch { selectPet(petId) }
    }
}
