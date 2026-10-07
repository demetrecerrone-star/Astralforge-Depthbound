package com.demetrecerrone.astralforge

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.random.Random

class BattleActivity : Activity() {

    private val handler = Handler(Looper.getMainLooper())
    private val battlePack = "astral_battle_sprite_assets_split.zip"

    private lateinit var root: FrameLayout
    private lateinit var playerSprite: ImageView
    private lateinit var enemySprite: ImageView
    private lateinit var effectLayer: FrameLayout
    private lateinit var playerHpBar: ProgressBar
    private lateinit var enemyHpBar: ProgressBar
    private lateinit var playerHpText: TextView
    private lateinit var enemyHpText: TextView
    private lateinit var enemyNameText: TextView
    private lateinit var depthText: TextView
    private lateinit var autoText: TextView
    private lateinit var attackButton: View

    private var playerIdle: Bitmap? = null
    private var enemyIdle: Bitmap? = null
    private var playerAttackFrames = emptyList<Bitmap>()
    private var playerHitFrames = emptyList<Bitmap>()
    private var playerDeathFrames = emptyList<Bitmap>()
    private var enemyAttackFrames = emptyList<Bitmap>()
    private var enemyHitFrames = emptyList<Bitmap>()
    private var enemyDeathFrames = emptyList<Bitmap>()

    private lateinit var progress: PlayerProgress
    private lateinit var settings: GameSettings

    private var playerHp = 1
    private var enemyHp = 1
    private var enemyMaxHp = 1
    private var enemyAttack = 1
    private var enemyDefense = 0
    private var depth = 1
    private var autoBattle = true
    private var busy = false
    private var battleOver = false
    private var combatStarted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AuthUi.setupWindow(this)

        progress = ProgressionStore.load(this)
        settings = GameSettingsStore.load(this)
        depth = progress.depth.coerceAtLeast(1)

        root = FrameLayout(this)
        buildScene()
        setContentView(root)

        if (progress.energy < ENERGY_COST) {
            showNoEnergyOverlay()
            return
        }

        progress = progress.copy(
            energy = (progress.energy - ENERGY_COST).coerceAtLeast(0)
        )
        ProgressionStore.save(this, progress)

