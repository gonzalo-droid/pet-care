package com.bestid.core.domain.catalog

import com.bestid.core.model.Species
import com.bestid.core.model.VaccineType

/**
 * Catálogo semilla de la sección 9 (sale de las cartillas vistas). La asignación por especie es una
 * propuesta que Gonzalo valida con un veterinario antes del lanzamiento. Los nombres son datos de
 * dominio (van también al PDF y al respaldo), no textos de interfaz.
 */
object VaccineCatalog {
    private val dog = setOf(Species.DOG)
    private val cat = setOf(Species.CAT)
    private val both = setOf(Species.DOG, Species.CAT)

    val seed: List<VaccineType> = listOf(
        VaccineType("parvovirus", "Parvovirus", dog, isCustom = false),
        VaccineType("distemper", "Distemper", dog, isCustom = false),
        VaccineType("hepatitis", "Hepatitis", dog, isCustom = false),
        VaccineType("leptospirosis", "Leptospirosis", dog, isCustom = false),
        VaccineType("parainfluenza", "Parainfluenza", dog, isCustom = false),
        VaccineType("coronavirus", "Coronavirus", dog, isCustom = false),
        VaccineType("kc", "Tos de perreras", dog, isCustom = false),
        VaccineType("rabia", "Rabia", both, isCustom = false),
        VaccineType("calicivirus", "Calicivirus", cat, isCustom = false),
        VaccineType("rinotraqueitis", "Rinotraqueítis", cat, isCustom = false),
        VaccineType("panleucopenia", "Panleucopenia", cat, isCustom = false),
        VaccineType("leucemia", "Leucemia", cat, isCustom = false),
    )

    /**
     * Tipos que se ofrecen primero para una especie. Para especies sin catálogo (aves, reptiles...)
     * no hay sugeridos: se muestran todos y "Otra".
     */
    fun suggestedFor(all: List<VaccineType>, species: Species): List<VaccineType> =
        all.filter { species in it.species }
}
