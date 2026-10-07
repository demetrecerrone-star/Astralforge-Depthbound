package com.demetrecerrone.astralforge

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import kotlin.math.sin
import kotlin.random.Random

class HubEffectView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private data class Particle(
        var x: Float,
        var y: Float,
        val speed: Float,
        val radius: Float,
        val drift: Float,
        val phase: Float
    )

    private val particles = mutableListOf<Particle>()
    private val particlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val random = Random(74031)
    private var lastFrame = System.nanoTime()

    var reducedMotion: Boolean = false
        set(value) {
            field = value
            lastFrame = System.nanoTime()
            invalidate()
        }

    init {
        isClickable = false
        isFocusable = false
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        particles.clear()
        repeat(48) {
            particles += Particle(
                x = random.nextFloat() * w,
                y = random.nextFloat() * h,
                speed = 8f + random.nextFloat() * 22f,
                radius = 1f + random.nextFloat() * 2.6f,
                drift = -5f + random.nextFloat() * 10f,
                phase = random.nextFloat() * 6.28318f
            )
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width <= 0 || height <= 0) return

        val now = System.nanoTime()
        val seconds = if (reducedMotion) 0.0 else now / 1_000_000_000.0
        val dt = if (reducedMotion) {
            0f
        } else {
            ((now - lastFrame) / 1_000_000_000.0)
                .toFloat()
                .coerceAtMost(0.05f)
        }
        lastFrame = now

        val portalPulse = if (reducedMotion) 0f else sin(seconds * 1.5).toFloat()
        val crystalPulse = if (reducedMotion) 0f else sin(seconds * 1.9 + 0.7).toFloat()

        drawGlow(
            canvas,
            width * 0.50f,
            height * 0.285f,
            width * (0.15f + 0.013f * portalPulse),
            Color.rgb(75, 90, 255),
            95
        )
        drawGlow(
            canvas,
            width * 0.50f,
            height * 0.46f,
            width * (0.095f + 0.009f * crystalPulse),
            Color.rgb(124, 69, 255),
            78
        )

        particles.forEachIndexed { index, particle ->
            if (!reducedMotion) {
                particle.y -= particle.speed * dt
                particle.x += particle.drift * dt

                if (particle.y < -10f) {
                    particle.y = height + 10f
                    particle.x = random.nextFloat() * width
                }
                if (particle.x < -10f) particle.x = width + 10f
                if (particle.x > width + 10f) particle.x = -10f
            }

            val twinkle = if (reducedMotion) {
                0.55
            } else {
                ((sin(
                    seconds * (1.0 + (index % 6) * 0.13) + particle.phase
                ) + 1.0) * 0.5)
            }

            particlePaint.color = Color.argb(
                (55 + twinkle * 150).toInt(),
                195,
                210,
                255
            )

            canvas.drawCircle(
                particle.x,
                particle.y,
                particle.radius * (0.8f + twinkle.toFloat() * 0.6f),
                particlePaint
            )
        }

        if (!reducedMotion) {
            postInvalidateOnAnimation()
        }
    }

    private fun drawGlow(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float,
        color: Int,
        alpha: Int
    ) {
        if (radius <= 1f) return

        glowPaint.shader = RadialGradient(
            cx,
            cy,
            radius,
            intArrayOf(
                Color.argb(
                    alpha,
                    Color.red(color),
                    Color.green(color),
                    Color.blue(color)
                ),
                Color.argb(
                    alpha / 3,
                    Color.red(color),
                    Color.green(color),
                    Color.blue(color)
                ),
                Color.TRANSPARENT
            ),
            floatArrayOf(0f, 0.45f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(cx, cy, radius, glowPaint)
        glowPaint.shader = null
    }
}
