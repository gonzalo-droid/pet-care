package com.bestid.feature.home

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.bestid.core.navigation.HomeGraph
import com.bestid.core.navigation.HomeRoute

/** Grafo de la pestaña. Las pantallas de detalle del hito correspondiente se agregan aquí. */
fun NavGraphBuilder.homeGraph() {
    navigation<HomeGraph>(startDestination = HomeRoute) {
        composable<HomeRoute> { HomeScreen() }
    }
}
