package com.demetrecerrone.astralforge

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val density = resources.displayMetrics.density
        fun dp(value: Int) = (value * density).toInt()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(24), dp(24), dp(24), dp(24))
            setBackgroundColor(Color.rgb(8, 10, 28))
        }

        val title = TextView(this).apply {
            text = getString(R.string.app_name)
            setTextColor(Color.rgb(232, 226, 255))
            textSize = 30f
            gravity = Gravity.CENTER
        }

        val subtitle = TextView(this).apply {
            text = getString(R.string.source_foundation_ready)
            setTextColor(Color.rgb(171, 160, 214))
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(0, dp(12), 0, 0)
        }

        root.addView(title)
        root.addView(subtitle)
        setContentView(root)
    }
}
