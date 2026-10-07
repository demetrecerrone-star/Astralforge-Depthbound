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
import kotlin.math.min

object AuthUi {
    private const val PURPLE = 0xFFB34DFF.toInt()
    private const val LAVENDER = 0xFFC8B9FF.toInt()
    private const val MUTED = 0xFFD2CBEA.toInt()

    fun dp(context: Context, value: Int): Int =
        (value * context.resources.displayMetrics.density).toInt()

    private fun screenHeight(context: Context): Int =
        context.resources.displayMetrics.heightPixels

    private fun fieldHeight(context: Context): Int =
        (screenHeight(context) * 0.078f).toInt().coerceAtLeast(48)

    private fun controlGap(context: Context): Int =
        (screenHeight(context) * 0.010f).toInt().coerceAtLeast(5)

    fun setupWindow(activity: Activity) {
        activity.window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )
        FullscreenUi.apply(activity)
    }

    fun createScreen(activity: Activity, topFraction: Float): LinearLayout {
        val root = FrameLayout(activity).apply {
            setBackgroundColor(Color.rgb(3, 5, 14))
        }

        val bgImage = ImageView(activity).apply {
            setImageResource(R.drawable.auth_background_landscape_clean)
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

        val metrics = activity.resources.displayMetrics
        val screenWidth = metrics.widthPixels
        val screenHeight = metrics.heightPixels
        val bottomInset = (screenHeight * 0.018f).toInt()
        val top = (screenHeight * topFraction).toInt()

        // The artwork already contains a centered dark auth panel.
        // Keep every interactive control centered inside that panel.
        val formWidth = (screenWidth * 0.48f).toInt()
            .coerceAtMost((screenWidth * 0.52f).toInt())
            .coerceAtLeast((screenWidth * 0.42f).toInt())

        val formHeight = (screenHeight - top - bottomInset)
            .coerceAtLeast((screenHeight * 0.42f).toInt())

        val scroll = ScrollView(activity).apply {
            isFillViewport = false
            isVerticalScrollBarEnabled = false
            overScrollMode = View.OVER_SCROLL_NEVER
            clipToPadding = false
        }

        val content = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(
                (screenWidth * 0.008f).toInt(),
                (screenHeight * 0.004f).toInt(),
                (screenWidth * 0.008f).toInt(),
                (screenHeight * 0.012f).toInt()
            )
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
                formWidth,
                formHeight
            ).apply {
                gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                topMargin = top
                bottomMargin = bottomInset
            }
        )

        activity.setContentView(root)
        FullscreenUi.apply(activity)
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
            setHintTextColor(Color.rgb(160, 151, 199))
            setTextColor(Color.WHITE)
            textSize = 14f
            setSingleLine(true)
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(context, 14), 0, dp(context, 14), 0)
            background = GradientDrawable().apply {
                setColor(Color.argb(214, 6, 10, 28))
                cornerRadius = dp(context, 12).toFloat()
                setStroke(dp(context, 1), Color.rgb(140, 115, 220))
            }
            compoundDrawablePadding = dp(context, 10)
            setCompoundDrawablesWithIntrinsicBounds(
                iconRes,
                0,
                if (isPassword) R.drawable.ic_eye else 0,
                0
            )
            this.inputType = inputType
            minHeight = 0
            minimumHeight = 0
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
        topMarginDp: Int = 0
    ) {
        val context = parent.context
        val gap = if (topMarginDp > 0) dp(context, topMarginDp) else controlGap(context)
        parent.addView(
            field,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                fieldHeight(context)
            ).apply {
                topMargin = gap
            }
        )
    }

    fun twoFieldRow(
        parent: LinearLayout,
        left: EditText,
        right: EditText,
        topMargin: Boolean = true
    ) {
        val context = parent.context
        val gap = controlGap(context)
        val row = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        row.addView(
            left,
            LinearLayout.LayoutParams(
                0,
                fieldHeight(context),
                1f
            ).apply {
                marginEnd = gap / 2
            }
        )

        row.addView(
            right,
            LinearLayout.LayoutParams(
                0,
                fieldHeight(context),
                1f
            ).apply {
                marginStart = gap / 2
            }
        )

        parent.addView(
            row,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                if (topMargin) this.topMargin = gap
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

        val requested = dp(context, heightDp)
        val cap = (screenHeight(context) * 0.105f).toInt()
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            min(requested, cap)
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

        val requested = dp(context, heightDp)
        val cap = (screenHeight(context) * 0.105f).toInt()
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            min(requested, cap)
        )
    }

    fun divider(context: Context): TextView = TextView(context).apply {
        text = "—   OR CONTINUE WITH   —"
        gravity = Gravity.CENTER
        setTextColor(LAVENDER)
        textSize = 11f
        letterSpacing = 0.13f
        setPadding(0, 0, 0, 0)
        minHeight = 0
        minimumHeight = 0
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
        includeFontPadding = false
    }

    fun subtitle(context: Context, text: String): TextView =
        TextView(context).apply {
            this.text = text
            gravity = Gravity.CENTER
            setTextColor(MUTED)
            textSize = 12f
            setPadding(0, dp(context, 2), 0, dp(context, 2))
            includeFontPadding = false
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
            textSize = 12f
            includeFontPadding = false
        })

        addView(TextView(context).apply {
            text = "  " + link
            setTextColor(PURPLE)
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            includeFontPadding = false
            isClickable = true
            setOnClickListener { onLink() }
        })
    }

    fun rememberBox(context: Context): CheckBox = CheckBox(context).apply {
        text = context.getString(R.string.remember_me)
        setTextColor(MUTED)
        textSize = 11.5f
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
        textSize = 11.5f
        gravity = Gravity.END
        setPadding(dp(context, 6), dp(context, 2), 0, dp(context, 2))
        includeFontPadding = false
        isClickable = true
        setOnClickListener { onClick() }
    }
}
