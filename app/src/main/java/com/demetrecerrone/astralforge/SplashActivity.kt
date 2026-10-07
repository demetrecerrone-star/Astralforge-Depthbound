package com.demetrecerrone.astralforge

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import kotlin.math.PI
import kotlin.math.sin

class SplashActivity : Activity() {

    private var leaving = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AuthUi.setupWindow(this)

        val settings = runCatching {
            GameSettingsStore.load(this)
        }.getOrElse {
            GameSettingsStore.defaults()
        }
        val motionEnabled = !settings.reducedMotion && !settings.batterySaver

        val root = FrameLayout(this).apply {
            setBackgroundColor(Color.BLACK)
            isClickable = true
            isFocusable = true
        }

        root.addView(
            ImageView(this).apply {
                setImageResource(R.drawable.dabsky_splash)
                scaleType = ImageView.ScaleType.CENTER_CROP
                contentDescription = null
            },
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        runCatching {
            root.addView(
                SplashMotionView(
                    context = this,
                    motionEnabled = motionEnabled
                ),
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            )
        }

        root.setOnClickListener {
            leaveSplash(root, motionEnabled)
        }

        setContentView(root)

        root.postDelayed(
            {
                leaveSplash(root, motionEnabled)
            },
            if (motionEnabled) 3000L else 1500L
        )
    }

    private fun leaveSplash(root: View, animate: Boolean) {
        if (leaving) return
        leaving = true

        fun openLogin() {
            startActivity(
                Intent(
                    this@SplashActivity,
                    MainActivity::class.java
                )
            )
            finish()
        }

        if (!animate) {
            openLogin()
            return
        }

        root.animate()
            .alpha(0f)
            .setDuration(360L)
            .withEndAction { openLogin() }
            .start()
    }
}

