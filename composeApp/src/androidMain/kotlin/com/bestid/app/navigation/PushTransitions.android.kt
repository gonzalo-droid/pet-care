package com.bestid.app.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavBackStackEntry

// Eje horizontal de Material: la pantalla que sale se va en 90 ms y la nueva entra en 210 ms,
// con un desplazamiento corto (1/12 del ancho) hacia donde va la navegación.
private const val OUT_MS = 90
private const val IN_MS = 210
private const val SHIFT = 12

private fun enterFrom(sign: Int): EnterTransition =
    slideInHorizontally(tween(OUT_MS + IN_MS)) { sign * it / SHIFT } + fadeIn(tween(IN_MS, delayMillis = OUT_MS))

private fun exitTo(sign: Int): ExitTransition =
    slideOutHorizontally(tween(OUT_MS + IN_MS)) { sign * it / SHIFT } + fadeOut(tween(OUT_MS))

internal actual object PushTransitions {
    actual val enter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = { enterFrom(1) }
    actual val exit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = { exitTo(-1) }
    actual val popEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = { enterFrom(-1) }
    actual val popExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = { exitTo(1) }
}
