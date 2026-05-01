package com.minicore.cartio.features.devlogs.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minicore.cartio.core.logging.AppLogger
import com.minicore.cartio.core.logging.LogEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DevLogsViewModel @Inject constructor() : ViewModel() {

    val entries: StateFlow<List<LogEntry>> = AppLogger.entries.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    fun clearLogs() = AppLogger.clear()
}
