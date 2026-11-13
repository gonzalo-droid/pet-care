package com.bestid.feature.health

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import com.bestid.core.navigation.CareEditRoute
import com.bestid.core.navigation.HealthGraph
import com.bestid.core.navigation.HealthRoute
import com.bestid.core.navigation.WeightNewRoute
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Pestaña Salud. */
fun NavGraphBuilder.healthGraph(
    onAddCare: () -> Unit,
    onOpenCare: (String) -> Unit,
    onAddWeight: () -> Unit,
    onAddPet: () -> Unit,
) {
    navigation<HealthGraph>(startDestination = HealthRoute) {
        composable<HealthRoute> {
            HealthScreen(onAddCare = onAddCare, onOpenCare = onOpenCare, onAddWeight = onAddWeight, onAddPet = onAddPet)
        }
    }
}

/** Formularios de cuidado y peso sobre las pestañas. */
fun NavGraphBuilder.healthFlows(onClose: () -> Unit, onAskPermission: () -> Unit) {
    composable<CareEditRoute> { entry ->
        CareFormScreen(careId = entry.toRoute<CareEditRoute>().careId, onClose = onClose, onAskPermission = onAskPermission)
    }
    composable<WeightNewRoute> { WeightFormScreen(onClose = onClose) }
}

val healthModule = module {
    viewModelOf(::HealthViewModel)
    viewModel { (careId: String?) -> CareFormViewModel(careId, get(), get(), get(), get(), get()) }
    viewModelOf(::WeightFormViewModel)
}
