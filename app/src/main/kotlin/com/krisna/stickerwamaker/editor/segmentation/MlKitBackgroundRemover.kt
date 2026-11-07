package com.krisna.stickerwamaker.editor.segmentation

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.Segmentation
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class MlKitBackgroundRemover : BackgroundRemover {

    private val options = SelfieSegmenterOptions.Builder()
        .setDetectorMode(SelfieSegmenterOptions.SINGLE_IMAGE_MODE)
        .build()

    private val segmenter = Segmentation.getClient(options)

    override suspend fun removeBackground(inputBitmap: Bitmap): Bitmap = suspendCancellableCoroutine { continuation ->
        val image = InputImage.fromBitmap(inputBitmap, 0)
        segmenter.process(image)
            .addOnSuccessListener { mask ->
                val maskBuffer = mask.buffer.asFloatBuffer()
                val maskWidth = mask.width
                val maskHeight = mask.height
                maskBuffer.rewind()

                val width = inputBitmap.width
                val height = inputBitmap.height
                val inputPixels = IntArray(width * height)
                inputBitmap.getPixels(inputPixels, 0, width, 0, 0, width, height)

                val outputPixels = IntArray(width * height)
                val scaleX = maskWidth.toFloat() / width
                val scaleY = maskHeight.toFloat() / height

                var foregroundCount = 0

                for (y in 0 until height) {
                    for (x in 0 until width) {
                        val index = y * width + x
                        val maskX = (x * scaleX).toInt().coerceIn(0, maskWidth - 1)
                        val maskY = (y * scaleY).toInt().coerceIn(0, maskHeight - 1)
                        val maskIndex = maskY * maskWidth + maskX

                        val confidence = maskBuffer.get(maskIndex)
                        if (confidence > 0.5f) {
                            outputPixels[index] = inputPixels[index]
                            foregroundCount++
                        } else {
                            outputPixels[index] = 0
                        }
                    }
                }

                if (foregroundCount < (width * height * 0.01f)) {
                    continuation.resume(inputBitmap)
                } else {
                    val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    output.setPixels(outputPixels, 0, width, 0, 0, width, height)
                    continuation.resume(output)
                }
            }
            .addOnFailureListener {
                continuation.resume(inputBitmap)
            }
    }
}

