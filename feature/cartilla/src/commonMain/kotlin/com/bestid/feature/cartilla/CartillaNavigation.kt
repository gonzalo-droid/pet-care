package com.bestid.feature.cartilla

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.bestid.core.navigation.CartillaGraph
import com.bestid.core.navigation.CartillaRoute

/** Grafo de la pestaña. Las pantallas de detalle del hito correspondiente se agregan aquí. */
fun NavGraphBuilder.cartillaGraph() {
    navigation<CartillaGraph>(startDestination = CartillaRoute) {
        composable<CartillaRoute> { CartillaScreen() }
    }
}
