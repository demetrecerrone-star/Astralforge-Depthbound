package com.demetrecerrone.astralforge

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView

object AuthUi {
    val textPrimary: Int = Color.rgb(241, 236, 255)
    val textSecondary: Int = Color.rgb(181, 173, 214)
    val violet: Int = Color.rgb(156, 89, 255)
    val violetSoft: Int = Color.rgb(199, 156, 255)

    fun dp(context: Context, value: Int): Int =
        (value * context.resources.displayMetrics.density).toInt()

    fun screenBackground(): GradientDrawable =
        GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(
                Color.rgb(3, 6, 20),
                Color.rgb(10, 10, 34),
                Color.rgb(23, 10, 54)
            )
        )

    fun panelBackground(context: Context): GradientDrawable =
        GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(context, 26).toFloat()
            setColor(Color.argb(210, 7, 10, 31))
            setStroke(dp(context, 1), Color.rgb(82, 57, 145))
        }

    fun fieldBackground(context: Context): GradientDrawable =
        GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(context, 14).toFloat()
            setColor(Color.argb(230, 11, 14, 40))
            setStroke(dp(context, 1), Color.rgb(113, 86, 185))
        }

    fun primaryBackground(context: Context): GradientDrawable =
        GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            intArrayOf(
                Color.rgb(93, 48, 182),
                Color.rgb(126, 62, 221),
                Color.rgb(91, 42, 176)
            )
        ).apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(context, 15).toFloat()
            setStroke(dp(context, 1), violetSoft)
        }

    fun secondaryBackground(context: Context): GradientDrawable =
        GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(context, 15).toFloat()
            setColor(Color.argb(155, 8, 12, 34))
            setStroke(dp(context, 1), Color.rgb(98, 76, 161))
        }

    fun googleBackground(context: Context): GradientDrawable =
        GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(context, 15).toFloat()
            setColor(Color.rgb(245, 245, 250))
        }

    fun styleField(context: Context, field: EditText) {
        field.background = fieldBackground(context)
        field.setTextColor(Color.WHITE)
        field.setHintTextColor(Color.rgb(139, 132, 171))
        field.textSize = 16f
        field.setPadding(
            dp(context, 18),
            dp(context, 14),
            dp(context, 18),
            dp(context, 14)
        )
        field.minHeight = dp(context, 56)
    }

    fun stylePrimary(context: Context, button: Button) {
        button.backgroundTintList = null
        button.background = primaryBackground(context)
        button.setTextColor(Color.WHITE)
        button.textSize = 16f
        button.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        button.isAllCaps = true
        button.minHeight = dp(context, 56)
    }

    fun styleSecondary(context: Context, button: Button) {
        button.backgroundTintList = null
        button.background = secondaryBackground(context)
        button.setTextColor(textPrimary)
        button.textSize = 15f
        button.typeface = Typeface.DEFAULT_BOLD
        button.isAllCaps = false
        button.minHeight = dp(context, 54)
    }

    fun styleGoogle(context: Context, button: Button) {
        button.backgroundTintList = null
        button.background = googleBackground(context)
        button.setTextColor(Color.rgb(28, 28, 36))
        button.textSize = 15f
        button.typeface = Typeface.DEFAULT_BOLD
        button.isAllCaps = false
        button.minHeight = dp(context, 54)
    }

    fun brandTitle(context: Context): TextView =
        TextView(context).apply {
            text = "ASTRALFORGE"
            setTextColor(textPrimary)
            textSize = 34f
            gravity = Gravity.CENTER
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            letterSpacing = 0.035f
        }

    fun brandSubtitle(context: Context): TextView =
        TextView(context).apply {
            text = "—  D E P T H B O U N D  —"
            setTextColor(Color.rgb(207, 193, 238))
            textSize = 13f
            gravity = Gravity.CENTER
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
            setPadding(0, dp(context, 2), 0, dp(context, 6))
        }

    fun ornament(context: Context): TextView =
        TextView(context).apply {
            text = "✦"
            setTextColor(violetSoft)
            textSize = 24f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(context, 8))
        }

    fun divider(context: Context, label: String): LinearLayout {
        val row = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        fun line(): View = View(context).apply {
            setBackgroundColor(Color.rgb(83, 70, 125))
        }
        val left = line()
        val right = line()
        val text = TextView(context).apply {
            this.text = label
            setTextColor(textSecondary)
            textSize = 11f
            gravity = Gravity.CENTER
            letterSpacing = 0.15f
            setPadding(dp(context, 12), 0, dp(context, 12), 0)
        }
        row.addView(left, LinearLayout.LayoutParams(0, dp(context, 1), 1f))
        row.addView(text)
        row.addView(right, LinearLayout.LayoutParams(0, dp(context, 1), 1f))
        return row
    }
}
