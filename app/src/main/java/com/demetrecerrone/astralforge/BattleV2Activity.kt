package com.demetrecerrone.astralforge

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import kotlin.math.max

class BattleV2Activity : Activity() {

    private val handler = Handler(Looper.getMainLooper())

    private lateinit var root: FrameLayout
    private lateinit var renderer: BattleV2RendererHost
    private lateinit var controller: BattleV2Controller
    private lateinit var progress: PlayerProgress
    private lateinit var settings: GameSettings

    private lateinit var playerHpBar: ProgressBar
    private lateinit var enemyHpBar: ProgressBar
    private lateinit var playerHpText: TextView
    private lateinit var enemyHpText: TextView
    private lateinit var statusText: TextView
    private lateinit var attackButton: TextView
    private lateinit var autoButton: TextView

    private var busy = false
    private var autoBattle = false
    private var actorsReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AuthUi.setupWindow(this)

        progress = ProgressionStore.load(this)
        settings = GameSettingsStore.load(this)

        controller =
            BattleV2Controller(
                playerMaxHp = progress.maxHp,
                playerAttack = progress.attack,
                playerDefense = progress.defense,
                playerCritChance = progress.critChance,
                depth = progress.depth
            )

        buildScene()
        refreshHud()
        loadPreviewActors()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        if (::renderer.isInitialized) {
            renderer.release()
        }
        super.onDestroy()
    }

    private fun buildScene() {
        root = FrameLayout(this)

        renderer =
            BattleV2RendererHost(this).apply {
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

        addEnemyHud()
        addPlayerHudAndControls()
        addPreviewBadge()
        addBackButton()

        renderer.onModeChanged = { mode ->
            if (::statusText.isInitialized) {
                statusText.text = mode
            }
        }

        setContentView(root)
    }

    private fun addEnemyHud() {
        val panel =
            LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(
                    AuthUi.dp(this@BattleV2Activity, 14),
                    AuthUi.dp(this@BattleV2Activity, 8),
                    AuthUi.dp(this@BattleV2Activity, 14),
                    AuthUi.dp(this@BattleV2Activity, 8)
                )
                background = panelBackground(
                    Color.rgb(175, 68, 101)
                )
            }

        panel.addView(
            TextView(this).apply {
                text = "BLUE SLIME  •  V2 TEST TARGET"
                textSize = 13f
                setTextColor(Color.WHITE)
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
            }
        )

        enemyHpBar = createHpBar(Color.rgb(229, 68, 98))
        panel.addView(
            enemyHpBar,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                AuthUi.dp(this, 12)
            ).apply {
                topMargin = AuthUi.dp(this@BattleV2Activity, 5)
            }
        )

        enemyHpText =
            TextView(this).apply {
                textSize = 10.5f
                setTextColor(Color.rgb(231, 217, 240))
                gravity = Gravity.END
            }
        panel.addView(enemyHpText)

        root.addView(
            panel,
            FrameLayout.LayoutParams(
                (resources.displayMetrics.widthPixels * 0.58f).toInt(),
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.TOP or Gravity.CENTER_HORIZONTAL
            ).apply {
                topMargin = AuthUi.dp(this@BattleV2Activity, 16)
            }
        )
    }

    private fun addPlayerHudAndControls() {
        val bottom =
            LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(
                    AuthUi.dp(this@BattleV2Activity, 14),
                    AuthUi.dp(this@BattleV2Activity, 8),
                    AuthUi.dp(this@BattleV2Activity, 14),
                    AuthUi.dp(this@BattleV2Activity, 10)
                )
                background = panelBackground(
                    Color.rgb(93, 76, 183)
                )
            }

        val topRow =
            LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

        topRow.addView(
            TextView(this).apply {
                text = "AUREN VALE  •  KNIGHT"
                textSize = 12.5f
                setTextColor(Color.WHITE)
                typeface = Typeface.DEFAULT_BOLD
            },
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        statusText =
            TextView(this).apply {
                text = "Loading preview actors..."
                textSize = 10.5f
                setTextColor(Color.rgb(185, 220, 255))
                gravity = Gravity.END
            }
        topRow.addView(statusText)

        bottom.addView(topRow)

        playerHpBar = createHpBar(Color.rgb(69, 189, 255))
        bottom.addView(
            playerHpBar,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                AuthUi.dp(this, 12)
            ).apply {
                topMargin = AuthUi.dp(this@BattleV2Activity, 4)
            }
        )

        playerHpText =
            TextView(this).apply {
                textSize = 10.5f
                setTextColor(Color.rgb(225, 217, 242))
                gravity = Gravity.END
            }
        bottom.addView(playerHpText)

        val buttons =
            LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
                setPadding(
                    0,
                    AuthUi.dp(this@BattleV2Activity, 7),
                    0,
                    0
                )
            }

        autoButton =
            makeAction("AUTO OFF", false) {
                autoBattle = !autoBattle
                autoButton.text =
                    if (autoBattle) "AUTO ON" else "AUTO OFF"

                if (
                    autoBattle &&
                    !busy &&
                    actorsReady &&
                    !controller.snapshot().finished
                ) {
                    handler.postDelayed(
                        { performPlayerAttack() },
                        250L
                    )
                }
            }
        buttons.addView(
            autoButton,
            LinearLayout.LayoutParams(
                0,
                AuthUi.dp(this, 48),
                0.85f
            ).apply {
                marginEnd = AuthUi.dp(this@BattleV2Activity, 5)
            }
        )

        val skillButton =
            makeAction("SKILL", false) {
                statusText.text =
                    "Skill timing slot is ready for 3D animation events."
            }
        buttons.addView(
            skillButton,
            LinearLayout.LayoutParams(
                0,
                AuthUi.dp(this, 48),
                0.85f
            ).apply {
                marginEnd = AuthUi.dp(this@BattleV2Activity, 5)
            }
        )

        attackButton =
            makeAction("ATTACK", true) {
                performPlayerAttack()
            }
        buttons.addView(
            attackButton,
            LinearLayout.LayoutParams(
                0,
                AuthUi.dp(this, 50),
                1.1f
            )
        )

        bottom.addView(buttons)

        root.addView(
            bottom,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM
            )
        )
    }

    private fun addPreviewBadge() {
        root.addView(
            TextView(this).apply {
                text = "3D V2 PREVIEW • NORMAL BATTLE REMAINS SAFE"
                textSize = 9.5f
                setTextColor(Color.rgb(218, 190, 255))
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                background = GradientDrawable().apply {
                    setColor(Color.argb(205, 8, 7, 26))
                    cornerRadius =
                        AuthUi.dp(this@BattleV2Activity, 10)
                            .toFloat()
                    setStroke(
                        AuthUi.dp(this@BattleV2Activity, 1),
                        Color.rgb(116, 84, 203)
                    )
                }
                setPadding(
                    AuthUi.dp(this@BattleV2Activity, 10),
                    AuthUi.dp(this@BattleV2Activity, 5),
                    AuthUi.dp(this@BattleV2Activity, 10),
                    AuthUi.dp(this@BattleV2Activity, 5)
                )
            },
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.TOP or Gravity.START
            ).apply {
                leftMargin = AuthUi.dp(this@BattleV2Activity, 12)
                topMargin = AuthUi.dp(this@BattleV2Activity, 14)
            }
        )
    }

    private fun addBackButton() {
        root.addView(
            makeAction("BACK", false) {
                finish()
            },
            FrameLayout.LayoutParams(
                AuthUi.dp(this, 82),
                AuthUi.dp(this, 38),
                Gravity.TOP or Gravity.END
            ).apply {
                topMargin = AuthUi.dp(this@BattleV2Activity, 14)
                rightMargin = AuthUi.dp(this@BattleV2Activity, 12)
            }
        )
    }

    private fun loadPreviewActors() {
        Thread {
            val playerSprites =
                EntitySpriteStore.loadCharacter(
                    this,
                    "knight"
                )
            val enemySprites =
                EntitySpriteStore.loadMonster(
                    this,
                    "blue_slime"
                )

            runOnUiThread {
                renderer.setActors(
                    playerSprites,
                    enemySprites
                )
                actorsReady = true
                statusText.text = renderer.modeLabel
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
        statusText.text = "Auren attacks"

        renderer.playPlayer(BattleV2Animation.ATTACK)
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

                AppHaptics.tap(this)

                if (outcome.defeated) {
                    renderer.playEnemy(
                        BattleV2Animation.DEATH
                    )
                    statusText.text = "Target defeated"
                    handler.postDelayed(
                        {
                            if (!isFinishing && !isDestroyed) {
                                renderer.playPlayer(
                                    BattleV2Animation.VICTORY
                                )
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
        statusText.text = "Blue Slime attacks"
        renderer.playEnemy(BattleV2Animation.ATTACK)

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

                if (outcome.defeated) {
                    renderer.playPlayer(
                        BattleV2Animation.DEATH
                    )
                    statusText.text = "Auren defeated"
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

    private fun refreshHud() {
        val state = controller.snapshot()

        playerHpBar.max = state.playerMaxHp
        playerHpBar.progress = state.playerHp
        playerHpText.text =
            state.playerHp.toString() +
                " / " +
                state.playerMaxHp

        enemyHpBar.max = state.enemyMaxHp
        enemyHpBar.progress = state.enemyHp
        enemyHpText.text =
            state.enemyHp.toString() +
                " / " +
                state.enemyMaxHp
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
                refreshHud()
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
