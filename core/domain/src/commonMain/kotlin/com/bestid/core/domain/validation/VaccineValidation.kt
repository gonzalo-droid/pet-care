package com.bestid.core.domain.validation

import com.bestid.core.model.VaccineItem
import kotlinx.datetime.LocalDate

/** Errores que impiden guardar una vacuna (sección 9). */
sealed interface VaccineError {
    data object NoType : VaccineError
    data object NoApplicationDate : VaccineError
    data object ApplicationDateInFuture : VaccineError
    data object NextDateNotAfterApplication : VaccineError
    data class ExpiryNotAfterManufacture(val itemIndex: Int) : VaccineError
}

/** Avisos que se muestran en durazno pero permiten guardar. */
sealed interface VaccineWarning {
    /** El sticker vence antes de la fecha de aplicación. */
    data class StickerExpiredBeforeApplication(val itemIndex: Int) : VaccineWarning
}

data class VaccineValidationResult(
    val errors: List<VaccineError>,
    val warnings: List<VaccineWarning>,
) {
    val isValid: Boolean get() = errors.isEmpty()
}

object VaccineValidation {
    fun validate(
        hasType: Boolean,
        applicationDate: LocalDate?,
        nextDate: LocalDate?,
        items: List<VaccineItem>,
        today: LocalDate,
    ): VaccineValidationResult {
        val errors = buildList {
            if (!hasType) add(VaccineError.NoType)
            when {
                applicationDate == null -> add(VaccineError.NoApplicationDate)
                applicationDate > today -> add(VaccineError.ApplicationDateInFuture)
            }
            if (applicationDate != null && nextDate != null && nextDate <= applicationDate) {
                add(VaccineError.NextDateNotAfterApplication)
            }
            items.forEachIndexed { index, item ->
                val made = item.manufactureDate
                val expires = item.expiryDate
                if (made != null && expires != null && expires <= made) {
                    add(VaccineError.ExpiryNotAfterManufacture(index))
                }
            }
        }
        val warnings = buildList {
            if (applicationDate != null) {
                items.forEachIndexed { index, item ->
                    val expires = item.expiryDate
                    if (expires != null && expires < applicationDate) {
                        add(VaccineWarning.StickerExpiredBeforeApplication(index))
                    }
                }
            }
        }
        return VaccineValidationResult(errors, warnings)
    }
}