        setupBattleStats()
        refreshHud()
        loadSpritesAsync()
        startEntrance()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    private fun buildScene() {
        root.setBackgroundColor(Color.rgb(3, 5, 18))

        root.addView(
            ImageView(this).apply {
                setImageResource(
                    R.drawable.file_000000003c3881f5b27141213085d016
                )
                scaleType = ImageView.ScaleType.CENTER_CROP
                contentDescription = null
            },
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        addDepthLayer(
            R.drawable.file_00000000032c81f591f756b1c82fa45a,
            0.88f,
            0.34f,
            Gravity.TOP or Gravity.CENTER_HORIZONTAL,
            0.12f,
            0.26f
        )
        addDepthLayer(
            R.drawable.file_00000000d9cc81f5bf3bb534253cd001,
            0.46f,
            0.42f,
            Gravity.START or Gravity.CENTER_VERTICAL,
            0.08f,
            0.42f
        )
        addDepthLayer(
            R.drawable.file_00000000863481f7b4231b7585dac471,
            0.43f,
            0.40f,
            Gravity.END or Gravity.CENTER_VERTICAL,
            0.08f,
            0.42f
        )

        root.addView(
            View(this).apply {
                background = GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    intArrayOf(
                        Color.argb(105, 2, 4, 15),
                        Color.argb(18, 2, 4, 15),
                        Color.argb(12, 2, 4, 15),
                        Color.argb(135, 2, 4, 15)
                    )
                )
            },
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        addDepthLayer(
            R.drawable.file_00000000b24081f5a82a6672ed3d906a,
            1f,
            0.17f,
            Gravity.TOP or Gravity.CENTER_HORIZONTAL,
            0f,
            0.66f
        )

        effectLayer = FrameLayout(this)
        root.addView(
            effectLayer,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        val screenW = resources.displayMetrics.widthPixels
        val screenH = resources.displayMetrics.heightPixels

        playerSprite = ImageView(this).apply {
            scaleType = ImageView.ScaleType.FIT_CENTER
            contentDescription = "Player"
            alpha = 0f
        }
        root.addView(
            playerSprite,
            FrameLayout.LayoutParams(
                (screenW * 0.40f).toInt(),
                (screenH * 0.34f).toInt(),
                Gravity.START or Gravity.CENTER_VERTICAL
            ).apply {
                marginStart = AuthUi.dp(this@BattleActivity, 8)
                topMargin = AuthUi.dp(this@BattleActivity, 58)
            }
        )

        enemySprite = ImageView(this).apply {
            scaleType = ImageView.ScaleType.FIT_CENTER
            contentDescription = "Enemy"
            alpha = 0f
        }
        root.addView(
            enemySprite,
            FrameLayout.LayoutParams(
                (screenW * 0.42f).toInt(),
                (screenH * 0.35f).toInt(),
                Gravity.END or Gravity.CENTER_VERTICAL
            ).apply {
                marginEnd = AuthUi.dp(this@BattleActivity, 6)
                topMargin = AuthUi.dp(this@BattleActivity, 34)
            }
        )

        addDepthLayer(
            R.drawable.file_00000000a11c81f5844f422e4a15578b,
            1f,
            0.24f,
            Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL,
            0f,
            0.80f
        )

        val enemyHud = createHpHud(
            "Astral Warden",
            "Lv. " + depth,
            100,
            Color.rgb(233, 61, 93)
        )
        enemyNameText = enemyHud.title
        enemyHpBar = enemyHud.bar
        enemyHpText = enemyHud.value

        val enemyHudParams = FrameLayout.LayoutParams(
            (screenW * 0.72f).toInt(),
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.TOP or Gravity.CENTER_HORIZONTAL
        ).apply {
            topMargin = AuthUi.dp(this@BattleActivity, 18)
        }
        root.addView(enemyHud.root, enemyHudParams)

        depthText = TextView(this).apply {
            text = "DEPTH " + depth + "  •  WAVE 1"
            textSize = 12f
            setTextColor(Color.rgb(220, 207, 255))
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            letterSpacing = 0.08f
            setShadowLayer(4f, 0f, 0f, Color.rgb(96, 64, 220))
        }
        root.addView(
            depthText,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                AuthUi.dp(this, 28),
                Gravity.TOP
            ).apply {
                topMargin = AuthUi.dp(this@BattleActivity, 106)
            }
        )

        val bottomPanel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(
                AuthUi.dp(this@BattleActivity, 12),
                AuthUi.dp(this@BattleActivity, 10),
                AuthUi.dp(this@BattleActivity, 12),
                AuthUi.dp(this@BattleActivity, 8)
            )
            background = GradientDrawable().apply {
                setColor(Color.argb(222, 4, 7, 24))
                setStroke(
                    AuthUi.dp(this@BattleActivity, 1),
                    Color.rgb(117, 89, 204)
                )
            }
        }

        val user = FirebaseAuth.getInstance().currentUser
        val playerName = when {
            user?.isAnonymous == true -> "Guest Delver"
            !user?.displayName.isNullOrBlank() -> user?.displayName ?: "Delver"
            else -> "Delver"
        }

        val playerHud = createHpHud(
            playerName,
            "Lv. " + progress.level + "  •  BP " + progress.powerRating,
            progress.maxHp,
            Color.rgb(64, 196, 255)
        )
        playerHpBar = playerHud.bar
        playerHpText = playerHud.value
        bottomPanel.addView(
            playerHud.root,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        val actionRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, AuthUi.dp(this@BattleActivity, 8), 0, 0)
        }

        autoText = makeSmallAction("AUTO ON") {
            autoBattle = !autoBattle
            autoText.text = if (autoBattle) "AUTO ON" else "AUTO OFF"
            autoText.setTextColor(
                if (autoBattle) Color.rgb(158, 232, 255)
                else Color.rgb(187, 174, 208)
            )
            if (autoBattle && !busy && !battleOver && combatStarted) {
                handler.postDelayed({ performPlayerAttack() }, 350L)
            }
        }
        actionRow.addView(
            autoText,
            LinearLayout.LayoutParams(
                0,
                AuthUi.dp(this, 54),
                1f
            ).apply {
                marginEnd = AuthUi.dp(this@BattleActivity, 6)
            }
        )

        attackButton = createAssetActionButton(
            "attack_button.png",
            "ATTACK"
        ) {
            performPlayerAttack()
        }
        actionRow.addView(
            attackButton,
            LinearLayout.LayoutParams(
                0,
                AuthUi.dp(this, 66),
                1.25f
            ).apply {
                marginStart = AuthUi.dp(this@BattleActivity, 4)
                marginEnd = AuthUi.dp(this@BattleActivity, 4)
            }
        )

