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
import com.google.firebase.auth.FirebaseAuth
import kotlin.math.min

class HomeActivity : Activity() {

    private val hubPack = "astral_hub_buttons_individual.zip"
    private val bottomPack = "bottomnav.zip"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AuthUi.setupWindow(this)

        val screenW = resources.displayMetrics.widthPixels
        val root = FrameLayout(this)

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
        animateBackground(backgroundImage)

        root.addView(
            View(this).apply {
                background = GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    intArrayOf(
                        Color.argb(48, 3, 5, 20),
                        Color.argb(14, 3, 5, 20),
                        Color.argb(24, 3, 5, 20),
                        Color.argb(118, 3, 5, 20)
                    )
                )
            },
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        root.addView(
            HubEffectView(this),
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        val progress = ProgressionStore.load(this)
        val user = FirebaseAuth.getInstance().currentUser
        val displayName = when {
            user?.isAnonymous == true -> "Guest Delver"
            !user?.displayName.isNullOrBlank() -> user?.displayName ?: "Delver"
            else -> "Delver"
        }

        root.addView(
            createStatusPanel(progress),
            FrameLayout.LayoutParams(
                min(screenW * 42 / 100, AuthUi.dp(this, 210)),
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.TOP or Gravity.END
            ).apply {
                topMargin = AuthUi.dp(this@HomeActivity, 28)
                marginEnd = AuthUi.dp(this@HomeActivity, 10)
            }
        )

        root.addView(
            TextView(this).apply {
                text = "ASTRAL FORGE: DEPTHBOUND"
                setTextColor(Color.WHITE)
                textSize = 16f
                typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                gravity = Gravity.CENTER
                letterSpacing = 0.03f
                setShadowLayer(8f, 0f, 0f, Color.rgb(95, 70, 255))
            },
            FrameLayout.LayoutParams(
                screenW * 54 / 100,
                AuthUi.dp(this, 42),
                Gravity.TOP or Gravity.CENTER_HORIZONTAL
            ).apply {
                topMargin = AuthUi.dp(this@HomeActivity, 28)
            }
        )

        root.addView(
            TextView(this).apply {
                text = displayName + "  •  Lv. " + progress.level
                setTextColor(Color.rgb(226, 219, 255))
                textSize = 11f
                gravity = Gravity.CENTER
            },
            FrameLayout.LayoutParams(
                screenW * 54 / 100,
                AuthUi.dp(this, 26),
                Gravity.TOP or Gravity.CENTER_HORIZONTAL
            ).apply {
                topMargin = AuthUi.dp(this@HomeActivity, 63)
            }
        )

        val dungeons = addHubButton(
            root, "dungeons.png", "Dungeons", screenW * 56 / 100, 0.22f, 0.245f
        ) {
            openSection("DUNGEONS", "Choose a depth, enter a dungeon, and claim its rewards.")
        }
        pulse(dungeons, 1.0f, 1.025f, 1850L)

        addHubButton(root, "shop.png", "Shop", screenW * 42 / 100, 0.015f, 0.405f) {
            openSection("SHOP", "Spend gold and Astral Shards on supplies and upgrades.")
        }

        addHubButton(root, "forge.png", "Forge", screenW * 42 / 100, 0.565f, 0.405f) {
            openSection("FORGE", "Enhance weapons, armor, and relics at the Astral Forge.")
        }

        val summon = addHubButton(
            root, "summon.png", "Summon", screenW * 43 / 100, 0.285f, 0.515f
        ) {
            openSection("SUMMON", "Call heroes, relics, and rare astral powers from beyond the veil.")
        }
        pulse(summon, 1.0f, 1.02f, 2100L)

        addHubButton(root, "heroes.png", "Heroes", screenW * 42 / 100, 0.02f, 0.635f) {
            startActivity(Intent(this, CharacterActivity::class.java))
        }

        addHubButton(root, "guild.png", "Guild", screenW * 42 / 100, 0.56f, 0.635f) {
            openSection("GUILD", "Guild progression, members, raids, contributions, and rewards.")
        }

        addHubButton(root, "quests.png", "Quests", screenW * 40 / 100, 0.585f, 0.748f) {
            openSection("QUESTS", "Main quests, side missions, dailies, and milestone rewards.")
        }

        val battleButton = ImageView(this).apply {
            setImageResource(R.drawable.file_00000000b44481f6b8c3df3c70bf55e3)
            scaleType = ImageView.ScaleType.FIT_CENTER
            adjustViewBounds = true
            contentDescription = "Battle"
            isClickable = true
            isFocusable = true
            setOnClickListener {
                openSection("BATTLE", "Enter the Depths and face the monsters below.")
            }
            installTouchFeedback()
        }
        val battleWidth = screenW * 60 / 100
        val battleParams = FrameLayout.LayoutParams(
            battleWidth,
            battleWidth * 34 / 100,
            Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        ).apply {
            bottomMargin = AuthUi.dp(this@HomeActivity, 102)
        }
        root.addView(battleButton, battleParams)
        pulse(battleButton, 1.0f, 1.035f, 1600L)

        val bottomNav = createBottomNav(screenW)
        val bottomNavParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            AuthUi.dp(this, 88),
            Gravity.BOTTOM
        ).apply {
            bottomMargin = AuthUi.dp(this@HomeActivity, 8)
        }
        root.addView(bottomNav, bottomNavParams)

