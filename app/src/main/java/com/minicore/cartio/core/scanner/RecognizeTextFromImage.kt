package com.minicore.cartio.core.scanner

fun interface RecognizeTextFromImage {
    suspend operator fun invoke(frame: ScanFrame): List<String>
}
