package com.demetrecerrone.astralforge

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.util.AttributeSet
import android.view.Choreographer
import android.view.SurfaceView
import android.widget.FrameLayout
import com.google.android.filament.View as FilamentView
import com.google.android.filament.android.UiHelper
import com.google.android.filament.utils.ModelViewer
import com.google.android.filament.utils.Utils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.max

class BattleV2ThreeDSceneView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs), Choreographer.FrameCallback {

    companion object {
        init {
            Utils.init()
        }

        private const val MODEL_PATH =
            "models/battle_v2_prototype_arena.glb"
    }

    private data class ActorTrack(
        val prefix: String,
        var animation: BattleV2Animation = BattleV2Animation.IDLE,
        var startedNanos: Long = 0L
    )

    private val surfaceView =
        SurfaceView(context).apply {
            setBackgroundColor(Color.TRANSPARENT)
            holder.setFormat(PixelFormat.TRANSLUCENT)
            isClickable = false
            isFocusable = false
        }

    private val choreographer = Choreographer.getInstance()
    private val playerTrack = ActorTrack("player")
    private val enemyTrack = ActorTrack("enemy")

    private var modelViewer: ModelViewer? = null
    private var animationIndices = emptyMap<String, Int>()
    private var running = false
    private var initialized = false
    private var initializing = false
    private var released = false
    private var lastFrameNanos = 0L
    private var targetFrameNanos = 16_666_667L

    var onReadyChanged: ((Boolean) -> Unit)? = null

    val isReady: Boolean
        get() = initialized && modelViewer?.asset != null

