package com.minicore.cartio.features.shopping.ui

import com.minicore.cartio.core.scanner.RecognizeTextFromImage
import com.minicore.cartio.core.scanner.ScanFrame
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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

private val FAKE_FRAME = ScanFrame(jpegBytes = ByteArray(1), rotationDegrees = 0)

@OptIn(ExperimentalCoroutinesApi::class)
class ScanTagViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Idle`() = runTest {
        val viewModel = ScanTagViewModel(recognizeText = RecognizeTextFromImage { emptyList() })
        assertEquals(ScanStatus.Idle, viewModel.uiState.value.status)
    }

    @Test
    fun `successful recognition with blocks yields Result state`() = runTest {
        val viewModel = ScanTagViewModel(
            recognizeText = RecognizeTextFromImage { listOf("SANTA MARIA CHUNKEY", "26.95") }
        )

        viewModel.onImageCaptured(FAKE_FRAME)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(ScanStatus.Result, state.status)
        assertEquals(listOf("SANTA MARIA CHUNKEY", "26.95"), state.blocks)
    }

    @Test
    fun `recognition with no blocks yields Empty state`() = runTest {
        val viewModel = ScanTagViewModel(recognizeText = RecognizeTextFromImage { emptyList() })

        viewModel.onImageCaptured(FAKE_FRAME)
        advanceUntilIdle()

        assertEquals(ScanStatus.Empty, viewModel.uiState.value.status)
        assertTrue(viewModel.uiState.value.blocks.isEmpty())
    }

    @Test
    fun `recognition failure yields Error state`() = runTest {
        val viewModel = ScanTagViewModel(
            recognizeText = RecognizeTextFromImage { throw RuntimeException("boom") }
        )

        viewModel.onImageCaptured(FAKE_FRAME)
        advanceUntilIdle()

        assertEquals(ScanStatus.Error, viewModel.uiState.value.status)
    }

    @Test
    fun `retry resets state to Idle`() = runTest {
        val viewModel = ScanTagViewModel(recognizeText = RecognizeTextFromImage { emptyList() })

        viewModel.onImageCaptured(FAKE_FRAME)
        advanceUntilIdle()
        assertEquals(ScanStatus.Empty, viewModel.uiState.value.status)

        viewModel.retry()

        assertEquals(ScanStatus.Idle, viewModel.uiState.value.status)
        assertTrue(viewModel.uiState.value.blocks.isEmpty())
    }

    @Test
    fun `onBlockSelected emits TextSelected event with chosen text`() = runTest {
        val viewModel = ScanTagViewModel(
            recognizeText = RecognizeTextFromImage { listOf("Radishes", "Bunch, Red") }
        )
        val events = mutableListOf<ScanTagEvent>()
        viewModel.events.onEach { events.add(it) }.launchIn(
            kotlinx.coroutines.CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        )

        viewModel.onImageCaptured(FAKE_FRAME)
        advanceUntilIdle()
        viewModel.onBlockSelected("Radishes")
        advanceUntilIdle()

        assertEquals(1, events.size)
        assertEquals(ScanTagEvent.TextSelected("Radishes"), events.first())
    }

    @Test
    fun `second capture while processing is ignored`() = runTest {
        var callCount = 0
        val viewModel = ScanTagViewModel(
            recognizeText = RecognizeTextFromImage {
                callCount++
                listOf("one")
            }
        )

        viewModel.onImageCaptured(FAKE_FRAME)
        viewModel.onImageCaptured(FAKE_FRAME)
        advanceUntilIdle()

        assertEquals(1, callCount)
    }
}
