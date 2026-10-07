package com.demetrecerrone.astralforge

import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.os.SystemClock
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

class SkeletalActorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var rig: LoadedRig? = null
    private var activeClip: RigAnimationClip? = null
    private var heldClip: RigAnimationClip? = null
    private var heldTimeMs: Long = 0L
    private var animationStartMs: Long = 0L
    private var returnToIdle = false
    private var completion: (() -> Unit)? = null
    private var completionDelivered = false

    var motionEnabled: Boolean = true
        set(value) {
            field = value
            invalidate()
        }

    private var desiredFacing = "right"

    val isRigLoaded: Boolean
        get() = rig != null

    fun setRig(loadedRig: LoadedRig) {
        rig = loadedRig
        desiredFacing = loadedRig.definition.defaultFacing
        heldClip = null
        heldTimeMs = 0L
        playIdle()
        invalidate()
    }

    fun clearRig() {
        rig = null
        activeClip = null
        heldClip = null
        completion = null
        invalidate()
    }

    fun setFacing(facing: String) {
        desiredFacing = facing.lowercase()
        invalidate()
    }

    fun playIdle() {
        val idle = rig?.definition?.animations?.get("idle")
        if (idle == null || !motionEnabled) {
            activeClip = null
            heldClip = null
            completion = null
            invalidate()
            return
        }

        activeClip = idle
        heldClip = null
        animationStartMs = SystemClock.uptimeMillis()
        returnToIdle = false
        completion = null
        completionDelivered = false
        postInvalidateOnAnimation()
    }

    fun play(
        animationName: String,
        returnToIdleAfter: Boolean = true,
        onComplete: (() -> Unit)? = null
    ) {
        val clip = rig?.definition?.animations?.get(animationName)

        if (clip == null) {
            onComplete?.invoke()
            return
        }

        if (!motionEnabled) {
            activeClip = null
            heldClip = clip
            heldTimeMs = clip.durationMs
            invalidate()
            onComplete?.invoke()

            if (returnToIdleAfter && animationName != "death") {
                heldClip = null
            }
            return
        }

        activeClip = clip
        heldClip = null
        animationStartMs = SystemClock.uptimeMillis()
        returnToIdle = returnToIdleAfter
        completion = onComplete
        completionDelivered = false
        postInvalidateOnAnimation()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val loaded = rig ?: return
        if (width <= 0 || height <= 0) return

        val definition = loaded.definition
        val now = SystemClock.uptimeMillis()

        var clip = activeClip
        var sampleTime = 0L

        if (clip != null) {
            val elapsed = (now - animationStartMs).coerceAtLeast(0L)

            if (clip.loop) {
                sampleTime = elapsed % clip.durationMs.coerceAtLeast(1L)
            } else if (elapsed >= clip.durationMs) {
                sampleTime = clip.durationMs
                heldClip = clip
                heldTimeMs = sampleTime
                activeClip = null

                if (!completionDelivered) {
                    completionDelivered = true
                    val callback = completion
                    completion = null
                    callback?.invoke()
                }

                if (returnToIdle) {
                    playIdle()
                    clip = activeClip
                    sampleTime = 0L
                }
            } else {
                sampleTime = elapsed
            }
        } else if (heldClip != null) {
            clip = heldClip
            sampleTime = heldTimeMs
        }

        val transforms = sampleTransforms(clip, sampleTime)
        val boneMap = definition.bones.associateBy { it.name }
        val worldMatrices = mutableMapOf<String, Matrix>()

        fun resolveBone(name: String): Matrix {
            worldMatrices[name]?.let { return it }

            val bone = boneMap[name]
            if (bone == null) {
                val identity = Matrix()
                worldMatrices[name] = identity
                return identity
            }

            val transform =
                transforms[name] ?: RigBoneTransform()

            val absoluteX = bone.x * definition.canvasWidth
            val absoluteY = bone.y * definition.canvasHeight

            val localX: Float
            val localY: Float
            val parentMatrix: Matrix?

            if (bone.parent != null) {
                val parent = boneMap[bone.parent]
                val parentX =
                    (parent?.x ?: 0f) * definition.canvasWidth
                val parentY =
                    (parent?.y ?: 0f) * definition.canvasHeight
                localX = absoluteX - parentX
                localY = absoluteY - parentY
                parentMatrix = resolveBone(bone.parent)
            } else {
                localX = absoluteX
                localY = absoluteY
                parentMatrix = null
            }

            val local = Matrix().apply {
                setTranslate(
                    localX + transform.x,
                    localY + transform.y
                )
                postRotate(transform.rotation)
                postScale(
                    transform.scaleX,
                    transform.scaleY
                )
            }

            val world = Matrix()
            if (parentMatrix != null) {
                world.set(parentMatrix)
                world.postConcat(local)
            } else {
                world.set(local)
            }

            worldMatrices[name] = world
            return world
        }

        val fitScale = min(
            width / definition.canvasWidth,
            height / definition.canvasHeight
        )
        val viewWidth =
            definition.canvasWidth * fitScale
        val viewHeight =
            definition.canvasHeight * fitScale
        val offsetX = (width - viewWidth) / 2f
        val offsetY = (height - viewHeight) / 2f

        val mirror =
            desiredFacing != definition.defaultFacing

        definition.attachments
            .sortedBy { it.z }
            .forEach { attachment ->
                val bitmap =
                    loaded.bitmaps[attachment.role] ?: return@forEach

                val bone =
                    boneMap[attachment.bone]
                        ?: boneMap["root"]
                        ?: return@forEach

                val boneMatrix =
                    resolveBone(bone.name)

                val boneAbsoluteX =
                    bone.x * definition.canvasWidth
                val boneAbsoluteY =
                    bone.y * definition.canvasHeight

                val localAttachmentX =
                    attachment.restX - boneAbsoluteX
                val localAttachmentY =
                    attachment.restY - boneAbsoluteY

                val imageScale = min(
                    attachment.maxWidth /
                        bitmap.width.coerceAtLeast(1),
                    attachment.maxHeight /
                        bitmap.height.coerceAtLeast(1)
                ) * attachment.restScale

                canvas.save()
                canvas.translate(offsetX, offsetY)
                canvas.scale(fitScale, fitScale)

                if (mirror) {
                    canvas.translate(
                        definition.canvasWidth,
                        0f
                    )
                    canvas.scale(-1f, 1f)
                }

                canvas.concat(boneMatrix)
                canvas.translate(
                    localAttachmentX,
                    localAttachmentY
                )
                canvas.rotate(attachment.restRotation)
                canvas.scale(imageScale, imageScale)
                // restX/restY in the v2 rigs are visual centers, not
                // pivot anchors. Center the art on the stored rest position;
                // the bone matrix itself already provides the joint pivot
                // used for animation.
                canvas.translate(
                    -bitmap.width / 2f,
                    -bitmap.height / 2f
                )
                canvas.drawBitmap(bitmap, 0f, 0f, null)
                canvas.restore()
            }

        if (activeClip != null && motionEnabled) {
            postInvalidateOnAnimation()
        }
    }

    private fun sampleTransforms(
        clip: RigAnimationClip?,
        timeMs: Long
    ): Map<String, RigBoneTransform> {
        if (clip == null || clip.keys.isEmpty()) {
            return emptyMap()
        }

        val names = linkedSetOf<String>()
        clip.keys.forEach { key ->
            names.addAll(key.transforms.keys)
        }

        val output = linkedMapOf<String, RigBoneTransform>()

        names.forEach { boneName ->
            val relevant = clip.keys
                .mapNotNull { key ->
                    key.transforms[boneName]?.let {
                        key.timeMs to it
                    }
                }

            if (relevant.isEmpty()) return@forEach

            val before =
                relevant.lastOrNull { it.first <= timeMs }
                    ?: relevant.first()
            val after =
                relevant.firstOrNull { it.first >= timeMs }
                    ?: relevant.last()

            if (before.first == after.first) {
                output[boneName] = before.second
            } else {
                val span =
                    (after.first - before.first).toFloat()
                        .coerceAtLeast(1f)
                val amount =
                    ((timeMs - before.first) / span)
                        .coerceIn(0f, 1f)

                output[boneName] = interpolate(
                    before.second,
                    after.second,
                    amount
                )
            }
        }

        return output
    }

    private fun interpolate(
        start: RigBoneTransform,
        end: RigBoneTransform,
        amount: Float
    ): RigBoneTransform {
        fun lerp(a: Float, b: Float): Float =
            a + (b - a) * amount

        return RigBoneTransform(
            x = lerp(start.x, end.x),
            y = lerp(start.y, end.y),
            rotation = lerp(
                start.rotation,
                end.rotation
            ),
            scaleX = lerp(
                start.scaleX,
                end.scaleX
            ),
            scaleY = lerp(
                start.scaleY,
                end.scaleY
            )
        )
    }
}
