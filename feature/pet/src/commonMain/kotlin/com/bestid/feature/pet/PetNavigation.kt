package com.bestid.feature.pet

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import com.bestid.core.navigation.PetEditRoute
import com.bestid.core.navigation.PetGraph
import com.bestid.core.navigation.PetRoute
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Pestaña Mascota. */
fun NavGraphBuilder.petGraph(onEditPet: (String) -> Unit, onAddPet: () -> Unit) {
    navigation<PetGraph>(startDestination = PetRoute) {
        composable<PetRoute> { PetScreen(onEditPet = onEditPet, onAddPet = onAddPet) }
    }
}

/** Flujos de mascota sobre las pestañas (sin barra inferior). */
fun NavGraphBuilder.petFlows(onClose: () -> Unit) {
    composable<PetEditRoute> { entry ->
        PetFormScreen(petId = entry.toRoute<PetEditRoute>().petId, onClose = onClose)
    }
}

val petModule = module {
    viewModelOf(::PetTabViewModel)
    viewModel { (petId: String?) -> PetFormViewModel(petId, get(), get(), get()) }
}
