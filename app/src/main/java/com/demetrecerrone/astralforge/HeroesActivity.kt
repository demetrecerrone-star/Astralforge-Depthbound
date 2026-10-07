package com.demetrecerrone.astralforge

import android.app.Activity
import android.content.Intent
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
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class HeroesActivity : Activity() {

    private lateinit var progress: PlayerProgress
    private lateinit var previewActor: SkeletalActorView
    private lateinit var previewFallback: ImageView
    private lateinit var previewSigil: TextView
    private lateinit var nameText: TextView
    private lateinit var metaText: TextView
    private lateinit var powerText: TextView
    private lateinit var skillTitleText: TextView
    private lateinit var skillBodyText: TextView
    private lateinit var loreText: TextView
    private lateinit var activeBadge: TextView
    private lateinit var favoriteButton: TextView
    private lateinit var activateButton: TextView
    private lateinit var rosterCountText: TextView
    private lateinit var rosterContainer: LinearLayout
    private lateinit var filterContainer: LinearLayout

    private var selectedHeroId = "knight"
    private var activeFilter = "ALL"
    private var visualLoadToken = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AuthUi.setupWindow(this)

        progress = ProgressionStore.load(this)
        selectedHeroId = HeroRosterStore.activeHeroId(this)

        val root = FrameLayout(this)

        root.addView(
            ImageView(this).apply {
                setImageResource(R.drawable.file_00000000bfc081f5b0a1931c13d627e8)
                scaleType = ImageView.ScaleType.CENTER_CROP
                scaleX = 1.04f
                scaleY = 1.04f
                alpha = 0.86f
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
                        Color.argb(180, 2, 4, 18),
                        Color.argb(120, 4, 6, 24),
                        Color.argb(205, 2, 4, 16)
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
            setPadding(
                dp(14),
                dp(46),
                dp(14),
                dp(42)
            )
        }

        content.addView(buildHeader())
        content.addView(buildPreviewPanel(), sectionParams(dp(12)))
        content.addView(buildDetailPanel(), sectionParams(dp(12)))
        content.addView(buildRosterHeader(), sectionParams(dp(18)))

        filterContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        content.addView(
            filterContainer,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(42)
            ).apply {
                topMargin = dp(8)
            }
        )

        rosterContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        content.addView(
            rosterContainer,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(6)
            }
        )

        content.addView(
            TextView(this).apply {
                text = "Select a hero to inspect them. Tap the large hero preview to play their attack animation."
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
        rebuildFilters()
        renderSelectedHero()
        rebuildRoster()
    }

    override fun onResume() {
        super.onResume()
        if (::rosterContainer.isInitialized) {
            progress = ProgressionStore.load(this)
            renderSelectedHero()
            rebuildRoster()
        }
    }

    private fun buildHeader(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL

            addView(
                TextView(this@HeroesActivity).apply {
                    text = "‹"
                    textSize = 36f
                    setTextColor(Color.WHITE)
                    gravity = Gravity.CENTER
                    contentDescription = "Back"
                    isClickable = true
                    isFocusable = true
                    setOnClickListener {
                        AppHaptics.tap(this@HeroesActivity)
                        finish()
                    }
                    installTouchFeedback()
                },
                LinearLayout.LayoutParams(
                    dp(46),
                    dp(52)
                )
            )

            addView(
                LinearLayout(this@HeroesActivity).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER

                    addView(
                        TextView(this@HeroesActivity).apply {
                            text = "HEROES"
                            textSize = 27f
                            setTextColor(Color.WHITE)
                            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                            letterSpacing = 0.08f
                            gravity = Gravity.CENTER
                            setShadowLayer(8f, 0f, 0f, Color.rgb(108, 71, 228))
                        }
                    )

                    addView(
                        TextView(this@HeroesActivity).apply {
                            text = "ASTRAL ROSTER"
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

            rosterCountText = TextView(this@HeroesActivity).apply {
                text = HeroRosterStore.allHeroes().size.toString() + "/" +
                    HeroRosterStore.allHeroes().size.toString()
                textSize = 11f
                setTextColor(Color.rgb(241, 203, 111))
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                background = rounded(
                    Color.argb(210, 8, 11, 34),
                    Color.rgb(162, 125, 225),
                    12
                )
                setPadding(dp(8), dp(5), dp(8), dp(5))
            }
            addView(
                rosterCountText,
                LinearLayout.LayoutParams(
                    dp(50),
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )
        }
    }

    private fun buildPreviewPanel(): FrameLayout {
        val panel = FrameLayout(this).apply {
            background = rounded(
                Color.argb(225, 4, 8, 26),
                Color.rgb(124, 90, 211),
                20
            )
            isClickable = true
            isFocusable = true
            setOnClickListener {
                AppHaptics.tap(this@HeroesActivity)
                if (previewActor.visibility == View.VISIBLE && previewActor.isRigLoaded) {
                    previewActor.play("attack")
                } else if (previewFallback.visibility == View.VISIBLE) {
                    previewFallback.animate()
                        .scaleX(1.06f)
                        .scaleY(1.06f)
                        .setDuration(100)
                        .withEndAction {
                            previewFallback.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(140)
                                .start()
                        }
                        .start()
                }
            }
        }

        panel.addView(
            View(this).apply {
                background = GradientDrawable(
                    GradientDrawable.Orientation.TL_BR,
                    intArrayOf(
                        Color.argb(125, 83, 52, 168),
                        Color.argb(20, 18, 28, 74),
                        Color.argb(90, 8, 12, 38)
                    )
                ).apply {
                    cornerRadius = dp(20).toFloat()
                }
            },
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        previewSigil = TextView(this).apply {
            text = "K"
            textSize = 64f
            setTextColor(Color.argb(150, 207, 188, 255))
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            gravity = Gravity.CENTER
            setShadowLayer(22f, 0f, 0f, Color.rgb(111, 75, 221))
        }
        panel.addView(
            previewSigil,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            ).apply {
                leftMargin = dp(30)
                rightMargin = dp(30)
                topMargin = dp(18)
                bottomMargin = dp(18)
            }
        )

        previewFallback = ImageView(this).apply {
            scaleType = ImageView.ScaleType.FIT_CENTER
            visibility = View.GONE
            contentDescription = "Selected hero"
        }
        panel.addView(
            previewFallback,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            ).apply {
                leftMargin = dp(22)
                rightMargin = dp(22)
                topMargin = dp(12)
                bottomMargin = dp(12)
            }
        )

        val settings = GameSettingsStore.load(this)
        previewActor = SkeletalActorView(this).apply {
            visibility = View.GONE
            motionEnabled = !settings.reducedMotion && !settings.batterySaver
            setFacing("right")
            contentDescription = "Selected hero animated preview"
        }
        panel.addView(
            previewActor,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            ).apply {
                leftMargin = dp(14)
                rightMargin = dp(14)
                topMargin = dp(4)
                bottomMargin = dp(4)
            }
        )

        activeBadge = TextView(this).apply {
            text = "ACTIVE HERO"
            textSize = 10f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(255, 226, 151))
            setPadding(dp(10), dp(5), dp(10), dp(5))
        }
        panel.addView(
            activeBadge,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.TOP or Gravity.START
            ).apply {
                topMargin = dp(10)
                marginStart = dp(10)
            }
        )

        favoriteButton = TextView(this).apply {
            text = "☆"
            textSize = 28f
            setTextColor(Color.rgb(255, 216, 116))
            gravity = Gravity.CENTER
            isClickable = true
            isFocusable = true
            contentDescription = "Favorite hero"
            setOnClickListener {
                AppHaptics.tap(this@HeroesActivity)
                HeroRosterStore.toggleFavorite(this@HeroesActivity, selectedHeroId)
                renderSelectedHero()
                rebuildRoster()
            }
            installTouchFeedback()
        }
        panel.addView(
            favoriteButton,
            FrameLayout.LayoutParams(
                dp(48),
                dp(48),
                Gravity.TOP or Gravity.END
            ).apply {
                topMargin = dp(4)
                marginEnd = dp(6)
            }
        )

        return panel.apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(270)
            )
        }
    }

    private fun buildDetailPanel(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(15), dp(16), dp(16))
            background = rounded(
                Color.argb(226, 5, 9, 29),
                Color.rgb(103, 76, 180),
                18
            )

            nameText = TextView(this@HeroesActivity).apply {
                textSize = 23f
                setTextColor(Color.WHITE)
                typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                gravity = Gravity.CENTER
                letterSpacing = 0.03f
            }
            addView(nameText)

            metaText = TextView(this@HeroesActivity).apply {
                textSize = 12.5f
                setTextColor(Color.rgb(193, 180, 226))
                gravity = Gravity.CENTER
                setPadding(0, dp(3), 0, 0)
            }
            addView(metaText)

            powerText = TextView(this@HeroesActivity).apply {
                textSize = 13.5f
                setTextColor(Color.rgb(255, 213, 115))
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                setPadding(0, dp(10), 0, 0)
            }
            addView(powerText)

            addView(divider(), LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(1)
            ).apply {
                topMargin = dp(12)
                bottomMargin = dp(12)
            })

            skillTitleText = TextView(this@HeroesActivity).apply {
                textSize = 14.5f
                setTextColor(Color.WHITE)
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
            }
            addView(skillTitleText)

            skillBodyText = TextView(this@HeroesActivity).apply {
                textSize = 12.5f
                setTextColor(Color.rgb(207, 197, 233))
                gravity = Gravity.CENTER
                setPadding(dp(4), dp(5), dp(4), 0)
            }
            addView(skillBodyText)

            loreText = TextView(this@HeroesActivity).apply {
                textSize = 11.5f
                setTextColor(Color.rgb(151, 140, 186))
                gravity = Gravity.CENTER
                setPadding(dp(8), dp(10), dp(8), 0)
            }
            addView(loreText)

            val actions = LinearLayout(this@HeroesActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
            }

            activateButton = actionButton("SET ACTIVE HERO", false).apply {
                setOnClickListener {
                    AppHaptics.tap(this@HeroesActivity)
                    val hero = HeroRosterStore.hero(selectedHeroId) ?: return@setOnClickListener
                    HeroRosterStore.setActiveHero(this@HeroesActivity, hero.id)
                    Toast.makeText(
                        this@HeroesActivity,
                        hero.displayName + " is now your active battle hero.",
                        Toast.LENGTH_SHORT
                    ).show()
                    renderSelectedHero()
                    rebuildRoster()
                }
            }
            actions.addView(
                activateButton,
                LinearLayout.LayoutParams(
                    0,
                    dp(48),
                    1.7f
                ).apply {
                    marginEnd = dp(5)
                }
            )

            val battleButton = actionButton("BATTLE", true).apply {
                setOnClickListener {
                    AppHaptics.tap(this@HeroesActivity)
                    HeroRosterStore.setActiveHero(this@HeroesActivity, selectedHeroId)
                    startActivity(Intent(this@HeroesActivity, BattleActivity::class.java))
                }
            }
            actions.addView(
                battleButton,
                LinearLayout.LayoutParams(
                    0,
                    dp(48),
                    1f
                ).apply {
                    marginStart = dp(5)
                }
            )

            addView(
                actions,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(48)
                ).apply {
                    topMargin = dp(14)
                }
            )
        }
    }

    private fun buildRosterHeader(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL

            addView(
                TextView(this@HeroesActivity).apply {
                    text = "ROSTER"
                    textSize = 18f
                    setTextColor(Color.WHITE)
                    typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                    letterSpacing = 0.08f
                },
                LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )

            addView(
                TextView(this@HeroesActivity).apply {
                    text = "Tap to preview"
                    textSize = 11f
                    setTextColor(Color.rgb(154, 142, 190))
                }
            )
        }
    }

    private fun renderSelectedHero() {
        val hero = HeroRosterStore.hero(selectedHeroId)
            ?: HeroRosterStore.activeHero(this).also { selectedHeroId = it.id }
        val active = HeroRosterStore.activeHeroId(this) == hero.id
        val accent = heroAccent(hero)

        nameText.text = hero.displayName
        metaText.text = hero.rarity.uppercase() + "  •  " +
            hero.heroClass.uppercase() + "  •  " +
            hero.role.uppercase() + "  •  " +
            hero.affinity.uppercase()
        powerText.text = "LV " + progress.level.toString() +
            "   •   POWER " + HeroRosterStore.heroPower(hero, progress).toString() +
            "   •   " + "★".repeat(hero.rarityStars)
        skillTitleText.text = hero.skillName.uppercase()
        skillTitleText.setTextColor(accent)
        skillBodyText.text = hero.skillText
        loreText.text = hero.lore

        activeBadge.text = if (active) "ACTIVE HERO" else "PREVIEW"
        activeBadge.setTextColor(
            if (active) Color.rgb(255, 226, 151)
            else Color.rgb(208, 191, 244)
        )
        activeBadge.background = rounded(
            Color.argb(230, 8, 10, 28),
            if (active) Color.rgb(218, 175, 81) else accent,
            11
        )

        val favorite = HeroRosterStore.isFavorite(this, hero.id)
        favoriteButton.text = if (favorite) "★" else "☆"

        activateButton.text = if (active) "ACTIVE IN BATTLE" else "SET ACTIVE HERO"
        activateButton.isEnabled = !active
        activateButton.alpha = if (active) 0.62f else 1f

        loadHeroVisual(hero)
    }

    private fun loadHeroVisual(hero: HeroDefinition) {
        visualLoadToken += 1
        val token = visualLoadToken

        previewActor.clearRig()
        previewActor.visibility = View.GONE
        previewFallback.setImageDrawable(null)
        previewFallback.visibility = View.GONE
        previewSigil.text = hero.heroClass.firstOrNull()?.uppercaseChar()?.toString() ?: "H"
        previewSigil.setTextColor(heroAccent(hero))
        previewSigil.visibility = View.VISIBLE

        Thread {
            val rig = if (BattleActorFactory.skeletalRigEnabled(hero.id)) {
                runCatching { RigLoader.load(this, hero.id) }.getOrNull()
            } else {
                null
            }

            val fallback = if (rig == null) {
                runCatching {
                    EntitySpriteStore.loadCharacter(this, hero.id).idle
                }.getOrNull()
            } else {
                null
            }

            runOnUiThread {
                if (token != visualLoadToken || isFinishing || isDestroyed) {
                    return@runOnUiThread
                }

                if (rig != null) {
                    previewActor.setRig(rig)
                    previewActor.setFacing("right")
                    previewActor.visibility = View.VISIBLE
                    previewFallback.visibility = View.GONE
                    previewSigil.visibility = View.GONE
                } else if (fallback != null) {
                    previewFallback.setImageBitmap(fallback)
                    previewFallback.visibility = View.VISIBLE
                    previewActor.visibility = View.GONE
                    previewSigil.visibility = View.GONE
                } else {
                    previewSigil.visibility = View.VISIBLE
                }
            }
        }.start()
    }

    private fun rebuildFilters() {
        filterContainer.removeAllViews()
        val filters = listOf(
            "ALL" to "ALL",
            "FRONT" to "FRONT",
            "STRIKE" to "STRIKE",
            "ARCANE" to "ARCANE"
        )

        filters.forEachIndexed { index, item ->
            val active = activeFilter == item.first
            val chip = TextView(this).apply {
                text = item.second
                textSize = 10.5f
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                setTextColor(
                    if (active) Color.WHITE
                    else Color.rgb(164, 151, 200)
                )
                background = rounded(
                    if (active) Color.argb(235, 63, 38, 124)
                    else Color.argb(210, 7, 11, 31),
                    if (active) Color.rgb(171, 125, 255)
                    else Color.rgb(72, 57, 118),
                    12
                )
                isClickable = true
                isFocusable = true
                setOnClickListener {
                    AppHaptics.tap(this@HeroesActivity)
                    activeFilter = item.first
                    rebuildFilters()
                    rebuildRoster()
                }
                installTouchFeedback()
            }
            filterContainer.addView(
                chip,
                LinearLayout.LayoutParams(
                    0,
                    dp(36),
                    1f
                ).apply {
                    marginStart = if (index == 0) 0 else dp(3)
                    marginEnd = if (index == filters.lastIndex) 0 else dp(3)
                }
            )
        }
    }

    private fun rebuildRoster() {
        rosterContainer.removeAllViews()
        val heroes = HeroRosterStore.allHeroes().filter { heroMatchesFilter(it) }

        rosterCountText.text = HeroRosterStore.allHeroes().size.toString() + "/" +
            HeroRosterStore.allHeroes().size.toString()

        heroes.chunked(2).forEach { pair ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            pair.forEachIndexed { index, hero ->
                row.addView(
                    buildHeroCard(hero),
                    LinearLayout.LayoutParams(
                        0,
                        dp(132),
                        1f
                    ).apply {
                        marginStart = if (index == 0) 0 else dp(5)
                        marginEnd = if (index == 0) dp(5) else 0
                    }
                )
            }

            if (pair.size == 1) {
                row.addView(
                    View(this),
                    LinearLayout.LayoutParams(
                        0,
                        dp(132),
                        1f
                    ).apply {
                        marginStart = dp(5)
                    }
                )
            }

            rosterContainer.addView(
                row,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(132)
                ).apply {
                    topMargin = dp(8)
                }
            )
        }
    }

    private fun buildHeroCard(hero: HeroDefinition): LinearLayout {
        val selected = selectedHeroId == hero.id
        val active = HeroRosterStore.activeHeroId(this) == hero.id
        val favorite = HeroRosterStore.isFavorite(this, hero.id)
        val accent = heroAccent(hero)

        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(10), dp(8), dp(10), dp(8))
            background = rounded(
                when {
                    selected -> Color.argb(238, 18, 15, 47)
                    else -> Color.argb(224, 5, 9, 27)
                },
                when {
                    active -> Color.rgb(222, 180, 88)
                    selected -> accent
                    else -> Color.rgb(73, 58, 118)
                },
                15,
                if (selected || active) 2 else 1
            )
            isClickable = true
            isFocusable = true
            contentDescription = hero.displayName + ", " + hero.heroClass
            setOnClickListener {
                AppHaptics.tap(this@HeroesActivity)
                selectedHeroId = hero.id
                renderSelectedHero()
                rebuildRoster()
            }
            installTouchFeedback()

            val top = LinearLayout(this@HeroesActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL

                addView(
                    TextView(this@HeroesActivity).apply {
                        text = hero.heroClass.firstOrNull()?.uppercaseChar()?.toString() ?: "H"
                        textSize = 20f
                        setTextColor(Color.WHITE)
                        typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                        gravity = Gravity.CENTER
                        background = GradientDrawable(
                            GradientDrawable.Orientation.TL_BR,
                            intArrayOf(
                                Color.argb(245, Color.red(accent), Color.green(accent), Color.blue(accent)),
                                Color.rgb(24, 21, 62)
                            )
                        ).apply {
                            shape = GradientDrawable.OVAL
                            setStroke(dp(1), Color.argb(220, 235, 224, 255))
                        }
                    },
                    LinearLayout.LayoutParams(dp(42), dp(42))
                )

                addView(
                    LinearLayout(this@HeroesActivity).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(dp(8), 0, 0, 0)

                        addView(
                            TextView(this@HeroesActivity).apply {
                                text = hero.displayName
                                maxLines = 1
                                textSize = 13f
                                setTextColor(Color.WHITE)
                                typeface = Typeface.DEFAULT_BOLD
                            }
                        )
                        addView(
                            TextView(this@HeroesActivity).apply {
                                text = hero.heroClass + " • " + hero.role
                                maxLines = 1
                                textSize = 10.5f
                                setTextColor(Color.rgb(178, 165, 208))
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
                    TextView(this@HeroesActivity).apply {
                        text = when {
                            active -> "ACTIVE"
                            favorite -> "★"
                            else -> ""
                        }
                        textSize = if (active) 8.5f else 17f
                        setTextColor(
                            if (active) Color.rgb(255, 218, 128)
                            else Color.rgb(255, 210, 103)
                        )
                        typeface = Typeface.DEFAULT_BOLD
                        gravity = Gravity.CENTER
                    },
                    LinearLayout.LayoutParams(
                        dp(46),
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                )
            }
            addView(
                top,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(46)
                )
            )

            addView(
                TextView(this@HeroesActivity).apply {
                    text = hero.rarity.uppercase() + "  " + "★".repeat(hero.rarityStars)
                    textSize = 10f
                    setTextColor(accent)
                    typeface = Typeface.DEFAULT_BOLD
                    gravity = Gravity.START
                    setPadding(0, dp(6), 0, 0)
                }
            )

            addView(
                TextView(this@HeroesActivity).apply {
                    text = "LV " + progress.level.toString() +
                        "  •  PWR " + HeroRosterStore.heroPower(hero, progress).toString() +
                        "  •  " + hero.affinity.uppercase()
                    textSize = 10.5f
                    setTextColor(Color.rgb(205, 195, 231))
                    gravity = Gravity.START
                    setPadding(0, dp(4), 0, 0)
                }
            )
        }
    }

    private fun heroMatchesFilter(hero: HeroDefinition): Boolean {
        return when (activeFilter) {
            "FRONT" -> hero.role in setOf(
                "Vanguard",
                "Guardian",
                "Bruiser",
                "Juggernaut"
            )
            "STRIKE" -> hero.role in setOf(
                "Assassin",
                "Marksman"
            )
            "ARCANE" -> hero.role in setOf(
                "Burst Mage",
                "Controller"
            )
            else -> true
        }
    }

    private fun heroAccent(hero: HeroDefinition): Int {
        return when (hero.affinity) {
            "Radiant" -> Color.rgb(237, 191, 87)
            "Arcane" -> Color.rgb(86, 159, 255)
            "Umbral" -> Color.rgb(179, 104, 239)
            "Void" -> Color.rgb(145, 91, 235)
            "Ember" -> Color.rgb(236, 106, 73)
            "Gale" -> Color.rgb(88, 207, 181)
            "Eclipse" -> Color.rgb(192, 102, 255)
            else -> Color.rgb(155, 112, 244)
        }
    }

    private fun actionButton(label: String, gold: Boolean): TextView {
        return TextView(this).apply {
            text = label
            textSize = 11.5f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            letterSpacing = 0.05f
            background = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                if (gold) {
                    intArrayOf(
                        Color.rgb(112, 69, 22),
                        Color.rgb(183, 126, 43)
                    )
                } else {
                    intArrayOf(
                        Color.rgb(70, 43, 143),
                        Color.rgb(111, 67, 190)
                    )
                }
            ).apply {
                cornerRadius = dp(12).toFloat()
                setStroke(
                    dp(1),
                    if (gold) Color.rgb(239, 194, 93)
                    else Color.rgb(176, 132, 255)
                )
            }
            isClickable = true
            isFocusable = true
            installTouchFeedback()
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
