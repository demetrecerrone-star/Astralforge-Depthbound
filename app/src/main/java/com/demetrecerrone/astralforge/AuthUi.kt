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
        activity.window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )
    }

    fun createScreen(activity: Activity, topFraction: Float): LinearLayout {
        val root = FrameLayout(activity).apply {
            setBackgroundColor(Color.rgb(3, 5, 14))
        }

        val bgImage = ImageView(activity).apply {
            setImageResource(R.drawable.file_00000000bfc081f5b0a1931c13d627e8)
            scaleType = ImageView.ScaleType.FIT_XY
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
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(
                    Color.argb(0, 1, 2, 10),
                    Color.argb(25, 1, 2, 10),
                    Color.argb(135, 1, 2, 10),
                    Color.argb(215, 1, 2, 10)
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

        val metrics = activity.resources.displayMetrics
        val screenWidth = metrics.widthPixels
        val screenHeight = metrics.heightPixels
        val horizontalInset = dp(activity, 16)
        val bottomInset = dp(activity, 8)
        val top = (screenHeight * topFraction).toInt()

        val targetWidth = (screenWidth * 0.64f).toInt()
        val maxWidth = dp(activity, 760)
        val minWidth = dp(activity, 320)
        val availableWidth = (screenWidth - horizontalInset * 2).coerceAtLeast(1)
        val formWidth = targetWidth
            .coerceAtLeast(minWidth)
            .coerceAtMost(minOf(maxWidth, availableWidth))

        val formHeight = (screenHeight - top - bottomInset)
            .coerceAtLeast(dp(activity, 150))

        val scroll = ScrollView(activity).apply {
            isFillViewport = false
            isVerticalScrollBarEnabled = false
            overScrollMode = View.OVER_SCROLL_NEVER
        }

        val content = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(
                dp(activity, 10),
                dp(activity, 2),
                dp(activity, 10),
                dp(activity, 10)
            )
        }

        scroll.addView(
            content,
            ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT,
                ScrollView.LayoutParams.WRAP_CONTENT
            )
        )

        root.addView(
            scroll,
            FrameLayout.LayoutParams(
                formWidth,
                formHeight
            ).apply {
                gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                topMargin = top
                bottomMargin = bottomInset
            }
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
            textSize = 14.5f
            setSingleLine(true)
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(context, 14), 0, dp(context, 14), 0)
            background = GradientDrawable().apply {
                setColor(Color.argb(206, 6, 10, 28))
                cornerRadius = dp(context, 12).toFloat()
                setStroke(dp(context, 1), Color.rgb(132, 111, 205))
            }
            compoundDrawablePadding = dp(context, 10)
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
                    val hidden =
                        edit.transformationMethod is PasswordTransformationMethod
                    edit.transformationMethod =
                        if (hidden) {
                            HideReturnsTransformationMethod.getInstance()
                        } else {
                            PasswordTransformationMethod.getInstance()
                        }
                    edit.setSelection(edit.text.length)
                    true
                } else {
                    false
                }
            }
        }

        return edit
    }

    fun addField(
        parent: LinearLayout,
        field: EditText,
        topMarginDp: Int = 8
    ) {
        val context = parent.context
        parent.addView(
            field,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(context, 48)
            ).apply {
                topMargin = dp(context, topMarginDp)
            }
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
                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> view.alpha = 1f
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
                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> view.alpha = 1f
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
        textSize = 12f
        letterSpacing = 0.14f
        setPadding(0, dp(context, 2), 0, dp(context, 2))
    }

    fun heading(
        context: Context,
        text: String,
        size: Float
    ): TextView = TextView(context).apply {
        this.text = text
        gravity = Gravity.CENTER
        setTextColor(Color.WHITE)
        textSize = size
        typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        letterSpacing = 0.05f
    }

    fun subtitle(context: Context, text: String): TextView =
        TextView(context).apply {
            this.text = text
            gravity = Gravity.CENTER
            setTextColor(MUTED)
            textSize = 13f
            setPadding(0, dp(context, 3), 0, dp(context, 3))
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
            textSize = 12.5f
        })

        addView(TextView(context).apply {
            text = "  " + link
            setTextColor(PURPLE)
            textSize = 12.5f
            typeface = Typeface.DEFAULT_BOLD
            isClickable = true
            setOnClickListener { onLink() }
        })
    }

    fun rememberBox(context: Context): CheckBox = CheckBox(context).apply {
        text = context.getString(R.string.remember_me)
        setTextColor(MUTED)
        textSize = 12f
        buttonTintList = ColorStateList.valueOf(PURPLE)
        minHeight = 0
        minimumHeight = 0
        setPadding(0, 0, 0, 0)
    }

    fun smallLink(
        context: Context,
        text: String,
        onClick: () -> Unit
    ): TextView = TextView(context).apply {
        this.text = text
        setTextColor(PURPLE)
        textSize = 12f
        gravity = Gravity.END
        setPadding(dp(context, 6), dp(context, 5), 0, dp(context, 5))
        isClickable = true
        setOnClickListener { onClick() }
    }
}
