package com.minicore.cartio.core.logging

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppLoggerTest {

    @After
    fun tearDown() {
        AppLogger.clear()
    }

    @Test
    fun `entries are empty on start`() {
        assertEquals(emptyList<LogEntry>(), AppLogger.entries.value)
    }

    @Test
    fun `clear empties the buffer`() {
        AppLogger.i("tag", "msg")
        AppLogger.clear()
        assertTrue(AppLogger.entries.value.isEmpty())
    }

    @Test
    fun `entry fields are recorded correctly`() {
        val before = System.currentTimeMillis()
        AppLogger.e("MyTag", "something failed")
        val entry = AppLogger.entries.value.last()
        assertTrue(entry.timestamp >= before)
        assertEquals(LogLevel.ERROR, entry.level)
        assertEquals("MyTag", entry.tag)
        assertEquals("something failed", entry.message)
    }

    @Test
    fun `throwable stack trace is appended to message`() {
        val ex = RuntimeException("boom")
        AppLogger.e("T", "error", ex)
        val message = AppLogger.entries.value.last().message
        assertTrue(message.startsWith("error\n"))
        assertTrue(message.contains("RuntimeException"))
    }

    @Test
    fun `buffer is capped at 500 entries`() {
        repeat(600) { AppLogger.d("tag", "msg $it") }
        assertEquals(500, AppLogger.entries.value.size)
    }

    @Test
    fun `oldest entries are dropped when buffer overflows`() {
        repeat(501) { AppLogger.d("tag", "msg $it") }
        assertEquals("msg 1", AppLogger.entries.value.first().message)
        assertEquals("msg 500", AppLogger.entries.value.last().message)
    }

    @Test
    fun `log levels are set correctly`() {
        AppLogger.d("t", "debug")
        AppLogger.i("t", "info")
        AppLogger.w("t", "warn")
        AppLogger.e("t", "error")
        val entries = AppLogger.entries.value
        assertEquals(LogLevel.DEBUG, entries[entries.size - 4].level)
        assertEquals(LogLevel.INFO, entries[entries.size - 3].level)
        assertEquals(LogLevel.WARN, entries[entries.size - 2].level)
        assertEquals(LogLevel.ERROR, entries[entries.size - 1].level)
    }
}
