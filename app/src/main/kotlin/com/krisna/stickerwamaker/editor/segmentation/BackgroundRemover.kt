package com.krisna.stickerwamaker.editor.segmentation

import android.graphics.Bitmap

interface BackgroundRemover {
    suspend fun removeBackground(inputBitmap: Bitmap): Bitmap
}

