package com.demetrecerrone.astralforge

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout

class BattleV2RendererHost @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private var threeDView: BattleV2ThreeDSceneView? = null
    private var fallbackView: BattleV2RenderView? = null

    private var playerSprites:
        EntitySpriteStore.EntitySpriteSet? = null
    private var enemySprites:
        EntitySpriteStore.EntitySpriteSet? = null
    private var playerEntityId = "knight"
    private var enemyEntityId = "blue_slime"

    private var fpsPreference = "SYSTEM"
    private var batterySaver = false
    private var released = false

    var modeLabel: String = "3D renderer initializing"
        private set

    var onModeChanged: ((String) -> Unit)? = null
        set(value) {
            field = value
            value?.invoke(modeLabel)
        }

    init {
        clipChildren = false
        clipToPadding = false
        showThreeDRenderer()
    }

    fun configureActors(playerId: String, enemyId: String) {
        playerEntityId = playerId
        enemyEntityId = enemyId
        if (!supportsPrototypePair()) {
            activateFallback()
        }
    }

    fun setPreferredFps(
        value: String,
        batterySaverEnabled: Boolean
    ) {
        fpsPreference = value
        batterySaver = batterySaverEnabled
        threeDView?.setPreferredFps(
            fpsPreference,
            batterySaver
        )
        fallbackView?.setPreferredFps(
            fpsPreference,
            batterySaver
        )
    }

    fun setActors(
        player: EntitySpriteStore.EntitySpriteSet,
        enemy: EntitySpriteStore.EntitySpriteSet
    ) {
        playerSprites = player
        enemySprites = enemy
        fallbackView?.setActors(player, enemy)
    }

    fun playPlayer(animation: BattleV2Animation) {
        threeDView?.playPlayer(animation)
        fallbackView?.playPlayer(animation)
    }

    fun playEnemy(animation: BattleV2Animation) {
        threeDView?.playEnemy(animation)
        fallbackView?.playEnemy(animation)
    }

    fun resetActors() {
        threeDView?.resetActors()
        fallbackView?.resetActors()
    }

    fun release() {
        if (released) return
        released = true
        threeDView?.release()
        fallbackView?.release()
    }

    private fun supportsPrototypePair(): Boolean =
        playerEntityId == "knight" &&
            enemyEntityId == "blue_slime"

    private fun showThreeDRenderer() {
        val view =
            BattleV2ThreeDSceneView(context).apply {
                setPreferredFps(
                    fpsPreference,
                    batterySaver
                )
                onReadyChanged = { ready ->
                    post {
                        if (released) return@post

                        if (ready && supportsPrototypePair()) {
                            modeLabel = "3D renderer ready"
                            onModeChanged?.invoke(modeLabel)
                        } else {
                            activateFallback()
                        }
                    }
                }
            }

        threeDView = view
        addView(
            view,
            LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )
        )
    }

    private fun activateFallback() {
        if (fallbackView != null) return

        val old3d = threeDView
        old3d?.release()
        if (old3d != null) {
            removeView(old3d)
        }
        threeDView = null

        val fallback =
            BattleV2RenderView(context).apply {
                setPreferredFps(
                    fpsPreference,
                    batterySaver
                )
            }

        fallbackView = fallback
        addView(
            fallback,
            LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )
        )

        val player = playerSprites
        val enemy = enemySprites
        if (player != null && enemy != null) {
            fallback.setActors(player, enemy)
        }

        modeLabel =
            "Dynamic actor renderer ready"
        onModeChanged?.invoke(modeLabel)
    }
}
