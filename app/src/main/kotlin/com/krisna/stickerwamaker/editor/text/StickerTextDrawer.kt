package com.krisna.stickerwamaker.editor.text

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface

object StickerTextDrawer {

    fun drawText(
        source: Bitmap,
        text: String,
        textColor: Int = Color.WHITE,
        strokeColor: Int = Color.BLACK,
        textSizeSp: Float = 36f,
        positionYFraction: Float = 0.85f
    ): Bitmap {
        if (text.isBlank()) return source

        val output = source.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(output)

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = strokeColor
            style = Paint.Style.STROKE
            strokeWidth = 10f
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            textSize = textSizeSp * (source.width / 400f)
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            style = Paint.Style.FILL
            textSize = textSizeSp * (source.width / 400f)
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }

        val x = source.width / 2f
        val y = source.height * positionYFraction.coerceIn(0.1f, 0.95f)

        canvas.drawText(text, x, y, strokePaint)
        canvas.drawText(text, x, y, fillPaint)

        return output
    }
}

