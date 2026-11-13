package com.bestid.feature.onboarding

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.bestid.core.navigation.NotificationPermissionRoute
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Pantallas del primer uso. En M2, solo el permiso de avisos (O5). */
fun NavGraphBuilder.onboardingFlows(onDone: () -> Unit) {
    composable<NotificationPermissionRoute> { NotificationPermissionScreen(onDone = onDone) }
}

val onboardingModule = module {
    viewModelOf(::NotificationPermissionViewModel)
}
