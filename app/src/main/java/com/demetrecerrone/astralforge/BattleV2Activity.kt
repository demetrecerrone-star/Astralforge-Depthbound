package com.demetrecerrone.astralforge

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import kotlin.math.max

class BattleV2Activity : Activity() {

    private val handler = Handler(Looper.getMainLooper())

    private lateinit var root: FrameLayout
    private lateinit var renderer: BattleV2RendererHost
    private lateinit var effects: BattleV2EffectsView
    private lateinit var productionActors: BattleV2ProductionActorView
    private lateinit var controller: BattleV2Controller
    private lateinit var progress: PlayerProgress
    private lateinit var settings: GameSettings
    private lateinit var activeHero: HeroDefinition

    private lateinit var playerHpFill: BattleV2HpFillView
    private lateinit var enemyHpFill: BattleV2HpFillView
    private lateinit var playerHpText: TextView
    private lateinit var enemyHpText: TextView
    private lateinit var playerNameText: TextView
    private lateinit var enemyNameText: TextView
    private lateinit var playerPortrait: ImageView
    private lateinit var enemyPortrait: ImageView
    private lateinit var statusText: TextView
    private lateinit var attackButton: FrameLayout
    private lateinit var autoButton: FrameLayout

    private var playerEntityId = "knight"
    private var enemyEntityId = "blue_slime"
    private var busy = false
    private var autoBattle = false
    private var actorsReady = false
    private val previewEnemies = listOf(
        "blue_slime",
        "goblin_raider",
        "skeleton_warrior",
        "dire_wolf",
        "dungeon_boss"
    )
    private var previewEnemyIndex = 0
    private var previewDepth = 1
    private var useProductionActors = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AuthUi.setupWindow(this)

        progress = ProgressionStore.load(this)
        settings = GameSettingsStore.load(this)
        activeHero = HeroRosterStore.activeHero(this)
        playerEntityId = activeHero.id
        previewEnemyIndex =
            getPreferences(MODE_PRIVATE)
                .getInt("preview_enemy_index", 0)
                .coerceIn(0, previewEnemies.lastIndex)
        enemyEntityId = previewEnemies[previewEnemyIndex]
        previewDepth = previewEnemyIndex + 1
        useProductionActors =
            playerEntityId == "knight" &&
                BattleV2ProductionActorView.hasProductionEnemy(
                    this,
                    enemyEntityId
                )

        controller =
            BattleV2Controller(
                playerMaxHp = progress.maxHp,
                playerAttack = progress.attack,
                playerDefense = progress.defense,
                playerCritChance = progress.critChance,
                depth = previewDepth
            )

