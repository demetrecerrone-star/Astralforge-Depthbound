package com.demetrecerrone.astralforge

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.math.min

class HomeActivity : Activity() {

    private val hubPack = "astral_hub_buttons_individual.zip"
    private val bottomPack = "bottomnav.zip"
    private val avatarPack = "avatarcorner.zip"
    private var performanceSignatureAtCreate: String? = null
    private var playerNameAtCreate: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AuthUi.setupWindow(this)

        val screenW = resources.displayMetrics.widthPixels
        val root = FrameLayout(this)
        val gameSettings = GameSettingsStore.load(this)
        performanceSignatureAtCreate = performanceSignature(gameSettings)
        val allowAmbientMotion =
            !gameSettings.reducedMotion && !gameSettings.batterySaver

        val preferredRate = when {
            gameSettings.batterySaver -> 30f
            gameSettings.fpsPreference == "30" -> 30f
            gameSettings.fpsPreference == "60" -> 60f
            else -> 0f
        }
        window.attributes = window.attributes.apply {
            preferredRefreshRate = preferredRate
        }

        val backgroundImage = ImageView(this).apply {
            setImageResource(R.drawable.file_00000000a45881f5ad657cc79abf1338)
            scaleType = ImageView.ScaleType.CENTER_CROP
            scaleX = 1.055f
            scaleY = 1.055f
            contentDescription = null
        }
        root.addView(
            backgroundImage,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )
        if (allowAmbientMotion) {
            animateBackground(backgroundImage)
        }

        root.addView(
            View(this).apply {
                background = GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    intArrayOf(
                        Color.argb(42, 3, 5, 20),
                        Color.argb(10, 3, 5, 20),
                        Color.argb(20, 3, 5, 20),
                        Color.argb(105, 3, 5, 20)
                    )
                )
            },
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        val hubEffects = HubEffectView(this).apply {
            reducedMotion = gameSettings.reducedMotion
            visualQuality = gameSettings.visualQuality
            particleDensity = gameSettings.particleDensity
            batterySaver = gameSettings.batterySaver
            fpsPreference = gameSettings.fpsPreference
        }
        root.addView(
            hubEffects,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        val progress = ProgressionStore.load(this)
        val displayName = PlayerIdentityStore.getName(this)
        playerNameAtCreate = displayName

        val playerCardWidth = min(screenW * 44 / 100, AuthUi.dp(this, 260))
        val playerCardHeight = AuthUi.dp(this, 84)
        val playerCard = createPlayerCorner(
            displayName,
            progress,
            playerCardWidth,
            playerCardHeight
        )
        val playerCardParams = FrameLayout.LayoutParams(
            playerCardWidth,
            playerCardHeight,
            Gravity.TOP or Gravity.START
        ).apply {
            topMargin = AuthUi.dp(this@HomeActivity, 4)
            marginStart = AuthUi.dp(this@HomeActivity, 1)
        }
        root.addView(playerCard, playerCardParams)

        val statusPanel = createStatusPanel(progress)
        val statusParams = FrameLayout.LayoutParams(
            min(screenW * 31 / 100, AuthUi.dp(this, 154)),
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.TOP or Gravity.END
        ).apply {
            topMargin = AuthUi.dp(this@HomeActivity, 8)
            marginEnd = AuthUi.dp(this@HomeActivity, 4)
        }
        root.addView(statusPanel, statusParams)

        val dungeons = addHubButton(
            root,
            "dungeons.png",
            "Dungeons",
            screenW * 48 / 100,
            0.26f,
            0.235f
        ) {
            openSection(
                "DUNGEONS",
                "Choose a depth, enter a dungeon, and claim its rewards."
            )
        }
        if (allowAmbientMotion) {
            pulse(dungeons, 1.0f, 1.022f, 1850L)
        }

        addHubButton(
            root,
            "shop.png",
            "Shop",
            screenW * 35 / 100,
            0.025f,
            0.352f
        ) {
            openSection(
                "SHOP",
                "Spend gold and Astral Shards on supplies and upgrades."
            )
        }

        addHubButton(
            root,
            "forge.png",
            "Forge",
            screenW * 35 / 100,
            0.625f,
            0.395f
        ) {
            openSection(
                "FORGE",
                "Enhance weapons, armor, and relics at the Astral Forge."
            )
        }

        val summon = addHubButton(
            root,
            "summon.png",
            "Summon",
            screenW * 37 / 100,
            0.315f,
            0.505f
        ) {
            openSection(
                "SUMMON",
                "Call heroes, relics, and rare astral powers from beyond the veil."
            )
        }
        if (allowAmbientMotion) {
            pulse(summon, 1.0f, 1.018f, 2100L)
        }

        addHubButton(
            root,
            "heroes.png",
            "Heroes",
            screenW * 34 / 100,
            0.03f,
            0.595f
        ) {
            startActivity(Intent(this, CharacterActivity::class.java))
        }

        addHubButton(
            root,
            "guild.png",
            "Guild",
            screenW * 34 / 100,
            0.63f,
            0.595f
        ) {
            openSection(
                "GUILD",
                "Guild progression, members, raids, contributions, and rewards."
            )
        }

        addHubButton(
            root,
            "quests.png",
            "Quests",
            screenW * 29 / 100,
            0.67f,
            0.69f
        ) {
            openSection(
                "QUESTS",
                "Main quests, side missions, dailies, and milestone rewards."
            )
        }

        val battleButton = ImageView(this).apply {
            setImageResource(R.drawable.file_00000000b44481f6b8c3df3c70bf55e3)
            scaleType = ImageView.ScaleType.FIT_CENTER
            adjustViewBounds = true
            contentDescription = "Battle"
            isClickable = true
            isFocusable = true
            background = GradientDrawable().apply {
                setColor(Color.argb(225, 0, 0, 0))
                cornerRadius = AuthUi.dp(this@HomeActivity, 18).toFloat()
                setStroke(
                    AuthUi.dp(this@HomeActivity, 1),
                    Color.argb(200, 130, 92, 225)
                )
            }
            setPadding(
                AuthUi.dp(this@HomeActivity, 5),
                AuthUi.dp(this@HomeActivity, 2),
                AuthUi.dp(this@HomeActivity, 5),
                AuthUi.dp(this@HomeActivity, 2)
            )
            setOnClickListener {
                startActivity(
                    Intent(
                        this@HomeActivity,
                        BattleActivity::class.java
                    )
                )
            }
            installTouchFeedback()
        }
        val battleWidth = screenW * 42 / 100
        val battleParams = FrameLayout.LayoutParams(
            battleWidth,
            battleWidth * 34 / 100,
            Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        ).apply {
            bottomMargin = AuthUi.dp(this@HomeActivity, 118)
        }
        root.addView(battleButton, battleParams)
        if (allowAmbientMotion) {
            pulse(battleButton, 1.0f, 1.026f, 1600L)
        }

        val bottomNav = createBottomNav(screenW)
        val bottomNavParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            AuthUi.dp(this, 74),
            Gravity.BOTTOM
        ).apply {
            bottomMargin = AuthUi.dp(this@HomeActivity, 8)
        }
        root.addView(bottomNav, bottomNavParams)

