package com.bestid.core.domain.validation

/** Error de validación de una mascota (sección 9). */
enum class PetError { NAME_EMPTY, NAME_TOO_LONG }

object PetValidation {
    const val NAME_MAX_LENGTH = 40

    fun validateName(name: String): PetError? {
        val trimmed = name.trim()
        return when {
            trimmed.isEmpty() -> PetError.NAME_EMPTY
            trimmed.length > NAME_MAX_LENGTH -> PetError.NAME_TOO_LONG
            else -> null
        }
    }
}
