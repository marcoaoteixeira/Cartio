package com.minicore.cartio.core.logging

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object AppLogger {

    private const val MAX_ENTRIES = 500

    private val _entries = MutableStateFlow<List<LogEntry>>(emptyList())
    val entries: StateFlow<List<LogEntry>> = _entries.asStateFlow()

    fun d(tag: String, msg: String) {
        Log.d(tag, msg)
        append(LogLevel.DEBUG, tag, msg)
    }

    fun i(tag: String, msg: String) {
        Log.i(tag, msg)
        append(LogLevel.INFO, tag, msg)
    }

    fun w(tag: String, msg: String) {
        Log.w(tag, msg)
        append(LogLevel.WARN, tag, msg)
    }

    fun e(tag: String, msg: String, throwable: Throwable? = null) {
        Log.e(tag, msg, throwable)
        val fullMessage = if (throwable != null) "$msg\n${throwable.stackTraceToString()}" else msg
        append(LogLevel.ERROR, tag, fullMessage)
    }

    fun clear() {
        _entries.value = emptyList()
    }

    private fun append(level: LogLevel, tag: String, message: String) {
        val entry = LogEntry(
            timestamp = System.currentTimeMillis(),
            level = level,
            tag = tag,
            message = message
        )
        _entries.update { current ->
            val next = current + entry
            if (next.size > MAX_ENTRIES) next.drop(next.size - MAX_ENTRIES) else next
        }
    }
}