    init {
        setBackgroundColor(Color.TRANSPARENT)
        addView(
            surfaceView,
            LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )
        )
    }

    fun setPreferredFps(value: String, batterySaver: Boolean) {
        targetFrameNanos =
            if (batterySaver || value == "30") {
                33_333_333L
            } else {
                16_666_667L
            }
    }

    fun initializeRenderer() {
        if (
            initialized ||
            initializing ||
            released ||
            !isAttachedToWindow
        ) {
            return
        }

        if (width <= 0 || height <= 0) {
            post { initializeRenderer() }
            return
        }

        initializing = true

        val success =
            runCatching {
                val helper =
                    UiHelper(
                        UiHelper.ContextErrorPolicy.DONT_CHECK
                    ).apply {
                        isOpaque = false
                    }

                val viewer =
                    ModelViewer(
                        surfaceView,
                        uiHelper = helper,
                        manipulator = null
                    )

                viewer.autoPlayAnimations = false
                viewer.scene.skybox = null
                viewer.view.antiAliasing =
                    FilamentView.AntiAliasing.FXAA
                viewer.view.blendMode =
                    FilamentView.BlendMode.TRANSLUCENT

                viewer.renderer.clearOptions =
                    viewer.renderer.clearOptions.apply {
                        clear = true
                        clearColor =
                            doubleArrayOf(
                                0.0,
                                0.0,
                                0.0,
                                0.0
                            )
                    }

                val bytes =
                    context.assets
                        .open(MODEL_PATH)
                        .use { it.readBytes() }

                val buffer =
                    ByteBuffer
                        .allocateDirect(bytes.size)
                        .order(ByteOrder.nativeOrder())
                        .put(bytes)
                        .apply { rewind() }

                viewer.loadModelGlb(buffer)
                check(viewer.asset != null) {
                    "Battle V2 3D arena failed to load."
                }

                viewer.transformToUnitCube()
                modelViewer = viewer
                rebuildAnimationMap()
                val now = System.nanoTime()
                playerTrack.startedNanos = now
                enemyTrack.startedNanos = now
                initialized = true
                startRendering()
                true
            }.getOrElse {
                modelViewer?.destroy()
                modelViewer = null
                animationIndices = emptyMap()
                false
            }

        initializing = false
        onReadyChanged?.invoke(success)
    }

    fun playPlayer(animation: BattleV2Animation) {
        playerTrack.animation = animation
        playerTrack.startedNanos = System.nanoTime()
        startRendering()
    }

    fun playEnemy(animation: BattleV2Animation) {
        enemyTrack.animation = animation
        enemyTrack.startedNanos = System.nanoTime()
        startRendering()
    }

    fun resetActors() {
        val now = System.nanoTime()
        playerTrack.animation = BattleV2Animation.IDLE
        enemyTrack.animation = BattleV2Animation.IDLE
        playerTrack.startedNanos = now
        enemyTrack.startedNanos = now
        startRendering()
    }

    fun release() {
        if (released) return
        released = true
        running = false
        choreographer.removeFrameCallback(this)
        onReadyChanged = null
        modelViewer?.destroy()
        modelViewer = null
        animationIndices = emptyMap()
        initialized = false
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        released = false
        post { initializeRenderer() }
    }

    override fun onDetachedFromWindow() {
        release()
        super.onDetachedFromWindow()
    }

    override fun doFrame(frameTimeNanos: Long) {
        if (!running || released) return

        if (
            lastFrameNanos == 0L ||
            frameTimeNanos - lastFrameNanos >= targetFrameNanos
        ) {
            lastFrameNanos = frameTimeNanos
            renderFrame(frameTimeNanos)
        }

        if (running && !released) {
            choreographer.postFrameCallback(this)
        }
    }

    private fun renderFrame(frameTimeNanos: Long) {
        val viewer = modelViewer ?: return
        val animator = viewer.animator ?: return

        applyTrack(animator, playerTrack, frameTimeNanos)
        applyTrack(animator, enemyTrack, frameTimeNanos)
        animator.updateBoneMatrices()
        viewer.render(frameTimeNanos)
    }

    private fun applyTrack(
        animator: com.google.android.filament.gltfio.Animator,
        track: ActorTrack,
        frameTimeNanos: Long
    ) {
        val idleIndex =
            animationIndices[track.prefix + "_idle"]

        if (idleIndex != null) {
            val duration =
                max(
                    animator.getAnimationDuration(idleIndex),
                    0.001f
                )
            val elapsed =
                (
                    (frameTimeNanos - track.startedNanos)
                        .coerceAtLeast(0L) /
                        1_000_000_000f
                )
            animator.applyAnimation(
                idleIndex,
                elapsed % duration
            )
        }

        if (track.animation == BattleV2Animation.IDLE) {
            return
        }

        val logicalName =
            when (track.animation) {
                BattleV2Animation.ATTACK -> "attack"
                BattleV2Animation.HIT -> "hit"
                BattleV2Animation.DEATH -> "death"
                BattleV2Animation.VICTORY -> "victory"
                BattleV2Animation.IDLE -> "idle"
            }

        val index =
            animationIndices[
                track.prefix + "_" + logicalName
            ] ?: return

        val duration =
            max(
                animator.getAnimationDuration(index),
                0.001f
            )

        val elapsed =
            (
                (frameTimeNanos - track.startedNanos)
                    .coerceAtLeast(0L) /
                    1_000_000_000f
            )

        animator.applyAnimation(
            index,
            elapsed.coerceAtMost(duration)
        )

        val spec =
            BattleV2AnimationTimeline.spec(
                track.animation
            )

        if (
            spec.returnToIdle &&
            elapsed >= duration
        ) {
            track.animation = BattleV2Animation.IDLE
            track.startedNanos = frameTimeNanos
        }
    }

    private fun rebuildAnimationMap() {
        val animator = modelViewer?.animator ?: return
        val map = linkedMapOf<String, Int>()

        for (index in 0 until animator.animationCount) {
            map[
                animator.getAnimationName(index).lowercase()
            ] = index
        }

        animationIndices = map
    }

    private fun startRendering() {
        if (
            running ||
            released ||
            modelViewer == null
        ) {
            return
        }

        running = true
        choreographer.postFrameCallback(this)
    }
}
