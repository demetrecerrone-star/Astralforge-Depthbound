package com.demetrecerrone.astralforge

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class LegalDocumentActivity : Activity() {

    companion object {
        const val EXTRA_DOCUMENT = "document"
        const val DOCUMENT_TERMS = "terms"
        const val DOCUMENT_PRIVACY = "privacy"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val density = resources.displayMetrics.density
        fun dp(value: Int) = (value * density).toInt()

        val isPrivacy = intent.getStringExtra(EXTRA_DOCUMENT) == DOCUMENT_PRIVACY

        val scroll = ScrollView(this).apply {
            setBackgroundColor(Color.rgb(8, 10, 28))
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(36), dp(24), dp(36))
        }

        val title = TextView(this).apply {
            text = getString(
                if (isPrivacy) R.string.privacy_policy_title
                else R.string.terms_of_service_title
            )
            setTextColor(Color.rgb(236, 230, 255))
            textSize = 28f
            gravity = Gravity.CENTER
        }

        val updated = TextView(this).apply {
            text = getString(R.string.legal_last_updated)
            setTextColor(Color.rgb(173, 163, 214))
            textSize = 13f
            gravity = Gravity.CENTER
            setPadding(0, dp(8), 0, dp(24))
        }

        val body = TextView(this).apply {
            text = getString(
                if (isPrivacy) R.string.privacy_policy_body
                else R.string.terms_of_service_body
            )
            setTextColor(Color.rgb(220, 215, 240))
            textSize = 15f
            setLineSpacing(0f, 1.25f)
        }

        val back = Button(this).apply {
            text = getString(R.string.back)
            setOnClickListener { finish() }
        }

        root.addView(title)
        root.addView(updated)
        root.addView(body)
        root.addView(
            back,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(24) }
        )

        scroll.addView(root)
        setContentView(scroll)
    }
}
