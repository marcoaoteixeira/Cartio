package com.nameless.cartio.features.settings.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nameless.cartio.features.settings.domain.ClearAllData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(clearAllData: ClearAllData = ClearAllData {}) =
        SettingsViewModel(clearAllData)

    @Test
    fun `sync is disabled by default`() {
        val viewModel = createViewModel()
        assertFalse(viewModel.syncEnabled.value)
    }

    @Test
    fun `toggleSync enables sync`() {
        val viewModel = createViewModel()
        viewModel.toggleSync(true)
        assertTrue(viewModel.syncEnabled.value)
    }

    @Test
    fun `toggleSync disables sync`() {
        val viewModel = createViewModel()
        viewModel.toggleSync(true)
        viewModel.toggleSync(false)
        assertFalse(viewModel.syncEnabled.value)
    }

    @Test
    fun `showClearDialog is false by default`() {
        val viewModel = createViewModel()
        assertFalse(viewModel.showClearDialog.value)
    }

    @Test
    fun `requestClearData shows dialog`() {
        val viewModel = createViewModel()
        viewModel.requestClearData()
        assertTrue(viewModel.showClearDialog.value)
    }

    @Test
    fun `dismissClearDialog hides dialog`() {
        val viewModel = createViewModel()
        viewModel.requestClearData()
        viewModel.dismissClearDialog()
        assertFalse(viewModel.showClearDialog.value)
    }

    @Test
    fun `confirmClearData calls use case and hides dialog`() = runTest {
        var cleared = false
        val viewModel = createViewModel(clearAllData = ClearAllData { cleared = true })

        viewModel.requestClearData()
        viewModel.confirmClearData()
        advanceUntilIdle()

        assertTrue(cleared)
        assertFalse(viewModel.showClearDialog.value)
    }

    @Test
    fun `confirmClearData hides dialog even if use case throws`() = runTest {
        val viewModel = createViewModel(clearAllData = ClearAllData { error("db error") })

        viewModel.requestClearData()
        viewModel.confirmClearData()
        advanceUntilIdle()

        assertFalse(viewModel.showClearDialog.value)
    }
}
