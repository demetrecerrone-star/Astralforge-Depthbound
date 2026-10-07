package com.demetrecerrone.astralforge

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.InputType
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.CheckBox
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

object AuthUi {
    private const val PURPLE = 0xFFB34DFF.toInt()
    private const val LAVENDER = 0xFFC8B9FF.toInt()
    private const val MUTED = 0xFFD2CBEA.toInt()

    fun dp(context: Context, value: Int): Int =
        (value * context.resources.displayMetrics.density).toInt()

    fun setupWindow(activity: Activity) {
        activity.window.statusBarColor = Color.TRANSPARENT
        activity.window.navigationBarColor = Color.rgb(3, 5, 14)
        activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        @Suppress("DEPRECATION")
        activity.window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
    }

    fun createScreen(activity: Activity, topFraction: Float): LinearLayout {
        val root = FrameLayout(activity)

        val bgImage = ImageView(activity).apply {
            setImageResource(R.drawable.file_00000000bfc081f5b0a1931c13d627e8)
            scaleType = ImageView.ScaleType.CENTER_CROP
            contentDescription = null
        }
        root.addView(
            bgImage,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        val shade = View(activity).apply {
            this.background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(
                    Color.argb(0, 1, 2, 10),
                    Color.argb(40, 1, 2, 10),
                    Color.argb(180, 1, 2, 10),
                    Color.argb(225, 1, 2, 10)
                )
            )
        }
        root.addView(
            shade,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        val scroll = ScrollView(activity).apply {
            isFillViewport = true
            overScrollMode = View.OVER_SCROLL_NEVER
        }
        val content = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            val screenHeight = activity.resources.displayMetrics.heightPixels
            val top = (screenHeight * topFraction).toInt()
            setPadding(dp(activity, 28), top, dp(activity, 28), dp(activity, 36))
        }
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
        activity.setContentView(root)
        return content
    }

    fun field(
        context: Context,
        hint: String,
        iconRes: Int,
        isPassword: Boolean = false,
        inputType: Int = InputType.TYPE_CLASS_TEXT
    ): EditText {
        val edit = EditText(context).apply {
            this.hint = hint
            setHintTextColor(Color.rgb(145, 137, 188))
            setTextColor(Color.WHITE)
            textSize = 16f
            setSingleLine(true)
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(context, 18), 0, dp(context, 18), 0)
            background = GradientDrawable().apply {
                setColor(Color.argb(206, 6, 10, 28))
                cornerRadius = dp(context, 14).toFloat()
                setStroke(dp(context, 1), Color.rgb(132, 111, 205))
            }
            compoundDrawablePadding = dp(context, 14)
            setCompoundDrawablesWithIntrinsicBounds(
                iconRes,
                0,
                if (isPassword) R.drawable.ic_eye else 0,
                0
            )
            this.inputType = inputType
        }

        if (isPassword) {
            edit.transformationMethod = PasswordTransformationMethod.getInstance()
            edit.setSelection(edit.text.length)
            edit.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP &&
                    event.x >= edit.width - edit.totalPaddingEnd
                ) {
                    val hidden = edit.transformationMethod is PasswordTransformationMethod
                    edit.transformationMethod =
                        if (hidden) HideReturnsTransformationMethod.getInstance()
                        else PasswordTransformationMethod.getInstance()
                    edit.setSelection(edit.text.length)
                    true
                } else {
                    false
                }
            }
        }
        return edit
    }

    fun addField(parent: LinearLayout, field: EditText, topMarginDp: Int = 12) {
        val context = parent.context
        parent.addView(
            field,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(context, 60)
            ).apply { topMargin = dp(context, topMarginDp) }
        )
    }

    fun assetButton(
        context: Context,
        drawableRes: Int,
        description: String,
        heightDp: Int,
        onClick: () -> Unit
    ): ImageView = ImageView(context).apply {
        setImageResource(drawableRes)
        scaleType = ImageView.ScaleType.FIT_CENTER
        adjustViewBounds = true
        isClickable = true
        isFocusable = true
        contentDescription = description
        setOnClickListener { onClick() }
        setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> view.alpha = 0.72f
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> view.alpha = 1f
            }
            false
        }
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(context, heightDp)
        )
    }

    fun zipAssetButton(
        context: Context,
        entryName: String,
        description: String,
        heightDp: Int,
        onClick: () -> Unit
    ): ImageView = ImageView(context).apply {
        ButtonAssetStore.load(context, entryName)?.let { setImageBitmap(it) }
        scaleType = ImageView.ScaleType.FIT_CENTER
        adjustViewBounds = true
        isClickable = true
        isFocusable = true
        contentDescription = description
        setOnClickListener { onClick() }
        setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> view.alpha = 0.72f
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> view.alpha = 1f
            }
            false
        }
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(context, heightDp)
        )
    }

    fun divider(context: Context): TextView = TextView(context).apply {
        text = "—   OR CONTINUE WITH   —"
        gravity = Gravity.CENTER
        setTextColor(LAVENDER)
        textSize = 13f
        letterSpacing = 0.17f
        setPadding(0, dp(context, 6), 0, dp(context, 6))
    }

    fun heading(context: Context, text: String, size: Float): TextView = TextView(context).apply {
        this.text = text
        gravity = Gravity.CENTER
        setTextColor(Color.WHITE)
        textSize = size
        typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        letterSpacing = 0.05f
    }

    fun subtitle(context: Context, text: String): TextView = TextView(context).apply {
        this.text = text
        gravity = Gravity.CENTER
        setTextColor(MUTED)
        textSize = 15f
        setPadding(0, dp(context, 8), 0, dp(context, 8))
    }

    fun linkRow(
        context: Context,
        prefix: String,
        link: String,
        onLink: () -> Unit
    ): LinearLayout = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER

        addView(TextView(context).apply {
            text = prefix
            setTextColor(MUTED)
            textSize = 14f
        })

        addView(TextView(context).apply {
            text = "  " + link
            setTextColor(PURPLE)
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            isClickable = true
            setOnClickListener { onLink() }
        })
    }

    fun rememberBox(context: Context): CheckBox = CheckBox(context).apply {
        text = context.getString(R.string.remember_me)
        setTextColor(MUTED)
        textSize = 13f
        buttonTintList = ColorStateList.valueOf(PURPLE)
    }

    fun smallLink(context: Context, text: String, onClick: () -> Unit): TextView =
        TextView(context).apply {
            this.text = text
            setTextColor(PURPLE)
            textSize = 13f
            gravity = Gravity.END
            setPadding(dp(context, 8), dp(context, 12), 0, dp(context, 12))
            isClickable = true
            setOnClickListener { onClick() }
        }
}
