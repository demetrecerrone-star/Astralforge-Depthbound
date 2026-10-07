package com.demetrecerrone.astralforge

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
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
import com.google.firebase.auth.FirebaseAuth

class MenuActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AuthUi.setupWindow(this)

        val root = FrameLayout(this)

        root.addView(
            ImageView(this).apply {
                setImageResource(R.drawable.file_00000000a45881f5ad657cc79abf1338)
                scaleType = ImageView.ScaleType.CENTER_CROP
                alpha = 0.78f
                contentDescription = null
            },
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        root.addView(
            View(this).apply {
                setBackgroundColor(Color.argb(178, 2, 4, 16))
            },
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        val scroll = ScrollView(this).apply {
            overScrollMode = View.OVER_SCROLL_NEVER
        }

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(
                AuthUi.dp(this@MenuActivity, 20),
                AuthUi.dp(this@MenuActivity, 52),
                AuthUi.dp(this@MenuActivity, 20),
                AuthUi.dp(this@MenuActivity, 34)
            )
        }

        content.addView(TextView(this).apply {
            text = "MENU"
            textSize = 30f
            setTextColor(Color.WHITE)
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            gravity = Gravity.CENTER
            letterSpacing = 0.08f
            setShadowLayer(7f, 0f, 0f, Color.rgb(111, 79, 255))
        })

        content.addView(TextView(this).apply {
            text = "Astral Forge: Depthbound"
            textSize = 13f
            setTextColor(Color.rgb(187, 176, 222))
            gravity = Gravity.CENTER
            setPadding(
                0,
                AuthUi.dp(this@MenuActivity, 4),
                0,
                AuthUi.dp(this@MenuActivity, 18)
            )
        })

        content.addView(menuRow(
            "SETTINGS",
            "Audio, effects, vibration, notifications, and accessibility."
        ) {
            startActivity(Intent(this, SettingsActivity::class.java))
        })

        content.addView(menuRow(
            "ACCOUNT",
            "View your signed-in account and player identity."
        ) {
            startActivity(Intent(this, AccountActivity::class.java))
        })

        content.addView(menuRow(
            "ACHIEVEMENTS",
            "Milestones, account accomplishments, and special rewards."
        ) {
            openSection(
                "ACHIEVEMENTS",
                "Achievements and milestone rewards will live here."
            )
        })

        content.addView(menuRow(
            "HELP & SUPPORT",
            "Gameplay help, controls, troubleshooting, and support."
        ) {
            startActivity(
                Intent(
                    this,
                    HelpSupportActivity::class.java
                )
            )
        })

        content.addView(menuRow(
            "ABOUT",
            "Version information, credits, and game details."
        ) {
            startActivity(
                Intent(
                    this,
                    AboutActivity::class.java
                )
            )
        })

        content.addView(menuRow(
            "SIGN OUT",
            "Return to the sign-in screen.",
            danger = true
        ) {
            FirebaseAuth.getInstance().signOut()
            getSharedPreferences("auth_prefs", MODE_PRIVATE)
                .edit()
                .clear()
                .apply()
            startActivity(Intent(this, MainActivity::class.java))
            finishAffinity()
        })

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
                AuthUi.dp(this@MenuActivity, 20),
                insets.systemWindowInsetTop + AuthUi.dp(this@MenuActivity, 20),
                AuthUi.dp(this@MenuActivity, 20),
                insets.systemWindowInsetBottom + AuthUi.dp(this@MenuActivity, 28)
            )
            insets
        }
        root.requestApplyInsets()

        setContentView(root)
    }

    private fun menuRow(
        title: String,
        subtitle: String,
        danger: Boolean = false,
        onClick: () -> Unit
    ): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(
                AuthUi.dp(this@MenuActivity, 18),
                AuthUi.dp(this@MenuActivity, 14),
                AuthUi.dp(this@MenuActivity, 18),
                AuthUi.dp(this@MenuActivity, 14)
            )
            background = GradientDrawable().apply {
                setColor(Color.argb(222, 7, 10, 31))
                cornerRadius = AuthUi.dp(this@MenuActivity, 14).toFloat()
                setStroke(
                    AuthUi.dp(this@MenuActivity, 1),
                    if (danger) Color.rgb(183, 65, 95)
                    else Color.rgb(129, 91, 220)
                )
            }
            isClickable = true
            isFocusable = true

            addView(TextView(this@MenuActivity).apply {
                text = title
                textSize = 16f
                setTextColor(
                    if (danger) Color.rgb(255, 163, 179)
                    else Color.WHITE
                )
                typeface = Typeface.DEFAULT_BOLD
                letterSpacing = 0.05f
            })

            addView(TextView(this@MenuActivity).apply {
                text = subtitle
                textSize = 12f
                setTextColor(Color.rgb(174, 163, 208))
                setPadding(
                    0,
                    AuthUi.dp(this@MenuActivity, 4),
                    0,
                    0
                )
            })

            setOnClickListener { onClick() }
            setOnTouchListener { view, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        AppHaptics.tap(this@MenuActivity)
                        view.animate()
                            .scaleX(0.985f)
                            .scaleY(0.985f)
                            .alpha(0.86f)
                            .setDuration(60)
                            .start()
                    }
                    MotionEvent.ACTION_UP,
                    MotionEvent.ACTION_CANCEL -> {
                        view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .alpha(1f)
                            .setDuration(90)
                            .start()
                    }
                }
                false
            }

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = AuthUi.dp(this@MenuActivity, 10)
            }
        }
    }

    private fun openSection(title: String, subtitle: String) {
        startActivity(
            Intent(this, GameSectionActivity::class.java)
                .putExtra("section_title", title)
                .putExtra("section_subtitle", subtitle)
        )
    }
}