        buildScene()
        refreshHud()
        loadPreviewActors()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        if (::productionActors.isInitialized) {
            productionActors.release()
        }
        if (::renderer.isInitialized) {
            renderer.release()
        }
        super.onDestroy()
    }

    private fun buildScene() {
        root = FrameLayout(this).apply {
            setBackgroundColor(Color.rgb(3, 5, 18))
        }

        root.addView(
            ImageView(this).apply {
                setImageResource(R.drawable.battle_v2_bg)
                scaleType = ImageView.ScaleType.CENTER_CROP
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
                        Color.argb(58, 2, 3, 14),
                        Color.argb(0, 2, 3, 14),
                        Color.argb(28, 2, 3, 14),
                        Color.argb(92, 2, 3, 14)
                    )
                )
            },
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        renderer =
            BattleV2RendererHost(this).apply {
                configureActors(playerEntityId, enemyEntityId)
                setPreferredFps(
                    settings.fpsPreference,
                    settings.batterySaver
                )
            }

        root.addView(
            renderer,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        if (useProductionActors) {
            val productionView =
                runCatching {
                    BattleV2ProductionActorView(
                        this,
                        enemyEntityId = enemyEntityId
                    )
                }.getOrNull()

            if (productionView != null) {
                renderer.disableForProductionActors()
                productionActors = productionView
                root.addView(
                    productionActors,
                    FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                )
            } else {
                useProductionActors = false
            }
        }

        effects = BattleV2EffectsView(this)
        root.addView(
            effects,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        addEnemyHud()
        addPlayerHudAndControls()
        addPreviewBadge()
        addEnemySelector()
        addBackButton()

        renderer.onModeChanged = { mode ->
            if (::statusText.isInitialized) {
                statusText.text = mode
            }
        }

        setContentView(root)
    }


    private fun addEnemyHud() {
        val screenW = resources.displayMetrics.widthPixels
        val screenH = resources.displayMetrics.heightPixels
        val hudW = (screenW * 0.475f).toInt()
        val hudH = (screenH * 0.195f).toInt()
        val hud = FrameLayout(this)

        hud.addView(
            ImageView(this).apply {
                setImageResource(R.drawable.battle_v2_enemy_hud)
                scaleType = ImageView.ScaleType.FIT_XY
                contentDescription = null
            },
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        enemyPortrait = ImageView(this).apply {
            scaleType = ImageView.ScaleType.FIT_CENTER
            contentDescription = "Enemy portrait"
        }
        hud.addView(
            enemyPortrait,
            rectParams(hudW, hudH, 0.050f, 0.130f, 0.195f, 0.65f)
        )

        enemyNameText = hudText(
            "Lv." + previewDepth + "  •  " +
                BattleActorFactory.enemyDisplayName(enemyEntityId),
            13f,
            Gravity.CENTER_VERTICAL
        )
        hud.addView(
            enemyNameText,
            rectParams(hudW, hudH, 0.255f, 0.18f, 0.60f, 0.24f)
        )

        enemyHpFill = BattleV2HpFillView(this).apply {
            setPalette(
                Color.rgb(139, 5, 43),
                Color.rgb(255, 56, 99)
            )
        }
        hud.addView(
            enemyHpFill,
            rectParams(hudW, hudH, 0.267f, 0.476f, 0.633f, 0.104f)
        )

        enemyHpText = hudText(
            "",
            11.5f,
            Gravity.END or Gravity.CENTER_VERTICAL
        ).apply {
            setPadding(0, 0, AuthUi.dp(this@BattleV2Activity, 8), 0)
        }
        hud.addView(
            enemyHpText,
            rectParams(hudW, hudH, 0.267f, 0.438f, 0.633f, 0.18f)
        )

        root.addView(
            hud,
            FrameLayout.LayoutParams(
                hudW,
                hudH,
                Gravity.TOP or Gravity.END
            ).apply {
                topMargin = (screenH * 0.088f).toInt()
                rightMargin = (screenW * 0.047f).toInt()
            }
        )
    }

    private fun addPlayerHudAndControls() {
        val screenW = resources.displayMetrics.widthPixels
        val screenH = resources.displayMetrics.heightPixels
        val hudW = (screenW * 0.475f).toInt()
        val hudH = (screenH * 0.205f).toInt()
        val hud = FrameLayout(this)

        hud.addView(
            ImageView(this).apply {
                setImageResource(R.drawable.battle_v2_player_hud)
                scaleType = ImageView.ScaleType.FIT_XY
                contentDescription = null
            },
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        playerPortrait = ImageView(this).apply {
            scaleType = ImageView.ScaleType.FIT_CENTER
            contentDescription = "Player portrait"
        }
        hud.addView(
            playerPortrait,
            rectParams(hudW, hudH, 0.052f, 0.135f, 0.205f, 0.67f)
        )

        playerNameText = hudText(
            "Lv." + progress.level + "  •  " + activeHero.displayName,
            13f,
            Gravity.CENTER_VERTICAL
        )
        hud.addView(
            playerNameText,
            rectParams(hudW, hudH, 0.278f, 0.17f, 0.50f, 0.23f)
        )

        playerHpFill = BattleV2HpFillView(this).apply {
            setPalette(
                Color.rgb(16, 80, 220),
                Color.rgb(58, 206, 255)
            )
        }
        hud.addView(
            playerHpFill,
            rectParams(hudW, hudH, 0.315f, 0.475f, 0.595f, 0.102f)
        )

        playerHpText = hudText(
            "",
            11.5f,
            Gravity.END or Gravity.CENTER_VERTICAL
        ).apply {
            setPadding(0, 0, AuthUi.dp(this@BattleV2Activity, 8), 0)
        }
        hud.addView(
            playerHpText,
            rectParams(hudW, hudH, 0.315f, 0.438f, 0.595f, 0.18f)
        )

        root.addView(
            hud,
            FrameLayout.LayoutParams(
                hudW,
                hudH,
                Gravity.BOTTOM or Gravity.START
            ).apply {
                leftMargin = (screenW * 0.030f).toInt()
                bottomMargin = (screenH * 0.030f).toInt()
            }
        )

        val size = (screenH * 0.138f).toInt()
        val gap = (screenW * 0.0045f).toInt()
        val buttons = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        autoButton = makeArtButton(
            R.drawable.battle_v2_auto,
            "Auto"
        ) {
            autoBattle = !autoBattle
            autoButton.alpha = if (autoBattle) 1f else 0.72f
            statusText.text =
                if (autoBattle) "Auto battle ON" else "Auto battle OFF"
            if (
                autoBattle &&
                !busy &&
                actorsReady &&
                !controller.snapshot().finished
            ) {
                handler.postDelayed(
                    { performPlayerAttack() },
                    220L
                )
            }
        }
        autoButton.alpha = 0.72f

        val skillButton = makeArtButton(
            R.drawable.battle_v2_skill,
            "Skill"
        ) {
            statusText.text =
                activeHero.skillName +
                    " is ready for the next animation pass"
        }

        attackButton = makeArtButton(
            R.drawable.battle_v2_attack,
            "Attack"
        ) {
            performPlayerAttack()
        }

        buttons.addView(
            autoButton,
            LinearLayout.LayoutParams(size, size).apply {
                marginEnd = gap
            }
        )
        buttons.addView(
            skillButton,
            LinearLayout.LayoutParams(size, size).apply {
                marginEnd = gap
            }
        )
        buttons.addView(
            attackButton,
            LinearLayout.LayoutParams(size, size)
        )

        root.addView(
            buttons,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                size,
                Gravity.BOTTOM or Gravity.END
            ).apply {
                rightMargin = (screenW * 0.045f).toInt()
                bottomMargin = (screenH * 0.060f).toInt()
            }
        )

        statusText = TextView(this).apply {
            text = "Loading battle renderer..."
            textSize = 9.5f
            setTextColor(Color.rgb(202, 215, 255))
            gravity = Gravity.END or Gravity.CENTER_VERTICAL
            setShadowLayer(4f, 0f, 0f, Color.BLACK)
        }
        root.addView(
            statusText,
            FrameLayout.LayoutParams(
                (screenW * 0.235f).toInt(),
                AuthUi.dp(this, 24),
                Gravity.TOP or Gravity.END
            ).apply {
                topMargin = AuthUi.dp(this@BattleV2Activity, 10)
                rightMargin = AuthUi.dp(this@BattleV2Activity, 82)
            }
        )
    }

    private fun addPreviewBadge() {
        val screenW = resources.displayMetrics.widthPixels
        val screenH = resources.displayMetrics.heightPixels
        root.addView(
            ImageView(this).apply {
                setImageResource(R.drawable.battle_v2_header)
                scaleType = ImageView.ScaleType.FIT_CENTER
                contentDescription = "Battle V2"
            },
            FrameLayout.LayoutParams(
                (screenW * 0.275f).toInt(),
                (screenH * 0.090f).toInt(),
                Gravity.TOP or Gravity.CENTER_HORIZONTAL
            ).apply {
                topMargin = AuthUi.dp(this@BattleV2Activity, 2)
            }
        )
    }
    private fun addEnemySelector() {
        root.addView(
            makeAction("ENEMY ▶", false) {
                if (busy) return@makeAction

                val nextIndex =
                    (previewEnemyIndex + 1) %
                        previewEnemies.size

                getPreferences(MODE_PRIVATE)
                    .edit()
                    .putInt(
                        "preview_enemy_index",
                        nextIndex
                    )
                    .apply()

                recreate()
            },
            FrameLayout.LayoutParams(
                AuthUi.dp(this, 82),
                AuthUi.dp(this, 30),
                Gravity.TOP or Gravity.START
            ).apply {
                topMargin =
                    AuthUi.dp(
                        this@BattleV2Activity,
                        8
                    )
                leftMargin =
                    AuthUi.dp(
                        this@BattleV2Activity,
                        8
                    )
            }
        )
    }

    private fun addBackButton() {
        root.addView(
            makeAction("BACK", false) {
                finish()
            },
            FrameLayout.LayoutParams(
                AuthUi.dp(this, 68),
                AuthUi.dp(this, 32),
                Gravity.TOP or Gravity.END
            ).apply {
                topMargin = AuthUi.dp(this@BattleV2Activity, 8)
                rightMargin = AuthUi.dp(this@BattleV2Activity, 8)
            }
        )
    }


    private fun loadPreviewActors() {
        Thread {
            val playerSprites =
                EntitySpriteStore.loadCharacter(
                    this,
                    playerEntityId
                )
            val enemySprites =
                EntitySpriteStore.loadMonster(
                    this,
                    enemyEntityId
                )

            runOnUiThread {
                renderer.setActors(
                    playerSprites,
                    enemySprites
                )
                playerSprites.idle?.let {
                    playerPortrait.setImageBitmap(cropVisiblePortrait(it))
                }
                val portrait =
                    if (
                        useProductionActors &&
                        ::productionActors.isInitialized
                    ) {
                        productionActors.enemyPortraitBitmap()
                    } else {
                        enemySprites.idle
                    }
                portrait?.let {
                    enemyPortrait.setImageBitmap(cropVisiblePortrait(it))
                }
                playerNameText.text =
                    "Lv." + progress.level +
                        "  •  " + activeHero.displayName
                enemyNameText.text =
                    "Lv." + previewDepth +
                        "  •  " + enemySprites.displayName
                actorsReady = true
                statusText.text = "Ready"
            }
        }.start()
    }
    private fun performPlayerAttack() {
        if (
            busy ||
            !actorsReady ||
            controller.snapshot().finished
        ) {
            return
        }

        busy = true
        attackButton.alpha = 0.6f
        statusText.text = activeHero.displayName + " attacks"

        renderer.playPlayer(BattleV2Animation.ATTACK)
        if (::productionActors.isInitialized) {
            productionActors.playPlayer(
                BattleV2Animation.ATTACK
            )
        }
        if (settings.battleEffects) {
            effects.playPlayerSlash()
        }
        val attackSpec =
            BattleV2AnimationTimeline.spec(
                BattleV2Animation.ATTACK
            )

        handler.postDelayed(
            {
                if (isFinishing || isDestroyed) return@postDelayed

                val outcome = controller.playerAttack()
                refreshHud()

                if (settings.damageNumbers) {
                    showDamageNumber(
                        outcome.damage,
                        outcome.critical,
                        enemySide = true
                    )
                }

                if (settings.battleEffects) {
                    effects.playEnemyHit(outcome.critical)
                    cameraKick(enemySide = true, critical = outcome.critical)
                }

                AppHaptics.tap(this)

                if (outcome.defeated) {
                    renderer.playEnemy(
                        BattleV2Animation.DEATH
                    )
                    if (::productionActors.isInitialized) {
                        productionActors.playEnemy(
                            BattleV2Animation.DEATH
                        )
                    }
                    if (settings.battleEffects) {
                        effects.playEnemyDeath()
                    }
                    statusText.text = "Target defeated"
                    handler.postDelayed(
                        {
                            if (!isFinishing && !isDestroyed) {
                                renderer.playPlayer(
                                    BattleV2Animation.VICTORY
                                )
                                if (::productionActors.isInitialized) {
                                    productionActors.playPlayer(
                                        BattleV2Animation.VICTORY
                                    )
                                }
                                if (settings.battleEffects) {
                                    effects.playVictory()
                                }
                                showResult(true)
                            }
                        },
                        BattleV2AnimationTimeline
                            .spec(BattleV2Animation.DEATH)
                            .durationMs
                    )
                } else {
                    renderer.playEnemy(
                        BattleV2Animation.HIT
                    )
                    if (::productionActors.isInitialized) {
                        productionActors.playEnemy(
                            BattleV2Animation.HIT
                        )
                    }
                }
            },
            attackSpec.impactMs ?: 0L
        )

        handler.postDelayed(
            {
                if (
                    !isFinishing &&
                    !isDestroyed &&
                    !controller.snapshot().finished
                ) {
                    performEnemyAttack()
                }
            },
            attackSpec.durationMs + 240L
        )
    }

    private fun performEnemyAttack() {
        statusText.text = BattleActorFactory.enemyDisplayName(enemyEntityId) + " attacks"
        renderer.playEnemy(BattleV2Animation.ATTACK)
        if (::productionActors.isInitialized) {
            productionActors.playEnemy(
                BattleV2Animation.ATTACK
            )
        }
        if (
            settings.battleEffects &&
            enemyEntityId == "blue_slime"
        ) {
            effects.playSlimeLunge()
        }

        val attackSpec =
            BattleV2AnimationTimeline.spec(
                BattleV2Animation.ATTACK
            )

        handler.postDelayed(
            {
                if (isFinishing || isDestroyed) return@postDelayed

                val outcome = controller.enemyAttack()
                refreshHud()

                if (settings.damageNumbers) {
                    showDamageNumber(
                        outcome.damage,
                        outcome.critical,
                        enemySide = false
                    )
                }

                if (settings.battleEffects) {
                    effects.playPlayerHit(outcome.critical)
                    cameraKick(enemySide = false, critical = outcome.critical)
                }

                if (outcome.defeated) {
                    renderer.playPlayer(
                        BattleV2Animation.DEATH
                    )
                    if (::productionActors.isInitialized) {
                        productionActors.playPlayer(
                            BattleV2Animation.DEATH
                        )
                    }
                    if (settings.battleEffects) {
                        effects.playPlayerDeath()
                    }
                    statusText.text = activeHero.displayName + " defeated"
                    handler.postDelayed(
                        {
                            if (!isFinishing && !isDestroyed) {
                                showResult(false)
                            }
                        },
                        BattleV2AnimationTimeline
                            .spec(BattleV2Animation.DEATH)
                            .durationMs
                    )
                } else {
                    renderer.playPlayer(
                        BattleV2Animation.HIT
                    )
                    if (::productionActors.isInitialized) {
                        productionActors.playPlayer(
                            BattleV2Animation.HIT
                        )
                    }
                }
            },
            attackSpec.impactMs ?: 0L
        )

        handler.postDelayed(
            {
                if (
                    !isFinishing &&
                    !isDestroyed &&
                    !controller.snapshot().finished
                ) {
                    busy = false
                    attackButton.alpha = 1f
                    statusText.text = "Ready"

                    if (autoBattle) {
                        handler.postDelayed(
                            { performPlayerAttack() },
                            320L
                        )
                    }
                }
            },
            attackSpec.durationMs + 260L
        )
    }


    private fun refreshHud(animate: Boolean = true) {
        val state = controller.snapshot()

        playerHpFill.setHealth(
            state.playerHp,
            state.playerMaxHp,
            animate
        )
        enemyHpFill.setHealth(
            state.enemyHp,
            state.enemyMaxHp,
            animate
        )
        playerHpText.text =
            state.playerHp.toString() +
                " / " +
                state.playerMaxHp
        enemyHpText.text =
            state.enemyHp.toString() +
                " / " +
                state.enemyMaxHp
    }
    private fun cameraKick(
        enemySide: Boolean,
        critical: Boolean
    ) {
        if (settings.reducedMotion) return

        val direction = if (enemySide) 1f else -1f
        val amount =
            AuthUi.dp(
                this,
                if (critical) 16 else 9
            ).toFloat() * direction

        renderer.animate()
            .translationX(amount)
            .translationY(
                AuthUi.dp(
                    this,
                    if (critical) -5 else -2
                ).toFloat()
            )
            .setDuration(45L)
            .withEndAction {
                renderer.animate()
                    .translationX(-amount * 0.42f)
                    .translationY(
                        AuthUi.dp(this, 2).toFloat()
                    )
                    .setDuration(55L)
                    .withEndAction {
                        renderer.animate()
                            .translationX(0f)
                            .translationY(0f)
                            .setDuration(70L)
                            .start()
                    }
                    .start()
            }
            .start()
    }

    private fun showDamageNumber(
        amount: Int,
        critical: Boolean,
        enemySide: Boolean
    ) {
        val value =
            TextView(this).apply {
                text =
                    if (critical) {
                        "CRIT  " + amount
                    } else {
                        amount.toString()
                    }
                textSize = if (critical) 25f else 21f
                setTextColor(
                    if (critical) Color.rgb(255, 205, 79)
                    else Color.WHITE
                )
                typeface = Typeface.DEFAULT_BOLD
                setShadowLayer(
                    7f,
                    0f,
                    0f,
                    if (critical) Color.rgb(171, 80, 16)
                    else Color.rgb(74, 49, 160)
                )
                gravity = Gravity.CENTER
                alpha = 0f
            }

        val w = resources.displayMetrics.widthPixels
        val h = resources.displayMetrics.heightPixels

        root.addView(
            value,
            FrameLayout.LayoutParams(
                AuthUi.dp(this, 150),
                AuthUi.dp(this, 60)
            ).apply {
                leftMargin =
                    if (enemySide) {
                        (w * 0.63f).toInt()
                    } else {
                        (w * 0.22f).toInt()
                    }
                topMargin = (h * 0.36f).toInt()
            }
        )

        value.translationY = AuthUi.dp(this, 18).toFloat()
        value.animate()
            .alpha(1f)
            .translationY(-AuthUi.dp(this, 22).toFloat())
            .setDuration(260L)
            .withEndAction {
                value.animate()
                    .alpha(0f)
                    .translationY(-AuthUi.dp(this, 52).toFloat())
                    .setDuration(380L)
                    .withEndAction {
                        root.removeView(value)
                    }
                    .start()
            }
            .start()
    }

    private fun showResult(victory: Boolean) {
        busy = true
        attackButton.alpha = 0.45f

        val overlay =
            FrameLayout(this).apply {
                setBackgroundColor(Color.argb(178, 1, 2, 11))
            }

        val panel =
            LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(
                    AuthUi.dp(this@BattleV2Activity, 24),
                    AuthUi.dp(this@BattleV2Activity, 20),
                    AuthUi.dp(this@BattleV2Activity, 24),
                    AuthUi.dp(this@BattleV2Activity, 20)
                )
                background = panelBackground(
                    if (victory) {
                        Color.rgb(207, 167, 79)
                    } else {
                        Color.rgb(173, 66, 94)
                    }
                )
            }

        panel.addView(
            TextView(this).apply {
                text =
                    if (victory) {
                        "V2 TEST VICTORY"
                    } else {
                        "V2 TEST DEFEAT"
                    }
                textSize = 23f
                setTextColor(Color.WHITE)
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
            }
        )

        panel.addView(
            TextView(this).apply {
                text =
                    "Combat logic, animation timing, HUD, " +
                        "and render surface stayed isolated."
                textSize = 11.5f
                setTextColor(Color.rgb(213, 203, 231))
                gravity = Gravity.CENTER
                setPadding(
                    0,
                    AuthUi.dp(this@BattleV2Activity, 8),
                    0,
                    AuthUi.dp(this@BattleV2Activity, 12)
                )
            }
        )

        panel.addView(
            makeAction("RESET PREVIEW", true) {
                root.removeView(overlay)
                controller.reset()
                renderer.resetActors()
                if (::productionActors.isInitialized) {
                    productionActors.resetActors()
                }
                effects.clearEffects()
                refreshHud(animate = false)
                busy = false
                attackButton.alpha = 1f
                statusText.text = "Ready"
                if (autoBattle) {
                    handler.postDelayed(
                        { performPlayerAttack() },
                        300L
                    )
                }
            },
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                AuthUi.dp(this, 48)
            )
        )

        panel.addView(
            makeAction("RETURN", false) {
                finish()
            },
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                AuthUi.dp(this, 44)
            ).apply {
                topMargin = AuthUi.dp(this@BattleV2Activity, 7)
            }
        )

        overlay.addView(
            panel,
            FrameLayout.LayoutParams(
                (resources.displayMetrics.widthPixels * 0.55f).toInt(),
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


    /**
     * Crops only transparent padding, leaving the silhouette untouched.
     * Sprite sheets often reserve most of their canvas around a small actor,
     * which makes the HUD portrait appear tiny even in a large slot.
     */
    private fun cropVisiblePortrait(source: Bitmap): Bitmap {
        if (source.isRecycled) return source

        val width = source.width
        val height = source.height
        if (width < 2 || height < 2 || !source.hasAlpha()) return source

        var left = width
        var top = height
        var right = -1
        var bottom = -1

        val row = IntArray(width)
        for (y in 0 until height) {
            source.getPixels(row, 0, width, 0, y, width, 1)
            for (x in 0 until width) {
                if (Color.alpha(row[x]) > 24) {
                    if (x < left) left = x
                    if (x > right) right = x
                    if (y < top) top = y
                    if (y > bottom) bottom = y
                }
            }
        }

        if (right < left || bottom < top) return source

        val padding = (max(width, height) * 0.025f).toInt()
        left = (left - padding).coerceAtLeast(0)
        top = (top - padding).coerceAtLeast(0)
        right = (right + padding).coerceAtMost(width - 1)
        bottom = (bottom + padding).coerceAtMost(height - 1)

        if (
            left == 0 && top == 0 &&
            right == width - 1 && bottom == height - 1
        ) return source

        return Bitmap.createBitmap(
            source,
            left,
            top,
            right - left + 1,
            bottom - top + 1
        )
    }

    private fun hudText(
        value: String,
        size: Float,
        gravityValue: Int
    ): TextView =
        TextView(this).apply {
            text = value
            textSize = size
            setTextColor(Color.rgb(245, 237, 218))
            typeface =
                Typeface.create(
                    Typeface.SERIF,
                    Typeface.BOLD
                )
            gravity = gravityValue
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            setShadowLayer(5f, 0f, 1f, Color.BLACK)
        }

    private fun rectParams(
        parentW: Int,
        parentH: Int,
        left: Float,
        top: Float,
        width: Float,
        height: Float
    ): FrameLayout.LayoutParams =
        FrameLayout.LayoutParams(
            (parentW * width).toInt().coerceAtLeast(1),
            (parentH * height).toInt().coerceAtLeast(1)
        ).apply {
            leftMargin = (parentW * left).toInt()
            topMargin = (parentH * top).toInt()
        }

    private fun makeArtButton(
        imageRes: Int,
        description: String,
        onClick: () -> Unit
    ): FrameLayout =
        FrameLayout(this).apply {
            isClickable = true
            isFocusable = true
            contentDescription = description

            addView(
                ImageView(this@BattleV2Activity).apply {
                    setImageResource(imageRes)
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    contentDescription = null
                },
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            )

            setOnClickListener {
                AppHaptics.tap(this@BattleV2Activity)
                onClick()
            }

            setOnTouchListener { view, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        view.animate()
                            .scaleX(0.94f)
                            .scaleY(0.94f)
                            .alpha(0.86f)
                            .setDuration(55L)
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

    private fun createHpBar(color: Int): ProgressBar {
        return ProgressBar(
            this,
            null,
            android.R.attr.progressBarStyleHorizontal
        ).apply {
            progressTintList = ColorStateList.valueOf(color)
            progressBackgroundTintList =
                ColorStateList.valueOf(Color.rgb(30, 26, 45))
        }
    }

    private fun panelBackground(stroke: Int): GradientDrawable =
        GradientDrawable().apply {
            setColor(Color.argb(224, 5, 7, 25))
            cornerRadius =
                AuthUi.dp(this@BattleV2Activity, 13)
                    .toFloat()
            setStroke(
                AuthUi.dp(this@BattleV2Activity, 1),
                stroke
            )
        }

    private fun makeAction(
        label: String,
        emphasized: Boolean,
        onClick: () -> Unit
    ): TextView {
        return TextView(this).apply {
            text = label
            textSize = if (emphasized) 13f else 11.5f
            setTextColor(
                if (emphasized) Color.rgb(255, 232, 179)
                else Color.WHITE
            )
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            letterSpacing = 0.05f
            background = GradientDrawable().apply {
                setColor(
                    if (emphasized) {
                        Color.argb(244, 17, 12, 22)
                    } else {
                        Color.argb(232, 8, 10, 31)
                    }
                )
                cornerRadius =
                    AuthUi.dp(this@BattleV2Activity, 11)
                        .toFloat()
                setStroke(
                    AuthUi.dp(this@BattleV2Activity, 1),
                    if (emphasized) {
                        Color.rgb(210, 166, 78)
                    } else {
                        Color.rgb(116, 84, 203)
                    }
                )
            }
            isClickable = true
            isFocusable = true
            setOnClickListener {
                AppHaptics.tap(this@BattleV2Activity)
                onClick()
            }
            setOnTouchListener { view, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        view.animate()
                            .scaleX(0.97f)
                            .scaleY(0.97f)
                            .alpha(0.84f)
                            .setDuration(55L)
                            .start()
                    }

                    MotionEvent.ACTION_UP,
                    MotionEvent.ACTION_CANCEL -> {
                        view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .alpha(1f)
                            .setDuration(85L)
                            .start()
                    }
                }
                false
            }
        }
    }
}