        root.setOnApplyWindowInsetsListener { _, insets: WindowInsets ->
            val systemTop = insets.systemWindowInsetTop
            val systemBottom = insets.systemWindowInsetBottom
            val safeGap = AuthUi.dp(this@HomeActivity, 8)

            playerCardParams.topMargin =
                systemTop + AuthUi.dp(this@HomeActivity, 2)
            playerCard.layoutParams = playerCardParams

            statusParams.topMargin =
                systemTop + AuthUi.dp(this@HomeActivity, 6)
            statusPanel.layoutParams = statusParams

            bottomNavParams.bottomMargin = systemBottom + safeGap
            bottomNav.layoutParams = bottomNavParams

            battleParams.bottomMargin =
                systemBottom + safeGap + AuthUi.dp(this@HomeActivity, 112)
            battleButton.layoutParams = battleParams

            insets
        }
        root.requestApplyInsets()

        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        val previous = performanceSignatureAtCreate ?: return
        val currentSettings = GameSettingsStore.load(this)
        val currentName = PlayerIdentityStore.getName(this)
        if (
            previous != performanceSignature(currentSettings) ||
            currentName != playerNameAtCreate
        ) {
            recreate()
        }
    }

    private fun createPlayerCorner(
        displayName: String,
        progress: PlayerProgress,
        cardWidth: Int,
        cardHeight: Int
    ): FrameLayout {
        val card = FrameLayout(this).apply {
            contentDescription = "Player profile"
            isClickable = false
            isFocusable = false
        }

        val avatarPlaceholderSize = cardHeight * 58 / 100
        val avatarPlaceholder = TextView(this).apply {
            text = displayName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "D"
            gravity = Gravity.CENTER
            textSize = 20f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(
                    Color.rgb(44, 24, 92),
                    Color.rgb(83, 52, 170),
                    Color.rgb(18, 35, 95)
                )
            ).apply {
                shape = GradientDrawable.OVAL
                setStroke(
                    AuthUi.dp(this@HomeActivity, 1),
                    Color.rgb(137, 102, 255)
                )
            }
            setShadowLayer(6f, 0f, 0f, Color.rgb(113, 82, 255))
        }
        card.addView(
            avatarPlaceholder,
            FrameLayout.LayoutParams(
                avatarPlaceholderSize,
                avatarPlaceholderSize,
                Gravity.START or Gravity.CENTER_VERTICAL
            ).apply {
                marginStart = cardWidth * 4 / 100
            }
        )

        ButtonAssetStore.load(
            this,
            avatarPack,
            "celestial_fantasy_profile_banner_frame.png"
        )?.let { bitmap ->
            card.addView(
                ImageView(this).apply {
                    setImageBitmap(bitmap)
                    scaleType = ImageView.ScaleType.FIT_XY
                    contentDescription = null
                },
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            )
        }

        ButtonAssetStore.load(
            this,
            avatarPack,
            "celestial_gold_and_amethyst_avatar_frame.png"
        )?.let { bitmap ->
            card.addView(
                ImageView(this).apply {
                    setImageBitmap(bitmap)
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    contentDescription = null
                },
                FrameLayout.LayoutParams(
                    cardHeight * 78 / 100,
                    cardHeight * 78 / 100,
                    Gravity.START or Gravity.CENTER_VERTICAL
                ).apply {
                    marginStart = cardWidth * 2 / 100
                }
            )
        }

        card.addView(
            TextView(this).apply {
                text = displayName
                maxLines = 1
                textSize = 11.5f
                setTextColor(Color.WHITE)
                typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                gravity = Gravity.CENTER_VERTICAL
                setShadowLayer(5f, 0f, 0f, Color.rgb(100, 78, 255))
            },
            FrameLayout.LayoutParams(
                cardWidth * 66 / 100,
                cardHeight * 29 / 100
            ).apply {
                leftMargin = cardWidth * 29 / 100
                topMargin = cardHeight * 22 / 100
            }
        )

        addLevelBadge(card, progress.level, cardWidth, cardHeight)
        addBattlePowerBadge(
            card,
            progress.powerRating,
            cardWidth,
            cardHeight
        )
        addRankBadge(
            card,
            progress.rank,
            cardWidth,
            cardHeight
        )

        return card
    }

    private fun addLevelBadge(
        card: FrameLayout,
        level: Int,
        cardWidth: Int,
        cardHeight: Int
    ) {
        val badgeWidth = cardWidth * 32 / 100
        val badgeHeight = cardHeight * 31 / 100
        val holder = FrameLayout(this)

        ButtonAssetStore.load(
            this,
            avatarPack,
            "celestial_lv_fantasy_game_badge.png"
        )?.let { bitmap ->
            holder.addView(
                ImageView(this).apply {
                    setImageBitmap(bitmap)
                    scaleType = ImageView.ScaleType.FIT_XY
                    contentDescription = null
                },
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            )
        }

        holder.addView(
            TextView(this).apply {
                text = level.toString()
                textSize = 10.5f
                setTextColor(Color.WHITE)
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                setShadowLayer(4f, 0f, 0f, Color.rgb(127, 88, 255))
            },
            FrameLayout.LayoutParams(
                badgeWidth * 48 / 100,
                FrameLayout.LayoutParams.MATCH_PARENT,
                Gravity.END
            ).apply {
                marginEnd = AuthUi.dp(this@HomeActivity, 5)
            }
        )

        card.addView(
            holder,
            FrameLayout.LayoutParams(
                badgeWidth,
                badgeHeight
            ).apply {
                leftMargin = cardWidth * 29 / 100
                topMargin = cardHeight * 54 / 100
            }
        )
    }

    private fun addBattlePowerBadge(
        card: FrameLayout,
        battlePower: Int,
        cardWidth: Int,
        cardHeight: Int
    ) {
        val badgeWidth = cardWidth * 39 / 100
        val badgeHeight = cardHeight * 31 / 100
        val holder = FrameLayout(this)

        ButtonAssetStore.load(
            this,
            avatarPack,
            "celestial_bp_battle_power_badge.png"
        )?.let { bitmap ->
            holder.addView(
                ImageView(this).apply {
                    setImageBitmap(bitmap)
                    scaleType = ImageView.ScaleType.FIT_XY
                    contentDescription = null
                },
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            )
        }

        holder.addView(
            TextView(this).apply {
                text = battlePower.toString()
                textSize = 10.5f
                setTextColor(Color.WHITE)
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                setShadowLayer(4f, 0f, 0f, Color.rgb(131, 84, 255))
            },
            FrameLayout.LayoutParams(
                badgeWidth * 54 / 100,
                FrameLayout.LayoutParams.MATCH_PARENT,
                Gravity.END
            ).apply {
                marginEnd = AuthUi.dp(this@HomeActivity, 5)
            }
        )

        card.addView(
            holder,
            FrameLayout.LayoutParams(
                badgeWidth,
                badgeHeight
            ).apply {
                leftMargin = cardWidth * 59 / 100
                topMargin = cardHeight * 54 / 100
            }
        )
    }

    private fun addRankBadge(
        card: FrameLayout,
        rank: String,
        cardWidth: Int,
        cardHeight: Int
    ) {
        val holder = FrameLayout(this)
        val badgeSize = cardHeight * 27 / 100

        ButtonAssetStore.load(
            this,
            avatarPack,
            "ornate_purple_celestial_rank_emblem.png"
        )?.let { bitmap ->
            holder.addView(
                ImageView(this).apply {
                    setImageBitmap(bitmap)
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    contentDescription = null
                },
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            )
        }

        holder.addView(
            TextView(this).apply {
                text = rank
                textSize = 9f
                setTextColor(Color.WHITE)
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                setShadowLayer(4f, 0f, 0f, Color.rgb(120, 75, 255))
            },
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        card.addView(
            holder,
            FrameLayout.LayoutParams(
                badgeSize,
                badgeSize
            ).apply {
                leftMargin = cardWidth * 20 / 100
                topMargin = cardHeight * 67 / 100
            }
        )
        val performance = GameSettingsStore.load(this)
        if (!performance.reducedMotion && !performance.batterySaver) {
            pulse(holder, 1f, 1.045f, 1900L)
        }
    }

    private fun createStatusPanel(progress: PlayerProgress): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.END
            setPadding(
                AuthUi.dp(this@HomeActivity, 7),
                AuthUi.dp(this@HomeActivity, 4),
                AuthUi.dp(this@HomeActivity, 7),
                AuthUi.dp(this@HomeActivity, 4)
            )
            background = GradientDrawable().apply {
                setColor(Color.argb(195, 5, 8, 24))
                cornerRadius = AuthUi.dp(this@HomeActivity, 8).toFloat()
                setStroke(
                    AuthUi.dp(this@HomeActivity, 1),
                    Color.rgb(202, 165, 92)
                )
            }

            addView(
                statusLine(
                    "Gold",
                    progress.gold.toString(),
                    Color.rgb(255, 204, 72)
                )
            )
            addView(
                statusLine(
                    "Shards",
                    progress.astralShards.toString(),
                    Color.rgb(128, 113, 255)
                )
            )
            addView(
                statusLine(
                    "Energy",
                    progress.energy.toString() + "/" + progress.maxEnergy,
                    Color.rgb(80, 192, 255)
                )
            )
        }
    }

    private fun statusLine(
        label: String,
        value: String,
        glowColor: Int
    ): TextView {
        return TextView(this).apply {
            text = label + "  " + value
            textSize = 10f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.END
            setPadding(
                0,
                AuthUi.dp(this@HomeActivity, 1),
                0,
                AuthUi.dp(this@HomeActivity, 1)
            )
            setShadowLayer(3f, 0f, 0f, glowColor)
        }
    }

    private fun addHubButton(
        root: FrameLayout,
        fileName: String,
        description: String,
        widthPx: Int,
        leftFraction: Float,
        topFraction: Float,
        onClick: () -> Unit
    ): FrameLayout {
        val bitmap = ButtonAssetStore.load(
            this,
            hubPack,
            fileName
        )
        val ratio = if (bitmap != null && bitmap.width > 0) {
            bitmap.height.toFloat() / bitmap.width.toFloat()
        } else {
            0.42f
        }

        val heightPx = (widthPx * ratio)
            .toInt()
            .coerceAtLeast(AuthUi.dp(this, 44))

        val holder = FrameLayout(this).apply {
            contentDescription = description
            isClickable = true
            isFocusable = true
            background = GradientDrawable().apply {
                setColor(Color.argb(220, 0, 0, 0))
                cornerRadius =
                    AuthUi.dp(this@HomeActivity, 15).toFloat()
                setStroke(
                    AuthUi.dp(this@HomeActivity, 1),
                    Color.argb(185, 113, 82, 205)
                )
            }
            setPadding(
                AuthUi.dp(this@HomeActivity, 4),
                AuthUi.dp(this@HomeActivity, 2),
                AuthUi.dp(this@HomeActivity, 4),
                AuthUi.dp(this@HomeActivity, 2)
            )
            setOnClickListener { onClick() }
            installTouchFeedback()
        }

        holder.addView(
            ImageView(this).apply {
                if (bitmap != null) setImageBitmap(bitmap)
                scaleType = ImageView.ScaleType.FIT_CENTER
                adjustViewBounds = true
                contentDescription = null
            },
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        val params = FrameLayout.LayoutParams(
            widthPx,
            heightPx
        )
        params.leftMargin =
            (resources.displayMetrics.widthPixels * leftFraction).toInt()
        params.topMargin =
            (resources.displayMetrics.heightPixels * topFraction).toInt()
        root.addView(holder, params)
        return holder
    }

    private fun createBottomNav(screenW: Int): LinearLayout {
        val nav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(
                AuthUi.dp(this@HomeActivity, 4),
                AuthUi.dp(this@HomeActivity, 2),
                AuthUi.dp(this@HomeActivity, 4),
                AuthUi.dp(this@HomeActivity, 2)
            )
            background = GradientDrawable().apply {
                setColor(Color.argb(218, 4, 7, 22))
                setStroke(
                    AuthUi.dp(this@HomeActivity, 1),
                    Color.rgb(126, 99, 225)
                )
            }
        }

        val navFiles = listOf(
            "home.png",
            "battle.png",
            "heroes.png",
            "gear.png",
            "menu.png"
        )
        val navLabels = listOf(
            "Home",
            "Battle",
            "Heroes",
            "Gear",
            "Menu"
        )
        val itemWidth =
            (screenW - AuthUi.dp(this, 8)) / navFiles.size

        navFiles.forEachIndexed { index, fileName ->
            val bitmap = ButtonAssetStore.load(
                this,
                bottomPack,
                fileName
            )
            val item = ImageView(this).apply {
                if (bitmap != null) setImageBitmap(bitmap)
                scaleType = ImageView.ScaleType.FIT_CENTER
                adjustViewBounds = true
                contentDescription = navLabels[index]
                isClickable = true
                isFocusable = true
                installTouchFeedback()

                setOnClickListener {
                    when (index) {
                        0 -> Unit
                        1 -> startActivity(
                            Intent(
                                this@HomeActivity,
                                BattleActivity::class.java
                            )
                        )
                        2 -> startActivity(
                            Intent(
                                this@HomeActivity,
                                CharacterActivity::class.java
                            )
                        )
                        3 -> openSection(
                            "GEAR",
                            "Weapons, armor, accessories, loadouts, and equipment power."
                        )
                        4 -> startActivity(
                            Intent(
                                this@HomeActivity,
                                MenuActivity::class.java
                            )
                        )
                    }
                }
            }
            nav.addView(
                item,
                LinearLayout.LayoutParams(
                    itemWidth,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
            )
        }

        return nav
    }

    private fun View.installTouchFeedback() {
        setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    AppHaptics.tap(this@HomeActivity)
                    view.animate()
                        .scaleX(0.965f)
                        .scaleY(0.965f)
                        .alpha(0.84f)
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

    private fun animateBackground(view: ImageView) {
        ObjectAnimator.ofFloat(
            view,
            View.TRANSLATION_X,
            -AuthUi.dp(this, 8).toFloat(),
            AuthUi.dp(this, 8).toFloat()
        ).apply {
            duration = 9000L
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }

        ObjectAnimator.ofFloat(
            view,
            View.TRANSLATION_Y,
            -AuthUi.dp(this, 6).toFloat(),
            AuthUi.dp(this, 6).toFloat()
        ).apply {
            duration = 11000L
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    private fun pulse(
        view: View,
        from: Float,
        to: Float,
        durationMs: Long
    ) {
        ObjectAnimator.ofFloat(
            view,
            View.SCALE_X,
            from,
            to
        ).apply {
            duration = durationMs
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
        ObjectAnimator.ofFloat(
            view,
            View.SCALE_Y,
            from,
            to
        ).apply {
            duration = durationMs
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    private fun performanceSignature(settings: GameSettings): String {
        return listOf(
            settings.reducedMotion.toString(),
            settings.visualQuality,
            settings.particleDensity.toString(),
            settings.batterySaver.toString(),
            settings.fpsPreference
        ).joinToString("|")
    }

    private fun openSection(
        title: String,
        subtitle: String
    ) {
        startActivity(
            Intent(this, GameSectionActivity::class.java)
                .putExtra("section_title", title)
                .putExtra("section_subtitle", subtitle)
        )
    }
}
