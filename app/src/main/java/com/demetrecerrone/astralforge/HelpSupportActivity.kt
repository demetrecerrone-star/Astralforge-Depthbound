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

class HelpSupportActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AuthUi.setupWindow(this)

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
                        Color.argb(165, 3, 4, 18),
                        Color.argb(205, 4, 5, 22),
                        Color.argb(232, 3, 4, 16)
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
                AuthUi.dp(this@HelpSupportActivity, 24),
                AuthUi.dp(this@HelpSupportActivity, 30),
                AuthUi.dp(this@HelpSupportActivity, 24),
                AuthUi.dp(this@HelpSupportActivity, 34)
            )
        }

        content.addView(
            TextView(this).apply {
                text = "HELP & SUPPORT"
                textSize = 29f
                setTextColor(Color.WHITE)
                typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                gravity = Gravity.CENTER
                letterSpacing = 0.07f
                setShadowLayer(9f, 0f, 0f, Color.rgb(124, 76, 255))
            }
        )

        content.addView(
            TextView(this).apply {
                text = "Depthbound Field Guide"
                textSize = 11f
                setTextColor(Color.rgb(230, 196, 125))
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                letterSpacing = 0.08f
                setPadding(
                    0,
                    AuthUi.dp(this@HelpSupportActivity, 3),
                    0,
                    AuthUi.dp(this@HelpSupportActivity, 4)
                )
            }
        )

        content.addView(
            TextView(this).apply {
                text =
                    "Gameplay guidance, account help, troubleshooting, and ways to report problems."
                textSize = 10.5f
                setTextColor(Color.rgb(202, 191, 229))
                gravity = Gravity.CENTER
                setPadding(
                    AuthUi.dp(this@HelpSupportActivity, 18),
                    0,
                    AuthUi.dp(this@HelpSupportActivity, 18),
                    AuthUi.dp(this@HelpSupportActivity, 13)
                )
            }
        )

        val guidePanel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                AuthUi.dp(this@HelpSupportActivity, 9),
                AuthUi.dp(this@HelpSupportActivity, 9),
                AuthUi.dp(this@HelpSupportActivity, 9),
                AuthUi.dp(this@HelpSupportActivity, 9)
            )
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(
                    Color.argb(238, 31, 14, 68),
                    Color.argb(242, 7, 8, 29)
                )
            ).apply {
                cornerRadius = AuthUi.dp(this@HelpSupportActivity, 15).toFloat()
                setStroke(
                    AuthUi.dp(this@HelpSupportActivity, 1),
                    Color.rgb(218, 174, 94)
                )
            }
        }

        guidePanel.addView(
            helpCard(
                icon = "✦",
                title = "GETTING STARTED",
                accent = Color.rgb(184, 125, 255),
                body =
                    "Use the Hub as your main base. Open Battle from the bottom bar, " +
                    "use Stats to spend earned attribute points, and use the top-right " +
                    "buttons for Daily Reward, Events, Account, Settings, and support tools."
            )
        )

        guidePanel.addView(
            helpCard(
                icon = "◇",
                title = "CONTROLS & NAVIGATION",
                accent = Color.rgb(103, 176, 255),
                body =
                    "Tap highlighted buttons and location labels to navigate. " +
                    "Android Back returns to the previous screen. The game is designed " +
                    "for landscape play, and interactive buttons provide press feedback."
            )
        )

        guidePanel.addView(
            helpCard(
                icon = "◈",
                title = "ACCOUNT & LOGIN",
                accent = Color.rgb(183, 141, 255),
                body =
                    "Player Account shows your player name, signed-in email, account type, " +
                    "and account ID. If the wrong character data appears after switching " +
                    "accounts, sign out completely and sign back into the intended account."
            )
        )

        guidePanel.addView(
            helpCard(
                icon = "⚔",
                title = "BATTLE & PROGRESSION",
                accent = Color.rgb(237, 154, 91),
                body =
                    "Battle is where combat encounters and progression systems live. " +
                    "Your Stats page tracks attributes and available points. More dungeon, " +
                    "gear, summon, guild, and hero systems are being connected as development continues."
            )
        )

        guidePanel.addView(
            helpCard(
                icon = "⚙",
                title = "PERFORMANCE & AUDIO",
                accent = Color.rgb(99, 212, 192),
                body =
                    "Open Settings for music, sound, haptics, visual quality, particle density, " +
                    "frame-rate preference, reduced motion, and battery-saving options. " +
                    "If animation feels heavy, lower visual quality or enable reduced motion."
            )
        )

        guidePanel.addView(
            helpCard(
                icon = "!",
                title = "KNOWN ISSUES",
                accent = Color.rgb(242, 199, 94),
                body =
                    "This is an active development build. Some destination systems are still " +
                    "placeholders, Daily Reward and Events currently show Coming Soon, and " +
                    "visual placement may continue to be tuned as the hub is rebuilt."
            )
        )

        guidePanel.addView(
            helpCard(
                icon = "?",
                title = "REPORTING A BUG",
                accent = Color.rgb(224, 92, 122),
                body =
                    "When reporting a bug, include what screen you were on, what you tapped, " +
                    "what you expected to happen, what actually happened, and whether the issue " +
                    "happens every time. Screenshots are especially useful for layout problems."
            )
        )

        content.addView(
            guidePanel,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        val bugButton = TextView(this).apply {
            text = "OPEN BUG TRACKER"
            textSize = 11f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            letterSpacing = 0.06f
            isClickable = true
            isFocusable = true
            background = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(
                    Color.rgb(68, 30, 112),
                    Color.rgb(29, 18, 67)
                )
            ).apply {
                cornerRadius = AuthUi.dp(this@HelpSupportActivity, 11).toFloat()
                setStroke(
                    AuthUi.dp(this@HelpSupportActivity, 1),
                    Color.rgb(220, 176, 93)
                )
            }
            installTouchFeedback()
            setOnClickListener {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(
                            "https://github.com/demetrecerrone-star/" +
                                "Astralforge-Depthbound/issues/new"
                        )
                    )
                )
            }
        }

        content.addView(
            bugButton,
            LinearLayout.LayoutParams(
                AuthUi.dp(this, 210),
                AuthUi.dp(this, 42)
            ).apply {
                topMargin = AuthUi.dp(this@HelpSupportActivity, 12)
            }
        )

        content.addView(
            TextView(this).apply {
                text = "Use Android Back to return to the hub."
                textSize = 9f
                setTextColor(Color.rgb(160, 148, 190))
                gravity = Gravity.CENTER
                setPadding(
                    0,
                    AuthUi.dp(this@HelpSupportActivity, 8),
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
                AuthUi.dp(this@HelpSupportActivity, 24),
                insets.systemWindowInsetTop + AuthUi.dp(this@HelpSupportActivity, 18),
                AuthUi.dp(this@HelpSupportActivity, 24),
                insets.systemWindowInsetBottom + AuthUi.dp(this@HelpSupportActivity, 26)
            )
            insets
        }
        root.requestApplyInsets()

        setContentView(root)
    }

    private fun helpCard(
        icon: String,
        title: String,
        accent: Int,
        body: String
    ): LinearLayout {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                setColor(Color.argb(226, 9, 9, 33))
                cornerRadius = AuthUi.dp(this@HelpSupportActivity, 10).toFloat()
                setStroke(
                    AuthUi.dp(this@HelpSupportActivity, 1),
                    Color.argb(210, Color.red(accent), Color.green(accent), Color.blue(accent))
                )
            }
        }

        val details = TextView(this).apply {
            text = body
            textSize = 10f
            setTextColor(Color.rgb(209, 199, 231))
            setPadding(
                AuthUi.dp(this@HelpSupportActivity, 14),
                0,
                AuthUi.dp(this@HelpSupportActivity, 14),
                AuthUi.dp(this@HelpSupportActivity, 11)
            )
            visibility = View.GONE
        }

        val arrow = TextView(this).apply {
            text = "+"
            textSize = 18f
            setTextColor(accent)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            isClickable = true
            isFocusable = true
            contentDescription = title
            setPadding(
                AuthUi.dp(this@HelpSupportActivity, 10),
                AuthUi.dp(this@HelpSupportActivity, 8),
                AuthUi.dp(this@HelpSupportActivity, 10),
                AuthUi.dp(this@HelpSupportActivity, 8)
            )
            installTouchFeedback()

            addView(
                TextView(this@HelpSupportActivity).apply {
                    text = icon
                    textSize = 18f
                    setTextColor(accent)
                    typeface = Typeface.DEFAULT_BOLD
                    gravity = Gravity.CENTER
                    setShadowLayer(6f, 0f, 0f, accent)
                },
                LinearLayout.LayoutParams(
                    AuthUi.dp(this@HelpSupportActivity, 36),
                    AuthUi.dp(this@HelpSupportActivity, 34)
                )
            )

            addView(
                TextView(this@HelpSupportActivity).apply {
                    text = title
                    textSize = 11f
                    setTextColor(Color.WHITE)
                    typeface = Typeface.DEFAULT_BOLD
                    letterSpacing = 0.04f
                },
                LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )

            addView(
                arrow,
                LinearLayout.LayoutParams(
                    AuthUi.dp(this@HelpSupportActivity, 32),
                    AuthUi.dp(this@HelpSupportActivity, 32)
                )
            )

            setOnClickListener {
                val opening = details.visibility != View.VISIBLE
                details.visibility = if (opening) View.VISIBLE else View.GONE
                arrow.text = if (opening) "−" else "+"
            }
        }

        card.addView(header)
        card.addView(details)
        card.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = AuthUi.dp(this@HelpSupportActivity, 7)
        }

        return card
    }

    private fun View.installTouchFeedback() {
        setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    AppHaptics.tap(this@HelpSupportActivity)
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
