package com.bestid.feature.health

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.bestid.core.navigation.HealthGraph
import com.bestid.core.navigation.HealthRoute

/** Grafo de la pestaña. Las pantallas de detalle del hito correspondiente se agregan aquí. */
fun NavGraphBuilder.healthGraph() {
    navigation<HealthGraph>(startDestination = HealthRoute) {
        composable<HealthRoute> { HealthScreen() }
    }
}
