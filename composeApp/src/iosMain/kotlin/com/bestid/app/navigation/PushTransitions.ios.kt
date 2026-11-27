package com.bestid.app.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.DefaultNavTransitions

/** El "push" de Navigation en iOS, sin cambios. */
internal actual object PushTransitions {
    actual val enter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition =
        DefaultNavTransitions.enterTransition
    actual val exit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition =
        DefaultNavTransitions.exitTransition
    actual val popEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition =
        DefaultNavTransitions.popEnterTransition(DefaultNavTransitions.enterTransition)
    actual val popExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition =
        DefaultNavTransitions.popExitTransition(DefaultNavTransitions.exitTransition)
}
