package com.bestid.feature.home

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.bestid.core.navigation.HomeGraph
import com.bestid.core.navigation.HomeRoute
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Pestaña Inicio. */
fun NavGraphBuilder.homeGraph(
    onOpenRecord: (String) -> Unit,
    onEditRecord: (String) -> Unit,
    onAddRecord: () -> Unit,
    onAddPet: () -> Unit,
    onOpenCare: (String) -> Unit,
    onAddWeight: () -> Unit,
) {
    navigation<HomeGraph>(startDestination = HomeRoute) {
        composable<HomeRoute> {
            HomeScreen(
                onOpenRecord = onOpenRecord,
                onEditRecord = onEditRecord,
                onAddRecord = onAddRecord,
                onAddPet = onAddPet,
                onOpenCare = onOpenCare,
                onAddWeight = onAddWeight,
            )
        }
    }
}

val homeModule = module {
    viewModelOf(::HomeViewModel)
}
