package com.krisna.stickerwamaker.editor.effect

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlin.math.cos
import kotlin.math.sin

interface StickerOutlineEffect {
    fun applyOutline(
        source: Bitmap,
        strokeWidth: Float,
        strokeColor: Int = Color.WHITE
    ): Bitmap
}

class CanvasStickerOutline : StickerOutlineEffect {

    override fun applyOutline(
        source: Bitmap,
        strokeWidth: Float,
        strokeColor: Int
    ): Bitmap {
        if (strokeWidth <= 0f) return source

        val output = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val alphaMask = source.extractAlpha()
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = strokeColor
            isFilterBitmap = true
        }

        val steps = 16
        val radius = strokeWidth.coerceIn(1f, 30f)
        for (i in 0 until steps) {
            val angle = (2 * Math.PI * i / steps).toFloat()
            val dx = (radius * cos(angle.toDouble())).toFloat()
            val dy = (radius * sin(angle.toDouble())).toFloat()
            canvas.drawBitmap(alphaMask, dx, dy, paint)
        }

        val halfRadius = radius * 0.5f
        for (i in 0 until steps) {
            val angle = (2 * Math.PI * i / steps).toFloat()
            val dx = (halfRadius * cos(angle.toDouble())).toFloat()
            val dy = (halfRadius * sin(angle.toDouble())).toFloat()
            canvas.drawBitmap(alphaMask, dx, dy, paint)
        }

        canvas.drawBitmap(source, 0f, 0f, null)
        return output
    }
}

