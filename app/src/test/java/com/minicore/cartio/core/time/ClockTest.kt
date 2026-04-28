package com.minicore.cartio.core.time

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClockTest {

    @Test
    fun `fixed clock returns the configured value`() {
        val clock = Clock { 42L }
        assertEquals(42L, clock.now())
    }

    @Test
    fun `system clock returns a non-zero current time`() {
        val before = System.currentTimeMillis()
        val now = SystemClock().now()
        val after = System.currentTimeMillis()
        assertTrue("now should be within sample window", now in before..after)
    }
}