private class SplashMotionView(
    context: Context,
    private val motionEnabled: Boolean
) : View(context) {

    private data class Smoke(
        val x: Float,
        val y: Float,
        val radius: Float,
        val speed: Float,
        val offset: Float,
        val alpha: Int
    )

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ripplePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val ripplePath = Path()
    private fun sinf(value: Float): Float =
        sin(value.toDouble()).toFloat()

    private var phase = 0f
    private var animator: ValueAnimator? = null

    private val smoke = listOf(
        Smoke(0.12f, 0.36f, 0.17f, 0.75f, 0.10f, 24),
        Smoke(0.25f, 0.25f, 0.15f, 0.58f, 0.42f, 20),
        Smoke(0.73f, 0.27f, 0.17f, 0.66f, 0.67f, 22),
        Smoke(0.88f, 0.39f, 0.16f, 0.72f, 0.84f, 24),
        Smoke(0.52f, 0.19f, 0.13f, 0.48f, 0.30f, 16)
    )

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)

        if (motionEnabled) {
            animator = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 12000L
                repeatCount = ValueAnimator.INFINITE
                interpolator = LinearInterpolator()
                addUpdateListener {
                    phase = it.animatedValue as Float
                    invalidate()
                }
                start()
            }
        } else {
            phase = 0.18f
        }
    }

    override fun onDetachedFromWindow() {
        animator?.cancel()
        animator = null
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return

        drawLogoGlow(canvas, w, h)
        drawSmoke(canvas, w, h)
        drawLightSweep(canvas, w, h)
        drawWaterShimmer(canvas, w, h)
        drawWaterRipples(canvas, w, h)
    }

    private fun drawLogoGlow(
        canvas: Canvas,
        w: Float,
        h: Float
    ) {
        val pulse = if (motionEnabled) {
            ((sinf(phase * PI.toFloat() * 4f) + 1f) * 0.5f)
        } else {
            0.45f
        }

        val cx = w * 0.50f
        val cy = h * 0.40f
        val radius = w * (0.18f + pulse * 0.015f)

        paint.style = Paint.Style.FILL
        paint.shader = RadialGradient(
            cx,
            cy,
            radius,
            intArrayOf(
                Color.argb((28 + pulse * 34).toInt(), 166, 72, 255),
                Color.argb((11 + pulse * 10).toInt(), 112, 46, 215),
                Color.TRANSPARENT
            ),
            floatArrayOf(0f, 0.48f, 1f),
            Shader.TileMode.CLAMP
        )

        canvas.drawCircle(cx, cy, radius, paint)
        paint.shader = null
    }

    private fun drawSmoke(
        canvas: Canvas,
        w: Float,
        h: Float
    ) {
        smoke.forEach { puff ->
            val drift = if (motionEnabled) {
                sinf(
                    (phase * puff.speed + puff.offset) *
                        PI.toFloat() * 2f
                )
            } else {
                0f
            }

            val lift = if (motionEnabled) {
                sinf(
                    (phase * puff.speed * 0.73f + puff.offset) *
                        PI.toFloat() * 2f
                )
            } else {
                0f
            }

            val cx = w * puff.x + drift * w * 0.035f
            val cy = h * puff.y - lift * h * 0.018f
            val radius = w * puff.radius

            paint.style = Paint.Style.FILL
            paint.shader = RadialGradient(
                cx,
                cy,
                radius,
                intArrayOf(
                    Color.argb(puff.alpha, 126, 61, 181),
                    Color.argb(puff.alpha / 2, 71, 31, 115),
                    Color.TRANSPARENT
                ),
                floatArrayOf(0f, 0.55f, 1f),
                Shader.TileMode.CLAMP
            )
            canvas.drawCircle(cx, cy, radius, paint)
        }

        paint.shader = null
    }

    private fun drawLightSweep(
        canvas: Canvas,
        w: Float,
        h: Float
    ) {
        val sweepProgress = if (motionEnabled) {
            (phase * 1.7f) % 1f
        } else {
            0.50f
        }

        val center = w * (-0.20f + sweepProgress * 1.40f)
        val spread = w * 0.10f

        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            center - spread,
            0f,
            center + spread,
            0f,
            intArrayOf(
                Color.TRANSPARENT,
                Color.argb(13, 225, 202, 255),
                Color.argb(30, 178, 100, 255),
                Color.argb(13, 225, 202, 255),
                Color.TRANSPARENT
            ),
            null,
            Shader.TileMode.CLAMP
        )

        canvas.drawRect(0f, 0f, w, h * 0.80f, paint)
        paint.shader = null
    }

    private fun drawWaterShimmer(
        canvas: Canvas,
        w: Float,
        h: Float
    ) {
        val top = h * 0.805f
        val progress = if (motionEnabled) {
            (phase * 2.1f) % 1f
        } else {
            0.48f
        }

        val center = w * (-0.10f + progress * 1.20f)
        val spread = w * 0.13f

        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            center - spread,
            top,
            center + spread,
            top,
            intArrayOf(
                Color.TRANSPARENT,
                Color.argb(12, 214, 184, 255),
                Color.argb(38, 157, 78, 255),
                Color.argb(12, 214, 184, 255),
                Color.TRANSPARENT
            ),
            null,
            Shader.TileMode.CLAMP
        )

        canvas.drawRect(
            0f,
            top,
            w,
            h,
            paint
        )

        paint.shader = null
    }

    private fun drawWaterRipples(
        canvas: Canvas,
        w: Float,
        h: Float
    ) {
        val density = resources.displayMetrics.density
        ripplePaint.strokeWidth = 1.15f * density

        val waterTop = h * 0.82f
        val twoPi = PI.toFloat() * 2f

        for (row in 0 until 7) {
            val baseY = waterTop + h * row * 0.023f
            val amplitude = density * (1.6f + row * 0.20f)
            val speed = 1.1f + row * 0.08f
            val rowPhase = if (motionEnabled) {
                phase * twoPi * speed
            } else {
                0.9f
            }

            ripplePath.reset()

            val segments = 64
            for (i in 0..segments) {
                val x = w * i / segments.toFloat()
                val wave =
                    sinf(
                        i * 0.46f +
                            rowPhase +
                            row * 0.75f
                    ) * amplitude

                val y = baseY + wave

                if (i == 0) {
                    ripplePath.moveTo(x, y)
                } else {
                    ripplePath.lineTo(x, y)
                }
            }

            ripplePaint.color = Color.argb(
                25 - row * 2,
                191,
                125,
                255
            )

            canvas.drawPath(ripplePath, ripplePaint)
        }
    }
}
