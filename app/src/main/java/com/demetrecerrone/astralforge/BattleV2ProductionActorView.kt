package com.demetrecerrone.astralforge

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView

class BattleV2ProductionActorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    private val enemyEntityId: String = "blue_slime"
) : FrameLayout(context, attrs) {

    private data class ActorFrames(
        val idle: Bitmap,
        val attack: Bitmap,
        val hit: Bitmap,
        val finisher: Bitmap
    )

    private val playerFrames: ActorFrames
    private val enemyFrames: ActorFrames

    private val playerStage =
        FrameLayout(context).apply {
            clipChildren = false
            clipToPadding = false
        }
    private val enemyStage =
        FrameLayout(context).apply {
            clipChildren = false
            clipToPadding = false
        }
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
            enemyAtlasResId(context, enemyEntityId)
        )

        addView(
            playerShadow,
            playerShadowParams(
                resources.displayMetrics.widthPixels,
                resources.displayMetrics.heightPixels
            )
        )
        playerShadow.background = shadowDrawable()

        addView(
            enemyShadow,
            enemyShadowParams(
                resources.displayMetrics.widthPixels,
                resources.displayMetrics.heightPixels
            )
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
            playerStageParams(
                resources.displayMetrics.widthPixels,
                resources.displayMetrics.heightPixels
            )
        )

        addView(
            enemyStage,
            enemyStageParams(
                resources.displayMetrics.widthPixels,
                resources.displayMetrics.heightPixels
            )
        )

        resetActors()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        post {
            applyResponsiveLayout()
            resetActors()
        }
    }

    override fun onLayout(
        changed: Boolean,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            applyResponsiveLayout()
        }
    }

    private fun applyResponsiveLayout() {
        val w =
            width.takeIf { it > 1 }
                ?: resources.displayMetrics.widthPixels
        val h =
            height.takeIf { it > 1 }
                ?: resources.displayMetrics.heightPixels

        playerStage.layoutParams = playerStageParams(w, h)
        enemyStage.layoutParams = enemyStageParams(w, h)
        playerShadow.layoutParams = playerShadowParams(w, h)
        enemyShadow.layoutParams = enemyShadowParams(w, h)

        playerStage.visibility = View.VISIBLE
        enemyStage.visibility = View.VISIBLE
        playerImage.visibility = View.VISIBLE
        enemyImage.visibility = View.VISIBLE
        playerStage.bringToFront()
        enemyStage.bringToFront()
        invalidate()
        requestLayout()
    }

    private fun playerStageParams(
        w: Int,
        h: Int
    ): FrameLayout.LayoutParams =
        FrameLayout.LayoutParams(
            (w * 0.245f).toInt().coerceAtLeast(dp(150)),
            (h * 0.455f).toInt().coerceAtLeast(dp(190)),
            Gravity.BOTTOM or Gravity.START
        ).apply {
            leftMargin = (w * 0.135f).toInt()
            bottomMargin = (h * 0.135f).toInt()
        }

    private fun enemyStageParams(
        w: Int,
        h: Int
    ): FrameLayout.LayoutParams =
        FrameLayout.LayoutParams(
            (w * 0.175f).toInt().coerceAtLeast(dp(105)),
            (h * 0.255f).toInt().coerceAtLeast(dp(105)),
            Gravity.BOTTOM or Gravity.END
        ).apply {
            rightMargin = (w * 0.160f).toInt()
            bottomMargin = (h * 0.180f).toInt()
        }

    private fun playerShadowParams(
        w: Int,
        h: Int
    ): FrameLayout.LayoutParams =
        FrameLayout.LayoutParams(
            (w * 0.120f).toInt().coerceAtLeast(dp(72)),
            (h * 0.026f).toInt().coerceAtLeast(dp(8)),
            Gravity.BOTTOM or Gravity.START
        ).apply {
            leftMargin = (w * 0.195f).toInt()
            bottomMargin = (h * 0.137f).toInt()
        }

    private fun enemyShadowParams(
        w: Int,
        h: Int
    ): FrameLayout.LayoutParams =
        FrameLayout.LayoutParams(
            (w * 0.095f).toInt().coerceAtLeast(dp(58)),
            (h * 0.022f).toInt().coerceAtLeast(dp(7)),
            Gravity.BOTTOM or Gravity.END
        ).apply {
            rightMargin = (w * 0.200f).toInt()
            bottomMargin = (h * 0.178f).toInt()
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
                playerStage.scaleX = 1.015f
                playerStage.scaleY = 1.015f
                playerStage.animate()
                    .translationX(dp(42).toFloat())
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

    private fun enemyIsSlime(): Boolean =
        enemyEntityId == "blue_slime"

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
                enemyStage.scaleX =
                    if (enemyIsSlime()) 1.035f else 1.015f
                enemyStage.scaleY =
                    if (enemyIsSlime()) 0.97f else 1.015f
                enemyStage.animate()
                    .translationX(
                        -dp(
                            if (enemyIsSlime()) 92 else 54
                        ).toFloat()
                    )
                    .translationY(
                        if (enemyIsSlime()) {
                            dp(2).toFloat()
                        } else {
                            -dp(4).toFloat()
                        }
                    )
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
                    .translationX(dp(if (enemyIsSlime()) 20 else 14).toFloat())
                    .rotation(if (enemyIsSlime()) 0f else 2.5f)
                    .scaleX(if (enemyIsSlime()) 1.13f else 0.98f)
                    .scaleY(if (enemyIsSlime()) 0.84f else 0.98f)
                    .alpha(0.74f)
                    .setDuration(115L)
                    .withEndAction {
                        enemyStage.animate()
                            .translationX(0f)
                            .rotation(0f)
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
                    .translationY(
                        dp(if (enemyIsSlime()) 30 else 44).toFloat()
                    )
                    .rotation(if (enemyIsSlime()) 0f else 7f)
                    .scaleX(if (enemyIsSlime()) 1.16f else 0.94f)
                    .scaleY(if (enemyIsSlime()) 0.72f else 0.94f)
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
            -dp(2).toFloat(),
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
            -dp(if (enemyIsSlime()) 3 else 2).toFloat(),
            0f
        ).apply {
            duration = if (enemyIsSlime()) 1050L else 1550L
            repeatCount = ObjectAnimator.INFINITE
        }

        val squashX = ObjectAnimator.ofFloat(
            enemyStage,
            View.SCALE_X,
            1f,
            if (enemyIsSlime()) 1.035f else 1.008f,
            1f
        ).apply {
            duration = if (enemyIsSlime()) 1050L else 1550L
            repeatCount = ObjectAnimator.INFINITE
        }

        val squashY = ObjectAnimator.ofFloat(
            enemyStage,
            View.SCALE_Y,
            1f,
            if (enemyIsSlime()) 0.965f else 1.008f,
            1f
        ).apply {
            duration = if (enemyIsSlime()) 1050L else 1550L
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

        fun cell(
            column: Int,
            row: Int
        ): Bitmap =
            Bitmap.createBitmap(
                atlas,
                column * cellW,
                row * cellH,
                cellW,
                cellH
            )

        // Action art reaches much farther than the idle silhouettes.
        // Pad every state so sword arcs, cape tips, slime trails, and
        // hit/death splashes never collide with an atlas-cell edge.
        val idleRaw = cell(0, 0)
        val attackRaw = cell(1, 0)
        val hitRaw = cell(0, 1)
        val finisherRaw = cell(1, 1)

        val frames =
            ActorFrames(
                idle = paddedFrame(idleRaw, 1.16f),
                attack = paddedFrame(attackRaw, 1.42f),
                hit = paddedFrame(hitRaw, 1.28f),
                finisher = paddedFrame(finisherRaw, 1.34f)
            )

        listOf(
            idleRaw,
            attackRaw,
            hitRaw,
            finisherRaw
        ).forEach {
            if (!it.isRecycled) {
                it.recycle()
            }
        }

        atlas.recycle()
        return frames
    }

    private fun paddedFrame(
        source: Bitmap,
        scale: Float
    ): Bitmap {
        val safeScale = scale.coerceAtLeast(1f)
        val width =
            (source.width * safeScale)
                .toInt()
                .coerceAtLeast(source.width)
        val height =
            (source.height * safeScale)
                .toInt()
                .coerceAtLeast(source.height)

        val output =
            Bitmap.createBitmap(
                width,
                height,
                Bitmap.Config.ARGB_8888
            )

        val canvas = Canvas(output)
        val left = (width - source.width) * 0.5f
        val top = (height - source.height) * 0.5f
        canvas.drawBitmap(
            source,
            left,
            top,
            null
        )

        return output
    }
    private fun shadowDrawable(): GradientDrawable =
        GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.argb(94, 5, 4, 18))
        }

    companion object {
        fun hasProductionEnemy(
            context: Context,
            enemyEntityId: String
        ): Boolean =
            enemyAtlasResIdOrZero(context, enemyEntityId) != 0

        private fun enemyAtlasResId(
            context: Context,
            enemyEntityId: String
        ): Int {
            val value =
                enemyAtlasResIdOrZero(context, enemyEntityId)
            require(value != 0) {
                "No production Battle V2 atlas for " +
                    enemyEntityId
            }
            return value
        }

        private fun enemyAtlasResIdOrZero(
            context: Context,
            enemyEntityId: String
        ): Int {
            val resourceName =
                when (enemyEntityId) {
                    "blue_slime" ->
                        "battle_v2_slime_actor_atlas"
                    else ->
                        "battle_v2_" +
                            enemyEntityId +
                            "_actor_atlas"
                }

            return context.resources.getIdentifier(
                resourceName,
                "drawable",
                context.packageName
            )
        }
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density)
            .toInt()
            .coerceAtLeast(1)
}
