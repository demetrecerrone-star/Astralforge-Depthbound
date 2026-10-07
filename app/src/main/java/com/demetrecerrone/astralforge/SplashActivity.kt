package com.demetrecerrone.astralforge

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import kotlin.math.PI
import kotlin.math.sin

class SplashActivity : Activity() {

    private var leaving = false
    private var motionAnimator: ValueAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AuthUi.setupWindow(this)

        val settings = runCatching {
            GameSettingsStore.load(this)
        }.getOrElse {
            GameSettingsStore.defaults()
        }

        val motionEnabled =
            !settings.reducedMotion && !settings.batterySaver

        val width = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels

        val root = FrameLayout(this).apply {
            setBackgroundColor(Color.BLACK)
            isClickable = true
            isFocusable = true
        }

        val background = ImageView(this).apply {
            setImageResource(R.drawable.dabsky_splash)
            scaleType = ImageView.ScaleType.CENTER_CROP
            contentDescription = null
            scaleX = 1.03f
            scaleY = 1.03f
        }
        root.addView(
            background,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        val glow = gradientOrb(
            intArrayOf(
                Color.argb(115, 171, 82, 255),
                Color.argb(52, 104, 36, 196),
                Color.TRANSPARENT
            )
        )
        root.addView(
            glow,
            FrameLayout.LayoutParams(
                (width * 0.34f).toInt(),
                (height * 0.58f).toInt(),
                Gravity.CENTER
            ).apply {
                topMargin = -(height * 0.09f).toInt()
            }
        )

        val smokeLeft = gradientOrb(
            intArrayOf(
                Color.argb(42, 124, 62, 177),
                Color.argb(18, 74, 37, 116),
                Color.TRANSPARENT
            )
        )
        root.addView(
            smokeLeft,
            FrameLayout.LayoutParams(
                (width * 0.30f).toInt(),
                (height * 0.72f).toInt(),
                Gravity.START or Gravity.CENTER_VERTICAL
            ).apply {
                leftMargin = -(width * 0.08f).toInt()
            }
        )

        val smokeRight = gradientOrb(
            intArrayOf(
                Color.argb(40, 146, 68, 207),
                Color.argb(16, 78, 35, 125),
                Color.TRANSPARENT
            )
        )
        root.addView(
            smokeRight,
            FrameLayout.LayoutParams(
                (width * 0.30f).toInt(),
                (height * 0.72f).toInt(),
                Gravity.END or Gravity.CENTER_VERTICAL
            ).apply {
                rightMargin = -(width * 0.08f).toInt()
            }
        )

        val shimmer = View(this).apply {
            alpha = 0.0f
            background = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(
                    Color.TRANSPARENT,
                    Color.argb(18, 210, 185, 255),
                    Color.argb(72, 157, 77, 255),
                    Color.argb(18, 210, 185, 255),
                    Color.TRANSPARENT
                )
            )
        }
        root.addView(
            shimmer,
            FrameLayout.LayoutParams(
                (width * 0.34f).toInt(),
                (height * 0.22f).toInt(),
                Gravity.BOTTOM
            ).apply {
                bottomMargin = (height * 0.015f).toInt()
            }
        )

        val ripples = mutableListOf<View>()
        repeat(3) { index ->
            val line = View(this).apply {
                alpha = 0.20f - index * 0.04f
                background = GradientDrawable(
                    GradientDrawable.Orientation.LEFT_RIGHT,
                    intArrayOf(
                        Color.TRANSPARENT,
                        Color.argb(70 - index * 12, 184, 113, 255),
                        Color.argb(95 - index * 14, 223, 193, 255),
                        Color.argb(70 - index * 12, 184, 113, 255),
                        Color.TRANSPARENT
                    )
                ).apply {
                    cornerRadius = 4f
                }
            }
            root.addView(
                line,
                FrameLayout.LayoutParams(
                    (width * (0.52f - index * 0.06f)).toInt(),
                    (height * 0.004f).toInt().coerceAtLeast(2),
                    Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                ).apply {
                    bottomMargin =
                        (height * (0.105f - index * 0.026f)).toInt()
                }
            )
            ripples += line
        }

        root.setOnClickListener {
            leaveSplash(root, motionEnabled)
        }

        setContentView(root)
        FullscreenUi.apply(this)

        if (motionEnabled) {
            startMotion(
                width = width,
                height = height,
                background = background,
                glow = glow,
                smokeLeft = smokeLeft,
                smokeRight = smokeRight,
                shimmer = shimmer,
                ripples = ripples
            )
        }

        root.postDelayed(
            {
                leaveSplash(root, motionEnabled)
            },
            if (motionEnabled) 3200L else 1600L
        )
    }

    private fun gradientOrb(colors: IntArray): View =
        View(this).apply {
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                gradientType = GradientDrawable.RADIAL_GRADIENT
                this.colors = colors
                gradientRadius =
                    resources.displayMetrics.widthPixels * 0.18f
            }
        }

    private fun startMotion(
        width: Int,
        height: Int,
        background: View,
        glow: View,
        smokeLeft: View,
        smokeRight: View,
        shimmer: View,
        ripples: List<View>
    ) {
        val twoPi = (PI * 2.0).toFloat()

        motionAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 6200L
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()

            addUpdateListener { animator ->
                val t = animator.animatedValue as Float

                val slowWave =
                    sin((t * twoPi).toDouble()).toFloat()
                val pulse =
                    ((sin((t * twoPi * 2f).toDouble()).toFloat() + 1f) * 0.5f)

                background.scaleX = 1.035f + pulse * 0.010f
                background.scaleY = 1.035f + pulse * 0.010f
                background.translationX =
                    width * 0.006f * slowWave
                background.translationY =
                    height * 0.004f *
                        sin((t * twoPi + 1.2f).toDouble()).toFloat()

                glow.alpha = 0.22f + pulse * 0.28f
                glow.scaleX = 0.92f + pulse * 0.15f
                glow.scaleY = 0.92f + pulse * 0.15f

                smokeLeft.translationX =
                    width * 0.045f * slowWave
                smokeLeft.translationY =
                    height * 0.018f *
                        sin((t * twoPi + 0.7f).toDouble()).toFloat()
                smokeLeft.alpha = 0.32f + pulse * 0.12f

                smokeRight.translationX =
                    -width * 0.042f *
                        sin((t * twoPi + 1.1f).toDouble()).toFloat()
                smokeRight.translationY =
                    height * 0.016f *
                        sin((t * twoPi + 2.0f).toDouble()).toFloat()
                smokeRight.alpha = 0.30f + (1f - pulse) * 0.12f

                shimmer.translationX =
                    -width * 0.58f + width * 1.55f * t
                shimmer.alpha = 0.18f + pulse * 0.32f

                ripples.forEachIndexed { index, ripple ->
                    val offset = index * 0.8f
                    val wave =
                        sin((t * twoPi * 1.35f + offset).toDouble()).toFloat()
                    ripple.translationX =
                        width * 0.016f * wave
                    ripple.scaleX =
                        0.88f + ((wave + 1f) * 0.5f) * 0.16f
                    ripple.alpha =
                        0.12f + ((wave + 1f) * 0.5f) * 0.18f
                }
            }

            start()
        }
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
            .setDuration(320L)
            .withEndAction { openLogin() }
            .start()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            FullscreenUi.apply(this)
        }
    }

    override fun onDestroy() {
        motionAnimator?.cancel()
        motionAnimator = null
        super.onDestroy()
    }
}
