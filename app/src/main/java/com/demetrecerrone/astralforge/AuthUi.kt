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
    val textPrimary: Int = Color.rgb(244, 239, 255)
    val textSecondary: Int = Color.rgb(185, 181, 205)
    val violet: Int = Color.rgb(111, 48, 226)
    val violetSoft: Int = Color.rgb(190, 118, 255)
    val blueLine: Int = Color.rgb(58, 135, 231)
    val cyanLine: Int = Color.rgb(87, 178, 194)
    val antiqueGold: Int = Color.rgb(195, 169, 132)

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
            cornerRadius = dp(context, 8).toFloat()
            setColor(Color.argb(224, 3, 7, 20))
            setStroke(dp(context, 1), Color.rgb(119, 91, 126))
        }

    fun fieldBackground(context: Context): GradientDrawable =
        GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(context, 10).toFloat()
            setColor(Color.argb(235, 5, 12, 27))
            setStroke(dp(context, 1), Color.rgb(92, 113, 151))
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
            setStroke(dp(context, 2), Color.rgb(133, 72, 255))
        }

    fun secondaryBackground(context: Context): GradientDrawable =
        GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(context, 10).toFloat()
            setColor(Color.argb(158, 4, 14, 31))
            setStroke(dp(context, 1), blueLine)
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
        field.textSize = 17f
        field.typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
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
        button.isAllCaps = false
        button.minHeight = dp(context, 56)
    }

    fun styleSecondary(context: Context, button: Button) {
        button.backgroundTintList = null
        button.background = secondaryBackground(context)
        button.setTextColor(textPrimary)
        button.textSize = 15f
        button.typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
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
            text = "Astralforge:"
            setTextColor(Color.rgb(245, 231, 212))
            textSize = 48f
            gravity = Gravity.CENTER
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            letterSpacing = 0.005f
            setShadowLayer(14f, 0f, 2f, Color.rgb(104, 44, 198))
            setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }

    fun brandSubtitle(context: Context): TextView =
        TextView(context).apply {
            text = "Depthbound"
            setTextColor(Color.rgb(228, 217, 255))
            textSize = 30f
            gravity = Gravity.CENTER
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            setShadowLayer(12f, 0f, 0f, Color.rgb(129, 59, 255))
            setLayerType(View.LAYER_TYPE_SOFTWARE, null)
            setPadding(0, dp(context, -6), 0, dp(context, 4))
        }

    fun ornament(context: Context): TextView =
        TextView(context).apply {
            text = "✦"
            setTextColor(violetSoft)
            textSize = 18f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(context, 4))
        }

    fun styleGuest(context: Context, button: Button) {
        button.backgroundTintList = null
        button.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(context, 10).toFloat()
            setColor(Color.argb(152, 5, 28, 39))
            setStroke(dp(context, 1), cyanLine)
        }
        button.setTextColor(Color.rgb(220, 245, 255))
        button.textSize = 16f
        button.typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
        button.isAllCaps = false
        button.minHeight = dp(context, 56)
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
