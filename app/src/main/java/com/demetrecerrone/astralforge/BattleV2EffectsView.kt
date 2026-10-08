package com.demetrecerrone.astralforge

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.os.SystemClock
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.sin

class BattleV2EffectsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private enum class Kind {
        PLAYER_SLASH,
        SLIME_LUNGE,
        PLAYER_HIT,
        ENEMY_HIT,
        ENEMY_DEATH,
        PLAYER_DEATH,
        VICTORY
    }

    private data class Fx(
        val kind: Kind,
        val startMs: Long,
        val durationMs: Long,
        val critical: Boolean = false
    )

    private val effects = mutableListOf<Fx>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val path = Path()
    private val rect = RectF()

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        isClickable = false
        isFocusable = false
    }

    fun playPlayerSlash(critical: Boolean = false) =
        add(Kind.PLAYER_SLASH, if (critical) 780L else 660L, critical)

    fun playSlimeLunge() =
        add(Kind.SLIME_LUNGE, 640L)

    fun playPlayerHit(critical: Boolean = false) =
        add(Kind.PLAYER_HIT, 520L, critical)

    fun playEnemyHit(critical: Boolean = false) =
        add(Kind.ENEMY_HIT, 520L, critical)

    fun playEnemyDeath() =
        add(Kind.ENEMY_DEATH, 1050L)

    fun playPlayerDeath() =
        add(Kind.PLAYER_DEATH, 1050L)

    fun playVictory() =
        add(Kind.VICTORY, 1400L)

    fun clearEffects() {
        effects.clear()
        invalidate()
    }

    private fun add(kind: Kind, duration: Long, critical: Boolean = false) {
        effects += Fx(
            kind = kind,
            startMs = SystemClock.uptimeMillis(),
            durationMs = duration,
            critical = critical
        )
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (effects.isEmpty()) return

        val now = SystemClock.uptimeMillis()
        val iterator = effects.iterator()
        while (iterator.hasNext()) {
            val fx = iterator.next()
            val p = ((now - fx.startMs).toFloat() / fx.durationMs)
                .coerceIn(0f, 1f)
            drawEffect(canvas, fx, p)
            if (p >= 1f) {
                iterator.remove()
            }
        }

        if (effects.isNotEmpty()) {
            postInvalidateOnAnimation()
        }
    }

    private fun drawEffect(canvas: Canvas, fx: Fx, p: Float) {
        when (fx.kind) {
            Kind.PLAYER_SLASH -> drawPlayerSlash(canvas, p, fx.critical)
            Kind.SLIME_LUNGE -> drawSlimeLunge(canvas, p)
            Kind.PLAYER_HIT -> drawImpact(canvas, p, false, fx.critical)
            Kind.ENEMY_HIT -> drawImpact(canvas, p, true, fx.critical)
            Kind.ENEMY_DEATH -> drawDeath(canvas, p, true)
            Kind.PLAYER_DEATH -> drawDeath(canvas, p, false)
            Kind.VICTORY -> drawVictory(canvas, p)
        }
    }

    private fun drawPlayerSlash(canvas: Canvas, p: Float, critical: Boolean) {
        val w = width.toFloat()
        val h = height.toFloat()
        val alpha = ((1f - p) * 255f).toInt().coerceIn(0, 255)
        val sweep = (70f + 145f * p).coerceAtMost(205f)

        rect.set(
            w * 0.34f,
            h * 0.20f,
            w * 0.83f,
            h * 0.76f
        )

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = h * if (critical) 0.030f else 0.022f
        paint.color = Color.argb(alpha, 118, 88, 255)
        paint.setShadowLayer(h * 0.030f, 0f, 0f, Color.rgb(87, 52, 255))
        canvas.drawArc(rect, 192f, sweep, false, paint)

        paint.strokeWidth *= 0.46f
        paint.color = Color.argb(alpha, 112, 225, 255)
        paint.setShadowLayer(h * 0.020f, 0f, 0f, Color.rgb(74, 192, 255))
        canvas.drawArc(rect, 198f, sweep * 0.96f, false, paint)

        paint.strokeWidth *= 0.42f
        paint.color = Color.argb(alpha, 255, 255, 255)
        paint.clearShadowLayer()
        canvas.drawArc(rect, 202f, sweep * 0.91f, false, paint)

        val sparkCount = if (critical) 18 else 11
        for (i in 0 until sparkCount) {
            val t = i.toFloat() / sparkCount
            val x = w * (0.49f + t * 0.30f)
            val y = h * (0.61f - sin(t * 3.14159f) * 0.27f)
            fill.color = Color.argb(alpha, 180 + (i % 2) * 70, 190, 255)
            canvas.drawCircle(x, y, h * (0.005f + 0.005f * (1f - p)), fill)
        }
    }

    private fun drawSlimeLunge(canvas: Canvas, p: Float) {
        val w = width.toFloat()
        val h = height.toFloat()
        val phase = if (p < 0.48f) p / 0.48f else (1f - p) / 0.52f
        val x = w * (0.72f - 0.28f * phase.coerceIn(0f, 1f))
        val y = h * (0.50f + 0.025f * sin(p * 6.283f))
        val alpha = ((1f - p * 0.78f) * 210f).toInt().coerceIn(0, 210)

        for (i in 0 until 5) {
            val trail = i * w * 0.025f
            fill.color = Color.argb(
                (alpha * (1f - i * 0.14f)).toInt().coerceAtLeast(0),
                55,
                154 + i * 8,
                255
            )
            canvas.drawOval(
                x + trail,
                y - h * (0.035f - i * 0.003f),
                x + trail + w * (0.055f - i * 0.006f),
                y + h * (0.035f - i * 0.003f),
                fill
            )
        }

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = h * 0.008f
        paint.color = Color.argb(alpha, 145, 225, 255)
        paint.setShadowLayer(h * 0.015f, 0f, 0f, Color.rgb(46, 122, 255))
        canvas.drawOval(
            x - w * 0.012f,
            y - h * 0.045f,
            x + w * 0.075f,
            y + h * 0.045f,
            paint
        )
        paint.clearShadowLayer()
    }

    private fun drawImpact(
        canvas: Canvas,
        p: Float,
        enemySide: Boolean,
        critical: Boolean
    ) {
        val w = width.toFloat()
        val h = height.toFloat()
        val cx = w * if (enemySide) 0.70f else 0.29f
        val cy = h * 0.49f
        val alpha = ((1f - p) * 255f).toInt().coerceIn(0, 255)
        val radius = h * (0.03f + p * if (critical) 0.20f else 0.13f)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = h * if (critical) 0.018f else 0.012f
        paint.color =
            if (critical) Color.argb(alpha, 255, 198, 69)
            else Color.argb(alpha, 165, 205, 255)
        paint.setShadowLayer(
            h * 0.025f,
            0f,
            0f,
            if (critical) Color.rgb(255, 116, 16)
            else Color.rgb(85, 78, 255)
        )
        canvas.drawCircle(cx, cy, radius, paint)
        paint.clearShadowLayer()

        val count = if (critical) 20 else 13
        for (i in 0 until count) {
            val angle = i * 6.283185f / count
            val len = radius * (1.0f + (i % 3) * 0.18f)
            val x1 = cx + cos(angle) * radius * 0.40f
            val y1 = cy + sin(angle) * radius * 0.40f
            val x2 = cx + cos(angle) * len
            val y2 = cy + sin(angle) * len
            paint.strokeWidth = h * 0.005f
            paint.color =
                if (critical) Color.argb(alpha, 255, 231, 145)
                else Color.argb(alpha, 130, 193, 255)
            canvas.drawLine(x1, y1, x2, y2, paint)
        }

        if (p < 0.22f) {
            fill.color = Color.argb(
                ((0.22f - p) / 0.22f * if (critical) 115f else 72f)
                    .toInt()
                    .coerceIn(0, 130),
                255,
                255,
                255
            )
            canvas.drawRect(0f, 0f, w, h, fill)
        }
    }

    private fun drawDeath(canvas: Canvas, p: Float, enemySide: Boolean) {
        val w = width.toFloat()
        val h = height.toFloat()
        val cx = w * if (enemySide) 0.70f else 0.29f
        val cy = h * 0.54f
        val alpha = ((1f - p) * 220f).toInt().coerceIn(0, 220)

        for (i in 0 until 22) {
            val angle = i * 6.283185f / 22f
            val distance = h * (0.03f + p * (0.13f + (i % 4) * 0.017f))
            val x = cx + cos(angle) * distance
            val y = cy + sin(angle) * distance * 0.62f + p * h * 0.09f
            fill.color =
                if (enemySide) {
                    Color.argb(alpha, 65, 168 + (i % 2) * 40, 255)
                } else {
                    Color.argb(alpha, 128 + (i % 2) * 45, 90, 255)
                }
            canvas.drawCircle(
                x,
                y,
                h * (0.006f + (i % 3) * 0.003f),
                fill
            )
        }

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = h * 0.010f
        paint.color =
            if (enemySide) Color.argb(alpha, 105, 210, 255)
            else Color.argb(alpha, 172, 120, 255)
        canvas.drawOval(
            cx - h * (0.04f + p * 0.20f),
            cy - h * (0.018f + p * 0.035f),
            cx + h * (0.04f + p * 0.20f),
            cy + h * (0.018f + p * 0.035f),
            paint
        )
    }

    private fun drawVictory(canvas: Canvas, p: Float) {
        val w = width.toFloat()
        val h = height.toFloat()
        val cx = w * 0.29f
        val cy = h * 0.49f
        val alpha = ((1f - p) * 205f).toInt().coerceIn(0, 205)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = h * 0.007f
        for (i in 0 until 3) {
            val radius = h * (0.08f + i * 0.035f + p * 0.06f)
            paint.color = Color.argb(
                (alpha * (1f - i * 0.18f)).toInt().coerceAtLeast(0),
                126,
                104 + i * 30,
                255
            )
            canvas.drawCircle(cx, cy, radius, paint)
        }

        path.reset()
        val r = h * (0.10f + p * 0.05f)
        path.moveTo(cx, cy - r)
        path.lineTo(cx + r * 0.18f, cy - r * 0.18f)
        path.lineTo(cx + r, cy)
        path.lineTo(cx + r * 0.18f, cy + r * 0.18f)
        path.lineTo(cx, cy + r)
        path.lineTo(cx - r * 0.18f, cy + r * 0.18f)
        path.lineTo(cx - r, cy)
        path.lineTo(cx - r * 0.18f, cy - r * 0.18f)
        path.close()
        fill.color = Color.argb(alpha / 2, 184, 164, 255)
        canvas.drawPath(path, fill)
    }
}
