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

class WeightFormatTest {
    @Test
    fun kilogramsWithDecimalComma() {
        assertEquals("3,8", formatKgNumber(3800))
        assertEquals("4,25", formatKgNumber(4250))
        assertEquals("0,05", formatKgNumber(50))
        assertEquals("12", formatKgNumber(12_000))
        assertEquals("150", formatKgNumber(150_000))
    }
}
