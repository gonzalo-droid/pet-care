package com.bestid.feature.pet

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.bestid.core.navigation.PetGraph
import com.bestid.core.navigation.PetRoute

/** Grafo de la pestaña. Las pantallas de detalle del hito correspondiente se agregan aquí. */
fun NavGraphBuilder.petGraph() {
    navigation<PetGraph>(startDestination = PetRoute) {
        composable<PetRoute> { PetScreen() }
    }
}
