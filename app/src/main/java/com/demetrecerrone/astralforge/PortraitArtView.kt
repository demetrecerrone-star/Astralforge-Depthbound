package com.demetrecerrone.astralforge

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.min
import kotlin.math.pow

class PortraitArtView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    var portraitId: String = PortraitStore.DEFAULT_ID
        set(value) {
            field = value
            invalidate()
        }

    var circularHitTest: Boolean = false
    var highlighted: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val size = min(width, height).toFloat()
        if (size <= 0f) return

        val cx = width / 2f
        val cy = height / 2f
        val r = size * 0.47f

        val clip = Path().apply {
            addCircle(cx, cy, r, Path.Direction.CW)
        }

        PortraitAssetStore.load(context, portraitId)?.let { bitmap ->
            canvas.save()
            canvas.clipPath(clip)
            paint.shader = null
            paint.color = Color.WHITE
            paint.isFilterBitmap = true
            canvas.drawBitmap(
                bitmap,
                null,
                RectF(
                    cx - r,
                    cy - r,
                    cx + r,
                    cy + r
                ),
                paint
            )
            canvas.restore()

            if (highlighted) {
                stroke.shader = null
                stroke.strokeWidth = size * 0.045f
                stroke.color = Color.rgb(255, 212, 112)
                canvas.drawCircle(cx, cy, r, stroke)

                stroke.strokeWidth = size * 0.012f
                stroke.color = Color.rgb(255, 246, 205)
                canvas.drawCircle(cx, cy, r * 0.92f, stroke)
            }
            return
        }

        canvas.save()
        canvas.clipPath(clip)
        drawBackground(canvas, cx, cy, r)

        when (portraitId) {
            "warrior_m" -> drawWarrior(canvas, cx, cy, r)
            "mage_m" -> drawMaleMage(canvas, cx, cy, r)
            "elf_m" -> drawElf(canvas, cx, cy, r)
            "ranger_f" -> drawRanger(canvas, cx, cy, r)
            "sorceress_f" -> drawSorceress(canvas, cx, cy, r)
            "knight_f" -> drawFemaleKnight(canvas, cx, cy, r)
            "orc" -> drawOrc(canvas, cx, cy, r)
            "goblin" -> drawGoblin(canvas, cx, cy, r)
            "demon" -> drawDemon(canvas, cx, cy, r)
            "wolfkin" -> drawWolfkin(canvas, cx, cy, r)
            "lich" -> drawLich(canvas, cx, cy, r)
            "dragonkin" -> drawDragonkin(canvas, cx, cy, r)
            "masked" -> drawMasked(canvas, cx, cy, r)
            else -> drawSkeleton(canvas, cx, cy, r)
        }

        canvas.restore()

        stroke.shader = null
        stroke.strokeWidth = size * 0.035f
        stroke.color = if (highlighted) {
            Color.rgb(255, 212, 112)
        } else {
            Color.rgb(138, 102, 255)
        }
        canvas.drawCircle(cx, cy, r, stroke)

        stroke.strokeWidth = size * 0.012f
        stroke.color = if (highlighted) {
            Color.rgb(255, 246, 205)
        } else {
            Color.argb(190, 207, 188, 255)
        }
        canvas.drawCircle(cx, cy, r * 0.91f, stroke)
    }

    private fun drawBackground(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        val colors = when (portraitId) {
            "warrior_m" -> intArrayOf(Color.rgb(70, 34, 31), Color.rgb(18, 10, 24))
            "mage_m" -> intArrayOf(Color.rgb(31, 52, 112), Color.rgb(22, 13, 56))
            "elf_m" -> intArrayOf(Color.rgb(35, 83, 74), Color.rgb(12, 26, 39))
            "ranger_f" -> intArrayOf(Color.rgb(46, 86, 52), Color.rgb(16, 29, 24))
            "sorceress_f" -> intArrayOf(Color.rgb(93, 38, 124), Color.rgb(26, 12, 54))
            "knight_f" -> intArrayOf(Color.rgb(78, 86, 112), Color.rgb(17, 21, 38))
            "orc" -> intArrayOf(Color.rgb(68, 88, 34), Color.rgb(23, 27, 16))
            "goblin" -> intArrayOf(Color.rgb(94, 104, 38), Color.rgb(26, 30, 13))
            "demon" -> intArrayOf(Color.rgb(116, 32, 36), Color.rgb(37, 9, 20))
            "wolfkin" -> intArrayOf(Color.rgb(65, 75, 94), Color.rgb(17, 21, 31))
            "lich" -> intArrayOf(Color.rgb(28, 102, 116), Color.rgb(15, 20, 43))
            "dragonkin" -> intArrayOf(Color.rgb(57, 80, 120), Color.rgb(20, 22, 44))
            "masked" -> intArrayOf(Color.rgb(64, 39, 91), Color.rgb(13, 13, 28))
            else -> intArrayOf(Color.rgb(66, 52, 94), Color.rgb(13, 14, 28))
        }

        paint.shader = RadialGradient(
            cx,
            cy - r * 0.2f,
            r * 1.25f,
            colors,
            null,
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(cx, cy, r * 1.04f, paint)
        paint.shader = null

        paint.color = Color.argb(42, 255, 255, 255)
        for (i in 0..5) {
            val x = cx - r * 0.7f + r * 0.28f * i
            canvas.drawCircle(x, cy - r * (0.55f - i * 0.08f), r * 0.025f, paint)
        }
    }

    private fun face(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        r: Float,
        skin: Int,
        width: Float = 0.55f,
        height: Float = 0.72f
    ) {
        paint.color = skin
        paint.shader = null
        canvas.drawOval(
            RectF(
                cx - r * width,
                cy - r * height,
                cx + r * width,
                cy + r * height
            ),
            paint
        )
    }

    private fun eyes(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        r: Float,
        color: Int,
        glow: Boolean = false,
        spread: Float = 0.23f
    ) {
        if (glow) {
            paint.shader = RadialGradient(
                cx - r * spread,
                cy - r * 0.08f,
                r * 0.16f,
                intArrayOf(Color.argb(210, Color.red(color), Color.green(color), Color.blue(color)), Color.TRANSPARENT),
                null,
                Shader.TileMode.CLAMP
            )
            canvas.drawCircle(cx - r * spread, cy - r * 0.08f, r * 0.16f, paint)
            paint.shader = RadialGradient(
                cx + r * spread,
                cy - r * 0.08f,
                r * 0.16f,
                intArrayOf(Color.argb(210, Color.red(color), Color.green(color), Color.blue(color)), Color.TRANSPARENT),
                null,
                Shader.TileMode.CLAMP
            )
            canvas.drawCircle(cx + r * spread, cy - r * 0.08f, r * 0.16f, paint)
            paint.shader = null
        }

        paint.color = color
        canvas.drawOval(
            RectF(
                cx - r * (spread + 0.10f),
                cy - r * 0.13f,
                cx - r * (spread - 0.10f),
                cy + r * 0.01f
            ),
            paint
        )
        canvas.drawOval(
            RectF(
                cx + r * (spread - 0.10f),
                cy - r * 0.13f,
                cx + r * (spread + 0.10f),
                cy + r * 0.01f
            ),
            paint
        )
    }

    private fun drawSkeleton(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        paint.color = Color.rgb(226, 219, 195)
        canvas.drawOval(
            RectF(cx - r * 0.49f, cy - r * 0.60f, cx + r * 0.49f, cy + r * 0.40f),
            paint
        )
        canvas.drawRoundRect(
            RectF(cx - r * 0.34f, cy + r * 0.18f, cx + r * 0.34f, cy + r * 0.67f),
            r * 0.08f,
            r * 0.08f,
            paint
        )

        paint.color = Color.rgb(28, 24, 39)
        canvas.drawOval(RectF(cx - r * 0.36f, cy - r * 0.25f, cx - r * 0.08f, cy + r * 0.03f), paint)
        canvas.drawOval(RectF(cx + r * 0.08f, cy - r * 0.25f, cx + r * 0.36f, cy + r * 0.03f), paint)

        val nose = Path().apply {
            moveTo(cx, cy + r * 0.02f)
            lineTo(cx - r * 0.10f, cy + r * 0.22f)
            lineTo(cx + r * 0.10f, cy + r * 0.22f)
            close()
        }
        canvas.drawPath(nose, paint)

        stroke.color = Color.rgb(88, 76, 77)
        stroke.strokeWidth = r * 0.025f
        for (i in -2..2) {
            val x = cx + i * r * 0.11f
            canvas.drawLine(x, cy + r * 0.32f, x, cy + r * 0.59f, stroke)
        }
        canvas.drawLine(cx - r * 0.31f, cy + r * 0.43f, cx + r * 0.31f, cy + r * 0.43f, stroke)

        stroke.color = Color.rgb(118, 101, 112)
        stroke.strokeWidth = r * 0.03f
        canvas.drawLine(cx - r * 0.05f, cy - r * 0.51f, cx + r * 0.09f, cy - r * 0.31f, stroke)
        canvas.drawLine(cx + r * 0.09f, cy - r * 0.31f, cx + r * 0.02f, cy - r * 0.18f, stroke)
    }

    private fun drawWarrior(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        paint.color = Color.rgb(52, 27, 22)
        canvas.drawOval(RectF(cx - r * 0.62f, cy - r * 0.76f, cx + r * 0.62f, cy + r * 0.72f), paint)
        face(canvas, cx, cy, r, Color.rgb(207, 149, 111), 0.48f, 0.62f)

        paint.color = Color.rgb(42, 22, 17)
        canvas.drawArc(
            RectF(cx - r * 0.52f, cy - r * 0.70f, cx + r * 0.52f, cy + r * 0.02f),
            180f,
            180f,
            true,
            paint
        )
        paint.color = Color.rgb(64, 34, 26)
        canvas.drawArc(
            RectF(cx - r * 0.39f, cy + r * 0.05f, cx + r * 0.39f, cy + r * 0.65f),
            0f,
            180f,
            true,
            paint
        )
        eyes(canvas, cx, cy, r, Color.rgb(238, 198, 102), false)

        stroke.color = Color.rgb(107, 49, 42)
        stroke.strokeWidth = r * 0.035f
        canvas.drawLine(cx - r * 0.31f, cy - r * 0.20f, cx - r * 0.10f, cy - r * 0.24f, stroke)
        canvas.drawLine(cx + r * 0.10f, cy - r * 0.24f, cx + r * 0.31f, cy - r * 0.20f, stroke)
    }

    private fun drawMaleMage(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        paint.color = Color.rgb(22, 33, 76)
        val hood = Path().apply {
            moveTo(cx, cy - r * 0.88f)
            lineTo(cx - r * 0.72f, cy + r * 0.70f)
            lineTo(cx + r * 0.72f, cy + r * 0.70f)
            close()
        }
        canvas.drawPath(hood, paint)
        face(canvas, cx, cy + r * 0.04f, r, Color.rgb(185, 178, 194), 0.43f, 0.57f)

        paint.color = Color.rgb(176, 190, 230)
        val hair = Path().apply {
            moveTo(cx - r * 0.40f, cy - r * 0.46f)
            lineTo(cx - r * 0.20f, cy - r * 0.75f)
            lineTo(cx - r * 0.04f, cy - r * 0.48f)
            lineTo(cx + r * 0.13f, cy - r * 0.73f)
            lineTo(cx + r * 0.42f, cy - r * 0.42f)
            close()
        }
        canvas.drawPath(hair, paint)
        eyes(canvas, cx, cy, r, Color.rgb(94, 215, 255), true)
    }

    private fun drawElf(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        paint.color = Color.rgb(199, 181, 145)
        val leftEar = Path().apply {
            moveTo(cx - r * 0.42f, cy - r * 0.13f)
            lineTo(cx - r * 0.88f, cy - r * 0.32f)
            lineTo(cx - r * 0.42f, cy + r * 0.13f)
            close()
        }
        val rightEar = Path().apply {
            moveTo(cx + r * 0.42f, cy - r * 0.13f)
            lineTo(cx + r * 0.88f, cy - r * 0.32f)
            lineTo(cx + r * 0.42f, cy + r * 0.13f)
            close()
        }
        canvas.drawPath(leftEar, paint)
        canvas.drawPath(rightEar, paint)
        face(canvas, cx, cy, r, Color.rgb(217, 197, 159), 0.43f, 0.62f)

        paint.color = Color.rgb(235, 225, 193)
        canvas.drawArc(RectF(cx - r * 0.51f, cy - r * 0.72f, cx + r * 0.51f, cy + r * 0.02f), 180f, 180f, true, paint)
        paint.color = Color.rgb(182, 154, 220)
        eyes(canvas, cx, cy, r, Color.rgb(113, 238, 190), true)
    }

    private fun drawRanger(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        paint.color = Color.rgb(34, 67, 43)
        val hood = Path().apply {
            moveTo(cx, cy - r * 0.90f)
            lineTo(cx - r * 0.71f, cy + r * 0.72f)
            lineTo(cx + r * 0.71f, cy + r * 0.72f)
            close()
        }
        canvas.drawPath(hood, paint)
        face(canvas, cx, cy + r * 0.04f, r, Color.rgb(191, 136, 103), 0.41f, 0.56f)

        paint.color = Color.rgb(68, 39, 24)
        canvas.drawArc(RectF(cx - r * 0.42f, cy - r * 0.53f, cx + r * 0.42f, cy + r * 0.18f), 184f, 172f, true, paint)
        eyes(canvas, cx, cy, r, Color.rgb(139, 222, 127), false)
    }

    private fun drawSorceress(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        paint.color = Color.rgb(67, 26, 84)
        canvas.drawOval(RectF(cx - r * 0.67f, cy - r * 0.76f, cx + r * 0.67f, cy + r * 0.79f), paint)
        face(canvas, cx, cy, r, Color.rgb(220, 174, 175), 0.43f, 0.60f)

        paint.color = Color.rgb(142, 74, 180)
        val hair = Path().apply {
            moveTo(cx - r * 0.46f, cy - r * 0.56f)
            cubicTo(cx - r * 0.15f, cy - r * 0.88f, cx + r * 0.18f, cy - r * 0.82f, cx + r * 0.47f, cy - r * 0.51f)
            lineTo(cx + r * 0.26f, cy - r * 0.31f)
            lineTo(cx - r * 0.34f, cy - r * 0.26f)
            close()
        }
        canvas.drawPath(hair, paint)
        eyes(canvas, cx, cy, r, Color.rgb(239, 109, 255), true)
    }

    private fun drawFemaleKnight(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        paint.color = Color.rgb(132, 143, 168)
        canvas.drawOval(RectF(cx - r * 0.58f, cy - r * 0.74f, cx + r * 0.58f, cy + r * 0.70f), paint)
        paint.color = Color.rgb(56, 63, 83)
        canvas.drawRoundRect(
            RectF(cx - r * 0.40f, cy - r * 0.25f, cx + r * 0.40f, cy + r * 0.36f),
            r * 0.08f,
            r * 0.08f,
            paint
        )
        face(canvas, cx, cy + r * 0.05f, r, Color.rgb(205, 164, 144), 0.32f, 0.38f)
        eyes(canvas, cx, cy + r * 0.03f, r, Color.rgb(132, 203, 255), false, 0.17f)

        paint.color = Color.rgb(225, 198, 98)
        canvas.drawRect(cx - r * 0.05f, cy - r * 0.72f, cx + r * 0.05f, cy - r * 0.42f, paint)
    }

    private fun drawOrc(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        paint.color = Color.rgb(92, 121, 61)
        val ears = Path().apply {
            moveTo(cx - r * 0.43f, cy - r * 0.08f)
            lineTo(cx - r * 0.83f, cy - r * 0.24f)
            lineTo(cx - r * 0.45f, cy + r * 0.18f)
            moveTo(cx + r * 0.43f, cy - r * 0.08f)
            lineTo(cx + r * 0.83f, cy - r * 0.24f)
            lineTo(cx + r * 0.45f, cy + r * 0.18f)
        }
        canvas.drawPath(ears, paint)
        face(canvas, cx, cy, r, Color.rgb(103, 139, 72), 0.52f, 0.63f)
        paint.color = Color.rgb(47, 39, 28)
        canvas.drawArc(RectF(cx - r * 0.54f, cy - r * 0.71f, cx + r * 0.54f, cy + r * 0.02f), 180f, 180f, true, paint)
        eyes(canvas, cx, cy, r, Color.rgb(242, 176, 67), false)

        paint.color = Color.rgb(231, 218, 179)
        val tuskL = Path().apply {
            moveTo(cx - r * 0.28f, cy + r * 0.37f)
            lineTo(cx - r * 0.18f, cy + r * 0.08f)
            lineTo(cx - r * 0.10f, cy + r * 0.39f)
            close()
        }
        val tuskR = Path().apply {
            moveTo(cx + r * 0.28f, cy + r * 0.37f)
            lineTo(cx + r * 0.18f, cy + r * 0.08f)
            lineTo(cx + r * 0.10f, cy + r * 0.39f)
            close()
        }
        canvas.drawPath(tuskL, paint)
        canvas.drawPath(tuskR, paint)
    }

    private fun drawGoblin(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        paint.color = Color.rgb(139, 155, 68)
        val leftEar = Path().apply {
            moveTo(cx - r * 0.34f, cy - r * 0.08f)
            lineTo(cx - r * 0.94f, cy - r * 0.31f)
            lineTo(cx - r * 0.38f, cy + r * 0.17f)
            close()
        }
        val rightEar = Path().apply {
            moveTo(cx + r * 0.34f, cy - r * 0.08f)
            lineTo(cx + r * 0.94f, cy - r * 0.31f)
            lineTo(cx + r * 0.38f, cy + r * 0.17f)
            close()
        }
        canvas.drawPath(leftEar, paint)
        canvas.drawPath(rightEar, paint)
        face(canvas, cx, cy, r, Color.rgb(146, 165, 73), 0.42f, 0.57f)
        eyes(canvas, cx, cy, r, Color.rgb(255, 226, 83), true, 0.24f)

        paint.color = Color.rgb(77, 60, 29)
        canvas.drawOval(RectF(cx - r * 0.19f, cy + r * 0.16f, cx + r * 0.19f, cy + r * 0.31f), paint)
    }

    private fun drawDemon(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        paint.color = Color.rgb(53, 33, 36)
        val hornL = Path().apply {
            moveTo(cx - r * 0.28f, cy - r * 0.48f)
            lineTo(cx - r * 0.72f, cy - r * 0.94f)
            lineTo(cx - r * 0.53f, cy - r * 0.27f)
            close()
        }
        val hornR = Path().apply {
            moveTo(cx + r * 0.28f, cy - r * 0.48f)
            lineTo(cx + r * 0.72f, cy - r * 0.94f)
            lineTo(cx + r * 0.53f, cy - r * 0.27f)
            close()
        }
        canvas.drawPath(hornL, paint)
        canvas.drawPath(hornR, paint)
        face(canvas, cx, cy, r, Color.rgb(151, 48, 53), 0.50f, 0.64f)
        eyes(canvas, cx, cy, r, Color.rgb(255, 148, 56), true)

        paint.color = Color.rgb(72, 21, 27)
        canvas.drawOval(RectF(cx - r * 0.28f, cy + r * 0.26f, cx + r * 0.28f, cy + r * 0.47f), paint)
    }

    private fun drawWolfkin(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        paint.color = Color.rgb(100, 111, 130)
        val earL = Path().apply {
            moveTo(cx - r * 0.33f, cy - r * 0.45f)
            lineTo(cx - r * 0.55f, cy - r * 0.91f)
            lineTo(cx - r * 0.12f, cy - r * 0.57f)
            close()
        }
        val earR = Path().apply {
            moveTo(cx + r * 0.33f, cy - r * 0.45f)
            lineTo(cx + r * 0.55f, cy - r * 0.91f)
            lineTo(cx + r * 0.12f, cy - r * 0.57f)
            close()
        }
        canvas.drawPath(earL, paint)
        canvas.drawPath(earR, paint)
        face(canvas, cx, cy, r, Color.rgb(113, 126, 145), 0.50f, 0.64f)

        paint.color = Color.rgb(151, 161, 175)
        canvas.drawOval(RectF(cx - r * 0.30f, cy + r * 0.05f, cx + r * 0.30f, cy + r * 0.50f), paint)
        paint.color = Color.rgb(30, 31, 38)
        canvas.drawOval(RectF(cx - r * 0.14f, cy + r * 0.09f, cx + r * 0.14f, cy + r * 0.27f), paint)
        eyes(canvas, cx, cy - r * 0.05f, r, Color.rgb(121, 211, 255), true)
    }

    private fun drawLich(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        paint.color = Color.rgb(25, 35, 60)
        val hood = Path().apply {
            moveTo(cx, cy - r * 0.91f)
            lineTo(cx - r * 0.73f, cy + r * 0.72f)
            lineTo(cx + r * 0.73f, cy + r * 0.72f)
            close()
        }
        canvas.drawPath(hood, paint)
        paint.color = Color.rgb(181, 205, 194)
        canvas.drawOval(RectF(cx - r * 0.43f, cy - r * 0.53f, cx + r * 0.43f, cy + r * 0.39f), paint)

        paint.color = Color.rgb(20, 34, 43)
        canvas.drawOval(RectF(cx - r * 0.31f, cy - r * 0.19f, cx - r * 0.07f, cy + r * 0.06f), paint)
        canvas.drawOval(RectF(cx + r * 0.07f, cy - r * 0.19f, cx + r * 0.31f, cy + r * 0.06f), paint)
        eyes(canvas, cx, cy - r * 0.02f, r, Color.rgb(74, 245, 230), true)

        paint.color = Color.rgb(207, 176, 77)
        val crown = Path().apply {
            moveTo(cx - r * 0.43f, cy - r * 0.49f)
            lineTo(cx - r * 0.30f, cy - r * 0.79f)
            lineTo(cx - r * 0.10f, cy - r * 0.57f)
            lineTo(cx, cy - r * 0.84f)
            lineTo(cx + r * 0.12f, cy - r * 0.57f)
            lineTo(cx + r * 0.32f, cy - r * 0.78f)
            lineTo(cx + r * 0.43f, cy - r * 0.49f)
            close()
        }
        canvas.drawPath(crown, paint)
    }

    private fun drawDragonkin(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        paint.color = Color.rgb(54, 70, 103)
        val hornL = Path().apply {
            moveTo(cx - r * 0.28f, cy - r * 0.48f)
            lineTo(cx - r * 0.57f, cy - r * 0.93f)
            lineTo(cx - r * 0.48f, cy - r * 0.31f)
            close()
        }
        val hornR = Path().apply {
            moveTo(cx + r * 0.28f, cy - r * 0.48f)
            lineTo(cx + r * 0.57f, cy - r * 0.93f)
            lineTo(cx + r * 0.48f, cy - r * 0.31f)
            close()
        }
        canvas.drawPath(hornL, paint)
        canvas.drawPath(hornR, paint)

        face(canvas, cx, cy, r, Color.rgb(76, 104, 151), 0.48f, 0.65f)
        paint.color = Color.rgb(112, 140, 180)
        canvas.drawOval(RectF(cx - r * 0.28f, cy + r * 0.04f, cx + r * 0.28f, cy + r * 0.52f), paint)
        eyes(canvas, cx, cy - r * 0.04f, r, Color.rgb(255, 201, 83), true)

        stroke.color = Color.rgb(155, 183, 218)
        stroke.strokeWidth = r * 0.025f
        for (i in -2..2) {
            val y = cy - r * 0.42f + i * r * 0.17f
            canvas.drawLine(cx - r * 0.33f, y, cx + r * 0.33f, y + r * 0.06f, stroke)
        }
    }

    private fun drawMasked(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        paint.color = Color.rgb(35, 24, 49)
        val hood = Path().apply {
            moveTo(cx, cy - r * 0.91f)
            lineTo(cx - r * 0.73f, cy + r * 0.72f)
            lineTo(cx + r * 0.73f, cy + r * 0.72f)
            close()
        }
        canvas.drawPath(hood, paint)

        paint.color = Color.rgb(105, 95, 119)
        canvas.drawRoundRect(
            RectF(cx - r * 0.42f, cy - r * 0.45f, cx + r * 0.42f, cy + r * 0.53f),
            r * 0.18f,
            r * 0.18f,
            paint
        )

        paint.color = Color.rgb(22, 18, 31)
        canvas.drawRoundRect(
            RectF(cx - r * 0.32f, cy - r * 0.12f, cx + r * 0.32f, cy + r * 0.10f),
            r * 0.07f,
            r * 0.07f,
            paint
        )
        eyes(canvas, cx, cy - r * 0.02f, r, Color.rgb(210, 128, 255), true, 0.20f)

        stroke.color = Color.rgb(170, 145, 198)
        stroke.strokeWidth = r * 0.025f
        canvas.drawLine(cx - r * 0.22f, cy + r * 0.27f, cx + r * 0.22f, cy + r * 0.27f, stroke)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isClickable || !isEnabled) return false

        if (circularHitTest && !insideCircle(event.x, event.y)) {
            return false
        }

        return when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                alpha = 0.82f
                scaleX = 0.96f
                scaleY = 0.96f
                true
            }
            MotionEvent.ACTION_UP -> {
                alpha = 1f
                scaleX = 1f
                scaleY = 1f
                if (!circularHitTest || insideCircle(event.x, event.y)) {
                    performClick()
                }
                true
            }
            MotionEvent.ACTION_CANCEL -> {
                alpha = 1f
                scaleX = 1f
                scaleY = 1f
                true
            }
            else -> true
        }
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun insideCircle(x: Float, y: Float): Boolean {
        val cx = width / 2f
        val cy = height / 2f
        val radius = min(width, height) * 0.49f
        val distanceSquared = (x - cx).pow(2) + (y - cy).pow(2)
        return distanceSquared <= radius.pow(2)
    }
}
