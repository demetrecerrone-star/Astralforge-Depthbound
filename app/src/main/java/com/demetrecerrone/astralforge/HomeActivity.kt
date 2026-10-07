package com.demetrecerrone.astralforge

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.net.Uri
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
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

        addTopRightActions(
            root = root,
            screenW = screenW,
            screenH = resources.displayMetrics.heightPixels
        )

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

    private fun addTopRightActions(
        root: FrameLayout,
        screenW: Int,
        screenH: Int
    ) {
        val panelWidth = (screenW * 0.19f).toInt()
        val panelHeight = (screenH * 0.073f).toInt()
            .coerceAtLeast(AuthUi.dp(this, 42))
        val gap = AuthUi.dp(this, 4)

        val actions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(gap, 0, gap, 0)
        }

        actions.addView(
            createTopRightAction(
                icon = "✦",
                label = "DAILY",
                description = "Daily Reward"
            ) {
                dismissHubMenuDropdown(root)
                showComingSoonPopup(root, "DAILY REWARD")
            },
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
            ).apply {
                marginEnd = gap / 2
            }
        )

        actions.addView(
            createTopRightAction(
                icon = "★",
                label = "EVENT",
                description = "Events"
            ) {
                dismissHubMenuDropdown(root)
                showComingSoonPopup(root, "EVENT")
            },
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
            ).apply {
                marginStart = gap / 2
                marginEnd = gap / 2
            }
        )

        actions.addView(
            createTopRightAction(
                icon = "☰",
                label = "MENU",
                description = "Menu"
            ) {
                toggleHubMenuDropdown(root)
            },
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
            ).apply {
                marginStart = gap / 2
            }
        )

        root.addView(
            actions,
            FrameLayout.LayoutParams(
                panelWidth,
                panelHeight,
                Gravity.TOP or Gravity.END
            ).apply {
                topMargin = (screenH * 0.018f).toInt()
                marginEnd = (screenW * 0.021f).toInt()
            }
        )
    }

    private fun createTopRightAction(
        icon: String,
        label: String,
        description: String,
        onClick: () -> Unit
    ): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            contentDescription = description
            isClickable = true
            isFocusable = true
            setPadding(
                AuthUi.dp(this@HomeActivity, 2),
                AuthUi.dp(this@HomeActivity, 2),
                AuthUi.dp(this@HomeActivity, 2),
                AuthUi.dp(this@HomeActivity, 1)
            )
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(
                    Color.argb(230, 47, 19, 93),
                    Color.argb(235, 12, 8, 39)
                )
            ).apply {
                cornerRadius = AuthUi.dp(this@HomeActivity, 10).toFloat()
                setStroke(
                    AuthUi.dp(this@HomeActivity, 1),
                    Color.rgb(221, 176, 93)
                )
            }

            addView(
                TextView(this@HomeActivity).apply {
                    text = icon
                    textSize = if (icon == "☰") 17f else 15f
                    setTextColor(Color.rgb(239, 220, 255))
                    typeface = Typeface.DEFAULT_BOLD
                    gravity = Gravity.CENTER
                    includeFontPadding = false
                    setShadowLayer(
                        7f,
                        0f,
                        0f,
                        Color.rgb(147, 69, 255)
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
                    text = label
                    textSize = 6.8f
                    setTextColor(Color.WHITE)
                    typeface = Typeface.DEFAULT_BOLD
                    gravity = Gravity.CENTER
                    letterSpacing = 0.06f
                    includeFontPadding = false
                    setShadowLayer(
                        4f,
                        0f,
                        0f,
                        Color.rgb(100, 69, 225)
                    )
                },
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    AuthUi.dp(this@HomeActivity, 13)
                )
            )

            installTouchFeedback()
            setOnClickListener { onClick() }
        }
    }

    private fun toggleHubMenuDropdown(root: FrameLayout) {
        val existing = root.findViewWithTag<View>("hub_menu_dropdown")
        if (existing != null) {
            root.removeView(existing)
            return
        }

        root.findViewWithTag<View>("hub_coming_soon")?.let {
            root.removeView(it)
        }
        root.findViewWithTag<View>("hub_signout_confirm")?.let {
            root.removeView(it)
        }

        val screenW = resources.displayMetrics.widthPixels
        val screenH = resources.displayMetrics.heightPixels
        val menuWidth = (screenW * 0.225f).toInt()
            .coerceAtLeast(AuthUi.dp(this, 210))

        val menu = LinearLayout(this).apply {
            tag = "hub_menu_dropdown"
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            elevation = AuthUi.dp(this@HomeActivity, 16).toFloat()
            setPadding(
                AuthUi.dp(this@HomeActivity, 7),
                AuthUi.dp(this@HomeActivity, 7),
                AuthUi.dp(this@HomeActivity, 7),
                AuthUi.dp(this@HomeActivity, 7)
            )
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(
                    Color.argb(250, 38, 16, 81),
                    Color.argb(252, 7, 7, 28)
                )
            ).apply {
                cornerRadius = AuthUi.dp(this@HomeActivity, 13).toFloat()
                setStroke(
                    AuthUi.dp(this@HomeActivity, 1),
                    Color.rgb(221, 176, 93)
                )
            }
        }

        menu.addView(
            TextView(this).apply {
                text = "ACCOUNT & SETTINGS"
                textSize = 9f
                setTextColor(Color.rgb(230, 196, 125))
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                letterSpacing = 0.10f
                setPadding(
                    0,
                    AuthUi.dp(this@HomeActivity, 1),
                    0,
                    AuthUi.dp(this@HomeActivity, 6)
                )
            },
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        fun addEntry(
            icon: String,
            title: String,
            subtitle: String,
            accent: Int,
            danger: Boolean = false,
            action: () -> Unit
        ) {
            menu.addView(
                createHubMenuRow(
                    icon = icon,
                    title = title,
                    subtitle = subtitle,
                    accentColor = accent,
                    danger = danger
                ) {
                    root.removeView(menu)
                    action()
                }
            )
        }

        addEntry(
            icon = "◈",
            title = "PLAYER ACCOUNT",
            subtitle = "Player name, email, identity, and account details.",
            accent = Color.rgb(169, 118, 255)
        ) {
            startActivity(
                Intent(
                    this@HomeActivity,
                    AccountActivity::class.java
                )
            )
        }

        addEntry(
            icon = "⚙",
            title = "SETTINGS",
            subtitle = "Audio, display, performance, controls, and accessibility.",
            accent = Color.rgb(106, 171, 255)
        ) {
            startActivity(
                Intent(
                    this@HomeActivity,
                    SettingsActivity::class.java
                )
            )
        }

        addEntry(
            icon = "↻",
            title = "UPDATE LOG",
            subtitle = "Recent changes, additions, fixes, and development notes.",
            accent = Color.rgb(101, 221, 192)
        ) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(
                        "https://github.com/demetrecerrone-star/" +
                            "Astralforge-Depthbound#update-log"
                    )
                )
            )
        }

        addEntry(
            icon = "?",
            title = "HELP & SUPPORT",
            subtitle = "Gameplay help, controls, troubleshooting, and support.",
            accent = Color.rgb(245, 201, 105)
        ) {
            openSection(
                "HELP & SUPPORT",
                "Gameplay help, controls, troubleshooting, and support options will live here."
            )
        }

        addEntry(
            icon = "i",
            title = "ABOUT",
            subtitle = "Version information, credits, and game details.",
            accent = Color.rgb(197, 145, 255)
        ) {
            openSection(
                "ABOUT",
                "Astral Forge: Depthbound • current development build."
            )
        }

        addEntry(
            icon = "⇥",
            title = "SIGN OUT",
            subtitle = "Sign out of this account and return to the login screen.",
            accent = Color.rgb(224, 82, 112),
            danger = true
        ) {
            showSignOutConfirmation(root)
        }

        root.addView(
            menu,
            FrameLayout.LayoutParams(
                menuWidth,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.TOP or Gravity.END
            ).apply {
                topMargin = (screenH * 0.098f).toInt()
                marginEnd = (screenW * 0.021f).toInt()
            }
        )
    }

    private fun dismissHubMenuDropdown(root: FrameLayout) {
        root.findViewWithTag<View>("hub_menu_dropdown")?.let {
            root.removeView(it)
        }
    }

    private fun createHubMenuRow(
        icon: String,
        title: String,
        subtitle: String,
        accentColor: Int,
        danger: Boolean = false,
        onClick: () -> Unit
    ): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            isClickable = true
            isFocusable = true
            setPadding(
                AuthUi.dp(this@HomeActivity, 8),
                AuthUi.dp(this@HomeActivity, 5),
                AuthUi.dp(this@HomeActivity, 8),
                AuthUi.dp(this@HomeActivity, 5)
            )
            background = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(
                    Color.argb(
                        if (danger) 235 else 222,
                        if (danger) 55 else 13,
                        if (danger) 13 else 10,
                        if (danger) 25 else 42
                    ),
                    Color.argb(225, 8, 8, 29)
                )
            ).apply {
                cornerRadius = AuthUi.dp(this@HomeActivity, 9).toFloat()
                setStroke(
                    AuthUi.dp(this@HomeActivity, 1),
                    accentColor
                )
            }
            installTouchFeedback()
            setOnClickListener { onClick() }

            addView(
                TextView(this@HomeActivity).apply {
                    text = icon
                    textSize = if (icon.length > 1) 15f else 18f
                    setTextColor(accentColor)
                    typeface = Typeface.DEFAULT_BOLD
                    gravity = Gravity.CENTER
                    setShadowLayer(
                        7f,
                        0f,
                        0f,
                        accentColor
                    )
                },
                LinearLayout.LayoutParams(
                    AuthUi.dp(this@HomeActivity, 34),
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
            )

            addView(
                LinearLayout(this@HomeActivity).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER_VERTICAL

                    addView(
                        TextView(this@HomeActivity).apply {
                            text = title
                            textSize = 10.2f
                            setTextColor(
                                if (danger) Color.rgb(255, 174, 190)
                                else Color.WHITE
                            )
                            typeface = Typeface.DEFAULT_BOLD
                            letterSpacing = 0.04f
                            maxLines = 1
                        }
                    )

                    addView(
                        TextView(this@HomeActivity).apply {
                            text = subtitle
                            textSize = 7.3f
                            setTextColor(
                                if (danger) Color.rgb(218, 145, 160)
                                else Color.rgb(183, 170, 213)
                            )
                            maxLines = 2
                            setPadding(
                                0,
                                AuthUi.dp(this@HomeActivity, 1),
                                0,
                                0
                            )
                        }
                    )
                },
                LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                AuthUi.dp(this@HomeActivity, 46)
            ).apply {
                bottomMargin = AuthUi.dp(this@HomeActivity, 5)
            }
        }
    }

    private fun showSignOutConfirmation(root: FrameLayout) {
        root.findViewWithTag<View>("hub_signout_confirm")?.let {
            root.removeView(it)
        }

        val screenW = resources.displayMetrics.widthPixels
        val screenH = resources.displayMetrics.heightPixels

        val panel = LinearLayout(this).apply {
            tag = "hub_signout_confirm"
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            elevation = AuthUi.dp(this@HomeActivity, 18).toFloat()
            setPadding(
                AuthUi.dp(this@HomeActivity, 16),
                AuthUi.dp(this@HomeActivity, 12),
                AuthUi.dp(this@HomeActivity, 16),
                AuthUi.dp(this@HomeActivity, 12)
            )
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(
                    Color.argb(252, 54, 14, 32),
                    Color.argb(252, 10, 7, 23)
                )
            ).apply {
                cornerRadius = AuthUi.dp(this@HomeActivity, 14).toFloat()
                setStroke(
                    AuthUi.dp(this@HomeActivity, 1),
                    Color.rgb(224, 82, 112)
                )
            }

            addView(
                TextView(this@HomeActivity).apply {
                    text = "SIGN OUT?"
                    textSize = 15f
                    setTextColor(Color.WHITE)
                    typeface = Typeface.DEFAULT_BOLD
                    gravity = Gravity.CENTER
                }
            )

            addView(
                TextView(this@HomeActivity).apply {
                    text = "You will return to the login screen."
                    textSize = 9f
                    setTextColor(Color.rgb(211, 177, 190))
                    gravity = Gravity.CENTER
                    setPadding(
                        0,
                        AuthUi.dp(this@HomeActivity, 3),
                        0,
                        AuthUi.dp(this@HomeActivity, 8)
                    )
                }
            )

            val actions = LinearLayout(this@HomeActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
            }

            fun confirmButton(
                label: String,
                accent: Int,
                action: () -> Unit
            ): TextView {
                return TextView(this@HomeActivity).apply {
                    text = label
                    textSize = 10f
                    setTextColor(Color.WHITE)
                    typeface = Typeface.DEFAULT_BOLD
                    gravity = Gravity.CENTER
                    isClickable = true
                    isFocusable = true
                    background = GradientDrawable().apply {
                        setColor(Color.argb(225, 13, 10, 37))
                        cornerRadius = AuthUi.dp(this@HomeActivity, 9).toFloat()
                        setStroke(
                            AuthUi.dp(this@HomeActivity, 1),
                            accent
                        )
                    }
                    installTouchFeedback()
                    setOnClickListener { action() }
                }
            }

            actions.addView(
                confirmButton(
                    "CANCEL",
                    Color.rgb(119, 93, 181)
                ) {
                    root.findViewWithTag<View>("hub_signout_confirm")?.let {
                        root.removeView(it)
                    }
                },
                LinearLayout.LayoutParams(
                    0,
                    AuthUi.dp(this@HomeActivity, 36),
                    1f
                ).apply {
                    marginEnd = AuthUi.dp(this@HomeActivity, 4)
                }
            )

            actions.addView(
                confirmButton(
                    "SIGN OUT",
                    Color.rgb(224, 82, 112)
                ) {
                    FirebaseAuth.getInstance().signOut()
                    getSharedPreferences("auth_prefs", MODE_PRIVATE)
                        .edit()
                        .clear()
                        .apply()
                    startActivity(
                        Intent(
                            this@HomeActivity,
                            MainActivity::class.java
                        )
                    )
                    finishAffinity()
                },
                LinearLayout.LayoutParams(
                    0,
                    AuthUi.dp(this@HomeActivity, 36),
                    1f
                ).apply {
                    marginStart = AuthUi.dp(this@HomeActivity, 4)
                }
            )

            addView(
                actions,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )
        }

        root.addView(
            panel,
            FrameLayout.LayoutParams(
                (screenW * 0.25f).toInt()
                    .coerceAtLeast(AuthUi.dp(this, 220)),
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
            )
        )
    }

    private fun showComingSoonPopup(
        root: FrameLayout,
        sectionTitle: String
    ) {
        val existing = root.findViewWithTag<View>("hub_coming_soon")
        if (existing != null) {
            root.removeView(existing)
        }

        val screenW = resources.displayMetrics.widthPixels
        val screenH = resources.displayMetrics.heightPixels
        val panel = FrameLayout(this).apply {
            tag = "hub_coming_soon"
            isClickable = true
            isFocusable = true
            elevation = AuthUi.dp(this@HomeActivity, 14).toFloat()
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(
                    Color.argb(247, 29, 13, 62),
                    Color.argb(249, 6, 7, 27)
                )
            ).apply {
                cornerRadius = AuthUi.dp(this@HomeActivity, 14).toFloat()
                setStroke(
                    AuthUi.dp(this@HomeActivity, 1),
                    Color.rgb(221, 176, 93)
                )
            }
        }

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(
                AuthUi.dp(this@HomeActivity, 18),
                AuthUi.dp(this@HomeActivity, 10),
                AuthUi.dp(this@HomeActivity, 18),
                AuthUi.dp(this@HomeActivity, 10)
            )

            addView(
                TextView(this@HomeActivity).apply {
                    text = sectionTitle
                    textSize = 9.5f
                    setTextColor(Color.rgb(224, 188, 112))
                    typeface = Typeface.DEFAULT_BOLD
                    gravity = Gravity.CENTER
                    letterSpacing = 0.08f
                },
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )

            addView(
                TextView(this@HomeActivity).apply {
                    text = "COMING SOON"
                    textSize = 18f
                    setTextColor(Color.WHITE)
                    typeface = Typeface.create(
                        Typeface.SERIF,
                        Typeface.BOLD
                    )
                    gravity = Gravity.CENTER
                    letterSpacing = 0.06f
                    setShadowLayer(
                        8f,
                        0f,
                        0f,
                        Color.rgb(133, 75, 255)
                    )
                },
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    1f
                )
            )
        }
        panel.addView(
            content,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        val close = TextView(this).apply {
            text = "×"
            textSize = 20f
            setTextColor(Color.rgb(232, 214, 255))
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            contentDescription = "Close"
            isClickable = true
            isFocusable = true
            background = GradientDrawable().apply {
                setColor(Color.argb(180, 11, 8, 34))
                shape = GradientDrawable.OVAL
                setStroke(
                    AuthUi.dp(this@HomeActivity, 1),
                    Color.rgb(139, 94, 225)
                )
            }
            installTouchFeedback()
            setOnClickListener {
                root.removeView(panel)
            }
        }
        panel.addView(
            close,
            FrameLayout.LayoutParams(
                AuthUi.dp(this, 28),
                AuthUi.dp(this, 28),
                Gravity.TOP or Gravity.END
            ).apply {
                topMargin = AuthUi.dp(this@HomeActivity, 5)
                marginEnd = AuthUi.dp(this@HomeActivity, 5)
            }
        )

        root.addView(
            panel,
            FrameLayout.LayoutParams(
                (screenW * 0.23f).toInt(),
                (screenH * 0.14f).toInt()
                    .coerceAtLeast(AuthUi.dp(this, 84)),
                Gravity.TOP or Gravity.END
            ).apply {
                topMargin = (screenH * 0.125f).toInt()
                marginEnd = (screenW * 0.022f).toInt()
            }
        )
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
