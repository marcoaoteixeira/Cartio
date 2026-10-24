package com.minicore.cartio.features.shopping.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minicore.cartio.core.scanner.RecognizeTextFromImage
import com.minicore.cartio.core.scanner.ScanFrame
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ScanStatus { Idle, Processing, Result, Empty, Error }

data class ScanTagUiState(
    val status: ScanStatus = ScanStatus.Idle,
    val blocks: List<String> = emptyList()
)

@HiltViewModel
class ScanTagViewModel @Inject constructor(
    private val recognizeText: RecognizeTextFromImage
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanTagUiState())
    val uiState: StateFlow<ScanTagUiState> = _uiState.asStateFlow()

    private val _events = Channel<ScanTagEvent>(Channel.BUFFERED)
    val events: Flow<ScanTagEvent> = _events.receiveAsFlow()

    fun onImageCaptured(frame: ScanFrame) {
        if (_uiState.value.status == ScanStatus.Processing) return
        _uiState.value = _uiState.value.copy(status = ScanStatus.Processing)
        viewModelScope.launch {
            val result = runCatching { recognizeText(frame) }
            _uiState.value = result.fold(
                onSuccess = { blocks ->
                    if (blocks.isEmpty()) {
                        ScanTagUiState(status = ScanStatus.Empty)
                    } else {
                        ScanTagUiState(status = ScanStatus.Result, blocks = blocks)
                    }
                },
                onFailure = { ScanTagUiState(status = ScanStatus.Error) }
            )
        }
    }

    fun retry() {
        _uiState.value = ScanTagUiState(status = ScanStatus.Idle)
    }

    fun onBlockSelected(text: String) {
        viewModelScope.launch {
            _events.send(ScanTagEvent.TextSelected(text))
        }
    }
}
