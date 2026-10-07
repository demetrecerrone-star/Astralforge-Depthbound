package com.demetrecerrone.astralforge

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

class GameSectionActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AuthUi.setupWindow(this)

        val title = intent.getStringExtra("section_title") ?: "DEPTHBOUND"
        val subtitle = intent.getStringExtra("section_subtitle")
            ?: "This system is ready for its next development pass."

        val root = FrameLayout(this)

        root.addView(
            ImageView(this).apply {
                setImageResource(R.drawable.file_00000000bfc081f5b0a1931c13d627e8)
                scaleType = ImageView.ScaleType.CENTER_CROP
                contentDescription = null
            },
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        root.addView(
            View(this).apply {
                setBackgroundColor(Color.argb(190, 2, 4, 16))
            },
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(
                AuthUi.dp(this@GameSectionActivity, 28),
                AuthUi.dp(this@GameSectionActivity, 60),
                AuthUi.dp(this@GameSectionActivity, 28),
                AuthUi.dp(this@GameSectionActivity, 40)
            )
        }

        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(
                AuthUi.dp(this@GameSectionActivity, 22),
                AuthUi.dp(this@GameSectionActivity, 28),
                AuthUi.dp(this@GameSectionActivity, 22),
                AuthUi.dp(this@GameSectionActivity, 28)
            )
            background = GradientDrawable().apply {
                setColor(Color.argb(220, 6, 9, 30))
                cornerRadius = AuthUi.dp(this@GameSectionActivity, 18).toFloat()
                setStroke(AuthUi.dp(this@GameSectionActivity, 1), Color.rgb(129, 91, 220))
            }
        }

        panel.addView(TextView(this).apply {
            text = title
            textSize = 27f
            setTextColor(Color.WHITE)
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            gravity = Gravity.CENTER
            letterSpacing = 0.05f
        })
        panel.addView(TextView(this).apply {
            text = subtitle
            textSize = 15f
            setTextColor(Color.rgb(205, 196, 235))
            gravity = Gravity.CENTER
            setPadding(
                0,
                AuthUi.dp(this@GameSectionActivity, 14),
                0,
                AuthUi.dp(this@GameSectionActivity, 12)
            )
        })
        panel.addView(TextView(this).apply {
            text = "System shell active • use Android Back to return to the hub"
            textSize = 12f
            setTextColor(Color.rgb(155, 143, 195))
            gravity = Gravity.CENTER
        })

        content.addView(
            panel,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )
        root.addView(
            content,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )
        setContentView(root)
    }
}
