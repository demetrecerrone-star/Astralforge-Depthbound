package com.demetrecerrone.astralforge

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView

class BattleV2ProductionActorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private data class ActorFrames(
        val idle: Bitmap,
        val attack: Bitmap,
        val hit: Bitmap,
        val finisher: Bitmap
    )

    private val playerFrames: ActorFrames
    private val enemyFrames: ActorFrames

    private val playerStage = FrameLayout(context)
    private val enemyStage = FrameLayout(context)
    private val playerImage = ImageView(context)
    private val enemyImage = ImageView(context)
    private val playerShadow = View(context)
    private val enemyShadow = View(context)

    private var playerIdleAnimator: AnimatorSet? = null
    private var enemyIdleAnimator: AnimatorSet? = null
    private var released = false

    init {
        clipChildren = false
        clipToPadding = false
        setBackgroundColor(Color.TRANSPARENT)

        playerFrames = loadFrames(
            R.drawable.battle_v2_auren_actor_atlas
        )
        enemyFrames = loadFrames(
            R.drawable.battle_v2_slime_actor_atlas
        )

        addView(
            playerShadow,
            FrameLayout.LayoutParams(
                dp(250),
                dp(24),
                Gravity.BOTTOM or Gravity.START
            ).apply {
                leftMargin = dp(120)
                bottomMargin = dp(106)
            }
        )
        playerShadow.background = shadowDrawable()

        addView(
            enemyShadow,
            FrameLayout.LayoutParams(
                dp(185),
                dp(20),
                Gravity.BOTTOM or Gravity.END
            ).apply {
                rightMargin = dp(132)
                bottomMargin = dp(134)
            }
        )
        enemyShadow.background = shadowDrawable()

        playerImage.scaleType = ImageView.ScaleType.FIT_CENTER
        playerImage.setImageBitmap(playerFrames.idle)
        playerStage.addView(
            playerImage,
            LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )
        )

        enemyImage.scaleType = ImageView.ScaleType.FIT_CENTER
        enemyImage.setImageBitmap(enemyFrames.idle)
        enemyStage.addView(
            enemyImage,
            LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )
        )

        addView(
            playerStage,
            FrameLayout.LayoutParams(
                dp(430),
                dp(390),
                Gravity.BOTTOM or Gravity.START
            ).apply {
                leftMargin = dp(54)
                bottomMargin = dp(88)
            }
        )

        addView(
            enemyStage,
            FrameLayout.LayoutParams(
                dp(330),
                dp(255),
                Gravity.BOTTOM or Gravity.END
            ).apply {
                rightMargin = dp(74)
                bottomMargin = dp(126)
            }
        )

        resetActors()
    }

    fun playPlayer(animation: BattleV2Animation) {
        stopPlayerIdle()
        playerStage.animate().cancel()

        when (animation) {
            BattleV2Animation.IDLE -> {
                resetPlayerPose()
                startPlayerIdle()
            }

            BattleV2Animation.ATTACK -> {
                playerImage.setImageBitmap(playerFrames.attack)
                playerStage.alpha = 1f
                playerStage.scaleX = 1.04f
                playerStage.scaleY = 1.04f
                playerStage.animate()
                    .translationX(dp(68).toFloat())
                    .translationY(-dp(5).toFloat())
                    .setDuration(270L)
                    .withEndAction {
                        playerStage.animate()
                            .translationX(0f)
                            .translationY(0f)
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(300L)
                            .withEndAction {
                                if (!released) {
                                    resetPlayerPose()
                                    startPlayerIdle()
                                }
                            }
                            .start()
                    }
                    .start()
            }

            BattleV2Animation.HIT -> {
                playerImage.setImageBitmap(playerFrames.hit)
                playerStage.animate()
                    .translationX(-dp(24).toFloat())
                    .rotation(-2.2f)
                    .alpha(0.72f)
                    .setDuration(125L)
                    .withEndAction {
                        playerStage.animate()
                            .translationX(0f)
                            .rotation(0f)
                            .alpha(1f)
                            .setDuration(200L)
                            .withEndAction {
                                if (!released) {
                                    resetPlayerPose()
                                    startPlayerIdle()
                                }
                            }
                            .start()
                    }
                    .start()
            }

            BattleV2Animation.DEATH -> {
                playerImage.setImageBitmap(playerFrames.hit)
                playerStage.animate()
                    .translationY(dp(58).toFloat())
                    .rotation(-9f)
                    .alpha(0.24f)
                    .scaleX(0.94f)
                    .scaleY(0.94f)
                    .setDuration(
                        BattleV2AnimationTimeline
                            .spec(BattleV2Animation.DEATH)
                            .durationMs
                    )
                    .start()
            }

            BattleV2Animation.VICTORY -> {
                playerImage.setImageBitmap(playerFrames.finisher)
                playerStage.alpha = 1f
                playerStage.animate()
                    .translationY(-dp(7).toFloat())
                    .scaleX(1.045f)
                    .scaleY(1.045f)
                    .setDuration(380L)
                    .start()
            }
        }
    }

    fun playEnemy(animation: BattleV2Animation) {
        stopEnemyIdle()
        enemyStage.animate().cancel()

        when (animation) {
            BattleV2Animation.IDLE -> {
                resetEnemyPose()
                startEnemyIdle()
            }

            BattleV2Animation.ATTACK -> {
                enemyImage.setImageBitmap(enemyFrames.attack)
                enemyStage.alpha = 1f
                enemyStage.scaleX = 1.08f
                enemyStage.scaleY = 0.94f
                enemyStage.animate()
                    .translationX(-dp(132).toFloat())
                    .translationY(dp(2).toFloat())
                    .setDuration(290L)
                    .withEndAction {
                        enemyStage.animate()
                            .translationX(0f)
                            .translationY(0f)
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(270L)
                            .withEndAction {
                                if (!released) {
                                    resetEnemyPose()
                                    startEnemyIdle()
                                }
                            }
                            .start()
                    }
                    .start()
            }

            BattleV2Animation.HIT -> {
                enemyImage.setImageBitmap(enemyFrames.hit)
                enemyStage.animate()
                    .translationX(dp(20).toFloat())
                    .scaleX(1.13f)
                    .scaleY(0.84f)
                    .alpha(0.74f)
                    .setDuration(115L)
                    .withEndAction {
                        enemyStage.animate()
                            .translationX(0f)
                            .scaleX(1f)
                            .scaleY(1f)
                            .alpha(1f)
                            .setDuration(210L)
                            .withEndAction {
                                if (!released) {
                                    resetEnemyPose()
                                    startEnemyIdle()
                                }
                            }
                            .start()
                    }
                    .start()
            }

            BattleV2Animation.DEATH -> {
                enemyImage.setImageBitmap(enemyFrames.finisher)
                enemyStage.animate()
                    .translationY(dp(30).toFloat())
                    .scaleX(1.16f)
                    .scaleY(0.72f)
                    .alpha(0.16f)
                    .setDuration(
                        BattleV2AnimationTimeline
                            .spec(BattleV2Animation.DEATH)
                            .durationMs
                    )
                    .start()
            }

            BattleV2Animation.VICTORY -> {
                resetEnemyPose()
            }
        }
    }

    fun resetActors() {
        released = false
        playerStage.animate().cancel()
        enemyStage.animate().cancel()
        resetPlayerPose()
        resetEnemyPose()
        startPlayerIdle()
        startEnemyIdle()
    }

    fun release() {
        released = true
        stopPlayerIdle()
        stopEnemyIdle()
        playerStage.animate().cancel()
        enemyStage.animate().cancel()

        listOf(
            playerFrames.idle,
            playerFrames.attack,
            playerFrames.hit,
            playerFrames.finisher,
            enemyFrames.idle,
            enemyFrames.attack,
            enemyFrames.hit,
            enemyFrames.finisher
        ).distinct().forEach {
            if (!it.isRecycled) {
                it.recycle()
            }
        }
    }

    private fun resetPlayerPose() {
        playerImage.setImageBitmap(playerFrames.idle)
        playerStage.translationX = 0f
        playerStage.translationY = 0f
        playerStage.rotation = 0f
        playerStage.scaleX = 1f
        playerStage.scaleY = 1f
        playerStage.alpha = 1f
    }

    private fun resetEnemyPose() {
        enemyImage.setImageBitmap(enemyFrames.idle)
        enemyStage.translationX = 0f
        enemyStage.translationY = 0f
        enemyStage.rotation = 0f
        enemyStage.scaleX = 1f
        enemyStage.scaleY = 1f
        enemyStage.alpha = 1f
    }

    private fun startPlayerIdle() {
        if (released) return
        stopPlayerIdle()

        val bob = ObjectAnimator.ofFloat(
            playerStage,
            View.TRANSLATION_Y,
            0f,
            -dp(4).toFloat(),
            0f
        ).apply {
            duration = 1900L
            repeatCount = ObjectAnimator.INFINITE
        }

        val breatheX = ObjectAnimator.ofFloat(
            playerStage,
            View.SCALE_X,
            1f,
            1.012f,
            1f
        ).apply {
            duration = 1900L
            repeatCount = ObjectAnimator.INFINITE
        }

        val breatheY = ObjectAnimator.ofFloat(
            playerStage,
            View.SCALE_Y,
            1f,
            1.012f,
            1f
        ).apply {
            duration = 1900L
            repeatCount = ObjectAnimator.INFINITE
        }

        playerIdleAnimator =
            AnimatorSet().apply {
                playTogether(bob, breatheX, breatheY)
                start()
            }
    }

    private fun startEnemyIdle() {
        if (released) return
        stopEnemyIdle()

        val bob = ObjectAnimator.ofFloat(
            enemyStage,
            View.TRANSLATION_Y,
            0f,
            -dp(7).toFloat(),
            0f
        ).apply {
            duration = 1050L
            repeatCount = ObjectAnimator.INFINITE
        }

        val squashX = ObjectAnimator.ofFloat(
            enemyStage,
            View.SCALE_X,
            1f,
            1.035f,
            1f
        ).apply {
            duration = 1050L
            repeatCount = ObjectAnimator.INFINITE
        }

        val squashY = ObjectAnimator.ofFloat(
            enemyStage,
            View.SCALE_Y,
            1f,
            0.965f,
            1f
        ).apply {
            duration = 1050L
            repeatCount = ObjectAnimator.INFINITE
        }

        enemyIdleAnimator =
            AnimatorSet().apply {
                playTogether(bob, squashX, squashY)
                start()
            }
    }

    private fun stopPlayerIdle() {
        playerIdleAnimator?.cancel()
        playerIdleAnimator = null
    }

    private fun stopEnemyIdle() {
        enemyIdleAnimator?.cancel()
        enemyIdleAnimator = null
    }

    private fun loadFrames(resId: Int): ActorFrames {
        val atlas =
            requireNotNull(
                BitmapFactory.decodeResource(resources, resId)
            ) {
                "Battle V2 actor atlas failed to decode."
            }

        val cellW = atlas.width / 2
        val cellH = atlas.height / 2

        val frames =
            ActorFrames(
                idle = Bitmap.createBitmap(
                    atlas,
                    0,
                    0,
                    cellW,
                    cellH
                ),
                attack = Bitmap.createBitmap(
                    atlas,
                    cellW,
                    0,
                    cellW,
                    cellH
                ),
                hit = Bitmap.createBitmap(
                    atlas,
                    0,
                    cellH,
                    cellW,
                    cellH
                ),
                finisher = Bitmap.createBitmap(
                    atlas,
                    cellW,
                    cellH,
                    cellW,
                    cellH
                )
            )

        atlas.recycle()
        return frames
    }

    private fun shadowDrawable(): GradientDrawable =
        GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.argb(94, 5, 4, 18))
        }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density)
            .toInt()
            .coerceAtLeast(1)
}
