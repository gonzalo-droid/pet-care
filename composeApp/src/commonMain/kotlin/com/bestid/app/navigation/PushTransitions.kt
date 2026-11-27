package com.bestid.app.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.navigation.NavBackStackEntry

/**
 * Transiciones al entrar a un detalle, un formulario o Ajustes (D53). Cada plataforma usa la suya:
 * en iOS, el "push" de Navigation; en Android, el eje horizontal de Material en lugar del fundido de
 * 700 ms que trae Navigation, en el que se ven las dos pantallas encimadas.
 */
internal expect object PushTransitions {
    val enter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition
    val exit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition
    val popEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition
    val popExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition
}
