package com.bestid.core.ui

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class DateFormatTest {
    @Test
    fun numericFormatIsDayMonthYearWithZeros() {
        assertEquals("21/12/2025", LocalDate(2025, 12, 21).formatNumeric())
        assertEquals("05/03/2026", LocalDate(2026, 3, 5).formatNumeric())
    }
}
