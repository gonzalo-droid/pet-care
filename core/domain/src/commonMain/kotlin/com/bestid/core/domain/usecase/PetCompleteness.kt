package com.bestid.core.domain.usecase

import com.bestid.core.model.Pet

enum class PetField { BREED, BIRTH_DATE, MARKS, OWNER }

/** Datos opcionales sin llenar; un texto en blanco cuenta como faltante. */
fun Pet.missingFields(): List<PetField> = buildList {
    if (breed.isNullOrBlank()) add(PetField.BREED)
    if (birthDate == null) add(PetField.BIRTH_DATE)
    if (distinguishingMarks.isNullOrBlank()) add(PetField.MARKS)
    if (ownerName.isNullOrBlank()) add(PetField.OWNER)
}
