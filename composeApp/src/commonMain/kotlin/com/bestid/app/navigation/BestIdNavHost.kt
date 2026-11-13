package com.bestid.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bestid.app.resources.Res
import com.bestid.app.resources.settings
import com.bestid.core.designsystem.component.BottomBarItem
import com.bestid.core.designsystem.component.IconAction
import com.bestid.core.designsystem.component.MainScaffold
import com.bestid.core.designsystem.component.PetSelector
import com.bestid.core.designsystem.icon.BestIdIcons
import com.bestid.core.navigation.ComponentCatalogRoute
import com.bestid.core.navigation.HomeGraph
import com.bestid.core.navigation.MainRoute
import com.bestid.core.navigation.SettingsRoute
import com.bestid.feature.cartilla.cartillaGraph
import com.bestid.feature.health.healthGraph
import com.bestid.feature.home.homeGraph
import com.bestid.feature.pet.petGraph
import org.jetbrains.compose.resources.stringResource

/**
 * Navegación raíz: las 4 pestañas viven en [MainRoute] con su propio NavHost (cada pestaña conserva
 * su pila y su estado); Ajustes y los flujos modales van por encima, sin barra inferior.
 */
@Composable
fun BestIdNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = MainRoute) {
        composable<MainRoute> {
            MainTabs(onOpenSettings = { navController.navigate(SettingsRoute) })
        }
        composable<SettingsRoute> {
            SettingsPlaceholder(
                onClose = { navController.popBackStack() },
                onOpenCatalog = { navController.navigate(ComponentCatalogRoute) },
            )
        }
        composable<ComponentCatalogRoute> {
            CatalogScreen(onClose = { navController.popBackStack() })
        }
    }
}

@Composable
private fun MainTabs(onOpenSettings: () -> Unit) {
    val tabsController = rememberNavController()
    val backStackEntry by tabsController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination
    val selected = TopLevelDestination.entries.indexOfFirst { tab ->
        destination?.hierarchy?.any { it.hasRoute(tab.graphClass) } == true
    }.coerceAtLeast(0)

    MainScaffold(
        tabs = TopLevelDestination.entries.map {
            BottomBarItem(stringResource(it.label), it.icon)
        },
        selectedTab = selected,
        onSelectTab = { index ->
            tabsController.navigate(TopLevelDestination.entries[index].graph) {
                popUpTo(tabsController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        },
        // Sin mascotas en M0: el selector muestra "Agregar mascota". Los datos llegan en M1.
        petSelector = { PetSelector(pets = emptyList(), selectedId = null, onSelect = {}, onAdd = {}) },
        actions = { IconAction(BestIdIcons.Settings, stringResource(Res.string.settings), onOpenSettings) },
    ) { padding ->
        NavHost(
            navController = tabsController,
            startDestination = HomeGraph,
            modifier = Modifier.padding(padding),
        ) {
            homeGraph()
            cartillaGraph()
            healthGraph()
            petGraph()
        }
    }
}
