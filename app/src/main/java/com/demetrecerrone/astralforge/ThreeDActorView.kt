package com.demetrecerrone.astralforge

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.Base64
import android.view.Choreographer
import android.view.TextureView
import android.widget.FrameLayout
import com.google.android.filament.View as FilamentView
import com.google.android.filament.android.UiHelper
import com.google.android.filament.utils.ModelViewer
import com.google.android.filament.utils.Utils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.max

class ThreeDActorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs), Choreographer.FrameCallback {

    companion object {
        init {
            Utils.init()
        }
    }

    private val textureView = TextureView(context).apply {
        isOpaque = false
        setBackgroundColor(Color.TRANSPARENT)
        isClickable = false
        isFocusable = false
    }

    private val choreographer = Choreographer.getInstance()
    private var modelViewer: ModelViewer? = null
    private var running = false
    private var modelAssetPath: String? = null
    private var desiredFacing = "right"

    private var animationIndices = emptyMap<String, Int>()
    private var idleAnimationIndex = -1
    private var activeAnimationIndex = -1
    private var activeAnimationStartNanos = 0L
    private var activeAnimationDurationSeconds = 0f
    private var activeAnimationLoop = false
    private var returnToIdleAfterAnimation = false
    private var completion: (() -> Unit)? = null
    private var completionDelivered = false

    var motionEnabled: Boolean = true
        set(value) {
            field = value
            if (!value) {
                freezeCurrentAnimation()
            } else if (isModelLoaded) {
                playIdle()
            }
        }

    val isModelLoaded: Boolean
        get() = modelViewer?.asset != null

