package com.demetrecerrone.astralforge

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class GameSectionActivity : Activity() {

    companion object {
        const val EXTRA_SECTION = "section"

        const val SECTION_DESCEND = "descend"
        const val SECTION_CHARACTER = "character"
        const val SECTION_EQUIPMENT = "equipment"
        const val SECTION_CLASS = "class"
        const val SECTION_ASCENSION = "ascension"
        const val SECTION_INVENTORY = "inventory"
        const val SECTION_SHOP = "shop"
        const val SECTION_SETTINGS = "settings"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val density = resources.displayMetrics.density
        fun dp(value: Int) = (value * density).toInt()

        val section = intent.getStringExtra(EXTRA_SECTION) ?: SECTION_CHARACTER
        val titleRes: Int
        val bodyRes: Int

        when (section) {
            SECTION_DESCEND -> {
                titleRes = R.string.section_descend_title
                bodyRes = R.string.section_descend_body
            }
            SECTION_EQUIPMENT -> {
                titleRes = R.string.section_equipment_title
                bodyRes = R.string.section_equipment_body
            }
            SECTION_CLASS -> {
                titleRes = R.string.section_class_title
                bodyRes = R.string.section_class_body
            }
            SECTION_ASCENSION -> {
                titleRes = R.string.section_ascension_title
                bodyRes = R.string.section_ascension_body
            }
            SECTION_INVENTORY -> {
                titleRes = R.string.section_inventory_title
                bodyRes = R.string.section_inventory_body
            }
            SECTION_SHOP -> {
                titleRes = R.string.section_shop_title
                bodyRes = R.string.section_shop_body
            }
            SECTION_SETTINGS -> {
                titleRes = R.string.section_settings_title
                bodyRes = R.string.section_settings_body
            }
            else -> {
                titleRes = R.string.section_character_title
                bodyRes = R.string.section_character_body
            }
        }

        val scroll = ScrollView(this).apply {
            setBackgroundColor(Color.rgb(8, 10, 28))
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(24), dp(42), dp(24), dp(32))
        }

        val title = TextView(this).apply {
            text = getString(titleRes)
            setTextColor(Color.rgb(236, 230, 255))
            textSize = 30f
            gravity = Gravity.CENTER
        }

        val status = TextView(this).apply {
            text = getString(R.string.section_skeleton_status)
            setTextColor(Color.rgb(181, 118, 255))
            textSize = 13f
            gravity = Gravity.CENTER
            setPadding(0, dp(8), 0, dp(22))
        }

        val body = TextView(this).apply {
            text = getString(bodyRes)
            setTextColor(Color.rgb(220, 215, 240))
            textSize = 16f
            setLineSpacing(0f, 1.25f)
        }

        val back = Button(this).apply {
            text = getString(R.string.back_to_hub)
            setOnClickListener { finish() }
        }

        root.addView(title)
        root.addView(status)
        root.addView(
            body,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )
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
