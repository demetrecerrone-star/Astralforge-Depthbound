package com.demetrecerrone.astralforge

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.view.View
import kotlin.math.min

class AuthFantasyBackgroundView(context: Context) : View(context) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()

    private val starPoints = arrayOf(
        0.08f to 0.08f, 0.19f to 0.12f, 0.32f to 0.07f, 0.45f to 0.14f,
        0.58f to 0.09f, 0.72f to 0.16f, 0.86f to 0.10f, 0.94f to 0.20f,
        0.12f to 0.24f, 0.27f to 0.22f, 0.39f to 0.27f, 0.64f to 0.25f,
        0.78f to 0.29f, 0.91f to 0.31f, 0.18f to 0.37f, 0.51f to 0.35f,
        0.69f to 0.40f, 0.84f to 0.42f, 0.06f to 0.46f, 0.34f to 0.45f
    )

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return

        drawSky(canvas, w, h)
        drawStars(canvas, w, h)
        drawMoon(canvas, w, h)
        drawDistantMist(canvas, w, h)
        drawCastle(canvas, w, h)
        drawFloatingRocks(canvas, w, h)
        drawForegroundMist(canvas, w, h)
        drawBottomVignette(canvas, w, h)
    }

    private fun drawSky(canvas: Canvas, w: Float, h: Float) {
        paint.shader = LinearGradient(
            0f, 0f, 0f, h,
            intArrayOf(
                Color.rgb(3, 7, 24),
                Color.rgb(9, 10, 38),
                Color.rgb(22, 11, 55),
                Color.rgb(10, 8, 30)
            ),
            floatArrayOf(0f, 0.34f, 0.68f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, w, h, paint)
        paint.shader = null

        paint.color = Color.argb(28, 131, 68, 220)
        canvas.drawCircle(w * 0.16f, h * 0.40f, w * 0.42f, paint)
        paint.color = Color.argb(22, 77, 111, 230)
        canvas.drawCircle(w * 0.86f, h * 0.28f, w * 0.38f, paint)
    }

    private fun drawStars(canvas: Canvas, w: Float, h: Float) {
        val base = min(w, h)
        starPoints.forEachIndexed { index, point ->
            val alpha = if (index % 3 == 0) 205 else 120
            paint.color = Color.argb(alpha, 218, 221, 255)
            val r = if (index % 5 == 0) base * 0.0028f else base * 0.0018f
            canvas.drawCircle(w * point.first, h * point.second, r, paint)

            if (index % 6 == 0) {
                paint.color = Color.argb(70, 178, 122, 255)
                canvas.drawCircle(w * point.first, h * point.second, r * 3.2f, paint)
            }
        }
    }

    private fun drawMoon(canvas: Canvas, w: Float, h: Float) {
        val cx = w * 0.77f
        val cy = h * 0.20f
        val r = w * 0.105f

        for (i in 5 downTo 1) {
            paint.color = Color.argb(10 + i * 8, 176, 140, 255)
            canvas.drawCircle(cx, cy, r * (1f + i * 0.36f), paint)
        }

        paint.color = Color.rgb(221, 222, 244)
        canvas.drawCircle(cx, cy, r, paint)

        paint.color = Color.argb(22, 88, 78, 128)
        canvas.drawCircle(cx - r * 0.28f, cy - r * 0.12f, r * 0.20f, paint)
        canvas.drawCircle(cx + r * 0.22f, cy + r * 0.19f, r * 0.14f, paint)
        canvas.drawCircle(cx - r * 0.05f, cy + r * 0.34f, r * 0.10f, paint)
    }

    private fun drawDistantMist(canvas: Canvas, w: Float, h: Float) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = h * 0.012f
        paint.color = Color.argb(24, 170, 137, 240)
        canvas.drawArc(-w * 0.25f, h * 0.23f, w * 0.75f, h * 0.70f, 208f, 128f, false, paint)
        paint.strokeWidth = h * 0.008f
        paint.color = Color.argb(20, 101, 142, 236)
        canvas.drawArc(w * 0.33f, h * 0.29f, w * 1.25f, h * 0.74f, 210f, 125f, false, paint)
        paint.style = Paint.Style.FILL
    }

    private fun drawCastle(canvas: Canvas, w: Float, h: Float) {
        val baseY = h * 0.73f
        paint.color = Color.rgb(6, 8, 24)

        path.reset()
        path.moveTo(w * 0.04f, baseY)
        path.lineTo(w * 0.08f, h * 0.58f)
        path.lineTo(w * 0.12f, h * 0.58f)
        path.lineTo(w * 0.14f, h * 0.49f)
        path.lineTo(w * 0.18f, h * 0.58f)
        path.lineTo(w * 0.22f, h * 0.58f)
        path.lineTo(w * 0.24f, h * 0.42f)
        path.lineTo(w * 0.29f, h * 0.34f)
        path.lineTo(w * 0.34f, h * 0.42f)
        path.lineTo(w * 0.35f, h * 0.58f)
        path.lineTo(w * 0.40f, h * 0.58f)
        path.lineTo(w * 0.42f, h * 0.47f)
        path.lineTo(w * 0.46f, h * 0.42f)
        path.lineTo(w * 0.50f, h * 0.47f)
        path.lineTo(w * 0.52f, h * 0.58f)
        path.lineTo(w * 0.57f, h * 0.58f)
        path.lineTo(w * 0.59f, h * 0.51f)
        path.lineTo(w * 0.63f, h * 0.47f)
        path.lineTo(w * 0.67f, h * 0.51f)
        path.lineTo(w * 0.69f, h * 0.58f)
        path.lineTo(w * 0.76f, h * 0.58f)
        path.lineTo(w * 0.78f, h * 0.46f)
        path.lineTo(w * 0.82f, h * 0.40f)
        path.lineTo(w * 0.86f, h * 0.46f)
        path.lineTo(w * 0.88f, h * 0.58f)
        path.lineTo(w * 0.94f, h * 0.58f)
        path.lineTo(w * 0.97f, baseY)
        path.close()
        canvas.drawPath(path, paint)

        paint.color = Color.argb(135, 98, 54, 180)
        val windows = arrayOf(
            0.29f to 0.50f, 0.29f to 0.55f, 0.45f to 0.52f, 0.62f to 0.55f,
            0.82f to 0.50f, 0.82f to 0.55f, 0.18f to 0.62f, 0.72f to 0.62f
        )
        windows.forEach {
            canvas.drawRoundRect(
                w * it.first - w * 0.008f,
                h * it.second - h * 0.012f,
                w * it.first + w * 0.008f,
                h * it.second + h * 0.012f,
                w * 0.004f,
                w * 0.004f,
                paint
            )
        }

        paint.color = Color.argb(60, 155, 87, 234)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = w * 0.006f
        canvas.drawPath(path, paint)
        paint.style = Paint.Style.FILL
    }

    private fun drawFloatingRocks(canvas: Canvas, w: Float, h: Float) {
        fun rock(cx: Float, cy: Float, rw: Float, rh: Float) {
            paint.color = Color.argb(235, 10, 10, 24)
            path.reset()
            path.moveTo(cx - rw, cy)
            path.lineTo(cx - rw * 0.45f, cy - rh * 0.55f)
            path.lineTo(cx + rw * 0.5f, cy - rh * 0.42f)
            path.lineTo(cx + rw, cy)
            path.lineTo(cx + rw * 0.35f, cy + rh * 0.95f)
            path.lineTo(cx - rw * 0.2f, cy + rh * 0.72f)
            path.close()
            canvas.drawPath(path, paint)

            paint.color = Color.argb(70, 136, 76, 220)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = w * 0.003f
            canvas.drawPath(path, paint)
            paint.style = Paint.Style.FILL
        }

        rock(w * 0.08f, h * 0.36f, w * 0.045f, h * 0.025f)
        rock(w * 0.93f, h * 0.39f, w * 0.035f, h * 0.022f)
        rock(w * 0.14f, h * 0.78f, w * 0.06f, h * 0.028f)
        rock(w * 0.90f, h * 0.74f, w * 0.05f, h * 0.026f)
    }

    private fun drawForegroundMist(canvas: Canvas, w: Float, h: Float) {
        val bands = arrayOf(
            Triple(0.18f, 0.70f, 0.32f),
            Triple(0.67f, 0.76f, 0.38f),
            Triple(0.42f, 0.86f, 0.46f)
        )
        bands.forEachIndexed { index, band ->
            paint.color = if (index == 1) {
                Color.argb(22, 139, 100, 225)
            } else {
                Color.argb(18, 112, 133, 221)
            }
            canvas.drawOval(
                w * (band.first - band.third),
                h * (band.second - 0.045f),
                w * (band.first + band.third),
                h * (band.second + 0.045f),
                paint
            )
        }
    }

    private fun drawBottomVignette(canvas: Canvas, w: Float, h: Float) {
        paint.shader = LinearGradient(
            0f, h * 0.62f, 0f, h,
            intArrayOf(
                Color.TRANSPARENT,
                Color.argb(95, 3, 4, 16),
                Color.argb(210, 2, 3, 12)
            ),
            floatArrayOf(0f, 0.58f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, h * 0.62f, w, h, paint)
        paint.shader = null
    }
}
