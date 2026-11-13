package com.bestid.core.domain

import com.bestid.core.domain.schedule.NextDueCalculator
import com.bestid.core.model.RecordSource
import com.bestid.core.model.VaccineRecord
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class NextDueCalculatorTest {
    private val today = LocalDate(2026, 11, 13)

    private fun record(id: String, next: LocalDate?, confirmed: Boolean = next != null) = VaccineRecord(
        id = id, petId = "p", applicationDate = LocalDate(2025, 12, 21), nextDate = next,
        nextDateConfirmed = confirmed, nextDateLabel = null, typeIds = listOf("rabia"), typeIdsToConfirm = emptyList(),
        items = emptyList(), vetId = null, source = RecordSource.MANUAL, sourcePhotoIds = emptyList(),
        stickerPhotoId = null, extraction = null, createdAt = Instant.fromEpochSeconds(0),
        updatedAt = Instant.fromEpochSeconds(0), deletedAt = null,
    )

    @Test
    fun noNextDatesMeansNothingDue() {
        assertNull(NextDueCalculator.nextDue(listOf(record("a", null)), today))
    }

    @Test
    fun picksTheClosestUpcomingDate() {
        val due = NextDueCalculator.nextDue(
            listOf(record("far", LocalDate(2027, 6, 1)), record("near", LocalDate(2026, 12, 31))),
            today,
        )!!
        assertEquals("near", due.record.id)
        assertEquals(48, due.daysLeft)
    }

    @Test
    fun todayCountsAsUpcomingWithZeroDays() {
        assertEquals(0, NextDueCalculator.nextDue(listOf(record("a", today)), today)!!.daysLeft)
    }

    @Test
    fun withoutUpcomingReturnsTheMostRecentOverdue() {
        val due = NextDueCalculator.nextDue(
            listOf(record("old", LocalDate(2026, 1, 1)), record("recent", LocalDate(2026, 11, 3))),
            today,
        )!!
        assertEquals("recent", due.record.id)
        assertEquals(-10, due.daysLeft)
    }
}
