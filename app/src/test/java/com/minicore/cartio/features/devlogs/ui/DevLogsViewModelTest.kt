package com.minicore.cartio.features.devlogs.ui

import com.minicore.cartio.core.logging.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DevLogsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        AppLogger.clear()
    }

    @After
    fun tearDown() {
        AppLogger.clear()
        Dispatchers.resetMain()
    }

    @Test
    fun `entries reflects logged messages`() = runTest {
        val viewModel = DevLogsViewModel()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.entries.collect {}
        }

        AppLogger.i("Tag", "hello")
        advanceUntilIdle()

        val entries = viewModel.entries.value
        assertEquals(1, entries.size)
        assertEquals("hello", entries.first().message)
    }

    @Test
    fun `clearLogs empties entries`() = runTest {
        val viewModel = DevLogsViewModel()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.entries.collect {}
        }

        AppLogger.e("Tag", "error")
        advanceUntilIdle()
        viewModel.clearLogs()
        advanceUntilIdle()

        assertTrue(viewModel.entries.value.isEmpty())
    }

    @Test
    fun `initial value is empty before any log`() = runTest {
        val viewModel = DevLogsViewModel()
        val initial = viewModel.entries.first()
        assertTrue(initial.isEmpty())
    }
}