        root.setOnApplyWindowInsetsListener { _, insets: WindowInsets ->
            val systemBottom = insets.systemWindowInsetBottom
            val safeGap = AuthUi.dp(this@HomeActivity, 8)

            bottomNavParams.bottomMargin = systemBottom + safeGap
            bottomNav.layoutParams = bottomNavParams

            battleParams.bottomMargin =
                systemBottom + safeGap + AuthUi.dp(this@HomeActivity, 94)
            battleButton.layoutParams = battleParams

            insets
        }
        root.requestApplyInsets()

        setContentView(root)
    }

    private fun createStatusPanel(progress: PlayerProgress): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.END
            setPadding(
                AuthUi.dp(this@HomeActivity, 10),
                AuthUi.dp(this@HomeActivity, 7),
                AuthUi.dp(this@HomeActivity, 10),
                AuthUi.dp(this@HomeActivity, 7)
            )
            background = GradientDrawable().apply {
                setColor(Color.argb(205, 5, 8, 24))
                cornerRadius = AuthUi.dp(this@HomeActivity, 10).toFloat()
                setStroke(AuthUi.dp(this@HomeActivity, 1), Color.rgb(202, 165, 92))
            }

            addView(statusLine("Gold", progress.gold.toString(), Color.rgb(255, 204, 72)))
            addView(statusLine("Shards", progress.astralShards.toString(), Color.rgb(128, 113, 255)))
            addView(statusLine("Energy", progress.energy.toString() + "/" + progress.maxEnergy, Color.rgb(80, 192, 255)))
        }
    }

    private fun statusLine(label: String, value: String, glowColor: Int): TextView {
        return TextView(this).apply {
            text = label + "  " + value
            textSize = 12f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.END
            setPadding(0, AuthUi.dp(this@HomeActivity, 2), 0, AuthUi.dp(this@HomeActivity, 2))
            setShadowLayer(4f, 0f, 0f, glowColor)
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
    ): ImageView {
        val bitmap = ButtonAssetStore.load(this, hubPack, fileName)
        val ratio = if (bitmap != null && bitmap.width > 0) {
            bitmap.height.toFloat() / bitmap.width.toFloat()
        } else {
            0.42f
        }

        val button = ImageView(this).apply {
            if (bitmap != null) setImageBitmap(bitmap)
            scaleType = ImageView.ScaleType.FIT_CENTER
            adjustViewBounds = true
            contentDescription = description
            isClickable = true
            isFocusable = true
            setOnClickListener { onClick() }
            installTouchFeedback()
        }

        val params = FrameLayout.LayoutParams(
            widthPx,
            (widthPx * ratio).toInt().coerceAtLeast(AuthUi.dp(this, 48))
        )
        params.leftMargin = (resources.displayMetrics.widthPixels * leftFraction).toInt()
        params.topMargin = (resources.displayMetrics.heightPixels * topFraction).toInt()
        root.addView(button, params)
        return button
    }

    private fun createBottomNav(screenW: Int): LinearLayout {
        val nav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(
                AuthUi.dp(this@HomeActivity, 8),
                AuthUi.dp(this@HomeActivity, 4),
                AuthUi.dp(this@HomeActivity, 8),
                AuthUi.dp(this@HomeActivity, 4)
            )
            background = GradientDrawable().apply {
                setColor(Color.argb(224, 4, 7, 22))
                setStroke(AuthUi.dp(this@HomeActivity, 1), Color.rgb(126, 99, 225))
            }
        }

        val navFiles = listOf("home.png", "battle.png", "heroes.png", "gear.png", "menu.png")
        val navLabels = listOf("Home", "Battle", "Heroes", "Gear", "Menu")
        val itemWidth = (screenW - AuthUi.dp(this, 16)) / navFiles.size

        navFiles.forEachIndexed { index, fileName ->
            val bitmap = ButtonAssetStore.load(this, bottomPack, fileName)
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
                        1 -> openSection("BATTLE", "Enter the Depths and face the monsters below.")
                        2 -> startActivity(Intent(this@HomeActivity, CharacterActivity::class.java))
                        3 -> openSection("GEAR", "Weapons, armor, accessories, loadouts, and equipment power.")
                        4 -> openSection("MENU", "Settings, account options, achievements, and additional systems.")
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

    private fun ImageView.installTouchFeedback() {
        setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
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

    private fun pulse(view: View, from: Float, to: Float, durationMs: Long) {
        ObjectAnimator.ofFloat(view, View.SCALE_X, from, to).apply {
            duration = durationMs
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
        ObjectAnimator.ofFloat(view, View.SCALE_Y, from, to).apply {
            duration = durationMs
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    private fun openSection(title: String, subtitle: String) {
        startActivity(
            Intent(this, GameSectionActivity::class.java)
                .putExtra("section_title", title)
                .putExtra("section_subtitle", subtitle)
        )
    }
}
