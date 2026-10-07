package com.demetrecerrone.astralforge

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.Typeface
import android.view.View
import kotlin.math.max
import kotlin.random.Random

internal class AuthLandscapeBackgroundView(
    context: Context
) : View(context) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val stars = List(110) { index ->
        val rng = Random(index * 7919 + 43)
        Triple(
            rng.nextFloat(),
            rng.nextFloat() * 0.72f,
            0.45f + rng.nextFloat() * 1.4f
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat().coerceAtLeast(1f)
        val h = height.toFloat().coerceAtLeast(1f)

        paint.shader = LinearGradient(
            0f,
            0f,
            0f,
            h,
            intArrayOf(
                Color.rgb(3, 5, 16),
                Color.rgb(8, 6, 28),
                Color.rgb(5, 4, 18)
            ),
            null,
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, w, h, paint)

        paint.shader = RadialGradient(
            w * 0.23f,
            h * 0.24f,
            max(w, h) * 0.40f,
            intArrayOf(
                Color.argb(145, 116, 53, 221),
                Color.argb(62, 56, 39, 143),
                Color.TRANSPARENT
            ),
            null,
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, w, h, paint)

        paint.shader = RadialGradient(
            w * 0.46f,
            h * 0.12f,
            max(w, h) * 0.30f,
            intArrayOf(
                Color.argb(76, 53, 94, 210),
                Color.TRANSPARENT
            ),
            null,
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, w, h, paint)

        paint.shader = null
        stars.forEachIndexed { index, star ->
            val x = star.first * w
            val y = star.second * h
            if (x > w * 0.54f && y > h * 0.14f) {
                return@forEachIndexed
            }
            val alpha = 80 + (index % 5) * 24
            paint.color = Color.argb(alpha, 222, 215, 255)
            canvas.drawCircle(x, y, star.third * resources.displayMetrics.density, paint)
        }

        val horizon = h * 0.72f
        paint.color = Color.argb(245, 5, 5, 14)
        val step = w / 12f
        val mountain = android.graphics.Path().apply {
            moveTo(0f, horizon)
            for (i in 0..12) {
                val x = i * step
                val rise =
                    when (i % 5) {
                        0 -> h * 0.08f
                        1 -> h * 0.14f
                        2 -> h * 0.05f
                        3 -> h * 0.11f
                        else -> h * 0.07f
                    }
                lineTo(x, horizon - rise)
            }
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        canvas.drawPath(mountain, paint)

        paint.color = Color.argb(230, 12, 8, 25)
        canvas.drawRect(
            w * 0.06f,
            h * 0.45f,
            w * 0.08f,
            h * 0.87f,
            paint
        )
        canvas.drawRect(
            w * 0.28f,
            h * 0.45f,
            w * 0.30f,
            h * 0.87f,
            paint
        )

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = h * 0.045f
        paint.color = Color.argb(230, 23, 15, 45)
        val archRect = android.graphics.RectF(
            w * 0.055f,
            h * 0.34f,
            w * 0.305f,
            h * 0.74f
        )
        canvas.drawArc(archRect, 190f, 160f, false, paint)
        paint.style = Paint.Style.FILL

        paint.shader = LinearGradient(
            w * 0.47f,
            0f,
            w,
            0f,
            intArrayOf(
                Color.TRANSPARENT,
                Color.argb(75, 2, 4, 14),
                Color.argb(172, 2, 4, 14)
            ),
            null,
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(w * 0.42f, 0f, w, h, paint)
        paint.shader = null

        drawTitle(canvas, w, h)
    }

    private fun drawTitle(
        canvas: Canvas,
        w: Float,
        h: Float
    ) {
        val density = resources.displayMetrics.scaledDensity

        paint.typeface =
            Typeface.create(Typeface.SERIF, Typeface.BOLD)
        paint.textSize = (h * 0.095f)
            .coerceIn(38f * density, 76f * density)
        paint.color = Color.argb(245, 239, 232, 255)
        paint.setShadowLayer(
            h * 0.025f,
            0f,
            0f,
            Color.rgb(126, 62, 222)
        )
        canvas.drawText(
            "ASTRALFORGE",
            w * 0.075f,
            h * 0.23f,
            paint
        )

        paint.clearShadowLayer()
        paint.typeface =
            Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.textSize = h * 0.045f
        paint.color = Color.argb(235, 203, 179, 250)
        canvas.drawText(
            "DEPTHBOUND",
            w * 0.087f,
            h * 0.30f,
            paint
        )

        paint.typeface = Typeface.SANS_SERIF
        paint.textSize = h * 0.025f
        paint.color = Color.argb(185, 158, 150, 190)
        canvas.drawText(
            "DESCEND.  ENDURE.  ASCEND.",
            w * 0.087f,
            h * 0.35f,
            paint
        )
    }
}
