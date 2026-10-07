package com.demetrecerrone.astralforge

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import kotlin.math.roundToInt

class StatsActivity : Activity() {

    private lateinit var progress: PlayerProgress

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AuthUi.setupWindow(this)

        progress = ProgressionStore.load(this)

        val root = FrameLayout(this)

        root.addView(
            ImageView(this).apply {
                setImageResource(R.drawable.file_00000000bfc081f5b0a1931c13d627e8)
                scaleType = ImageView.ScaleType.CENTER_CROP
                scaleX = 1.04f
                scaleY = 1.04f
                alpha = 0.84f
                contentDescription = null
            },
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        root.addView(
            View(this).apply {
                background = GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    intArrayOf(
                        Color.argb(190, 2, 4, 18),
                        Color.argb(145, 5, 7, 26),
                        Color.argb(220, 2, 4, 16)
                    )
                )
            },
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        val scroll = ScrollView(this).apply {
            overScrollMode = View.OVER_SCROLL_NEVER
            isFillViewport = true
        }

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(46), dp(14), dp(42))
        }

        content.addView(buildHeader())
        content.addView(buildProfilePanel(), sectionParams(dp(12)))
        content.addView(buildProgressPanel(), sectionParams(dp(12)))
        content.addView(buildAttributePanel(), sectionParams(dp(12)))
        content.addView(buildCombatPanel(), sectionParams(dp(12)))
        content.addView(buildResourcesPanel(), sectionParams(dp(12)))

        content.addView(
            TextView(this).apply {
                text = "Stats update automatically as your Depthbound progression changes."
                textSize = 11.5f
                setTextColor(Color.rgb(139, 130, 176))
                gravity = Gravity.CENTER
                setPadding(dp(12), dp(16), dp(12), 0)
            }
        )

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

        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        val latest = ProgressionStore.load(this)
        if (latest != progress) {
            recreate()
        }
    }

    private fun buildHeader(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL

            addView(
                TextView(this@StatsActivity).apply {
                    text = "‹"
                    textSize = 36f
                    setTextColor(Color.WHITE)
                    gravity = Gravity.CENTER
                    contentDescription = "Back"
                    isClickable = true
                    isFocusable = true
                    setOnClickListener {
                        AppHaptics.tap(this@StatsActivity)
                        finish()
                    }
                    installTouchFeedback()
                },
                LinearLayout.LayoutParams(dp(46), dp(52))
            )

            addView(
                LinearLayout(this@StatsActivity).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER

                    addView(
                        TextView(this@StatsActivity).apply {
                            text = "STATS"
                            textSize = 27f
                            setTextColor(Color.WHITE)
                            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                            letterSpacing = 0.08f
                            gravity = Gravity.CENTER
                            setShadowLayer(8f, 0f, 0f, Color.rgb(108, 71, 228))
                        }
                    )

                    addView(
                        TextView(this@StatsActivity).apply {
                            text = "DELVER PROFILE"
                            textSize = 10.5f
                            setTextColor(Color.rgb(182, 163, 232))
                            letterSpacing = 0.14f
                            gravity = Gravity.CENTER
                        }
                    )
                },
                LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )

            addView(
                TextView(this@StatsActivity).apply {
                    text = progress.rank
                    textSize = 17f
                    setTextColor(Color.rgb(255, 222, 137))
                    typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                    gravity = Gravity.CENTER
                    background = rounded(
                        Color.argb(220, 10, 12, 35),
                        Color.rgb(207, 164, 79),
                        12
                    )
                },
                LinearLayout.LayoutParams(dp(48), dp(40))
            )
        }
    }

    private fun buildProfilePanel(): LinearLayout {
        val activeHero = HeroRosterStore.activeHero(this)
        val name = PlayerIdentityStore.getName(this)

        return panel().apply {
            gravity = Gravity.CENTER_HORIZONTAL

            addView(
                TextView(this@StatsActivity).apply {
                    text = name
                    textSize = 23f
                    setTextColor(Color.WHITE)
                    typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                    gravity = Gravity.CENTER
                }
            )

            addView(
                TextView(this@StatsActivity).apply {
                    text = progress.title
                    textSize = 12.5f
                    setTextColor(Color.rgb(191, 178, 225))
                    gravity = Gravity.CENTER
                    setPadding(0, dp(3), 0, 0)
                }
            )

            addView(
                divider(),
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(1)
                ).apply {
                    topMargin = dp(13)
                    bottomMargin = dp(12)
                }
            )

            val topRow = LinearLayout(this@StatsActivity).apply {
                orientation = LinearLayout.HORIZONTAL
            }
            topRow.addView(
                metricTile("LEVEL", progress.level.toString(), Color.rgb(171, 125, 255)),
                LinearLayout.LayoutParams(0, dp(74), 1f).apply {
                    marginEnd = dp(4)
                }
            )
            topRow.addView(
                metricTile("POWER", progress.powerRating.toString(), Color.rgb(237, 191, 87)),
                LinearLayout.LayoutParams(0, dp(74), 1f).apply {
                    marginStart = dp(4)
                    marginEnd = dp(4)
                }
            )
            topRow.addView(
                metricTile("DEPTH", progress.depth.toString(), Color.rgb(89, 191, 255)),
                LinearLayout.LayoutParams(0, dp(74), 1f).apply {
                    marginStart = dp(4)
                }
            )
            addView(topRow)

            addView(
                TextView(this@StatsActivity).apply {
                    text = "ACTIVE HERO  •  " + activeHero.displayName + "  •  " + activeHero.heroClass
                    textSize = 11f
                    setTextColor(Color.rgb(219, 204, 245))
                    typeface = Typeface.DEFAULT_BOLD
                    gravity = Gravity.CENTER
                    setPadding(0, dp(12), 0, 0)
                }
            )
        }
    }

    private fun buildProgressPanel(): LinearLayout {
        val percent = if (progress.xpToNext > 0) {
            (progress.xp.toFloat() / progress.xpToNext.toFloat() * 100f)
                .roundToInt()
                .coerceIn(0, 100)
        } else {
            0
        }

        return panel().apply {
            addView(sectionTitle("PROGRESSION"))

            addView(
                statRow("Current Rank", progress.rank)
            )
            addView(
                statRow("Class", progress.playerClass)
            )
            addView(
                statRow("Current Depth", progress.depth.toString())
            )
            addView(
                statRow("Unspent Points", progress.attributePoints.toString())
            )

            addView(
                TextView(this@StatsActivity).apply {
                    text = "EXPERIENCE  " + progress.xp + " / " + progress.xpToNext + "  (" + percent + "%)"
                    textSize = 11.5f
                    setTextColor(Color.rgb(207, 196, 234))
                    typeface = Typeface.DEFAULT_BOLD
                    setPadding(0, dp(12), 0, dp(7))
                }
            )

            addView(
                ProgressBar(
                    this@StatsActivity,
                    null,
                    android.R.attr.progressBarStyleHorizontal
                ).apply {
                    max = progress.xpToNext.coerceAtLeast(1)
                    this.progress = progress.xp.coerceIn(0, max)
                    progressTintList = ColorStateList.valueOf(Color.rgb(145, 72, 255))
                    progressBackgroundTintList = ColorStateList.valueOf(Color.rgb(35, 29, 66))
                },
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(14)
                )
            )
        }
    }

    private fun buildAttributePanel(): LinearLayout {
        return panel().apply {
            addView(sectionTitle("CORE ATTRIBUTES"))

            addView(
                TextView(this@StatsActivity).apply {
                    text = "UNSPENT ATTRIBUTE POINTS  •  " + progress.attributePoints
                    textSize = 11.5f
                    setTextColor(
                        if (progress.attributePoints > 0) {
                            Color.rgb(255, 220, 127)
                        } else {
                            Color.rgb(151, 140, 186)
                        }
                    )
                    typeface = Typeface.DEFAULT_BOLD
                    gravity = Gravity.CENTER
                    background = rounded(
                        Color.argb(195, 10, 12, 35),
                        if (progress.attributePoints > 0) {
                            Color.rgb(210, 168, 78)
                        } else {
                            Color.rgb(74, 59, 115)
                        },
                        11
                    )
                    setPadding(dp(8), dp(7), dp(8), dp(7))
                },
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = dp(8)
                }
            )

            val rows = listOf(
                Triple("Strength", progress.strength, Color.rgb(235, 103, 84)),
                Triple("Vitality", progress.vitality, Color.rgb(101, 206, 131)),
                Triple("Agility", progress.agility, Color.rgb(91, 190, 237)),
                Triple("Intelligence", progress.intelligence, Color.rgb(151, 115, 243)),
                Triple("Luck", progress.luck, Color.rgb(237, 191, 87))
            )

            rows.forEach { row ->
                addView(
                    attributeRow(
                        row.first,
                        row.second,
                        row.third
                    )
                )
            }

            addView(
                TextView(this@StatsActivity).apply {
                    text = "Each point permanently increases that attribute and immediately updates derived combat stats."
                    textSize = 10.5f
                    setTextColor(Color.rgb(139, 130, 176))
                    gravity = Gravity.CENTER
                    setPadding(dp(6), dp(9), dp(6), 0)
                }
            )
        }
    }

    private fun buildCombatPanel(): LinearLayout {
        return panel().apply {
            addView(sectionTitle("COMBAT STATS"))

            val rowOne = LinearLayout(this@StatsActivity).apply {
                orientation = LinearLayout.HORIZONTAL
            }
            rowOne.addView(
                metricTile("MAX HP", progress.maxHp.toString(), Color.rgb(102, 206, 132)),
                LinearLayout.LayoutParams(0, dp(74), 1f).apply {
                    marginEnd = dp(4)
                }
            )
            rowOne.addView(
                metricTile("ATTACK", progress.attack.toString(), Color.rgb(235, 103, 84)),
                LinearLayout.LayoutParams(0, dp(74), 1f).apply {
                    marginStart = dp(4)
                    marginEnd = dp(4)
                }
            )
            rowOne.addView(
                metricTile("DEFENSE", progress.defense.toString(), Color.rgb(100, 171, 237)),
                LinearLayout.LayoutParams(0, dp(74), 1f).apply {
                    marginStart = dp(4)
                }
            )
            addView(rowOne)

            val rowTwo = LinearLayout(this@StatsActivity).apply {
                orientation = LinearLayout.HORIZONTAL
            }
            rowTwo.addView(
                metricTile("MAGIC", progress.magicPower.toString(), Color.rgb(167, 117, 248)),
                LinearLayout.LayoutParams(0, dp(74), 1f).apply {
                    marginEnd = dp(4)
                }
            )
            rowTwo.addView(
                metricTile("CRIT", progress.critChance.toString() + "%", Color.rgb(238, 194, 92)),
                LinearLayout.LayoutParams(0, dp(74), 1f).apply {
                    marginStart = dp(4)
                    marginEnd = dp(4)
                }
            )
            rowTwo.addView(
                metricTile("ENERGY", progress.energy.toString(), Color.rgb(86, 196, 239)),
                LinearLayout.LayoutParams(0, dp(74), 1f).apply {
                    marginStart = dp(4)
                }
            )
            addView(
                rowTwo,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(74)
                ).apply {
                    topMargin = dp(8)
                }
            )
        }
    }

    private fun buildResourcesPanel(): LinearLayout {
        val energyPercent = if (progress.maxEnergy > 0) {
            (progress.energy.toFloat() / progress.maxEnergy.toFloat() * 100f)
                .roundToInt()
                .coerceIn(0, 100)
        } else {
            0
        }

        return panel().apply {
            addView(sectionTitle("RESOURCES"))

            addView(statRow("Gold", progress.gold.toString(), Color.rgb(244, 203, 93)))
            addView(statRow("Astral Shards", progress.astralShards.toString(), Color.rgb(151, 127, 255)))
            addView(statRow("Energy", progress.energy.toString() + " / " + progress.maxEnergy))

            addView(
                ProgressBar(
                    this@StatsActivity,
                    null,
                    android.R.attr.progressBarStyleHorizontal
                ).apply {
                    max = 100
                    progress = energyPercent
                    progressTintList = ColorStateList.valueOf(Color.rgb(70, 187, 244))
                    progressBackgroundTintList = ColorStateList.valueOf(Color.rgb(35, 29, 66))
                },
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(12)
                ).apply {
                    topMargin = dp(10)
                }
            )
        }
    }

    private fun panel(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(15), dp(16), dp(16))
            background = rounded(
                Color.argb(226, 5, 9, 29),
                Color.rgb(103, 76, 180),
                18
            )
        }
    }

    private fun sectionTitle(value: String): TextView {
        return TextView(this).apply {
            text = value
            textSize = 15f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.08f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(10))
        }
    }

    private fun statRow(
        label: String,
        value: String,
        valueColor: Int = Color.WHITE
    ): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(6), 0, dp(6))

            addView(
                TextView(this@StatsActivity).apply {
                    text = label
                    textSize = 12.5f
                    setTextColor(Color.rgb(166, 154, 201))
                },
                LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )

            addView(
                TextView(this@StatsActivity).apply {
                    text = value
                    textSize = 14f
                    setTextColor(valueColor)
                    typeface = Typeface.DEFAULT_BOLD
                    gravity = Gravity.END
                }
            )
        }
    }

    private fun attributeRow(
        label: String,
        value: Int,
        accent: Int
    ): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(5), 0, dp(5))

            addView(
                View(this@StatsActivity).apply {
                    background = GradientDrawable().apply {
                        shape = GradientDrawable.OVAL
                        setColor(accent)
                    }
                },
                LinearLayout.LayoutParams(dp(8), dp(8)).apply {
                    marginEnd = dp(9)
                }
            )

            addView(
                TextView(this@StatsActivity).apply {
                    text = label.uppercase()
                    textSize = 11.5f
                    setTextColor(Color.rgb(184, 171, 216))
                    typeface = Typeface.DEFAULT_BOLD
                },
                LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )

            addView(
                TextView(this@StatsActivity).apply {
                    text = value.toString()
                    textSize = 16f
                    setTextColor(accent)
                    typeface = Typeface.DEFAULT_BOLD
                    gravity = Gravity.END
                },
                LinearLayout.LayoutParams(
                    dp(44),
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )

            addView(
                TextView(this@StatsActivity).apply {
                    text = "+"
                    textSize = 20f
                    setTextColor(
                        if (progress.attributePoints > 0) Color.WHITE
                        else Color.rgb(104, 96, 126)
                    )
                    typeface = Typeface.DEFAULT_BOLD
                    gravity = Gravity.CENTER
                    contentDescription = "Add one point to " + label
                    isEnabled = progress.attributePoints > 0
                    isClickable = progress.attributePoints > 0
                    isFocusable = progress.attributePoints > 0
                    alpha = if (progress.attributePoints > 0) 1f else 0.45f
                    background = rounded(
                        if (progress.attributePoints > 0) {
                            Color.argb(235, 61, 38, 124)
                        } else {
                            Color.argb(150, 21, 21, 31)
                        },
                        if (progress.attributePoints > 0) accent
                        else Color.rgb(58, 52, 75),
                        10
                    )
                    if (progress.attributePoints > 0) {
                        installTouchFeedback()
                        setOnClickListener {
                            AppHaptics.tap(this@StatsActivity)
                            allocateAttribute(label)
                        }
                    }
                },
                LinearLayout.LayoutParams(dp(38), dp(34)).apply {
                    marginStart = dp(8)
                }
            )
        }
    }

    private fun allocateAttribute(label: String) {
        if (progress.attributePoints <= 0) return

        progress = when (label) {
            "Strength" -> progress.copy(
                strength = progress.strength + 1,
                attributePoints = progress.attributePoints - 1
            )
            "Vitality" -> progress.copy(
                vitality = progress.vitality + 1,
                attributePoints = progress.attributePoints - 1
            )
            "Agility" -> progress.copy(
                agility = progress.agility + 1,
                attributePoints = progress.attributePoints - 1
            )
            "Intelligence" -> progress.copy(
                intelligence = progress.intelligence + 1,
                attributePoints = progress.attributePoints - 1
            )
            "Luck" -> progress.copy(
                luck = progress.luck + 1,
                attributePoints = progress.attributePoints - 1
            )
            else -> return
        }

        ProgressionStore.save(this, progress)
        recreate()
    }

    private fun metricTile(
        label: String,
        value: String,
        accent: Int
    ): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = rounded(
                Color.argb(205, 9, 12, 35),
                Color.argb(
                    180,
                    Color.red(accent),
                    Color.green(accent),
                    Color.blue(accent)
                ),
                12
            )

            addView(
                TextView(this@StatsActivity).apply {
                    text = label
                    textSize = 9.5f
                    setTextColor(Color.rgb(166, 154, 201))
                    typeface = Typeface.DEFAULT_BOLD
                    letterSpacing = 0.05f
                    gravity = Gravity.CENTER
                }
            )

            addView(
                TextView(this@StatsActivity).apply {
                    text = value
                    textSize = 18f
                    setTextColor(accent)
                    typeface = Typeface.DEFAULT_BOLD
                    gravity = Gravity.CENTER
                    setPadding(0, dp(2), 0, 0)
                }
            )
        }
    }

    private fun divider(): View {
        return View(this).apply {
            setBackgroundColor(Color.argb(130, 117, 91, 173))
        }
    }

    private fun rounded(
        fill: Int,
        stroke: Int,
        radiusDp: Int,
        strokeDp: Int = 1
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(fill)
            cornerRadius = dp(radiusDp).toFloat()
            setStroke(dp(strokeDp), stroke)
        }
    }

    private fun sectionParams(topMargin: Int): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            this.topMargin = topMargin
        }
    }

    private fun View.installTouchFeedback() {
        setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    view.animate()
                        .scaleX(0.975f)
                        .scaleY(0.975f)
                        .alpha(0.88f)
                        .setDuration(70)
                        .start()
                }
                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> {
                    view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .alpha(1f)
                        .setDuration(110)
                        .start()
                }
            }
            false
        }
    }

    private fun dp(value: Int): Int = AuthUi.dp(this, value)
}
