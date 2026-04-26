package com.nameless.cartio.features.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nameless.cartio.features.settings.domain.ClearAllData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val clearAllData: ClearAllData
) : ViewModel() {

    private val _syncEnabled = MutableStateFlow(false)
    val syncEnabled: StateFlow<Boolean> = _syncEnabled.asStateFlow()

    private val _showClearDialog = MutableStateFlow(false)
    val showClearDialog: StateFlow<Boolean> = _showClearDialog.asStateFlow()

    fun toggleSync(enabled: Boolean) {
        _syncEnabled.value = enabled
    }

    fun requestClearData() {
        _showClearDialog.value = true
    }

    fun dismissClearDialog() {
        _showClearDialog.value = false
    }

    fun confirmClearData() {
        viewModelScope.launch {
            runCatching { clearAllData() }
            _showClearDialog.value = false
        }
    }
}
