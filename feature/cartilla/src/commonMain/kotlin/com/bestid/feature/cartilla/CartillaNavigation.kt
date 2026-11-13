package com.bestid.feature.cartilla

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import com.bestid.core.navigation.CardCaptureRoute
import com.bestid.core.navigation.CartillaGraph
import com.bestid.core.navigation.CartillaRoute
import com.bestid.core.navigation.VaccineDetailRoute
import com.bestid.core.navigation.VaccineEditRoute
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Pestaña Cartilla. */
fun NavGraphBuilder.cartillaGraph(
    onOpenRecord: (String) -> Unit,
    onAddRecord: () -> Unit,
    onAddPet: () -> Unit,
) {
    navigation<CartillaGraph>(startDestination = CartillaRoute) {
        composable<CartillaRoute> {
            CartillaScreen(onOpenRecord = onOpenRecord, onAddRecord = onAddRecord, onAddPet = onAddPet)
        }
    }
}

/** Captura, detalle y formulario de vacuna, sobre las pestañas (sin barra inferior). */
fun NavGraphBuilder.cartillaFlows(
    onBack: () -> Unit,
    onEditRecord: (String) -> Unit,
    onAskPermission: () -> Unit,
    onOpenPhoto: (String) -> Unit,
    onCaptured: (photoIds: List<String>) -> Unit,
) {
    composable<CardCaptureRoute> {
        CardCaptureScreen(
            onClose = onBack,
            onContinue = onCaptured,
            onByHand = { onCaptured(emptyList()) },
            onOpenPhoto = onOpenPhoto,
        )
    }
    composable<VaccineDetailRoute> { entry ->
        VaccineDetailScreen(
            recordId = entry.toRoute<VaccineDetailRoute>().recordId,
            onBack = onBack,
            onEdit = onEditRecord,
            onOpenPhoto = onOpenPhoto,
        )
    }
    composable<VaccineEditRoute> { entry ->
        val route = entry.toRoute<VaccineEditRoute>()
        VaccineFormScreen(
            recordId = route.recordId,
            photoIds = route.photoIds?.split(",")?.filter { it.isNotBlank() }.orEmpty(),
            onClose = onBack,
            onAskPermission = onAskPermission,
            onOpenPhoto = onOpenPhoto,
        )
    }
}

val cartillaModule = module {
    viewModelOf(::CartillaViewModel)
    viewModel { (recordId: String) -> VaccineDetailViewModel(recordId, get(), get(), get(), get(), get(), get(), get()) }
    viewModel { (recordId: String?, photoIds: List<String>) ->
        VaccineFormViewModel(recordId, photoIds, get(), get(), get(), get(), get(), get(), get(), get(), get(), get())
    }
    viewModelOf(::CardCaptureViewModel)
}
