package com.demetrecerrone.astralforge

import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.os.SystemClock
import android.util.AttributeSet
import android.view.View
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

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

    var meshDeformationEnabled: Boolean = true
        set(value) {
            field = value
            invalidate()
        }

    private var desiredFacing = "right"
    private var boneMap: Map<String, RigBoneDefinition> = emptyMap()
    private var orderedAttachments: List<RigAttachmentDefinition> = emptyList()

    private val bitmapPaint = Paint(
        Paint.ANTI_ALIAS_FLAG or
            Paint.FILTER_BITMAP_FLAG or
            Paint.DITHER_FLAG
    )

    private data class Affine(
        val a: Float,
        val b: Float,
        val c: Float,
        val d: Float,
        val tx: Float,
        val ty: Float
    ) {
        fun toMatrix(): Matrix {
            return Matrix().apply {
                setValues(
                    floatArrayOf(
                        a, c, tx,
                        b, d, ty,
                        0f, 0f, 1f
                    )
                )
            }
        }
    }

    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    val isRigLoaded: Boolean
        get() = rig != null

    fun setRig(loadedRig: LoadedRig) {
        rig = loadedRig
        desiredFacing = loadedRig.definition.defaultFacing
        boneMap = loadedRig.definition.bones.associateBy { it.name }
        orderedAttachments =
            loadedRig.definition.attachments.sortedBy { it.z }
        heldClip = null
        heldTimeMs = 0L
        playIdle()
        invalidate()
    }

    fun clearRig() {
        rig = null
        boneMap = emptyMap()
        orderedAttachments = emptyList()
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
        val worldTransforms = mutableMapOf<String, Affine>()

        fun multiply(parent: Affine, local: Affine): Affine {
            return Affine(
                a = parent.a * local.a + parent.c * local.b,
                b = parent.b * local.a + parent.d * local.b,
                c = parent.a * local.c + parent.c * local.d,
                d = parent.b * local.c + parent.d * local.d,
                tx = parent.a * local.tx +
                    parent.c * local.ty +
                    parent.tx,
                ty = parent.b * local.tx +
                    parent.d * local.ty +
                    parent.ty
            )
        }

        fun localTransform(
            x: Float,
            y: Float,
            transform: RigBoneTransform
        ): Affine {
            val radians =
                Math.toRadians(transform.rotation.toDouble())
            val cosine = cos(radians).toFloat()
            val sine = sin(radians).toFloat()

            return Affine(
                a = cosine * transform.scaleX,
                b = sine * transform.scaleX,
                c = -sine * transform.scaleY,
                d = cosine * transform.scaleY,
                tx = x + transform.x,
                ty = y + transform.y
            )
        }

        fun resolveBone(name: String): Affine {
            worldTransforms[name]?.let { return it }

            val bone = boneMap[name]
            if (bone == null) {
                val identity =
                    Affine(1f, 0f, 0f, 1f, 0f, 0f)
                worldTransforms[name] = identity
                return identity
            }

            val transform =
                transforms[name] ?: RigBoneTransform()

            val absoluteX =
                bone.x * definition.canvasWidth
            val absoluteY =
                bone.y * definition.canvasHeight

            val world = if (bone.parent != null) {
                val parent = boneMap[bone.parent]
                val parentX =
                    (parent?.x ?: 0f) * definition.canvasWidth
                val parentY =
                    (parent?.y ?: 0f) * definition.canvasHeight

                multiply(
                    resolveBone(bone.parent),
                    localTransform(
                        absoluteX - parentX,
                        absoluteY - parentY,
                        transform
                    )
                )
            } else {
                localTransform(
                    absoluteX,
                    absoluteY,
                    transform
                )
            }

            worldTransforms[name] = world
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

        orderedAttachments.forEach { attachment ->
            val bitmap =
                loaded.bitmaps[attachment.role]
                    ?: return@forEach

            val bone =
                boneMap[attachment.bone]
                    ?: boneMap["root"]
                    ?: return@forEach

            val boneTransform =
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

            canvas.concat(boneTransform.toMatrix())
            canvas.translate(
                localAttachmentX,
                localAttachmentY
            )
            canvas.rotate(attachment.restRotation)

            val meshScale =
                if (
                    meshDeformationEnabled &&
                    shouldMeshDeform(
                        definition.id,
                        attachment.role
                    )
                ) {
                    1.035f
                } else {
                    1f
                }

            canvas.scale(
                imageScale * meshScale,
                imageScale * meshScale
            )
            canvas.translate(
                -bitmap.width / 2f,
                -bitmap.height / 2f
            )

            if (
                meshDeformationEnabled &&
                shouldMeshDeform(
                    definition.id,
                    attachment.role
                )
            ) {
                drawDeformedBitmap(
                    canvas = canvas,
                    bitmap = bitmap,
                    entityId = definition.id,
                    role = attachment.role,
                    animationName = clip?.name ?: "idle",
                    animationProgress = if (clip != null) {
                        (
                            sampleTime.toFloat() /
                                clip.durationMs
                                    .coerceAtLeast(1L)
                                    .toFloat()
                        ).coerceIn(0f, 1f)
                    } else {
                        0f
                    }
                )
            } else {
                canvas.drawBitmap(
                    bitmap,
                    0f,
                    0f,
                    bitmapPaint
                )
            }
            canvas.restore()
        }

        if (activeClip != null && motionEnabled) {
            postInvalidateOnAnimation()
        }
    }

    private fun shouldMeshDeform(
        entityId: String,
        role: String
    ): Boolean {
        return when (entityId) {
            "knight" -> role in setOf(
                "cape_back",
                "torso",
                "hips",
                "upper_arm_left",
                "lower_arm_left",
                "upper_arm_right",
                "lower_arm_right",
                "upper_leg_left",
                "lower_leg_left",
                "upper_leg_right",
                "lower_leg_right"
            )

            "dire_wolf" -> role in setOf(
                "neck_mane",
                "spine_front",
                "spine_mid",
                "hips",
                "front_upper_left",
                "front_lower_left",
                "front_upper_right",
                "front_lower_right",
                "hind_upper_left",
                "hind_lower_left",
                "hind_upper_right",
                "hind_lower_right",
                "tail_1",
                "tail_2",
                "tail_3"
            )

            else -> false
        }
    }

    private fun drawDeformedBitmap(
        canvas: Canvas,
        bitmap: android.graphics.Bitmap,
        entityId: String,
        role: String,
        animationName: String,
        animationProgress: Float
    ) {
        val wideBody = role in setOf(
            "cape_back",
            "torso",
            "spine_front",
            "spine_mid",
            "hips",
            "neck_mane"
        )

        val meshWidth = if (wideBody) 6 else 4
        val meshHeight =
            if (role == "cape_back") 8 else 6

        val vertices = FloatArray(
            (meshWidth + 1) *
                (meshHeight + 1) *
                2
        )

        val phase =
            animationProgress *
                (PI * 2.0).toFloat()

        val actionPulse = when (animationName) {
            "attack" ->
                sin(
                    animationProgress *
                        PI.toFloat()
                ).coerceAtLeast(0f)

            "hit" ->
                sin(
                    animationProgress *
                        PI.toFloat()
                ).coerceAtLeast(0f)

            "death" ->
                animationProgress

            else -> 0f
        }

        val idleWave =
            if (animationName == "idle") {
                sin(phase)
            } else {
                0f
            }

        var offset = 0

        for (row in 0..meshHeight) {
            val v =
                row.toFloat() /
                    meshHeight.toFloat()

            for (column in 0..meshWidth) {
                val u =
                    column.toFloat() /
                        meshWidth.toFloat()

                var x = u * bitmap.width
                var y = v * bitmap.height

                if (entityId == "knight") {
                    when (role) {
                        "cape_back" -> {
                            x += (
                                sin(
                                    phase +
                                        v * 2.4f +
                                        u * 0.7f
                                ) *
                                    (5f + 10f * v) *
                                    (if (
                                        animationName ==
                                            "idle"
                                    ) {
                                        1f
                                    } else {
                                        0.45f
                                    })
                                )

                            x +=
                                actionPulse *
                                    18f *
                                    v

                            y +=
                                cos(
                                    phase * 0.75f +
                                        u * 2.2f
                                ) *
                                    3.5f *
                                    v
                        }

                        "torso" -> {
                            x +=
                                idleWave *
                                    4.5f *
                                    (0.5f - v)

                            x +=
                                actionPulse *
                                    8f *
                                    (v - 0.45f)

                            y +=
                                sin(
                                    u *
                                        PI.toFloat()
                                ) *
                                    idleWave *
                                    2.8f
                        }

                        "hips" -> {
                            x +=
                                (u - 0.5f) *
                                    idleWave *
                                    3f

                            y +=
                                actionPulse *
                                    3f *
                                    sin(
                                        u *
                                            PI.toFloat()
                                    )
                        }

                        else -> {
                            val side =
                                if (
                                    role.contains("left")
                                ) {
                                    -1f
                                } else {
                                    1f
                                }

                            x +=
                                sin(
                                    v *
                                        PI.toFloat()
                                ) *
                                    (
                                        idleWave * 2.2f +
                                            actionPulse * 6f
                                        ) *
                                    side
                        }
                    }
                } else if (
                    entityId == "dire_wolf"
                ) {
                    when (role) {
                        "spine_front",
                        "spine_mid",
                        "hips",
                        "neck_mane" -> {
                            y +=
                                sin(
                                    u *
                                        PI.toFloat() +
                                        phase * 0.5f
                                ) *
                                    (
                                        if (
                                            animationName ==
                                                "idle"
                                        ) {
                                            3.5f
                                        } else {
                                            1.5f
                                        }
                                        )

                            y -=
                                actionPulse *
                                    sin(
                                        u *
                                            PI.toFloat()
                                    ) *
                                    8f

                            x +=
                                actionPulse *
                                    (u - 0.5f) *
                                    14f
                        }

                        "tail_1",
                        "tail_2",
                        "tail_3" -> {
                            y +=
                                sin(
                                    phase +
                                        v * 2.2f
                                ) *
                                    5.5f *
                                    v

                            x +=
                                actionPulse *
                                    7f *
                                    v
                        }

                        else -> {
                            val rearLeg =
                                role.startsWith(
                                    "hind_"
                                )

                            x +=
                                sin(
                                    v *
                                        PI.toFloat()
                                ) *
                                    (
                                        idleWave * 1.8f +
                                            actionPulse *
                                                (
                                                    if (
                                                        rearLeg
                                                    ) {
                                                        5f
                                                    } else {
                                                        7f
                                                    }
                                                    )
                                        )
                        }
                    }
                }

                vertices[offset++] = x
                vertices[offset++] = y
            }
        }

        canvas.drawBitmapMesh(
            bitmap,
            meshWidth,
            meshHeight,
            vertices,
            0,
            null,
            0,
            bitmapPaint
        )
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
                val rawAmount =
                    ((timeMs - before.first) / span)
                        .coerceIn(0f, 1f)
                val amount =
                    easedAmount(clip.name, rawAmount)

                output[boneName] = interpolate(
                    before.second,
                    after.second,
                    amount
                )
            }
        }

        return output
    }

    private fun easedAmount(
        animationName: String,
        amount: Float
    ): Float {
        val t = amount.coerceIn(0f, 1f)

        return when (animationName) {
            "attack" -> {
                if (t < 0.5f) {
                    4f * t * t * t
                } else {
                    val p = -2f * t + 2f
                    1f - (p * p * p) / 2f
                }
            }
            "hit" -> {
                1f - (1f - t) * (1f - t)
            }
            else -> {
                t * t * t *
                    (t * (t * 6f - 15f) + 10f)
            }
        }
    }

    private fun interpolate(
        start: RigBoneTransform,
        end: RigBoneTransform,
        amount: Float
    ): RigBoneTransform {
        fun lerp(a: Float, b: Float): Float =
            a + (b - a) * amount

        fun lerpAngle(a: Float, b: Float): Float {
            var delta = (b - a) % 360f
            if (delta > 180f) delta -= 360f
            if (delta < -180f) delta += 360f
            return a + delta * amount
        }

        return RigBoneTransform(
            x = lerp(start.x, end.x),
            y = lerp(start.y, end.y),
            rotation = lerpAngle(
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
