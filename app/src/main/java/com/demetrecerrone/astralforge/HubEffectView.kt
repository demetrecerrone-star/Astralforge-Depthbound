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

    var visualQuality: String = "HIGH"
        set(value) {
            field = when (value) {
                "LOW", "MEDIUM", "HIGH" -> value
                else -> "HIGH"
            }
            rebuildParticles()
            invalidate()
        }

    var particleDensity: Int = 75
        set(value) {
            field = value.coerceIn(0, 100)
            rebuildParticles()
            invalidate()
        }

    var batterySaver: Boolean = false
        set(value) {
            field = value
            rebuildParticles()
            lastFrame = System.nanoTime()
            invalidate()
        }

    var fpsPreference: String = "SYSTEM"
        set(value) {
            field = when (value) {
                "30", "60", "SYSTEM" -> value
                else -> "SYSTEM"
            }
            lastFrame = System.nanoTime()
            invalidate()
        }

    init {
        isClickable = false
        isFocusable = false
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        rebuildParticles()
    }

    private fun targetParticleCount(): Int {
        val base = when (visualQuality) {
            "LOW" -> 26
            "MEDIUM" -> 44
            else -> 64
        }
        val densityScale = particleDensity / 100f
        val batteryScale = if (batterySaver) 0.55f else 1f
        return (base * densityScale * batteryScale)
            .toInt()
            .coerceIn(0, 72)
    }

    private fun rebuildParticles() {
        if (width <= 0 || height <= 0) return
        val target = targetParticleCount()
        particles.clear()
        repeat(target) {
            particles += Particle(
                x = random.nextFloat() * width,
                y = random.nextFloat() * height,
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
        val staticMode = reducedMotion
        val seconds = if (staticMode) 0.0 else now / 1_000_000_000.0
        val dt = if (staticMode) {
            0f
        } else {
            ((now - lastFrame) / 1_000_000_000.0)
                .toFloat()
                .coerceAtMost(0.05f)
        }
        lastFrame = now

        val qualityAlpha = when (visualQuality) {
            "LOW" -> 0.58f
            "MEDIUM" -> 0.78f
            else -> 1f
        }
        val batteryAlpha = if (batterySaver) 0.72f else 1f

        val portalPulse = if (staticMode || batterySaver) {
            0f
        } else {
            sin(seconds * 1.5).toFloat()
        }
        val crystalPulse = if (staticMode || batterySaver) {
            0f
        } else {
            sin(seconds * 1.9 + 0.7).toFloat()
        }

        drawGlow(
            canvas,
            width * 0.50f,
            height * 0.285f,
            width * (0.15f + 0.013f * portalPulse),
            Color.rgb(75, 90, 255),
            (95 * qualityAlpha * batteryAlpha).toInt()
        )
        drawGlow(
            canvas,
            width * 0.50f,
            height * 0.46f,
            width * (0.095f + 0.009f * crystalPulse),
            Color.rgb(124, 69, 255),
            (78 * qualityAlpha * batteryAlpha).toInt()
        )

        particles.forEachIndexed { index, particle ->
            if (!staticMode) {
                val speedScale = if (batterySaver) 0.55f else 1f
                particle.y -= particle.speed * dt * speedScale
                particle.x += particle.drift * dt * speedScale

                if (particle.y < -10f) {
                    particle.y = height + 10f
                    particle.x = random.nextFloat() * width
                }
                if (particle.x < -10f) particle.x = width + 10f
                if (particle.x > width + 10f) particle.x = -10f
            }

            val twinkle = if (staticMode || batterySaver) {
                0.55
            } else {
                ((sin(
                    seconds * (1.0 + (index % 6) * 0.13) + particle.phase
                ) + 1.0) * 0.5)
            }

            val particleAlpha = (
                (55 + twinkle * 150) * qualityAlpha * batteryAlpha
            ).toInt().coerceIn(0, 255)

            particlePaint.color = Color.argb(
                particleAlpha,
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

        if (!staticMode) {
            postInvalidateDelayed(frameDelayMs())
        }
    }

    private fun frameDelayMs(): Long {
        if (batterySaver) return 33L
        return when (fpsPreference) {
            "30" -> 33L
            "60" -> 16L
            else -> 16L
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
        if (radius <= 1f || alpha <= 0) return

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
