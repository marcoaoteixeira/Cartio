package com.minicore.cartio.core.scanner

import android.graphics.BitmapFactory
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class RecognizeTextFromImageUseCase @Inject constructor() : RecognizeTextFromImage {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override suspend fun invoke(frame: ScanFrame): List<String> {
        val bitmap = BitmapFactory.decodeByteArray(frame.jpegBytes, 0, frame.jpegBytes.size)
            ?: return emptyList()
        val image = InputImage.fromBitmap(bitmap, frame.rotationDegrees)
        val result = recognizer.process(image).await()
        return result.textBlocks
            .map { it.text.trim() }
            .filter { it.isNotEmpty() }
    }
}
