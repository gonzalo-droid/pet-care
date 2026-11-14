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
fun NavGraphBuilder.petGraph(onEditPet: (String) -> Unit, onAddPet: () -> Unit, onShare: () -> Unit) {
    navigation<PetGraph>(startDestination = PetRoute) {
        composable<PetRoute> { PetScreen(onEditPet = onEditPet, onAddPet = onAddPet, onShare = onShare) }
    }
}

/**
 * Flujos de mascota sobre las pestañas (sin barra inferior). Con `thenCapture`, al guardar una
 * mascota nueva se sigue con [onCaptureCard] (primer uso escaneando, O1 → O2).
 */
fun NavGraphBuilder.petFlows(onClose: () -> Unit, onCaptureCard: () -> Unit) {
    composable<PetEditRoute> { entry ->
        val route = entry.toRoute<PetEditRoute>()
        PetFormScreen(
            petId = route.petId,
            onClose = onClose,
            onSaved = if (route.thenCapture) onCaptureCard else onClose,
        )
    }
}

val petModule = module {
    viewModelOf(::PetTabViewModel)
    viewModel { (petId: String?) -> PetFormViewModel(petId, get(), get(), get(), get()) }
}
