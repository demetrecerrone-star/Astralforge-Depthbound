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
            setImageResource(R.drawable.hub_background_rebuild)
            scaleType = ImageView.ScaleType.FIT_XY
            scaleX = 1f
            scaleY = 1f
            contentDescription = null
        }
        root.addView(
            backgroundImage,
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

        // Rebuilt hub baseline: the new artwork contains the six location
        // environments, their labels, the blank avatar frame, the lower
        // player frame, and the top-right resource frame. Old hub overlays
        // are intentionally not drawn over it.
        playerNameAtCreate = PlayerIdentityStore.getName(this)

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
            val systemBottom = insets.systemWindowInsetBottom
            bottomNavParams.bottomMargin =
                systemBottom + AuthUi.dp(this@HomeActivity, 8)
            bottomNav.layoutParams = bottomNavParams
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
            setOnClickListener { onClick() }
            installTouchFeedback()
        }

        // Only darken the actual word/nameplate area. The emblem side of
        // the button stays transparent so the hub artwork remains visible.
        holder.addView(
            android.view.View(this).apply {
                background = GradientDrawable().apply {
                    setColor(Color.argb(218, 0, 0, 0))
                    cornerRadius =
                        AuthUi.dp(this@HomeActivity, 10).toFloat()
                }
            },
            FrameLayout.LayoutParams(
                (widthPx * 55 / 100),
                (heightPx * 28 / 100),
                Gravity.END or Gravity.CENTER_VERTICAL
            ).apply {
                marginEnd = widthPx * 5 / 100
            }
        )

        holder.addView(
            ImageView(this).apply {
                if (bitmap != null) setImageBitmap(bitmap)
                scaleType = ImageView.ScaleType.FIT_CENTER
                adjustViewBounds = true
                contentDescription = null
                setPadding(
                    AuthUi.dp(this@HomeActivity, 2),
                    0,
                    AuthUi.dp(this@HomeActivity, 2),
                    0
                )
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

        val itemWidth = (screenW - AuthUi.dp(this, 8)) / 5

        fun addImageItem(
            fileName: String,
            label: String,
            onClick: () -> Unit
        ) {
            val bitmap = ButtonAssetStore.load(
                this,
                bottomPack,
                fileName
            )
            val item = ImageView(this).apply {
                if (bitmap != null) setImageBitmap(bitmap)
                scaleType = ImageView.ScaleType.FIT_CENTER
                adjustViewBounds = true
                contentDescription = label
                isClickable = true
                isFocusable = true
                installTouchFeedback()
                setOnClickListener { onClick() }
            }
            nav.addView(
                item,
                LinearLayout.LayoutParams(
                    itemWidth,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
            )
        }

        addImageItem("home.png", "Home") {
            Unit
        }

        addImageItem("battle.png", "Battle") {
            startActivity(
                Intent(
                    this@HomeActivity,
                    BattleActivity::class.java
                )
            )
        }

        val statsItem = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            contentDescription = "Stats"
            isClickable = true
            isFocusable = true
            setPadding(
                AuthUi.dp(this@HomeActivity, 3),
                AuthUi.dp(this@HomeActivity, 3),
                AuthUi.dp(this@HomeActivity, 3),
                AuthUi.dp(this@HomeActivity, 2)
            )
            installTouchFeedback()
            setOnClickListener {
                startActivity(
                    Intent(
                        this@HomeActivity,
                        StatsActivity::class.java
                    )
                )
            }

            addView(
                TextView(this@HomeActivity).apply {
                    text = "✦"
                    textSize = 23f
                    setTextColor(Color.rgb(203, 176, 255))
                    typeface = Typeface.DEFAULT_BOLD
                    gravity = Gravity.CENTER
                    setShadowLayer(
                        7f,
                        0f,
                        0f,
                        Color.rgb(109, 74, 233)
                    )
                },
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    1f
                )
            )

            addView(
                TextView(this@HomeActivity).apply {
                    text = "STATS"
                    textSize = 8.5f
                    setTextColor(Color.WHITE)
                    typeface = Typeface.DEFAULT_BOLD
                    gravity = Gravity.CENTER
                    letterSpacing = 0.08f
                },
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    AuthUi.dp(this@HomeActivity, 20)
                )
            )
        }
        nav.addView(
            statsItem,
            LinearLayout.LayoutParams(
                itemWidth,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        )

        addImageItem("gear.png", "Gear") {
            openSection(
                "GEAR",
                "Weapons, armor, accessories, loadouts, and equipment power."
            )
        }

        addImageItem("menu.png", "Menu") {
            startActivity(
                Intent(
                    this@HomeActivity,
                    MenuActivity::class.java
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
