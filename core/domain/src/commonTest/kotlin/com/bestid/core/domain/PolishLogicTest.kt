package com.bestid.core.domain

import com.bestid.core.domain.schedule.countdownProgress
import com.bestid.core.domain.usecase.PetField
import com.bestid.core.domain.usecase.groupedByYear
import com.bestid.core.domain.usecase.missingFields
import com.bestid.core.model.Pet
import com.bestid.core.model.RecordSource
import com.bestid.core.model.Species
import com.bestid.core.model.VaccineRecord
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class PolishLogicTest {
    private fun d(y: Int, m: Int, day: Int) = LocalDate(y, m, day)

    private fun record(date: LocalDate) = VaccineRecord(
        id = date.toString(), petId = "p", applicationDate = date, nextDate = null,
        nextDateConfirmed = false, nextDateLabel = null, typeIds = listOf("rabia"), typeIdsToConfirm = emptyList(),
        items = emptyList(), vetId = null, source = RecordSource.MANUAL, sourcePhotoIds = emptyList(),
        stickerPhotoId = null, extraction = null, createdAt = Instant.fromEpochSeconds(0),
        updatedAt = Instant.fromEpochSeconds(0), deletedAt = null,
    )

    private fun pet(
        breed: String? = "Mestizo",
        birthDate: LocalDate? = d(2020, 1, 1),
        marks: String? = "mancha",
        owner: String? = "Ana",
    ) = Pet(
        id = "p", name = "Fido", species = Species.DOG, breed = breed, sex = null, birthDate = birthDate,
        birthDatePrecision = null, distinguishingMarks = marks, ownerName = owner, ownerAddress = null,
        photoPath = null, createdAt = Instant.fromEpochSeconds(0), updatedAt = Instant.fromEpochSeconds(0),
        deletedAt = null,
    )

    @Test fun progressMidway() = assertEquals(0.5f, countdownProgress(d(2026, 1, 1), d(2026, 1, 11), d(2026, 1, 6)))
    @Test fun progressClampsOverdue() = assertEquals(1f, countdownProgress(d(2026, 1, 1), d(2026, 1, 11), d(2026, 2, 1)))
    @Test fun progressBeforeStartIsZero() = assertEquals(0f, countdownProgress(d(2026, 1, 1), d(2026, 1, 11), d(2025, 12, 1)))
    @Test fun progressNullWithoutFrom() = assertNull(countdownProgress(null, d(2026, 1, 11), d(2026, 1, 6)))
    @Test fun progressNullWhenRangeEmpty() = assertNull(countdownProgress(d(2026, 1, 11), d(2026, 1, 11), d(2026, 1, 6)))

    @Test fun groupsByYearDescending() {
        val grouped = listOf(record(d(2022, 3, 1)), record(d(2025, 12, 21)), record(d(2025, 6, 1))).groupedByYear()
        assertEquals(
            listOf(2025 to listOf(d(2025, 12, 21), d(2025, 6, 1)), 2022 to listOf(d(2022, 3, 1))),
            grouped.map { (y, rs) -> y to rs.map { it.applicationDate } },
        )
    }

    @Test fun missingFieldsInOrder() =
        assertEquals(listOf(PetField.BREED, PetField.BIRTH_DATE, PetField.OWNER), pet(breed = " ", birthDate = null, owner = null).missingFields())

    @Test fun noMissingFieldsWhenComplete() = assertEquals(emptyList(), pet().missingFields())
}
