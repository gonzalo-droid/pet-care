package com.bestid.core.domain

import com.bestid.core.domain.catalog.VaccineCatalog
import com.bestid.core.model.Species
import kotlin.test.Test
import kotlin.test.assertEquals

class VaccineCatalogTest {
    @Test
    fun catalogHasTheTwelveSeedTypes() {
        assertEquals(12, VaccineCatalog.seed.size)
        assertEquals(12, VaccineCatalog.seed.map { it.id }.toSet().size)
    }

    @Test
    fun suggestionsBySpecies() {
        val cat = VaccineCatalog.suggestedFor(VaccineCatalog.seed, Species.CAT).map { it.id }
        assertEquals(listOf("rabia", "calicivirus", "rinotraqueitis", "panleucopenia", "leucemia"), cat)
        assertEquals(8, VaccineCatalog.suggestedFor(VaccineCatalog.seed, Species.DOG).size)
        assertEquals(0, VaccineCatalog.suggestedFor(VaccineCatalog.seed, Species.BIRD).size)
    }
}
