package com.demetrecerrone.astralforge

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView

class SettingsActivity : Activity() {

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST = 4107
    }

    private var settings = GameSettings()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AuthUi.setupWindow(this)
        settings = GameSettingsStore.load(this)

        if (needsNotificationPermission() && settings.notifications) {
            settings = settings.copy(notifications = false)
            save()
        }

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
            View(this).apply {
                setBackgroundColor(Color.argb(192, 2, 4, 16))
            },
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        val scroll = ScrollView(this).apply {
            overScrollMode = View.OVER_SCROLL_NEVER
        }

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
        })

        content.addView(TextView(this).apply {
            text = "Changes save automatically"
            textSize = 11.5f
            setTextColor(Color.rgb(157, 145, 194))
            gravity = Gravity.CENTER
            setPadding(
                0,
                AuthUi.dp(this@SettingsActivity, 4),
                0,
                AuthUi.dp(this@SettingsActivity, 12)
            )
        })

        content.addView(sectionTitle("AUDIO"))

        content.addView(toggleRow(
            "Music",
            "Play the current background soundtrack.",
            settings.music
        ) {
            settings = settings.copy(music = it)
            save()
            AppMusicManager.sync(this)
        })

        content.addView(sliderRow(
            "Music Volume",
            "Controls background music volume.",
            settings.musicVolume
        ) {
            settings = settings.copy(musicVolume = it)
            save()
            AppMusicManager.setVolume(it)
        })

        content.addView(toggleRow(
            "Sound Effects",
            "Enable interface, skill, hit, and combat sounds.",
            settings.soundEffects
        ) {
            settings = settings.copy(soundEffects = it)
            save()
        })

        content.addView(sliderRow(
            "SFX Volume",
            "Volume used by interface and combat effects.",
            settings.soundEffectsVolume
        ) {
            settings = settings.copy(soundEffectsVolume = it)
            save()
        })

        content.addView(sectionTitle("GRAPHICS & PERFORMANCE"))

        content.addView(choiceRow(
            "Visual Quality",
            "Controls glow intensity and effect detail.",
            settings.visualQuality,
            listOf("LOW", "MEDIUM", "HIGH"),
            ::qualityLabel
        ) { value ->
            settings = settings.copy(visualQuality = value)
            save()
        })

        content.addView(sliderRow(
            "Particle Density",
            "Controls how many ambient particles are drawn.",
            settings.particleDensity
        ) {
            settings = settings.copy(particleDensity = it)
            save()
        })

        content.addView(toggleRow(
            "Battery Saver",
            "Reduces particles, caps animated effects near 30 FPS, and disables hub parallax/pulsing.",
            settings.batterySaver
        ) {
            settings = settings.copy(batterySaver = it)
            save()
        })

        content.addView(choiceRow(
            "FPS Preference",
            "Choose the preferred refresh rate for animated game effects.",
            settings.fpsPreference,
            listOf("SYSTEM", "30", "60"),
            ::fpsLabel
        ) { value ->
            settings = settings.copy(fpsPreference = value)
            save()
        })

        content.addView(sectionTitle("GAMEPLAY"))

        content.addView(toggleRow(
            "Vibration",
            "Use haptic feedback for supported taps and combat events.",
            settings.vibration
        ) {
            settings = settings.copy(vibration = it)
            save()
            if (it) AppHaptics.tap(this)
        })

        content.addView(toggleRow(
            "Battle Effects",
            "Show full attack, skill, impact, and ability effects.",
            settings.battleEffects
        ) {
            settings = settings.copy(battleEffects = it)
            save()
        })

        content.addView(toggleRow(
            "Damage Numbers",
            "Show damage and healing values during battle.",
            settings.damageNumbers
        ) {
            settings = settings.copy(damageNumbers = it)
            save()
        })

        content.addView(sectionTitle("ACCESSIBILITY"))

        content.addView(toggleRow(
            "Reduced Motion",
            "Freezes nonessential hub motion while keeping the interface readable.",
            settings.reducedMotion
        ) {
            settings = settings.copy(reducedMotion = it)
            save()
        })

        content.addView(sectionTitle("NOTIFICATIONS"))

        content.addView(toggleRow(
            "Game Notifications",
            "Allow future energy, event, reward, and important game reminders.",
            settings.notifications
        ) { enabled ->
            if (enabled && needsNotificationPermission()) {
                requestPermissions(
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST
                )
            } else {
                settings = settings.copy(notifications = enabled)
                save()
            }
        })

        content.addView(sectionTitle("BATTLE V2 PREVIEW"))

        content.addView(actionRow(
            "OPEN BATTLE V2 TEST",
            "Launch the isolated Battle V2 renderer. The normal Battle button remains unchanged.",
            danger = false
        ) {
            startActivity(
                Intent(
                    this@SettingsActivity,
                    BattleV2Activity::class.java
                )
            )
        })

        content.addView(sectionTitle("RESET"))

        content.addView(actionRow(
            "RESET SETTINGS",
            "Restore all settings to their default values.",
            danger = true
        ) {
            settings = GameSettingsStore.reset(this)
            AppMusicManager.sync(this)
            AppMusicManager.setVolume(settings.musicVolume)
            recreate()
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
        return basePanel().apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL

            val textColumn = LinearLayout(this@SettingsActivity).apply {
                orientation = LinearLayout.VERTICAL
                addView(primaryText(title))
                addView(secondaryText(subtitle))
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
                thumbTintList = ColorStateList(
                    arrayOf(
                        intArrayOf(android.R.attr.state_checked),
                        intArrayOf()
                    ),
                    intArrayOf(
                        Color.rgb(224, 205, 255),
                        Color.rgb(150, 143, 169)
                    )
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
        }
    }

    private fun sliderRow(
        title: String,
        subtitle: String,
        initialValue: Int,
        onChanged: (Int) -> Unit
    ): LinearLayout {
        return basePanel().apply {
            orientation = LinearLayout.VERTICAL

            val header = LinearLayout(this@SettingsActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            header.addView(
                primaryText(title),
                LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )

            val valueText = TextView(this@SettingsActivity).apply {
                text = initialValue.coerceIn(0, 100).toString() + "%"
                textSize = 13f
                setTextColor(Color.rgb(222, 207, 255))
                typeface = Typeface.DEFAULT_BOLD
            }
            header.addView(valueText)

            addView(
                header,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )

            addView(secondaryText(subtitle))

            addView(SeekBar(this@SettingsActivity).apply {
                max = 100
                progress = initialValue.coerceIn(0, 100)
                progressTintList = ColorStateList.valueOf(
                    Color.rgb(130, 86, 233)
                )
                thumbTintList = ColorStateList.valueOf(
                    Color.rgb(222, 203, 255)
                )
                setPadding(
                    0,
                    AuthUi.dp(this@SettingsActivity, 5),
                    0,
                    0
                )
                setOnSeekBarChangeListener(
                    object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(
                            seekBar: SeekBar?,
                            progress: Int,
                            fromUser: Boolean
                        ) {
                            valueText.text = progress.toString() + "%"
                            if (fromUser) onChanged(progress)
                        }

                        override fun onStartTrackingTouch(seekBar: SeekBar?) {
                            AppHaptics.tap(this@SettingsActivity)
                        }

                        override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
                    }
                )
            })
        }
    }

    private fun choiceRow(
        title: String,
        subtitle: String,
        initialValue: String,
        choices: List<String>,
        labelFor: (String) -> String,
        onChanged: (String) -> Unit
    ): LinearLayout {
        var current = if (initialValue in choices) initialValue else choices.first()

        return basePanel().apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            isClickable = true
            isFocusable = true

            val textColumn = LinearLayout(this@SettingsActivity).apply {
                orientation = LinearLayout.VERTICAL
                addView(primaryText(title))
                addView(secondaryText(subtitle))
            }
            addView(
                textColumn,
                LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )

            val valueText = TextView(this@SettingsActivity).apply {
                text = labelFor(current)
                textSize = 13f
                setTextColor(Color.rgb(222, 207, 255))
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                setPadding(
                    AuthUi.dp(this@SettingsActivity, 10),
                    0,
                    0,
                    0
                )
            }
            addView(valueText)

            setOnClickListener {
                AppHaptics.tap(this@SettingsActivity)
                val index = choices.indexOf(current)
                current = choices[(index + 1) % choices.size]
                valueText.text = labelFor(current)
                onChanged(current)
            }

            setOnTouchListener { view, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        view.animate()
                            .scaleX(0.987f)
                            .scaleY(0.987f)
                            .alpha(0.9f)
                            .setDuration(55)
                            .start()
                    }
                    MotionEvent.ACTION_UP,
                    MotionEvent.ACTION_CANCEL -> {
                        view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .alpha(1f)
                            .setDuration(85)
                            .start()
                    }
                }
                false
            }
        }
    }

    private fun actionRow(
        title: String,
        subtitle: String,
        danger: Boolean,
        onClick: () -> Unit
    ): LinearLayout {
        return basePanel(
            if (danger) Color.rgb(173, 66, 94)
            else Color.rgb(116, 84, 203)
        ).apply {
            orientation = LinearLayout.VERTICAL
            isClickable = true
            isFocusable = true

            addView(TextView(this@SettingsActivity).apply {
                text = title
                textSize = 15f
                setTextColor(
                    if (danger) Color.rgb(255, 168, 185)
                    else Color.WHITE
                )
                typeface = Typeface.DEFAULT_BOLD
            })
            addView(secondaryText(subtitle))

            setOnClickListener {
                AppHaptics.tap(this@SettingsActivity)
                onClick()
            }
            setOnTouchListener { view, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        view.animate()
                            .scaleX(0.985f)
                            .scaleY(0.985f)
                            .alpha(0.88f)
                            .setDuration(60)
                            .start()
                    }
                    MotionEvent.ACTION_UP,
                    MotionEvent.ACTION_CANCEL -> {
                        view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .alpha(1f)
                            .setDuration(90)
                            .start()
                    }
                }
                false
            }
        }
    }

    private fun basePanel(
        strokeColor: Int = Color.rgb(116, 84, 203)
    ): LinearLayout {
        return LinearLayout(this).apply {
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
                    strokeColor
                )
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = AuthUi.dp(this@SettingsActivity, 8)
            }
        }
    }

    private fun primaryText(value: String): TextView {
        return TextView(this).apply {
            text = value
            textSize = 15f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
        }
    }

    private fun secondaryText(value: String): TextView {
        return TextView(this).apply {
            text = value
            textSize = 11.5f
            setTextColor(Color.rgb(169, 158, 201))
            setPadding(
                0,
                AuthUi.dp(this@SettingsActivity, 3),
                0,
                0
            )
        }
    }

    private fun qualityLabel(value: String): String {
        return when (value) {
            "LOW" -> "Low"
            "MEDIUM" -> "Medium"
            else -> "High"
        }
    }

    private fun fpsLabel(value: String): String {
        return when (value) {
            "30" -> "30 FPS"
            "60" -> "60 FPS"
            else -> "System"
        }
    }

    private fun needsNotificationPermission(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        if (requestCode == NOTIFICATION_PERMISSION_REQUEST) {
            val granted =
                grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED

            settings = settings.copy(notifications = granted)
            save()
            recreate()
        }
    }

    private fun save() {
        GameSettingsStore.save(this, settings)
    }
}