    init {
        clipChildren = false
        clipToPadding = false
        addView(
            textureView,
            LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )
        )
    }

    fun loadModel(assetPath: String): Boolean {
        return runCatching {
            val viewer = ensureViewer()
            val bytes = readModelBytes(assetPath)
            val buffer = ByteBuffer
                .allocateDirect(bytes.size)
                .order(ByteOrder.nativeOrder())
                .put(bytes)
                .apply { rewind() }

            viewer.loadModelGlb(buffer)
            check(viewer.asset != null) {
                "Unable to load 3D model: " + assetPath
            }
            viewer.transformToUnitCube()
            viewer.autoPlayAnimations = false

            modelAssetPath = assetPath
            rebuildAnimationMap()
            setFacing(desiredFacing)
            startRendering()
            playIdle()
            true
        }.getOrElse {
            clearModel()
            false
        }
    }

    fun clearModel() {
        modelViewer?.destroyModel()
        modelAssetPath = null
        animationIndices = emptyMap()
        idleAnimationIndex = -1
        activeAnimationIndex = -1
        activeAnimationStartNanos = 0L
        activeAnimationDurationSeconds = 0f
        activeAnimationLoop = false
        completion = null
        completionDelivered = false
    }

    fun setFacing(facing: String) {
        desiredFacing = facing.lowercase()
        textureView.scaleX =
            if (desiredFacing == "left") -1f else 1f
    }

    fun playIdle() {
        val index = idleAnimationIndex
        if (index < 0) {
            activeAnimationIndex = -1
            completion = null
            return
        }

        if (!motionEnabled) {
            applyAnimationFrame(index, 0f)
            activeAnimationIndex = -1
            completion = null
            return
        }

        activeAnimationIndex = index
        activeAnimationStartNanos = System.nanoTime()
        activeAnimationDurationSeconds =
            animationDuration(index)
        activeAnimationLoop = true
        returnToIdleAfterAnimation = false
        completion = null
        completionDelivered = false
        startRendering()
    }

    fun play(
        animationName: String,
        returnToIdleAfter: Boolean = true,
        onComplete: (() -> Unit)? = null
    ) {
        val index =
            animationIndices[animationName.lowercase()]
                ?: animationIndices.entries.firstOrNull {
                    it.key.contains(animationName.lowercase())
                }?.value

        if (index == null) {
            onComplete?.invoke()
            if (returnToIdleAfter) playIdle()
            return
        }

        val duration = animationDuration(index)

        if (!motionEnabled) {
            applyAnimationFrame(index, duration)
            onComplete?.invoke()
            if (returnToIdleAfter && animationName != "death") {
                playIdle()
            }
            return
        }

        activeAnimationIndex = index
        activeAnimationStartNanos = System.nanoTime()
        activeAnimationDurationSeconds = duration
        activeAnimationLoop = false
        returnToIdleAfterAnimation = returnToIdleAfter
        completion = onComplete
        completionDelivered = false
        startRendering()
    }

    fun release() {
        running = false
        choreographer.removeFrameCallback(this)
        completion = null
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (modelAssetPath != null && modelViewer == null) {
            val path = modelAssetPath
            if (path != null) loadModel(path)
        } else if (isModelLoaded) {
            startRendering()
        }
    }

    override fun onDetachedFromWindow() {
        release()
        modelViewer = null
        super.onDetachedFromWindow()
    }

    override fun doFrame(frameTimeNanos: Long) {
        if (!running) return

        val viewer = modelViewer
        val animator = viewer?.animator

        if (
            animator != null &&
            activeAnimationIndex >= 0
        ) {
            val elapsedSeconds =
                (frameTimeNanos - activeAnimationStartNanos)
                    .coerceAtLeast(0L) / 1_000_000_000f

            if (activeAnimationLoop) {
                val duration =
                    max(activeAnimationDurationSeconds, 0.001f)
                val sample = elapsedSeconds % duration
                animator.applyAnimation(
                    activeAnimationIndex,
                    sample
                )
                animator.updateBoneMatrices()
            } else {
                val duration =
                    max(activeAnimationDurationSeconds, 0.001f)
                val sample =
                    elapsedSeconds.coerceAtMost(duration)
                animator.applyAnimation(
                    activeAnimationIndex,
                    sample
                )
                animator.updateBoneMatrices()

                if (elapsedSeconds >= duration) {
                    val shouldReturn =
                        returnToIdleAfterAnimation
                    val callback = completion

                    activeAnimationIndex = -1
                    completion = null

                    if (!completionDelivered) {
                        completionDelivered = true
                        callback?.invoke()
                    }

                    if (shouldReturn) {
                        playIdle()
                    }
                }
            }
        }

        viewer?.render(frameTimeNanos)

        if (running) {
            choreographer.postFrameCallback(this)
        }
    }

    private fun ensureViewer(): ModelViewer {
        modelViewer?.let { return it }

        val uiHelper =
            UiHelper(
                UiHelper.ContextErrorPolicy.DONT_CHECK
            ).apply {
                isOpaque = false
            }

        return ModelViewer(
            textureView,
            uiHelper = uiHelper,
            manipulator = null
        ).also { viewer ->
            viewer.autoPlayAnimations = false
            viewer.scene.skybox = null
            viewer.view.blendMode =
                FilamentView.BlendMode.TRANSLUCENT
            viewer.view.antiAliasing =
                FilamentView.AntiAliasing.FXAA
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
            modelViewer = viewer
        }
    }

    private fun readModelBytes(
        assetPath: String
    ): ByteArray {
        return if (assetPath.endsWith(".b64")) {
            val text =
                context.assets.open(assetPath)
                    .bufferedReader()
                    .use { it.readText() }
            Base64.decode(text, Base64.DEFAULT)
        } else {
            context.assets.open(assetPath)
                .use { it.readBytes() }
        }
    }

    private fun rebuildAnimationMap() {
        val animator = modelViewer?.animator
        if (animator == null) {
            animationIndices = emptyMap()
            idleAnimationIndex = -1
            return
        }

        val map = linkedMapOf<String, Int>()
        for (index in 0 until animator.animationCount) {
            val name =
                animator.getAnimationName(index)
                    .lowercase()
            map[name] = index
        }

        animationIndices = map
        idleAnimationIndex =
            map["idle"]
                ?: map.entries.firstOrNull {
                    it.key.contains("idle")
                }?.value
                ?: if (animator.animationCount > 0) 0 else -1
    }

    private fun animationDuration(
        index: Int
    ): Float {
        return modelViewer?.animator
            ?.getAnimationDuration(index)
            ?.coerceAtLeast(0.001f)
            ?: 0.001f
    }

    private fun applyAnimationFrame(
        index: Int,
        seconds: Float
    ) {
        val animator = modelViewer?.animator ?: return
        animator.applyAnimation(index, seconds)
        animator.updateBoneMatrices()
        startRendering()
    }

    private fun freezeCurrentAnimation() {
        if (!isModelLoaded) return
        val index = activeAnimationIndex
        if (index >= 0) {
            applyAnimationFrame(index, 0f)
        }
        activeAnimationIndex = -1
        completion = null
    }

    private fun startRendering() {
        if (running) return
        running = true
        choreographer.postFrameCallback(this)
    }
}
