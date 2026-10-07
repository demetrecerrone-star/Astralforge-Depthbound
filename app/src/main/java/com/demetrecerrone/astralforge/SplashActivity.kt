package com.demetrecerrone.astralforge

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.TextView
import kotlin.math.PI
import kotlin.math.sin

class SplashActivity : Activity() {

    private var leaving = false
    private var animator: ValueAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
            isClickable = true
            isFocusable = true
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(
                    Color.rgb(3, 3, 12),
                    Color.rgb(11, 5, 24),
                    Color.rgb(22, 7, 43),
                    Color.rgb(6, 4, 16)
                )
            )
        }

        val skyGlow = orb(
            intArrayOf(
                Color.argb(120, 131, 55, 230),
                Color.argb(55, 84, 25, 150),
                Color.TRANSPARENT
            ),
            width * 0.25f
        )
        root.addView(
            skyGlow,
            FrameLayout.LayoutParams(
                (width * 0.48f).toInt(),
                (height * 0.78f).toInt(),
                Gravity.TOP or Gravity.CENTER_HORIZONTAL
            ).apply {
                topMargin = -(height * 0.20f).toInt()
            }
        )

        val leftHaze = orb(
            intArrayOf(
                Color.argb(50, 99, 46, 145),
                Color.argb(22, 49, 19, 79),
                Color.TRANSPARENT
            ),
            width * 0.18f
        )
        root.addView(
            leftHaze,
            FrameLayout.LayoutParams(
                (width * 0.36f).toInt(),
                (height * 0.80f).toInt(),
                Gravity.START or Gravity.CENTER_VERTICAL
            ).apply {
                leftMargin = -(width * 0.11f).toInt()
            }
        )

        val rightHaze = orb(
            intArrayOf(
                Color.argb(52, 122, 55, 171),
                Color.argb(22, 58, 21, 91),
                Color.TRANSPARENT
            ),
            width * 0.18f
        )
        root.addView(
            rightHaze,
            FrameLayout.LayoutParams(
                (width * 0.36f).toInt(),
                (height * 0.80f).toInt(),
                Gravity.END or Gravity.CENTER_VERTICAL
            ).apply {
                rightMargin = -(width * 0.11f).toInt()
            }
        )

        repeat(10) { index ->
            val star = View(this).apply {
                alpha = 0.35f + (index % 4) * 0.10f
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(
                        if (index % 3 == 0) {
                            Color.rgb(190, 150, 255)
                        } else {
                            Color.rgb(225, 215, 255)
                        }
                    )
                }
            }

            val starSize =
                (height * (0.004f + (index % 3) * 0.002f))
                    .toInt()
                    .coerceAtLeast(2)

            root.addView(
                star,
                FrameLayout.LayoutParams(
                    starSize,
                    starSize,
                    Gravity.TOP or Gravity.START
                ).apply {
                    leftMargin =
                        (width * (0.08f + index * 0.085f)).toInt()
                    topMargin =
                        (height * (0.08f + (index % 5) * 0.075f))
                            .toInt()
                }
            )
        }

        val emblemGlow = orb(
            intArrayOf(
                Color.argb(165, 170, 80, 255),
                Color.argb(55, 105, 35, 190),
                Color.TRANSPARENT
            ),
            width * 0.12f
        )
        root.addView(
            emblemGlow,
            FrameLayout.LayoutParams(
                (width * 0.25f).toInt(),
                (height * 0.42f).toInt(),
                Gravity.CENTER
            ).apply {
                topMargin = -(height * 0.08f).toInt()
            }
        )

        val sigil = TextView(this).apply {
            text = "✦"
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(218, 188, 255))
            textSize = 72f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            includeFontPadding = false
            setShadowLayer(
                24f,
                0f,
                0f,
                Color.rgb(157, 76, 255)
            )
        }
        root.addView(
            sigil,
            FrameLayout.LayoutParams(
                (width * 0.17f).toInt(),
                (height * 0.24f).toInt(),
                Gravity.CENTER
            ).apply {
                topMargin = -(height * 0.13f).toInt()
            }
        )

        val brand = TextView(this).apply {
            text = "DABSKY"
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(238, 228, 255))
            textSize = 40f
            letterSpacing = 0.16f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            includeFontPadding = false
            setShadowLayer(
                20f,
                0f,
                0f,
                Color.rgb(137, 60, 230)
            )
        }
        root.addView(
            brand,
            FrameLayout.LayoutParams(
                (width * 0.46f).toInt(),
                (height * 0.15f).toInt(),
                Gravity.CENTER
            ).apply {
                topMargin = (height * 0.14f).toInt()
            }
        )

        val subtitle = TextView(this).apply {
            text = "PRESENTS"
            gravity = Gravity.CENTER
            setTextColor(Color.argb(180, 201, 183, 231))
            textSize = 12f
            letterSpacing = 0.32f
            includeFontPadding = false
        }
        root.addView(
            subtitle,
            FrameLayout.LayoutParams(
                (width * 0.34f).toInt(),
                (height * 0.07f).toInt(),
                Gravity.CENTER
            ).apply {
                topMargin = (height * 0.27f).toInt()
            }
        )

        val horizon = View(this).apply {
            alpha = 0.36f
            background = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(
                    Color.TRANSPARENT,
                    Color.argb(125, 121, 60, 210),
                    Color.argb(190, 204, 169, 255),
                    Color.argb(125, 121, 60, 210),
                    Color.TRANSPARENT
                )
            )
        }
        root.addView(
            horizon,
            FrameLayout.LayoutParams(
                (width * 0.78f).toInt(),
                (height * 0.004f).toInt().coerceAtLeast(2),
                Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            ).apply {
                bottomMargin = (height * 0.24f).toInt()
            }
        )

        val shimmer = View(this).apply {
            alpha = 0.0f
            background = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(
                    Color.TRANSPARENT,
                    Color.argb(18, 205, 180, 255),
                    Color.argb(90, 148, 72, 255),
                    Color.argb(18, 205, 180, 255),
                    Color.TRANSPARENT
                )
            )
        }
        root.addView(
            shimmer,
            FrameLayout.LayoutParams(
                (width * 0.32f).toInt(),
                (height * 0.15f).toInt(),
                Gravity.BOTTOM
            ).apply {
                bottomMargin = (height * 0.045f).toInt()
            }
        )

        val reflection = TextView(this).apply {
            text = "DABSKY"
            gravity = Gravity.CENTER
            setTextColor(Color.argb(48, 182, 122, 255))
            textSize = 26f
            letterSpacing = 0.16f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            includeFontPadding = false
            scaleY = -1f
            alpha = 0.22f
        }
        root.addView(
            reflection,
            FrameLayout.LayoutParams(
                (width * 0.38f).toInt(),
                (height * 0.11f).toInt(),
                Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            ).apply {
                bottomMargin = (height * 0.08f).toInt()
            }
        )

        val ripples = mutableListOf<View>()
        repeat(5) { index ->
            val ripple = View(this).apply {
                alpha = 0.16f
                background = GradientDrawable(
                    GradientDrawable.Orientation.LEFT_RIGHT,
                    intArrayOf(
                        Color.TRANSPARENT,
                        Color.argb(
                            72 - index * 8,
                            164,
                            98,
                            236
                        ),
                        Color.argb(
                            98 - index * 10,
                            221,
                            193,
                            255
                        ),
                        Color.argb(
                            72 - index * 8,
                            164,
                            98,
                            236
                        ),
                        Color.TRANSPARENT
                    )
                ).apply {
                    cornerRadius = 4f
                }
            }

            root.addView(
                ripple,
                FrameLayout.LayoutParams(
                    (width * (0.56f - index * 0.055f)).toInt(),
                    (height * 0.004f).toInt().coerceAtLeast(2),
                    Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                ).apply {
                    bottomMargin =
                        (height * (0.19f - index * 0.028f)).toInt()
                }
            )

            ripples += ripple
        }

        root.setOnClickListener {
            leaveSplash(root, motionEnabled)
        }

        setContentView(root)
        FullscreenUi.apply(this)

        if (motionEnabled) {
            startAnimation(
                width = width,
                height = height,
                skyGlow = skyGlow,
                leftHaze = leftHaze,
                rightHaze = rightHaze,
                emblemGlow = emblemGlow,
                sigil = sigil,
                brand = brand,
                shimmer = shimmer,
                reflection = reflection,
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

    private fun orb(
        colors: IntArray,
        radius: Float
    ): View = View(this).apply {
        background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            gradientType = GradientDrawable.RADIAL_GRADIENT
            this.colors = colors
            gradientRadius = radius
        }
    }

    private fun startAnimation(
        width: Int,
        height: Int,
        skyGlow: View,
        leftHaze: View,
        rightHaze: View,
        emblemGlow: View,
        sigil: View,
        brand: View,
        shimmer: View,
        reflection: View,
        ripples: List<View>
    ) {
        val twoPi = (PI * 2.0).toFloat()

        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 6200L
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()

            addUpdateListener { valueAnimator ->
                val t = valueAnimator.animatedValue as Float

                val wave =
                    sin((t * twoPi).toDouble()).toFloat()
                val pulse =
                    (
                        sin((t * twoPi * 2f).toDouble()).toFloat() +
                            1f
                    ) * 0.5f

                skyGlow.alpha = 0.58f + pulse * 0.22f
                skyGlow.scaleX = 0.96f + pulse * 0.07f
                skyGlow.scaleY = 0.96f + pulse * 0.07f

                leftHaze.translationX = width * 0.04f * wave
                leftHaze.translationY =
                    height * 0.016f *
                        sin((t * twoPi + 0.8f).toDouble()).toFloat()
                leftHaze.alpha = 0.58f + pulse * 0.12f

                rightHaze.translationX =
                    -width * 0.04f *
                        sin((t * twoPi + 1.3f).toDouble()).toFloat()
                rightHaze.translationY =
                    height * 0.015f *
                        sin((t * twoPi + 2.1f).toDouble()).toFloat()
                rightHaze.alpha =
                    0.56f + (1f - pulse) * 0.12f

                emblemGlow.alpha = 0.60f + pulse * 0.28f
                emblemGlow.scaleX = 0.90f + pulse * 0.18f
                emblemGlow.scaleY = 0.90f + pulse * 0.18f

                sigil.rotation = wave * 1.5f
                sigil.scaleX = 0.98f + pulse * 0.04f
                sigil.scaleY = 0.98f + pulse * 0.04f

                brand.translationY =
                    height * 0.004f *
                        sin((t * twoPi + 0.3f).toDouble()).toFloat()

                shimmer.translationX =
                    -width * 0.60f + width * 1.60f * t
                shimmer.alpha = 0.16f + pulse * 0.26f

                reflection.alpha = 0.14f + pulse * 0.12f

                ripples.forEachIndexed { index, ripple ->
                    val rippleWave =
                        sin(
                            (
                                t * twoPi * 1.25f +
                                    index * 0.72f
                                ).toDouble()
                        ).toFloat()

                    ripple.translationX =
                        width * 0.014f * rippleWave
                    ripple.scaleX =
                        0.90f +
                            ((rippleWave + 1f) * 0.5f) *
                            0.18f
                    ripple.alpha =
                        0.10f +
                            ((rippleWave + 1f) * 0.5f) *
                            0.18f
                }
            }

            start()
        }
    }

    private fun leaveSplash(
        root: View,
        animate: Boolean
    ) {
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
        animator?.cancel()
        animator = null
        super.onDestroy()
    }
}
