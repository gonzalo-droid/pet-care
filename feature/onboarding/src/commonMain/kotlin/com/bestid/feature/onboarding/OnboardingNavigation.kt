package com.bestid.feature.onboarding

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.bestid.core.navigation.NotificationPermissionRoute
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Pantallas del primer uso con ruta propia: el permiso de avisos (O5). La Bienvenida (O1) la muestra la raíz si no hay mascotas. */
fun NavGraphBuilder.onboardingFlows(onDone: () -> Unit) {
    composable<NotificationPermissionRoute> { NotificationPermissionScreen(onDone = onDone) }
}

val onboardingModule = module {
    viewModelOf(::NotificationPermissionViewModel)
}
