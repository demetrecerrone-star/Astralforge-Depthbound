package com.demetrecerrone.astralforge

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class AboutActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AuthUi.setupWindow(this)

        val versionName = runCatching {
            packageManager
                .getPackageInfo(packageName, 0)
                .versionName
                .orEmpty()
        }.getOrDefault("Development Build")

        val root = FrameLayout(this)

        root.addView(
            ImageView(this).apply {
                setImageResource(R.drawable.hub_background_rebuild)
                scaleType = ImageView.ScaleType.CENTER_CROP
                alpha = 0.72f
                contentDescription = null
            },
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        root.addView(
            View(this).apply {
                background = GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    intArrayOf(
                        Color.argb(160, 3, 4, 18),
                        Color.argb(210, 4, 5, 22),
                        Color.argb(235, 3, 4, 16)
                    )
                )
            },
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        val scroll = ScrollView(this).apply {
            overScrollMode = View.OVER_SCROLL_NEVER
            isFillViewport = true
        }

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(
                AuthUi.dp(this@AboutActivity, 24),
                AuthUi.dp(this@AboutActivity, 28),
                AuthUi.dp(this@AboutActivity, 24),
                AuthUi.dp(this@AboutActivity, 34)
            )
        }

        content.addView(
            TextView(this).apply {
                text = "ASTRAL FORGE: DEPTHBOUND"
                textSize = 29f
                setTextColor(Color.WHITE)
                typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                gravity = Gravity.CENTER
                letterSpacing = 0.06f
                setShadowLayer(10f, 0f, 0f, Color.rgb(123, 76, 255))
            }
        )

        content.addView(
            TextView(this).apply {
                text = "ABOUT"
                textSize = 10f
                setTextColor(Color.rgb(230, 196, 125))
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                letterSpacing = 0.12f
                setPadding(
                    0,
                    AuthUi.dp(this@AboutActivity, 3),
                    0,
                    AuthUi.dp(this@AboutActivity, 10)
                )
            }
        )

        val infoPanel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                AuthUi.dp(this@AboutActivity, 16),
                AuthUi.dp(this@AboutActivity, 14),
                AuthUi.dp(this@AboutActivity, 16),
                AuthUi.dp(this@AboutActivity, 14)
            )
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(
                    Color.argb(239, 31, 14, 68),
                    Color.argb(244, 7, 8, 29)
                )
            ).apply {
                cornerRadius = AuthUi.dp(this@AboutActivity, 15).toFloat()
                setStroke(
                    AuthUi.dp(this@AboutActivity, 1),
                    Color.rgb(218, 174, 94)
                )
            }
        }

        infoPanel.addView(sectionTitle("GAME"))
        infoPanel.addView(bodyText(
            "Astral Forge: Depthbound is a dark celestial fantasy RPG built around " +
                "character progression, dungeon combat, heroes, summons, guilds, forging, " +
                "gear, and long-term account progression."
        ))

        infoPanel.addView(detailRow("VERSION", versionName))
        infoPanel.addView(detailRow("STATUS", "Active Development Build"))
        infoPanel.addView(detailRow("PLATFORM", "Android • Landscape"))

        infoPanel.addView(sectionTitle("CREDITS"))
        infoPanel.addView(bodyText("Created and directed by Dabski"))
        infoPanel.addView(bodyText("Development assistance: ChatGPT by OpenAI"))
        infoPanel.addView(bodyText(
            "Additional art, interface, gameplay systems, and production assets are " +
                "being developed specifically for Astral Forge: Depthbound."
        ))

        infoPanel.addView(sectionTitle("PROJECT"))
        infoPanel.addView(bodyText(
            "Depthbound is currently under active development. Features, balance, " +
                "artwork, interface layout, and systems may change as the game evolves."
        ))

        content.addView(
            infoPanel,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        val actions = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                0,
                AuthUi.dp(this@AboutActivity, 12),
                0,
                0
            )
        }

        actions.addView(actionButton(
            "UPDATE LOG",
            "Read the latest development changes.",
            Color.rgb(101, 221, 192)
        ) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(
                        "https://github.com/demetrecerrone-star/" +
                            "Astralforge-Depthbound#update-log"
                    )
                )
            )
        })

        actions.addView(actionButton(
            "HELP & SUPPORT",
            "Open the Depthbound Field Guide and support center.",
            Color.rgb(245, 201, 105)
        ) {
            startActivity(
                Intent(
                    this@AboutActivity,
                    HelpSupportActivity::class.java
                )
            )
        })

        actions.addView(actionButton(
            "PROJECT REPOSITORY",
            "View the Astral Forge: Depthbound project on GitHub.",
            Color.rgb(167, 120, 255)
        ) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(
                        "https://github.com/demetrecerrone-star/" +
                            "Astralforge-Depthbound"
                    )
                )
            )
        })

        actions.addView(actionButton(
            "PRIVACY POLICY",
            "Read how account, local game, and service data are handled.",
            Color.rgb(106, 171, 255)
        ) {
            startActivity(
                Intent(
                    this@AboutActivity,
                    LegalActivity::class.java
                ).putExtra(
                    LegalActivity.EXTRA_PAGE,
                    LegalActivity.PAGE_PRIVACY
                )
            )
        })

        actions.addView(actionButton(
            "TERMS & LEGAL",
            "Read the development-build terms, acceptable use, and legal notices.",
            Color.rgb(206, 151, 255)
        ) {
            startActivity(
                Intent(
                    this@AboutActivity,
                    LegalActivity::class.java
                ).putExtra(
                    LegalActivity.EXTRA_PAGE,
                    LegalActivity.PAGE_TERMS
                )
            )
        })

        content.addView(
            actions,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        content.addView(
            TextView(this).apply {
                text = "Use Android Back to return."
                textSize = 9f
                setTextColor(Color.rgb(160, 148, 190))
                gravity = Gravity.CENTER
                setPadding(
                    0,
                    AuthUi.dp(this@AboutActivity, 8),
                    0,
                    0
                )
            }
        )

        scroll.addView(
            content,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        )

        root.addView(
            scroll,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        root.setOnApplyWindowInsetsListener { _, insets: WindowInsets ->
            content.setPadding(
                AuthUi.dp(this@AboutActivity, 24),
                insets.systemWindowInsetTop + AuthUi.dp(this@AboutActivity, 18),
                AuthUi.dp(this@AboutActivity, 24),
                insets.systemWindowInsetBottom + AuthUi.dp(this@AboutActivity, 26)
            )
            insets
        }
        root.requestApplyInsets()

        setContentView(root)
    }

    private fun sectionTitle(textValue: String): TextView {
        return TextView(this).apply {
            text = textValue
            textSize = 10f
            setTextColor(Color.rgb(230, 196, 125))
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.10f
            setPadding(
                0,
                AuthUi.dp(this@AboutActivity, 7),
                0,
                AuthUi.dp(this@AboutActivity, 5)
            )
        }
    }

    private fun bodyText(textValue: String): TextView {
        return TextView(this).apply {
            text = textValue
            textSize = 10.3f
            setTextColor(Color.rgb(211, 201, 234))
            setPadding(
                0,
                0,
                0,
                AuthUi.dp(this@AboutActivity, 7)
            )
        }
    }

    private fun detailRow(label: String, value: String): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(
                0,
                AuthUi.dp(this@AboutActivity, 4),
                0,
                AuthUi.dp(this@AboutActivity, 4)
            )

            addView(
                TextView(this@AboutActivity).apply {
                    text = label
                    textSize = 10f
                    setTextColor(Color.rgb(166, 153, 204))
                    typeface = Typeface.DEFAULT_BOLD
                },
                LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )

            addView(
                TextView(this@AboutActivity).apply {
                    text = value
                    textSize = 10.5f
                    setTextColor(Color.WHITE)
                    gravity = Gravity.END
                }
            )
        }
    }

    private fun actionButton(
        title: String,
        subtitle: String,
        accent: Int,
        onClick: () -> Unit
    ): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            isClickable = true
            isFocusable = true
            setPadding(
                AuthUi.dp(this@AboutActivity, 15),
                AuthUi.dp(this@AboutActivity, 10),
                AuthUi.dp(this@AboutActivity, 15),
                AuthUi.dp(this@AboutActivity, 10)
            )
            background = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(
                    Color.argb(225, 17, 11, 49),
                    Color.argb(228, 7, 8, 29)
                )
            ).apply {
                cornerRadius = AuthUi.dp(this@AboutActivity, 11).toFloat()
                setStroke(
                    AuthUi.dp(this@AboutActivity, 1),
                    accent
                )
            }

            addView(TextView(this@AboutActivity).apply {
                text = title
                textSize = 11f
                setTextColor(Color.WHITE)
                typeface = Typeface.DEFAULT_BOLD
                letterSpacing = 0.05f
            })

            addView(TextView(this@AboutActivity).apply {
                text = subtitle
                textSize = 8.8f
                setTextColor(Color.rgb(181, 169, 211))
                setPadding(
                    0,
                    AuthUi.dp(this@AboutActivity, 2),
                    0,
                    0
                )
            })

            installTouchFeedback()
            setOnClickListener { onClick() }

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = AuthUi.dp(this@AboutActivity, 7)
            }
        }
    }

    private fun showComingSoon(root: FrameLayout, title: String) {
        root.findViewWithTag<View>("about_coming_soon")?.let {
            root.removeView(it)
        }

        val panel = LinearLayout(this).apply {
            tag = "about_coming_soon"
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            elevation = AuthUi.dp(this@AboutActivity, 18).toFloat()
            setPadding(
                AuthUi.dp(this@AboutActivity, 18),
                AuthUi.dp(this@AboutActivity, 14),
                AuthUi.dp(this@AboutActivity, 18),
                AuthUi.dp(this@AboutActivity, 14)
            )
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(
                    Color.argb(250, 34, 15, 73),
                    Color.argb(252, 7, 7, 28)
                )
            ).apply {
                cornerRadius = AuthUi.dp(this@AboutActivity, 14).toFloat()
                setStroke(
                    AuthUi.dp(this@AboutActivity, 1),
                    Color.rgb(220, 176, 93)
                )
            }

            addView(TextView(this@AboutActivity).apply {
                text = title
                textSize = 11f
                setTextColor(Color.rgb(230, 196, 125))
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
            })

            addView(TextView(this@AboutActivity).apply {
                text = "COMING SOON"
                textSize = 17f
                setTextColor(Color.WHITE)
                typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                gravity = Gravity.CENTER
                setPadding(
                    0,
                    AuthUi.dp(this@AboutActivity, 4),
                    0,
                    AuthUi.dp(this@AboutActivity, 8)
                )
            })

            addView(TextView(this@AboutActivity).apply {
                text = "CLOSE"
                textSize = 10f
                setTextColor(Color.WHITE)
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                isClickable = true
                isFocusable = true
                background = GradientDrawable().apply {
                    setColor(Color.argb(220, 15, 11, 44))
                    cornerRadius = AuthUi.dp(this@AboutActivity, 9).toFloat()
                    setStroke(
                        AuthUi.dp(this@AboutActivity, 1),
                        Color.rgb(132, 95, 210)
                    )
                }
                installTouchFeedback()
                setOnClickListener {
                    root.findViewWithTag<View>("about_coming_soon")?.let {
                        root.removeView(it)
                    }
                }
            }, LinearLayout.LayoutParams(
                AuthUi.dp(this@AboutActivity, 110),
                AuthUi.dp(this@AboutActivity, 36)
            ))
        }

        root.addView(
            panel,
            FrameLayout.LayoutParams(
                (resources.displayMetrics.widthPixels * 0.34f).toInt(),
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
            )
        )
    }

    private fun View.installTouchFeedback() {
        setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    AppHaptics.tap(this@AboutActivity)
                    view.animate()
                        .scaleX(0.985f)
                        .scaleY(0.985f)
                        .alpha(0.84f)
                        .setDuration(65)
                        .start()
                }
                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> {
                    view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .alpha(1f)
                        .setDuration(100)
                        .start()
                }
            }
            false
        }
    }
}
