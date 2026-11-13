package com.bestid.app.navigation

import com.bestid.app.resources.Res
import com.bestid.app.resources.tab_cartilla
import com.bestid.app.resources.tab_health
import com.bestid.app.resources.tab_home
import com.bestid.app.resources.tab_pet
import com.bestid.core.designsystem.icon.BestIdIcons
import com.bestid.core.navigation.CartillaGraph
import com.bestid.core.navigation.HealthGraph
import com.bestid.core.navigation.HomeGraph
import com.bestid.core.navigation.PetGraph
import com.bestid.core.navigation.Route
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import kotlin.reflect.KClass

/** Las 4 pestañas de la sección 4, en orden. */
enum class TopLevelDestination(
    val graph: Route,
    val graphClass: KClass<out Route>,
    val label: StringResource,
    val icon: DrawableResource,
) {
    HOME(HomeGraph, HomeGraph::class, Res.string.tab_home, BestIdIcons.Home),
    CARTILLA(CartillaGraph, CartillaGraph::class, Res.string.tab_cartilla, BestIdIcons.Vaccines),
    HEALTH(HealthGraph, HealthGraph::class, Res.string.tab_health, BestIdIcons.Health),
    PET(PetGraph, PetGraph::class, Res.string.tab_pet, BestIdIcons.PetsFilled),
}
