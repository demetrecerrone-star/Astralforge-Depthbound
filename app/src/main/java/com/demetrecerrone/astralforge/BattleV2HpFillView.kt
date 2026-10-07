package com.demetrecerrone.astralforge

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import kotlin.math.max

class BattleV2HpFillView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val shinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val fillRect = RectF()
    private var fraction = 1f
    private var startColor = Color.rgb(18, 105, 255)
    private var endColor = Color.rgb(73, 216, 255)
    private var animator: ValueAnimator? = null

    fun setPalette(start: Int, end: Int) {
        startColor = start
        endColor = end
        invalidate()
    }

    fun setHealth(current: Int, maximum: Int, animate: Boolean = true) {
        val next = (current.toFloat() / max(1, maximum).toFloat())
            .coerceIn(0f, 1f)

        animator?.cancel()
        if (!animate || !isLaidOut) {
            fraction = next
            invalidate()
            return
        }

        animator = ValueAnimator.ofFloat(fraction, next).apply {
            duration = 260L
            addUpdateListener {
                fraction = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    override fun onDetachedFromWindow() {
        animator?.cancel()
        animator = null
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (fraction <= 0f || width <= 0 || height <= 0) return

        val filledWidth = width * fraction
        val radius = height * 0.48f
        fillRect.set(0f, 0f, filledWidth, height.toFloat())

        fillPaint.shader = LinearGradient(
            0f, 0f, width.toFloat(), 0f,
            startColor, endColor, Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(fillRect, radius, radius, fillPaint)
        fillPaint.shader = null

        shinePaint.color = Color.argb(78, 255, 255, 255)
        canvas.drawRoundRect(
            1f,
            1f,
            (filledWidth - 1f).coerceAtLeast(1f),
            (height * 0.34f).coerceAtLeast(2f),
            radius,
            radius,
            shinePaint
        )
    }
}
