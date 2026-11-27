package com.bestid.app.navigation

import com.bestid.core.designsystem.theme.BestIdTheme
import com.bestid.core.designsystem.component.UndoSnackbar
import androidx.compose.ui.Alignment
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.SnackbarHost
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.runtime.remember
import androidx.navigation.NavOptionsBuilder
import androidx.lifecycle.Lifecycle
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
import com.bestid.core.navigation.BackupRoute
import com.bestid.core.navigation.CardCaptureRoute
import com.bestid.core.navigation.ShareRoute
import com.bestid.feature.settings.settingsFlows
import com.bestid.feature.share.shareFlows
import com.bestid.core.navigation.CareEditRoute
import com.bestid.core.navigation.PhotoViewerRoute
import androidx.navigation.toRoute
import com.bestid.feature.onboarding.WelcomeScreen
import com.bestid.core.navigation.ComponentCatalogRoute
import com.bestid.core.navigation.LicensesRoute
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
    /** Navega solo si la pantalla actual ya está activa: dos toques rápidos no apilan dos destinos. */
    private fun go(route: Any, builder: NavOptionsBuilder.() -> Unit = {}) {
        if (nav.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) nav.navigate(route, builder)
    }

    val openRecord: (String) -> Unit = { go(VaccineDetailRoute(it)) }

    /** "Agregar vacuna" empieza por las fotos de la cartilla (O2), que también deja seguir a mano. */
    val addRecord: () -> Unit = { go(CardCaptureRoute) }
    val editRecord: (String) -> Unit = { go(VaccineEditRoute(it)) }
    val addPet: () -> Unit = { go(PetEditRoute()) }

    /** Primer uso con la cartilla (O1): mascota → fotos → vacuna. */
    val startWithCard: () -> Unit = { go(PetEditRoute(thenCapture = true)) }

    /** Reemplaza el formulario de mascota por la captura, para que "atrás" no vuelva al formulario. */
    val captureAfterPet: () -> Unit = {
        go(CardCaptureRoute) { popUpTo<PetEditRoute> { inclusive = true } }
    }

    /** Reemplaza la captura por el formulario de vacuna con las páginas elegidas. */
    val captured: (List<String>) -> Unit = { ids ->
        go(VaccineEditRoute(photoIds = ids.joinToString(",").ifEmpty { null })) {
            popUpTo<CardCaptureRoute> { inclusive = true }
        }
    }
    val openPhoto: (String) -> Unit = { go(PhotoViewerRoute(it)) }
    val share: () -> Unit = { go(ShareRoute) }
    val openBackup: () -> Unit = { go(BackupRoute()) }

    /** "Tengo un respaldo" (Bienvenida): la misma pantalla, con Importar como acción principal. */
    val restoreBackup: () -> Unit = { go(BackupRoute(restore = true)) }

    /** Tras "Borrar todos los datos" se vuelve a la raíz, que muestra la Bienvenida. */
    val deletedAll: () -> Unit = { nav.popBackStack(MainRoute, inclusive = false) }
    val editPet: (String) -> Unit = { go(PetEditRoute(it)) }
    val addCare: () -> Unit = { go(CareEditRoute()) }
    val openCare: (String) -> Unit = { go(CareEditRoute(it)) }
    val addWeight: () -> Unit = { go(WeightNewRoute) }
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
    val actions = remember(navController) { AppActions(navController) }
    val mainViewModel: MainViewModel = koinViewModel()
    val tap by ReminderTaps.events.collectAsStateWithLifecycle()
    LaunchedEffect(tap) {
        tap?.let {
            actions.open(it)
            ReminderTaps.consume()
        }
    }
    NavHost(
        navController = navController,
        startDestination = MainRoute,
        enterTransition = PushTransitions.enter,
        exitTransition = PushTransitions.exit,
        popEnterTransition = PushTransitions.popEnter,
        popExitTransition = PushTransitions.popExit,
    ) {
        composable<MainRoute> {
            val main by mainViewModel.state.collectAsStateWithLifecycle()
            when {
                !main.loaded -> Box(Modifier.fillMaxSize().background(BestIdTheme.colors.bg))
                // El "Deshacer" de eliminar la última mascota se muestra sobre la Bienvenida.
                main.showWelcome -> Box(Modifier.fillMaxSize()) {
                    WelcomeScreen(onStartWithCard = actions.startWithCard, onStartWithoutCard = actions.addPet, onRestore = actions.restoreBackup)
                    SnackbarHost(
                        LocalUndoController.current.hostState,
                        modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding(),
                    ) { UndoSnackbar(it) }
                }
                else -> MainTabs(actions = actions, onOpenSettings = { navController.navigate(SettingsRoute) }, viewModel = mainViewModel)
            }
        }
        cartillaFlows(
            onBack = actions.back,
            onEditRecord = actions.editRecord,
            onAskPermission = actions.askPermission,
            onOpenPhoto = actions.openPhoto,
            onCaptured = actions.captured,
        )
        healthFlows(onClose = actions.back, onAskPermission = actions.askPermission)
        petFlows(onClose = actions.back, onCaptureCard = actions.captureAfterPet)
        composable<PhotoViewerRoute> { entry ->
            PhotoViewerScreen(path = entry.toRoute<PhotoViewerRoute>().path, onClose = actions.back)
        }
        onboardingFlows(onDone = actions.back)
        shareFlows(onClose = actions.back)
        settingsFlows(
            onClose = actions.back,
            onOpenBackup = actions.openBackup,
            onOpenCatalog = { navController.navigate(ComponentCatalogRoute) },
            onOpenLicenses = { navController.navigate(LicensesRoute) },
            onDeletedAll = actions.deletedAll,
        )
        composable<ComponentCatalogRoute> {
            CatalogScreen(onClose = actions.back)
        }
    }
}

/** Fundido entre pestañas: la anterior sale rápido y la nueva entra después, sin verse las dos a la vez. */
private val tabExit = fadeOut(tween(durationMillis = 90))
private val tabEnter = fadeIn(tween(durationMillis = 210, delayMillis = 90))

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
            // Las pestañas son hermanas: fundido corto sin desplazamiento. El predeterminado en iOS es el
            // "push" (desliza desde la derecha y oscurece la anterior), que es para entrar a un detalle.
            enterTransition = { tabEnter },
            exitTransition = { tabExit },
            popEnterTransition = { tabEnter },
            popExitTransition = { tabExit },
        ) {
            homeGraph(
                onOpenRecord = actions.openRecord,
                onEditRecord = actions.editRecord,
                onAddRecord = actions.addRecord,
                onAddPet = actions.addPet,
                onOpenCare = actions.openCare,
                onAddWeight = actions.addWeight,
            )
            cartillaGraph(onOpenRecord = actions.openRecord, onAddRecord = actions.addRecord, onAddPet = actions.addPet, onShare = actions.share)
            healthGraph(onAddCare = actions.addCare, onOpenCare = actions.openCare, onAddWeight = actions.addWeight, onAddPet = actions.addPet)
            petGraph(onEditPet = actions.editPet, onAddPet = actions.addPet, onShare = actions.share)
        }
    }
}
