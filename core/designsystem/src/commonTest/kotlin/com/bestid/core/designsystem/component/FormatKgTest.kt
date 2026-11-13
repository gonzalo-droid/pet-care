package com.bestid.core.designsystem.component

import kotlin.test.Test
import kotlin.test.assertEquals

class FormatKgTest {
    @Test
    fun usesDecimalComma() {
        assertEquals("4,2", formatKg(4.2))
        assertEquals("0,1", formatKg(0.05))
        assertEquals("12,0", formatKg(12.0))
        assertEquals("3,7", formatKg(3.66))
    }
}
