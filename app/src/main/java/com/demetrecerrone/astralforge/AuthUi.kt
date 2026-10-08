package com.demetrecerrone.astralforge

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.text.InputType
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.CheckBox
import android.widget.ToggleButton
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

object AuthUi {
    private const val PURPLE = 0xFFC25BFF.toInt()
    private const val LAVENDER = 0xFFE4D9FF.toInt()
    private const val MUTED = 0xFFE1D9F4.toInt()

    fun dp(context: Context, value: Int): Int =
        (value * context.resources.displayMetrics.density).toInt()

    private fun screenHeight(context: Context): Int =
        context.resources.displayMetrics.heightPixels

    private fun screenWidth(context: Context): Int =
        context.resources.displayMetrics.widthPixels

    private fun fieldHeight(context: Context): Int =
        (screenHeight(context) * 0.102f).toInt().coerceAtLeast(64)

    private fun controlGap(context: Context): Int =
        (screenHeight(context) * 0.008f).toInt().coerceAtLeast(4)

    fun setupWindow(activity: Activity) {
        activity.window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )
        FullscreenUi.apply(activity)
    }

    fun createScreen(
        activity: Activity,
        backgroundRes: Int,
        topFraction: Float,
        formWidthFraction: Float
    ): LinearLayout {
        val root = FrameLayout(activity).apply {
            setBackgroundColor(Color.rgb(3, 5, 14))
        }

        root.addView(
            ImageView(activity).apply {
                setImageResource(backgroundRes)
                scaleType = ImageView.ScaleType.CENTER_CROP
                contentDescription = null
            },
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        val sw = screenWidth(activity)
        val sh = screenHeight(activity)
        val top = (sh * topFraction).toInt()
        val bottomInset = (sh * 0.018f).toInt()
        val formWidth = (sw * formWidthFraction).toInt()
        val formHeight = (sh - top - bottomInset)
            .coerceAtLeast((sh * 0.42f).toInt())

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
                (sw * 0.004f).toInt(),
                (sh * 0.004f).toInt(),
                (sw * 0.004f).toInt(),
                (sh * 0.008f).toInt()
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
            FrameLayout.LayoutParams(formWidth, formHeight).apply {
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
        frameRes: Int,
        isPassword: Boolean = false,
        inputType: Int = InputType.TYPE_CLASS_TEXT,
        compact: Boolean = false
    ): EditText {
        return EditText(context).apply {
            this.hint = hint
            setHintTextColor(Color.rgb(219, 208, 247))
            setTextColor(Color.WHITE)
            textSize = if (compact) 11.5f else 13f
            setSingleLine(true)
            setHorizontallyScrolling(true)
            ellipsize = android.text.TextUtils.TruncateAt.END
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(context, 8), 0, dp(context, 8), 0)
            setBackgroundResource(frameRes)
            addOnLayoutChangeListener { view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
                val fieldWidth = (right - left).coerceAtLeast(1)
                val leftPad =
                    (fieldWidth * if (compact) 0.29f else 0.22f).toInt()
                val rightPad =
                    if (isPassword) {
                        (fieldWidth * if (compact) 0.19f else 0.20f).toInt()
                    } else {
                        (fieldWidth * if (compact) 0.06f else 0.08f).toInt()
                    }
                if (paddingLeft != leftPad || paddingRight != rightPad) {
                    setPadding(leftPad, 0, rightPad, 0)
                }
            }
            this.inputType = inputType
            minHeight = 0
            minimumHeight = 0
            includeFontPadding = false

            if (isPassword) {
                transformationMethod = PasswordTransformationMethod.getInstance()
                setSelection(text.length)
                setOnTouchListener { _, event ->
                    if (
                        event.action == MotionEvent.ACTION_UP &&
                        event.x >= width * 0.80f
                    ) {
                        val hidden =
                            transformationMethod is PasswordTransformationMethod
                        transformationMethod =
                            if (hidden) {
                                HideReturnsTransformationMethod.getInstance()
                            } else {
                                PasswordTransformationMethod.getInstance()
                            }
                        setSelection(text.length)
                        true
                    } else {
                        false
                    }
                }
            }
        }
    }

    fun addField(
        parent: LinearLayout,
        field: EditText,
        topMarginDp: Int = 0
    ) {
        val context = parent.context
        val gap =
            if (topMarginDp > 0) dp(context, topMarginDp)
            else controlGap(context)

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
        widthFraction: Float,
        heightFraction: Float,
        onClick: () -> Unit
    ): ImageView = ImageView(context).apply {
        setImageResource(drawableRes)
        scaleType = ImageView.ScaleType.FIT_XY
        isClickable = true
        isFocusable = true
        contentDescription = description
        setOnClickListener { onClick() }
        setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    view.alpha = 0.78f
                    view.scaleX = 0.985f
                    view.scaleY = 0.985f
                }
                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> {
                    view.alpha = 1f
                    view.scaleX = 1f
                    view.scaleY = 1f
                }
            }
            false
        }

        layoutParams = LinearLayout.LayoutParams(
            (screenWidth(context) * widthFraction).toInt(),
            (screenHeight(context) * heightFraction).toInt()
        ).apply {
            gravity = Gravity.CENTER_HORIZONTAL
        }
    }

    fun divider(context: Context): TextView = TextView(context).apply {
        text = "✦   OR CONTINUE WITH   ✦"
        gravity = Gravity.CENTER
        setTextColor(LAVENDER)
        textSize = 10.5f
        letterSpacing = 0.12f
        includeFontPadding = false
        setPadding(0, dp(context, 1), 0, dp(context, 1))
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
            textSize = 10.5f
            setPadding(0, dp(context, 1), 0, dp(context, 2))
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
            textSize = 11f
            includeFontPadding = false
        })

        addView(TextView(context).apply {
            text = "  " + link
            setTextColor(PURPLE)
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            includeFontPadding = false
            isClickable = true
            setOnClickListener { onLink() }
        })
    }

    fun rememberBox(context: Context): CheckBox = CheckBox(context).apply {
        text = context.getString(R.string.remember_me)
        setTextColor(MUTED)
        textSize = 10.5f
        buttonTintList = ColorStateList.valueOf(PURPLE)
        minHeight = 0
        minimumHeight = 0
        setPadding(0, 0, 0, 0)
    }


    // Coordinates are expressed on the 1672 by 941 source artwork.
    fun embeddedCanvas(activity: Activity, drawable: Int): FrameLayout {
        val root = FrameLayout(activity)
        root.addView(ImageView(activity).apply {
            setImageResource(drawable)
            scaleType = ImageView.ScaleType.FIT_XY
        }, FrameLayout.LayoutParams(-1, -1))
        activity.setContentView(root)
        FullscreenUi.apply(activity)
        return root
    }

    fun place(root: FrameLayout, view: View, x: Int, y: Int, w: Int, h: Int) {
        val dm = root.context.resources.displayMetrics
        root.addView(view, FrameLayout.LayoutParams(
            (w * dm.widthPixels / 1672f).toInt(),
            (h * dm.heightPixels / 941f).toInt()
        ).apply {
            leftMargin = (x * dm.widthPixels / 1672f).toInt()
            topMargin = (y * dm.heightPixels / 941f).toInt()
        })
    }

    fun overlayField(context: Context, hintText: String, inputTypeValue: Int,
                     password: Boolean = false): EditText =
        EditText(context).apply {
            hint = hintText
            setHintTextColor(0xFFDFD4F7.toInt())
            setTextColor(Color.WHITE)
            textSize = 14f
            gravity = Gravity.CENTER_VERTICAL
            background = null
            setSingleLine(true)
            setPadding(0, 0, 0, 0)
            inputType = inputTypeValue
            if (password) transformationMethod = PasswordTransformationMethod.getInstance()
        }

    fun overlayEye(root: FrameLayout, edit: EditText, x: Int, y: Int) {
        place(root, View(root.context).apply {
            contentDescription = "Show or hide password"
            isClickable = true
            isFocusable = true
            setOnClickListener {
                val hidden = edit.transformationMethod is PasswordTransformationMethod
                edit.transformationMethod = if (hidden) {
                    HideReturnsTransformationMethod.getInstance()
                } else {
                    PasswordTransformationMethod.getInstance()
                }
                edit.setSelection(edit.text.length)
            }
        }, x, y, 74, 70)
    }

    fun overlayButton(root: FrameLayout, title: String,
                      x: Int, y: Int, w: Int, h: Int,
                      onClick: () -> Unit) {
        place(root, TextView(root.context).apply {
            text = title
            gravity = Gravity.CENTER
            textSize = 14f
            setTextColor(Color.WHITE)
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            isClickable = true
            isFocusable = true
            contentDescription = title
            setOnClickListener { onClick() }
        }, x, y, w, h)
    }

    fun overlayLink(root: FrameLayout, title: String,
                    x: Int, y: Int, w: Int, h: Int,
                    onClick: () -> Unit) {
        place(root, TextView(root.context).apply {
            text = title
            gravity = Gravity.CENTER
            textSize = 13f
            setTextColor(0xFFD4A1FF.toInt())
            isClickable = true
            setOnClickListener { onClick() }
        }, x, y, w, h)
    }

    fun separateRemember(context: Context, checked: Boolean): ToggleButton =
        ToggleButton(context).apply {
            textOff = "Remember Me"
            textOn = "✦ Remember On"
            isChecked = checked
            textSize = 12f
            isAllCaps = false
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply {
                setColor(0xCC140B2E.toInt())
                cornerRadius = 15f
                setStroke(2, 0xFFB266EB.toInt())
            }
        }

    fun smallLink(
        context: Context,
        text: String,
        onClick: () -> Unit
    ): TextView = TextView(context).apply {
        this.text = text
        setTextColor(PURPLE)
        textSize = 10.5f
        gravity = Gravity.END
        setPadding(dp(context, 6), dp(context, 1), 0, dp(context, 1))
        includeFontPadding = false
        isClickable = true
        setOnClickListener { onClick() }
    }
}