        val settingsButton = createAssetActionButton(
            "settings_button.png",
            "SETTINGS"
        ) {
            startActivity(Intent(this@BattleActivity, SettingsActivity::class.java))
        }
        actionRow.addView(
            settingsButton,
            LinearLayout.LayoutParams(
                0,
                AuthUi.dp(this, 54),
                1f
            ).apply {
                marginStart = AuthUi.dp(this@BattleActivity, 6)
            }
        )

        bottomPanel.addView(
            actionRow,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        val bottomParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.BOTTOM
        )
        root.addView(bottomPanel, bottomParams)

        root.setOnApplyWindowInsetsListener { _, insets: WindowInsets ->
            enemyHudParams.topMargin =
                insets.systemWindowInsetTop + AuthUi.dp(this@BattleActivity, 10)
            enemyHud.root.layoutParams = enemyHudParams

            bottomParams.bottomMargin = insets.systemWindowInsetBottom
            bottomPanel.layoutParams = bottomParams
            insets
        }
        root.requestApplyInsets()
    }

    private fun addDepthLayer(
        resId: Int,
        widthFraction: Float,
        heightFraction: Float,
        gravity: Int,
        topFraction: Float,
        alphaValue: Float
    ) {
        val screenW = resources.displayMetrics.widthPixels
        val screenH = resources.displayMetrics.heightPixels
        root.addView(
            ImageView(this).apply {
                setImageResource(resId)
                scaleType = ImageView.ScaleType.FIT_CENTER
                alpha = alphaValue
                contentDescription = null
            },
            FrameLayout.LayoutParams(
                (screenW * widthFraction).toInt(),
                (screenH * heightFraction).toInt(),
                gravity
            ).apply {
                topMargin = (screenH * topFraction).toInt()
            }
        )
    }

    private data class HpHud(
        val root: LinearLayout,
        val title: TextView,
        val bar: ProgressBar,
        val value: TextView
    )

    private fun createHpHud(
        title: String,
        subtitle: String,
        maxHp: Int,
        barColor: Int
    ): HpHud {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                AuthUi.dp(this@BattleActivity, 12),
                AuthUi.dp(this@BattleActivity, 8),
                AuthUi.dp(this@BattleActivity, 12),
                AuthUi.dp(this@BattleActivity, 8)
            )
            background = GradientDrawable().apply {
                setColor(Color.argb(220, 5, 8, 26))
                cornerRadius = AuthUi.dp(this@BattleActivity, 12).toFloat()
                setStroke(
                    AuthUi.dp(this@BattleActivity, 1),
                    Color.rgb(176, 134, 228)
                )
            }
        }

        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val titleView = TextView(this).apply {
            text = title
            textSize = 14f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            setShadowLayer(4f, 0f, 0f, Color.rgb(91, 61, 190))
        }
        top.addView(
            titleView,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        top.addView(
            TextView(this).apply {
                text = subtitle
                textSize = 11f
                setTextColor(Color.rgb(191, 179, 221))
            }
        )
        container.addView(top)

        val bar = ProgressBar(
            this,
            null,
            android.R.attr.progressBarStyleHorizontal
        ).apply {
            max = maxHp.coerceAtLeast(1)
            progress = max
            progressTintList = ColorStateList.valueOf(barColor)
            progressBackgroundTintList =
                ColorStateList.valueOf(Color.rgb(29, 25, 46))
        }

        container.addView(
            bar,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                AuthUi.dp(this, 12)
            ).apply {
                topMargin = AuthUi.dp(this@BattleActivity, 5)
            }
        )

        val value = TextView(this).apply {
            text = maxHp.toString() + " / " + maxHp
            textSize = 10.5f
            setTextColor(Color.rgb(218, 207, 239))
            gravity = Gravity.END
        }
        container.addView(value)

        return HpHud(container, titleView, bar, value)
    }

    private fun setupBattleStats() {
        progress = ProgressionStore.load(this)
        playerHp = progress.maxHp
        enemyMaxHp = 105 + depth * 35
        enemyHp = enemyMaxHp
        enemyAttack = 10 + depth * 4
        enemyDefense = 3 + depth * 2
        playerHpBar.max = progress.maxHp
        playerHpBar.progress = playerHp
        enemyHpBar.max = enemyMaxHp
        enemyHpBar.progress = enemyHp
    }

    private fun refreshHud() {
        playerHpBar.progress = playerHp.coerceAtLeast(0)
        enemyHpBar.progress = enemyHp.coerceAtLeast(0)
        playerHpText.text =
            playerHp.coerceAtLeast(0).toString() + " / " + progress.maxHp
        enemyHpText.text =
            enemyHp.coerceAtLeast(0).toString() + " / " + enemyMaxHp
        depthText.text = "DEPTH " + depth + "  •  WAVE 1"
    }

    private fun loadSpritesAsync() {
        Thread {
            val playerPick = SpriteZipStore.pickIdle(this, "characters.zip")
            val enemyPick =
                SpriteZipStore.pickIdle(this, "monsters-first-dungeon.zip")

            val loadedPlayerAttack =
                SpriteZipStore.loadActionFrames(
                    this, "character-frames.zip", "attack", playerPick.hint
                )
            val loadedPlayerHit =
                SpriteZipStore.loadActionFrames(
                    this, "character-frames.zip", "hit", playerPick.hint
                )
            val loadedPlayerDeath =
                SpriteZipStore.loadActionFrames(
                    this, "character-frames.zip", "death", playerPick.hint
                )

            val loadedEnemyAttack =
                SpriteZipStore.loadActionFrames(
                    this, "monster-frames.zip", "attack", enemyPick.hint
                )
            val loadedEnemyHit =
                SpriteZipStore.loadActionFrames(
                    this, "monster-frames.zip", "hit", enemyPick.hint
                )
            val loadedEnemyDeath =
                SpriteZipStore.loadActionFrames(
                    this, "monster-frames.zip", "death", enemyPick.hint
                )

            runOnUiThread {
                playerIdle = playerPick.bitmap ?: loadedPlayerAttack.firstOrNull()
                enemyIdle = enemyPick.bitmap ?: loadedEnemyAttack.firstOrNull()
                playerAttackFrames = loadedPlayerAttack
                playerHitFrames = loadedPlayerHit
                playerDeathFrames = loadedPlayerDeath
                enemyAttackFrames = loadedEnemyAttack
                enemyHitFrames = loadedEnemyHit
                enemyDeathFrames = loadedEnemyDeath

                playerIdle?.let { playerSprite.setImageBitmap(it) }
                enemyIdle?.let { enemySprite.setImageBitmap(it) }

                enemyNameText.text =
                    SpriteZipStore.displayNameFromHint(
                        enemyPick.hint,
                        "Astral Warden"
                    )
            }
        }.start()
    }

    private fun startEntrance() {
        showEffect("spawn_burst.png", 0.22f, 0.49f, 0.32f)
        showEffect("portal_arrival.png", 0.70f, 0.46f, 0.34f)

        playerSprite.translationX = -AuthUi.dp(this, 38).toFloat()
        enemySprite.translationX = AuthUi.dp(this, 38).toFloat()

        playerSprite.animate()
            .alpha(1f)
            .translationX(0f)
            .setDuration(if (settings.reducedMotion) 120L else 460L)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        enemySprite.animate()
            .alpha(1f)
            .translationX(0f)
            .setDuration(if (settings.reducedMotion) 120L else 520L)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                combatStarted = true
                if (autoBattle) {
                    handler.postDelayed({ performPlayerAttack() }, 550L)
                }
            }
            .start()
    }

    private fun performPlayerAttack() {
        if (!combatStarted || busy || battleOver) return
        busy = true

        val variance = Random.nextInt(0, 7)
        val baseDamage = max(1, progress.attack + variance - enemyDefense)
        val crit = Random.nextInt(0, 100) < progress.critChance
        val damage =
            if (crit) (baseDamage * 1.75f).roundToInt() else baseDamage

        if (!settings.reducedMotion) {
            playerSprite.animate()
                .translationX(AuthUi.dp(this, 26).toFloat())
                .setDuration(120L)
                .withEndAction {
                    playerSprite.animate()
                        .translationX(0f)
                        .setDuration(150L)
                        .start()
                }
                .start()
        }

        playSequence(
            playerSprite,
            playerAttackFrames,
            playerIdle,
            if (settings.reducedMotion) 55L else 85L
        )

        handler.postDelayed(
            {
                if (settings.battleEffects) {
                    showEffect(
                        if (crit) "gold_crit_flash.png" else "wide_slash.png",
                        0.72f,
                        0.47f,
                        if (crit) 0.29f else 0.25f
                    )
                }

                enemyHp = (enemyHp - damage).coerceAtLeast(0)
                refreshHud()

                if (settings.damageNumbers) {
                    showDamageSplat(damage, crit, 0.72f, 0.37f)
                }

                if (crit) AppHaptics.tap(this)

                playSequence(
                    enemySprite,
                    enemyHitFrames,
                    enemyIdle,
                    70L
                )

                if (enemyHp <= 0) {
                    handleVictory()
                } else {
                    handler.postDelayed(
                        { performEnemyAttack() },
                        if (settings.reducedMotion) 280L else 650L
                    )
                }
            },
            if (settings.reducedMotion) 100L else 250L
        )
    }

    private fun performEnemyAttack() {
        if (battleOver) return

        val damage = max(
            1,
            enemyAttack + Random.nextInt(0, 6) - progress.defense / 2
        )

        if (!settings.reducedMotion) {
            enemySprite.animate()
                .translationX(-AuthUi.dp(this, 24).toFloat())
                .setDuration(120L)
                .withEndAction {
                    enemySprite.animate()
                        .translationX(0f)
                        .setDuration(150L)
                        .start()
                }
                .start()
        }

        playSequence(
            enemySprite,
            enemyAttackFrames,
            enemyIdle,
            if (settings.reducedMotion) 55L else 85L
        )

        handler.postDelayed(
            {
                if (settings.battleEffects) {
                    showEffect("red_hit_flash.png", 0.27f, 0.50f, 0.23f)
                }

                playerHp = (playerHp - damage).coerceAtLeast(0)
                refreshHud()

                if (settings.damageNumbers) {
                    showDamageSplat(damage, false, 0.27f, 0.40f)
                }

                playSequence(
                    playerSprite,
                    playerHitFrames,
                    playerIdle,
                    70L
                )

                if (playerHp <= 0) {
                    handleDefeat()
                } else {
                    busy = false
                    if (autoBattle) {
                        handler.postDelayed(
                            { performPlayerAttack() },
                            if (settings.reducedMotion) 320L else 700L
                        )
                    }
                }
            },
            if (settings.reducedMotion) 100L else 250L
        )
    }

    private fun handleVictory() {
        battleOver = true
        busy = true
        attackButton.isEnabled = false

        playSequence(
            enemySprite,
            enemyDeathFrames,
            null,
            if (settings.reducedMotion) 55L else 95L
        ) {
            enemySprite.animate()
                .alpha(0f)
                .setDuration(if (settings.reducedMotion) 100L else 360L)
                .start()
        }

        if (settings.battleEffects) {
            showEffect(
                "enemy_defeat_dissolve.png",
                0.72f,
                0.48f,
                0.34f
            )
        }

        val goldReward = 18 + depth * 9
        val xpReward = 28 + depth * 16
        val shardReward = if (depth % 5 == 0) 1 else 0

        var current = ProgressionStore.load(this)
        current = current.copy(
            gold = current.gold + goldReward,
            astralShards = current.astralShards + shardReward,
            depth = current.depth + 1
        )
        ProgressionStore.save(this, current)
        progress = ProgressionStore.addXp(this, xpReward)

        handler.postDelayed(
            {
                var detail =
                    "+" + goldReward + " Gold   •   +" + xpReward + " XP"
                if (shardReward > 0) {
                    detail += "   •   +" + shardReward + " Shard"
                }

                showResultOverlay(
                    true,
                    "victory_banner.png",
                    "Depth Cleared",
                    detail
                )
            },
            if (settings.reducedMotion) 160L else 650L
        )
    }

    private fun handleDefeat() {
        battleOver = true
        busy = true
        attackButton.isEnabled = false

        playSequence(
            playerSprite,
            playerDeathFrames,
            null,
            if (settings.reducedMotion) 55L else 95L
        ) {
            playerSprite.animate()
                .alpha(0.18f)
                .setDuration(if (settings.reducedMotion) 100L else 300L)
                .start()
        }

        handler.postDelayed(
            {
                showResultOverlay(
                    false,
                    "defeat_banner.png",
                    "Defeated at Depth " + depth,
                    "Strengthen your hero and return to the Depths."
                )
            },
            if (settings.reducedMotion) 150L else 500L
        )
    }

    private fun playSequence(
        target: ImageView,
        frames: List<Bitmap>,
        restore: Bitmap?,
        frameMs: Long,
        onEnd: (() -> Unit)? = null
    ) {
        if (frames.isEmpty() || settings.reducedMotion) {
            restore?.let { target.setImageBitmap(it) }
            onEnd?.invoke()
            return
        }

        var index = 0
        val runnable = object : Runnable {
            override fun run() {
                if (index < frames.size) {
                    target.setImageBitmap(frames[index])
                    index += 1
                    handler.postDelayed(this, frameMs)
                } else {
                    if (restore != null) target.setImageBitmap(restore)
                    onEnd?.invoke()
                }
            }
        }
        handler.post(runnable)
    }

    private fun showDamageSplat(
        value: Int,
        crit: Boolean,
        xFraction: Float,
        yFraction: Float
    ) {
        val wrapper = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            alpha = 0f
            scaleX = if (crit) 1.18f else 1f
            scaleY = if (crit) 1.18f else 1f
        }

        if (crit) {
            ButtonAssetStore.load(this, battlePack, "crit_text.png")?.let {
                wrapper.addView(
                    ImageView(this).apply {
                        setImageBitmap(it)
                        scaleType = ImageView.ScaleType.FIT_CENTER
                    },
                    LinearLayout.LayoutParams(
                        AuthUi.dp(this, 92),
                        AuthUi.dp(this, 34)
                    )
                )
            }
        }

        val digits = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        value.toString().forEach { char ->
            val name =
                if (crit) "crit_number_" + char + ".png"
                else "normal_" + char + ".png"
            val bitmap = ButtonAssetStore.load(this, battlePack, name)

            if (bitmap != null) {
                digits.addView(
                    ImageView(this).apply {
                        setImageBitmap(bitmap)
                        scaleType = ImageView.ScaleType.FIT_CENTER
                    },
                    LinearLayout.LayoutParams(
                        AuthUi.dp(this, if (crit) 30 else 25),
                        AuthUi.dp(this, if (crit) 42 else 36)
                    ).apply {
                        marginEnd = -AuthUi.dp(this@BattleActivity, 3)
                    }
                )
            } else {
                digits.addView(
                    TextView(this).apply {
                        text = char.toString()
                        textSize = if (crit) 26f else 22f
                        setTextColor(
                            if (crit) Color.rgb(255, 196, 69)
                            else Color.WHITE
                        )
                        typeface = Typeface.DEFAULT_BOLD
                    }
                )
            }
        }
        wrapper.addView(digits)

        val screenW = resources.displayMetrics.widthPixels
        val screenH = resources.displayMetrics.heightPixels

        effectLayer.addView(
            wrapper,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                leftMargin =
                    (screenW * xFraction).toInt() -
                        AuthUi.dp(this@BattleActivity, 45)
                topMargin = (screenH * yFraction).toInt()
            }
        )

        wrapper.translationY = AuthUi.dp(this, 14).toFloat()
        wrapper.animate()
            .alpha(1f)
            .translationY(-AuthUi.dp(this, 28).toFloat())
            .setDuration(if (settings.reducedMotion) 300L else 650L)
            .withEndAction {
                wrapper.animate()
                    .alpha(0f)
                    .translationY(-AuthUi.dp(this, 52).toFloat())
                    .setDuration(if (settings.reducedMotion) 180L else 380L)
                    .withEndAction { effectLayer.removeView(wrapper) }
                    .start()
            }
            .start()
    }

    private fun showEffect(
        assetName: String,
        xFraction: Float,
        yFraction: Float,
        widthFraction: Float
    ) {
        if (!settings.battleEffects) return

        val bitmap = ButtonAssetStore.load(this, battlePack, assetName) ?: return
        val screenW = resources.displayMetrics.widthPixels
        val screenH = resources.displayMetrics.heightPixels
        val width = (screenW * widthFraction).toInt()

        val effect = ImageView(this).apply {
            setImageBitmap(bitmap)
            scaleType = ImageView.ScaleType.FIT_CENTER
            alpha = 0f
            scaleX = 0.72f
            scaleY = 0.72f
            contentDescription = null
        }

        effectLayer.addView(
            effect,
            FrameLayout.LayoutParams(width, width).apply {
                leftMargin = (screenW * xFraction).toInt() - width / 2
                topMargin = (screenH * yFraction).toInt() - width / 2
            }
        )

        effect.animate()
            .alpha(if (settings.visualQuality == "LOW") 0.72f else 1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(if (settings.reducedMotion) 100L else 220L)
            .withEndAction {
                effect.animate()
                    .alpha(0f)
                    .scaleX(1.12f)
                    .scaleY(1.12f)
                    .setDuration(if (settings.reducedMotion) 120L else 360L)
                    .withEndAction { effectLayer.removeView(effect) }
                    .start()
            }
            .start()
    }

    private fun createAssetActionButton(
        assetName: String,
        fallbackText: String,
        onClick: () -> Unit
    ): View {
        val holder = FrameLayout(this).apply {
            isClickable = true
            isFocusable = true
            contentDescription = fallbackText
            background = GradientDrawable().apply {
                setColor(Color.argb(210, 9, 12, 35))
                cornerRadius = AuthUi.dp(this@BattleActivity, 12).toFloat()
                setStroke(
                    AuthUi.dp(this@BattleActivity, 1),
                    Color.rgb(131, 93, 221)
                )
            }
            setOnClickListener {
                AppHaptics.tap(this@BattleActivity)
                onClick()
            }
            installPressFeedback()
        }

        val bitmap = ButtonAssetStore.load(this, battlePack, assetName)
        if (bitmap != null) {
            holder.addView(
                ImageView(this).apply {
                    setImageBitmap(bitmap)
                    scaleType = ImageView.ScaleType.FIT_CENTER
                },
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            )
        } else {
            holder.addView(
                TextView(this).apply {
                    text = fallbackText
                    gravity = Gravity.CENTER
                    textSize = 13f
                    setTextColor(Color.WHITE)
                    typeface = Typeface.DEFAULT_BOLD
                },
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            )
        }
        return holder
    }

    private fun makeSmallAction(
        textValue: String,
        onClick: () -> Unit
    ): TextView {
        return TextView(this).apply {
            text = textValue
            gravity = Gravity.CENTER
            textSize = 11.5f
            setTextColor(Color.rgb(158, 232, 255))
            typeface = Typeface.DEFAULT_BOLD
            background = GradientDrawable().apply {
                setColor(Color.argb(210, 9, 12, 35))
                cornerRadius = AuthUi.dp(this@BattleActivity, 12).toFloat()
                setStroke(
                    AuthUi.dp(this@BattleActivity, 1),
                    Color.rgb(106, 93, 189)
                )
            }
            isClickable = true
            isFocusable = true
            setOnClickListener {
                AppHaptics.tap(this@BattleActivity)
                onClick()
            }
            installPressFeedback()
        }
    }

    private fun View.installPressFeedback() {
        setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    view.animate()
                        .scaleX(0.96f)
                        .scaleY(0.96f)
                        .alpha(0.84f)
                        .setDuration(60L)
                        .start()
                }
                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> {
                    view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .alpha(1f)
                        .setDuration(90L)
                        .start()
                }
            }
            false
        }
    }

    private fun showResultOverlay(
        victory: Boolean,
        titleAsset: String,
        headline: String,
        detail: String
    ) {
        val overlay = FrameLayout(this).apply {
            setBackgroundColor(Color.argb(190, 1, 3, 14))
            alpha = 0f
        }

        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(
                AuthUi.dp(this@BattleActivity, 20),
                AuthUi.dp(this@BattleActivity, 18),
                AuthUi.dp(this@BattleActivity, 20),
                AuthUi.dp(this@BattleActivity, 18)
            )
            background = GradientDrawable().apply {
                setColor(Color.argb(238, 6, 8, 28))
                cornerRadius = AuthUi.dp(this@BattleActivity, 18).toFloat()
                setStroke(
                    AuthUi.dp(this@BattleActivity, 1),
                    if (victory) Color.rgb(215, 176, 82)
                    else Color.rgb(176, 69, 105)
                )
            }
        }

        ButtonAssetStore.load(this, battlePack, titleAsset)?.let {
            panel.addView(
                ImageView(this).apply {
                    setImageBitmap(it)
                    scaleType = ImageView.ScaleType.FIT_CENTER
                },
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    AuthUi.dp(this, 92)
                )
            )
        }

        panel.addView(
            TextView(this).apply {
                text = headline
                textSize = 18f
                setTextColor(Color.WHITE)
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
            }
        )
        panel.addView(
            TextView(this).apply {
                text = detail
                textSize = 12f
                setTextColor(Color.rgb(201, 190, 228))
                gravity = Gravity.CENTER
                setPadding(
                    0,
                    AuthUi.dp(this@BattleActivity, 8),
                    0,
                    AuthUi.dp(this@BattleActivity, 12)
                )
            }
        )

        if (victory && settings.battleEffects) {
            ButtonAssetStore.load(this, battlePack, "loot_burst.png")?.let {
                panel.addView(
                    ImageView(this).apply {
                        setImageBitmap(it)
                        scaleType = ImageView.ScaleType.FIT_CENTER
                    },
                    LinearLayout.LayoutParams(
                        AuthUi.dp(this, 132),
                        AuthUi.dp(this, 86)
                    )
                )
            }
        }

        panel.addView(
            resultButton(if (victory) "NEXT BATTLE" else "RETRY") {
                finish()
                startActivity(
                    Intent(this@BattleActivity, BattleActivity::class.java)
                )
            },
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                AuthUi.dp(this, 50)
            ).apply {
                topMargin = AuthUi.dp(this@BattleActivity, 8)
            }
        )

        panel.addView(
            resultButton("RETURN TO HUB") { finish() },
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                AuthUi.dp(this, 46)
            ).apply {
                topMargin = AuthUi.dp(this@BattleActivity, 8)
            }
        )

        overlay.addView(
            panel,
            FrameLayout.LayoutParams(
                (resources.displayMetrics.widthPixels * 0.84f).toInt(),
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
            )
        )

        root.addView(
            overlay,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        overlay.animate()
            .alpha(1f)
            .setDuration(if (settings.reducedMotion) 120L else 300L)
            .start()
    }

    private fun resultButton(
        textValue: String,
        onClick: () -> Unit
    ): TextView {
        return TextView(this).apply {
            text = textValue
            gravity = Gravity.CENTER
            textSize = 13f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.05f
            background = GradientDrawable().apply {
                setColor(Color.rgb(33, 23, 69))
                cornerRadius = AuthUi.dp(this@BattleActivity, 12).toFloat()
                setStroke(
                    AuthUi.dp(this@BattleActivity, 1),
                    Color.rgb(151, 109, 235)
                )
            }
            isClickable = true
            isFocusable = true
            setOnClickListener {
                AppHaptics.tap(this@BattleActivity)
                onClick()
            }
            installPressFeedback()
        }
    }

    private fun showNoEnergyOverlay() {
        battleOver = true
        combatStarted = false

        val overlay = FrameLayout(this).apply {
            setBackgroundColor(Color.argb(192, 1, 3, 14))
        }

        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(
                AuthUi.dp(this@BattleActivity, 20),
                AuthUi.dp(this@BattleActivity, 22),
                AuthUi.dp(this@BattleActivity, 20),
                AuthUi.dp(this@BattleActivity, 22)
            )
            background = GradientDrawable().apply {
                setColor(Color.argb(235, 6, 8, 28))
                cornerRadius = AuthUi.dp(this@BattleActivity, 18).toFloat()
                setStroke(
                    AuthUi.dp(this@BattleActivity, 1),
                    Color.rgb(147, 97, 225)
                )
            }
        }

        panel.addView(
            TextView(this).apply {
                text = "NOT ENOUGH ENERGY"
                textSize = 19f
                setTextColor(Color.WHITE)
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
            }
        )
        panel.addView(
            TextView(this).apply {
                text =
                    "Battles cost " + ENERGY_COST +
                        " Energy. You currently have " +
                        progress.energy + "."
                textSize = 12f
                setTextColor(Color.rgb(199, 188, 226))
                gravity = Gravity.CENTER
                setPadding(
                    0,
                    AuthUi.dp(this@BattleActivity, 9),
                    0,
                    AuthUi.dp(this@BattleActivity, 14)
                )
            }
        )
        panel.addView(
            resultButton("RETURN TO HUB") { finish() },
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                AuthUi.dp(this, 48)
            )
        )

        overlay.addView(
            panel,
            FrameLayout.LayoutParams(
                (resources.displayMetrics.widthPixels * 0.82f).toInt(),
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
            )
        )
        root.addView(
            overlay,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )
    }

    companion object {
        private const val ENERGY_COST = 5
    }
}
