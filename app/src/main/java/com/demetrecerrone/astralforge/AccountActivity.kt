package com.demetrecerrone.astralforge

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.WindowInsets
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth

class AccountActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AuthUi.setupWindow(this)

        val root = FrameLayout(this)
        root.addView(
            ImageView(this).apply {
                setImageResource(R.drawable.file_00000000a45881f5ad657cc79abf1338)
                scaleType = ImageView.ScaleType.CENTER_CROP
                alpha = 0.70f
                contentDescription = null
            },
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        root.addView(
            android.view.View(this).apply {
                setBackgroundColor(Color.argb(192, 2, 4, 16))
            },
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(
                AuthUi.dp(this@AccountActivity, 20),
                AuthUi.dp(this@AccountActivity, 56),
                AuthUi.dp(this@AccountActivity, 20),
                AuthUi.dp(this@AccountActivity, 32)
            )
        }

        val user = FirebaseAuth.getInstance().currentUser
        val displayName = when {
            user?.isAnonymous == true -> "Guest Delver"
            !user?.displayName.isNullOrBlank() -> user?.displayName ?: "Delver"
            else -> "Delver"
        }

        content.addView(TextView(this).apply {
            text = "ACCOUNT"
            textSize = 29f
            setTextColor(Color.WHITE)
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(
                0,
                0,
                0,
                AuthUi.dp(this@AccountActivity, 18)
            )
        })

        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                AuthUi.dp(this@AccountActivity, 18),
                AuthUi.dp(this@AccountActivity, 18),
                AuthUi.dp(this@AccountActivity, 18),
                AuthUi.dp(this@AccountActivity, 18)
            )
            background = GradientDrawable().apply {
                setColor(Color.argb(220, 7, 10, 31))
                cornerRadius = AuthUi.dp(this@AccountActivity, 16).toFloat()
                setStroke(
                    AuthUi.dp(this@AccountActivity, 1),
                    Color.rgb(124, 87, 214)
                )
            }
        }

        panel.addView(accountLine("PLAYER", displayName))
        panel.addView(accountLine(
            "EMAIL",
            if (user?.isAnonymous == true) "Guest account"
            else user?.email ?: "Not available"
        ))
        panel.addView(accountLine(
            "ACCOUNT TYPE",
            if (user?.isAnonymous == true) "Guest"
            else "Registered"
        ))
        panel.addView(accountLine(
            "USER ID",
            user?.uid?.take(12)?.plus("…") ?: "Not signed in"
        ))

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

        root.setOnApplyWindowInsetsListener { _, insets: WindowInsets ->
            content.setPadding(
                AuthUi.dp(this@AccountActivity, 20),
                insets.systemWindowInsetTop + AuthUi.dp(this@AccountActivity, 24),
                AuthUi.dp(this@AccountActivity, 20),
                insets.systemWindowInsetBottom + AuthUi.dp(this@AccountActivity, 24)
            )
            insets
        }
        root.requestApplyInsets()

        setContentView(root)
    }

    private fun accountLine(label: String, value: String): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(
                0,
                AuthUi.dp(this@AccountActivity, 9),
                0,
                AuthUi.dp(this@AccountActivity, 9)
            )

            addView(TextView(this@AccountActivity).apply {
                text = label
                textSize = 12f
                setTextColor(Color.rgb(166, 154, 201))
                typeface = Typeface.DEFAULT_BOLD
            }, LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ))

            addView(TextView(this@AccountActivity).apply {
                text = value
                textSize = 13f
                setTextColor(Color.WHITE)
                gravity = Gravity.END
            })
        }
    }
}
