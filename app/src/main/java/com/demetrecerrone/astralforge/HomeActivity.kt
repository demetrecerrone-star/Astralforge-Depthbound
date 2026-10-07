package com.demetrecerrone.astralforge

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth

class HomeActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AuthUi.setupWindow(this)

        val root = FrameLayout(this)
        root.addView(ImageView(this).apply {
            setImageResource(R.drawable.file_00000000bfc081f5b0a1931c13d627e8)
            scaleType = ImageView.ScaleType.CENTER_CROP
        }, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))

        root.addView(android.view.View(this).apply {
            setBackgroundColor(Color.argb(150, 2, 4, 15))
        }, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))

        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(AuthUi.dp(this@HomeActivity, 28), AuthUi.dp(this@HomeActivity, 28), AuthUi.dp(this@HomeActivity, 28), AuthUi.dp(this@HomeActivity, 28))
        }

        val user = FirebaseAuth.getInstance().currentUser
        panel.addView(TextView(this).apply {
            text = "ASTRAL FORGE: DEPTHBOUND"
            textSize = 26f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        })
        panel.addView(TextView(this).apply {
            text = if (user?.isAnonymous == true) "Guest session ready." else "Authentication successful."
            textSize = 16f
            setTextColor(Color.rgb(205, 196, 235))
            gravity = Gravity.CENTER
            setPadding(0, AuthUi.dp(this@HomeActivity, 12), 0, AuthUi.dp(this@HomeActivity, 20))
        })
        panel.addView(Button(this).apply {
            text = "SIGN OUT"
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply {
                setColor(Color.argb(210, 45, 18, 95))
                cornerRadius = AuthUi.dp(this@HomeActivity, 14).toFloat()
                setStroke(AuthUi.dp(this@HomeActivity, 1), Color.rgb(179, 77, 255))
            }
            setOnClickListener {
                FirebaseAuth.getInstance().signOut()
                getSharedPreferences("auth_prefs", MODE_PRIVATE).edit().clear().apply()
                startActivity(Intent(this@HomeActivity, MainActivity::class.java))
                finish()
            }
        }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, AuthUi.dp(this, 52)))

        root.addView(panel, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
        setContentView(root)
    }
}
