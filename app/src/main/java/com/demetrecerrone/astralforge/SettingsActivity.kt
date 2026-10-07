package com.demetrecerrone.astralforge

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.WindowInsets
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView

class SettingsActivity : Activity() {

    private var settings = GameSettings()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AuthUi.setupWindow(this)
        settings = GameSettingsStore.load(this)

        val root = FrameLayout(this)

        root.addView(
            ImageView(this).apply {
                setImageResource(R.drawable.file_00000000a45881f5ad657cc79abf1338)
                scaleType = ImageView.ScaleType.CENTER_CROP
                alpha = 0.70f
                contentDescription = null
            },
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        root.addView(
            android.view.View(this).apply {
                setBackgroundColor(Color.argb(192, 2, 4, 16))
            },
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                AuthUi.dp(this@SettingsActivity, 18),
                AuthUi.dp(this@SettingsActivity, 52),
                AuthUi.dp(this@SettingsActivity, 18),
                AuthUi.dp(this@SettingsActivity, 32)
            )
        }

        content.addView(TextView(this).apply {
            text = "SETTINGS"
            textSize = 29f
            setTextColor(Color.WHITE)
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            gravity = Gravity.CENTER
            letterSpacing = 0.07f
            setPadding(
                0,
                0,
                0,
                AuthUi.dp(this@SettingsActivity, 16)
            )
        })

        content.addView(sectionTitle("AUDIO"))
        content.addView(toggleRow(
            "Music",
            "Enable background music.",
            settings.music
        ) {
            settings = settings.copy(music = it)
            save()
            AppMusicManager.sync(this)
        })
        content.addView(toggleRow(
            "Sound Effects",
            "Enable combat and interface sound effects.",
            settings.soundEffects
        ) {
            settings = settings.copy(soundEffects = it)
            save()
        })

        content.addView(sectionTitle("GAMEPLAY"))
        content.addView(toggleRow(
            "Vibration",
            "Use haptic feedback during combat and UI actions.",
            settings.vibration
        ) {
            settings = settings.copy(vibration = it)
            save()
        })
        content.addView(toggleRow(
            "Battle Effects",
            "Show full attack, skill, and impact effects.",
            settings.battleEffects
        ) {
            settings = settings.copy(battleEffects = it)
            save()
        })
        content.addView(toggleRow(
            "Damage Numbers",
            "Show damage and healing numbers during battle.",
            settings.damageNumbers
        ) {
            settings = settings.copy(damageNumbers = it)
            save()
        })

        content.addView(sectionTitle("ACCESSIBILITY"))
        content.addView(toggleRow(
            "Reduced Motion",
            "Reduce pulsing, parallax, and nonessential motion.",
            settings.reducedMotion
        ) {
            settings = settings.copy(reducedMotion = it)
            save()
        })

        content.addView(sectionTitle("NOTIFICATIONS"))
        content.addView(toggleRow(
            "Notifications",
            "Allow game reminders and important updates.",
            settings.notifications
        ) {
            settings = settings.copy(notifications = it)
            save()
        })

        content.addView(TextView(this).apply {
            text = "Settings save automatically."
            textSize = 12f
            setTextColor(Color.rgb(149, 137, 188))
            gravity = Gravity.CENTER
            setPadding(
                0,
                AuthUi.dp(this@SettingsActivity, 14),
                0,
                0
            )
        })

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

        root.setOnApplyWindowInsetsListener { _, insets: WindowInsets ->
            content.setPadding(
                AuthUi.dp(this@SettingsActivity, 18),
                insets.systemWindowInsetTop + AuthUi.dp(this@SettingsActivity, 18),
                AuthUi.dp(this@SettingsActivity, 18),
                insets.systemWindowInsetBottom + AuthUi.dp(this@SettingsActivity, 24)
            )
            insets
        }
        root.requestApplyInsets()

        setContentView(root)
    }

    private fun sectionTitle(textValue: String): TextView {
        return TextView(this).apply {
            text = textValue
            textSize = 13f
            setTextColor(Color.rgb(195, 181, 237))
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.12f
            setPadding(
                AuthUi.dp(this@SettingsActivity, 4),
                AuthUi.dp(this@SettingsActivity, 14),
                0,
                AuthUi.dp(this@SettingsActivity, 7)
            )
        }
    }

    private fun toggleRow(
        title: String,
        subtitle: String,
        initialValue: Boolean,
        onChanged: (Boolean) -> Unit
    ): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(
                AuthUi.dp(this@SettingsActivity, 16),
                AuthUi.dp(this@SettingsActivity, 12),
                AuthUi.dp(this@SettingsActivity, 12),
                AuthUi.dp(this@SettingsActivity, 12)
            )
            background = GradientDrawable().apply {
                setColor(Color.argb(218, 7, 10, 31))
                cornerRadius = AuthUi.dp(this@SettingsActivity, 13).toFloat()
                setStroke(
                    AuthUi.dp(this@SettingsActivity, 1),
                    Color.rgb(116, 84, 203)
                )
            }

            val textColumn = LinearLayout(this@SettingsActivity).apply {
                orientation = LinearLayout.VERTICAL

                addView(TextView(this@SettingsActivity).apply {
                    text = title
                    textSize = 15f
                    setTextColor(Color.WHITE)
                    typeface = Typeface.DEFAULT_BOLD
                })

                addView(TextView(this@SettingsActivity).apply {
                    text = subtitle
                    textSize = 11.5f
                    setTextColor(Color.rgb(169, 158, 201))
                    setPadding(
                        0,
                        AuthUi.dp(this@SettingsActivity, 3),
                        0,
                        0
                    )
                })
            }

            addView(
                textColumn,
                LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )

            addView(Switch(this@SettingsActivity).apply {
                isChecked = initialValue
                thumbTintList = ColorStateList.valueOf(
                    Color.rgb(206, 184, 255)
                )
                trackTintList = ColorStateList(
                    arrayOf(
                        intArrayOf(android.R.attr.state_checked),
                        intArrayOf()
                    ),
                    intArrayOf(
                        Color.rgb(118, 72, 210),
                        Color.rgb(56, 54, 76)
                    )
                )
                setOnCheckedChangeListener { _, checked ->
                    onChanged(checked)
                }
            })

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = AuthUi.dp(this@SettingsActivity, 8)
            }
        }
    }

    private fun save() {
        GameSettingsStore.save(this, settings)
    }
}
