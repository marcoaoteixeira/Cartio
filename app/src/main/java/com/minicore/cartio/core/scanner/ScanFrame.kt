package com.minicore.cartio.core.scanner

/**
 * A single captured camera frame (in-memory JPEG), never written to disk.
 * Plain class, not a data class: content-equality on [jpegBytes] isn't
 * meaningful here (array equals is by reference) and nothing relies on it.
 */
class ScanFrame(val jpegBytes: ByteArray, val rotationDegrees: Int)
