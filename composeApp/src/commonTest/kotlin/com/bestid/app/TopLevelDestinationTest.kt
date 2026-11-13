package com.bestid.app

import com.bestid.app.navigation.TopLevelDestination
import com.bestid.core.navigation.CartillaGraph
import com.bestid.core.navigation.HealthGraph
import com.bestid.core.navigation.HomeGraph
import com.bestid.core.navigation.PetGraph
import kotlin.test.Test
import kotlin.test.assertEquals

class TopLevelDestinationTest {
    @Test
    fun fourTabsInDocumentedOrder() {
        assertEquals(
            listOf(HomeGraph, CartillaGraph, HealthGraph, PetGraph),
            TopLevelDestination.entries.map { it.graph },
        )
    }

    @Test
    fun graphClassMatchesGraph() {
        TopLevelDestination.entries.forEach { assertEquals(it.graph::class, it.graphClass) }
    }
}
