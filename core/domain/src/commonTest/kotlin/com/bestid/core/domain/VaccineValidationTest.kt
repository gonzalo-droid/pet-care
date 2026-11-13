package com.bestid.core.domain

import com.bestid.core.domain.validation.PetError
import com.bestid.core.domain.validation.PetValidation
import com.bestid.core.domain.validation.VaccineError
import com.bestid.core.domain.validation.VaccineValidation
import com.bestid.core.domain.validation.VaccineWarning
import com.bestid.core.model.VaccineItem
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ValidationTest {
    private val today = LocalDate(2026, 11, 13)

    @Test
    fun petNameMustHave1To40Characters() {
        assertEquals(PetError.NAME_EMPTY, PetValidation.validateName("   "))
        assertEquals(PetError.NAME_TOO_LONG, PetValidation.validateName("a".repeat(41)))
        assertNull(PetValidation.validateName("Lilith"))
        assertNull(PetValidation.validateName(" " + "a".repeat(40) + " "))
    }

    @Test
    fun vaccineNeedsTypeAndApplicationDate() {
        val r = VaccineValidation.validate(hasType = false, applicationDate = null, nextDate = null, items = emptyList(), today = today)
        assertEquals(listOf(VaccineError.NoType, VaccineError.NoApplicationDate), r.errors)
    }

    @Test
    fun applicationDateCannotBeInTheFuture() {
        val r = VaccineValidation.validate(true, today.plusDays(1), null, emptyList(), today)
        assertEquals(listOf(VaccineError.ApplicationDateInFuture), r.errors)
        assertTrue(VaccineValidation.validate(true, today, null, emptyList(), today).isValid)
    }

    @Test
    fun nextDateMustBeAfterApplication() {
        val app = LocalDate(2025, 12, 21)
        assertEquals(
            listOf(VaccineError.NextDateNotAfterApplication),
            VaccineValidation.validate(true, app, app, emptyList(), today).errors,
        )
        assertTrue(VaccineValidation.validate(true, app, LocalDate(2026, 12, 31), emptyList(), today).isValid)
    }

    @Test
    fun expiryMustBeAfterManufacture() {
        val item = VaccineItem("Felocell 3", "774233A", LocalDate(2026, 5, 5), LocalDate(2024, 11, 8))
        val r = VaccineValidation.validate(true, LocalDate(2025, 12, 21), null, listOf(item), today)
        assertEquals(listOf(VaccineError.ExpiryNotAfterManufacture(0)), r.errors)
    }

    @Test
    fun stickerExpiredBeforeApplicationIsOnlyAWarning() {
        // Primer registro de Lilith: el Felocell 3 vencía el 05/11/2024 y se aplicó el 29/06/2024 (válido).
        // Aquí se fuerza un sticker vencido para comprobar el aviso.
        val item = VaccineItem("Felocell 3", "669022", LocalDate(2023, 5, 11), LocalDate(2024, 6, 1))
        val r = VaccineValidation.validate(true, LocalDate(2024, 6, 29), null, listOf(item), today)
        assertTrue(r.isValid)
        assertEquals(listOf(VaccineWarning.StickerExpiredBeforeApplication(0)), r.warnings)
    }
}

private fun LocalDate.plusDays(days: Int) = kotlinx.datetime.LocalDate.fromEpochDays(toEpochDays() + days)
