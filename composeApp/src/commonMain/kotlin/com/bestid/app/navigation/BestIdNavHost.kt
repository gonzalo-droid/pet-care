package com.bestid.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.bestid.core.designsystem.component.LocalUndoController
import com.bestid.core.designsystem.component.MainScaffold
import com.bestid.core.designsystem.component.PetSelector
import com.bestid.core.designsystem.icon.BestIdIcons
import com.bestid.core.domain.reminder.ReminderRefType
import com.bestid.core.navigation.CareEditRoute
import com.bestid.core.navigation.ComponentCatalogRoute
import com.bestid.core.navigation.NotificationPermissionRoute
import com.bestid.core.navigation.WeightNewRoute
import com.bestid.core.platform.notification.ReminderTap
import com.bestid.core.platform.notification.ReminderTaps
import com.bestid.core.navigation.HomeGraph
import com.bestid.core.navigation.MainRoute
import com.bestid.core.navigation.PetEditRoute
import com.bestid.core.navigation.SettingsRoute
import com.bestid.core.navigation.VaccineDetailRoute
import com.bestid.core.navigation.VaccineEditRoute
import com.bestid.core.ui.toSelectorItem
import com.bestid.feature.cartilla.cartillaFlows
import com.bestid.feature.cartilla.cartillaGraph
import com.bestid.feature.health.healthFlows
import com.bestid.feature.health.healthGraph
import com.bestid.feature.home.homeGraph
import com.bestid.feature.onboarding.onboardingFlows
import com.bestid.feature.pet.petFlows
import com.bestid.feature.pet.petGraph
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Acciones de navegación que comparten las pestañas. */
private class AppActions(private val nav: NavHostController) {
    val openRecord: (String) -> Unit = { nav.navigate(VaccineDetailRoute(it)) }
    val addRecord: () -> Unit = { nav.navigate(VaccineEditRoute()) }
    val editRecord: (String) -> Unit = { nav.navigate(VaccineEditRoute(it)) }
    val addPet: () -> Unit = { nav.navigate(PetEditRoute()) }
    val editPet: (String) -> Unit = { nav.navigate(PetEditRoute(it)) }
    val addCare: () -> Unit = { nav.navigate(CareEditRoute()) }
    val openCare: (String) -> Unit = { nav.navigate(CareEditRoute(it)) }
    val addWeight: () -> Unit = { nav.navigate(WeightNewRoute) }
    val back: () -> Unit = { nav.popBackStack() }

    /** Cierra el formulario y muestra el permiso de avisos (O5). */
    val askPermission: () -> Unit = {
        nav.popBackStack()
        nav.navigate(NotificationPermissionRoute)
    }

    /** Tocar un aviso abre el registro (sección 11). */
    fun open(tap: ReminderTap) = when (tap.refType) {
        ReminderRefType.VACCINE -> nav.navigate(VaccineDetailRoute(tap.refId))
        ReminderRefType.CARE -> nav.navigate(CareEditRoute(tap.refId))
    }
}

/**
 * Navegación raíz: las 4 pestañas viven en [MainRoute] con su propio NavHost (cada pestaña conserva
 * su pila y su estado); los formularios, el detalle y Ajustes van por encima, sin barra inferior.
 */
@Composable
fun BestIdNavHost(navController: NavHostController = rememberNavController()) {
    val actions = AppActions(navController)
    val tap by ReminderTaps.events.collectAsStateWithLifecycle()
    LaunchedEffect(tap) {
        tap?.let {
            actions.open(it)
            ReminderTaps.consume()
        }
    }
    NavHost(navController = navController, startDestination = MainRoute) {
        composable<MainRoute> {
            MainTabs(actions = actions, onOpenSettings = { navController.navigate(SettingsRoute) })
        }
        cartillaFlows(onBack = actions.back, onEditRecord = actions.editRecord, onAskPermission = actions.askPermission)
        healthFlows(onClose = actions.back, onAskPermission = actions.askPermission)
        petFlows(onClose = actions.back)
        onboardingFlows(onDone = actions.back)
        composable<SettingsRoute> {
            SettingsPlaceholder(onClose = actions.back, onOpenCatalog = { navController.navigate(ComponentCatalogRoute) })
        }
        composable<ComponentCatalogRoute> {
            CatalogScreen(onClose = actions.back)
        }
    }
}

@Composable
private fun MainTabs(
    actions: AppActions,
    onOpenSettings: () -> Unit,
    viewModel: MainViewModel = koinViewModel(),
) {
    val tabsController = rememberNavController()
    val backStackEntry by tabsController.currentBackStackEntryAsState()
    val main by viewModel.state.collectAsStateWithLifecycle()
    val destination = backStackEntry?.destination
    val selected = TopLevelDestination.entries.indexOfFirst { tab ->
        destination?.hierarchy?.any { it.hasRoute(tab.graphClass) } == true
    }.coerceAtLeast(0)

    MainScaffold(
        tabs = TopLevelDestination.entries.map { BottomBarItem(stringResource(it.label), it.icon) },
        selectedTab = selected,
        onSelectTab = { index ->
            tabsController.navigate(TopLevelDestination.entries[index].graph) {
                popUpTo(tabsController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        },
        petSelector = {
            PetSelector(
                pets = main.pets.map { it.toSelectorItem() },
                selectedId = main.selectedId,
                onSelect = viewModel::select,
                onAdd = actions.addPet,
            )
        },
        actions = { IconAction(BestIdIcons.Settings, stringResource(Res.string.settings), onOpenSettings) },
        snackbarHostState = LocalUndoController.current.hostState,
    ) { padding ->
        NavHost(
            navController = tabsController,
            startDestination = HomeGraph,
            modifier = Modifier.padding(padding),
        ) {
            homeGraph(
                onOpenRecord = actions.openRecord,
                onAddRecord = actions.addRecord,
                onAddPet = actions.addPet,
                onOpenCare = actions.openCare,
                onAddWeight = actions.addWeight,
            )
            cartillaGraph(onOpenRecord = actions.openRecord, onAddRecord = actions.addRecord, onAddPet = actions.addPet)
            healthGraph(onAddCare = actions.addCare, onOpenCare = actions.openCare, onAddWeight = actions.addWeight, onAddPet = actions.addPet)
            petGraph(onEditPet = actions.editPet, onAddPet = actions.addPet)
        }
    }
}
