package com.demetrecerrone.astralforge

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.os.SystemClock
import android.util.AttributeSet
import android.view.View
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.min
import kotlin.math.sin

class FullBodyMeshActorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var actor: FullBodyMeshActorAssets? = null
    private var desiredFacing = "right"
    private var animationName = "idle"
    private var animationStartMs = 0L
    private var returnToIdle = false
    private var completion: (() -> Unit)? = null
    private var completionDelivered = false

    var motionEnabled: Boolean = true
        set(value) {
            field = value
            invalidate()
        }

    private val paint = Paint(
        Paint.ANTI_ALIAS_FLAG or
            Paint.FILTER_BITMAP_FLAG or
            Paint.DITHER_FLAG
    )

    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    fun setActor(value: FullBodyMeshActorAssets) {
        actor = value
        desiredFacing = value.defaultFacing
        playIdle()
    }

    fun clearActor() {
        actor = null
        completion = null
        invalidate()
    }

    fun setFacing(facing: String) {
        desiredFacing = facing.lowercase()
        invalidate()
    }

    fun playIdle() {
        animationName = "idle"
        animationStartMs = SystemClock.uptimeMillis()
        returnToIdle = false
        completion = null
        completionDelivered = false
        postInvalidateOnAnimation()
    }

    fun play(
        name: String,
        returnToIdleAfter: Boolean = true,
        onComplete: (() -> Unit)? = null
    ) {
        if (actor == null) {
            onComplete?.invoke()
            return
        }

        if (!motionEnabled) {
            animationName =
                if (name == "death") "death" else "idle"
            invalidate()
            onComplete?.invoke()
            return
        }

        animationName = name
        animationStartMs = SystemClock.uptimeMillis()
        returnToIdle = returnToIdleAfter
        completion = onComplete
        completionDelivered = false
        postInvalidateOnAnimation()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val loaded = actor ?: return
        if (width <= 0 || height <= 0) return

        val duration = durationFor(
            loaded.entityId,
            animationName
        )
        val elapsed =
            (SystemClock.uptimeMillis() - animationStartMs)
                .coerceAtLeast(0L)

        val loop = animationName == "idle"
        var progress =
            if (loop) {
                (elapsed % duration).toFloat() /
                    duration.toFloat()
            } else {
                (elapsed.toFloat() / duration.toFloat())
                    .coerceIn(0f, 1f)
            }

        if (!loop && elapsed >= duration) {
            progress = 1f

            if (!completionDelivered) {
                completionDelivered = true
                val callback = completion
                completion = null
                callback?.invoke()
            }

            if (
                returnToIdle &&
                animationName != "death"
            ) {
                playIdle()
                progress = 0f
            }
        }

        val virtual =
            loaded.body.width.toFloat().coerceAtLeast(1f)
        val fitScale = min(
            width / virtual,
            height / virtual
        )
        val drawWidth = virtual * fitScale
        val drawHeight = virtual * fitScale
        val offsetX = (width - drawWidth) / 2f
        val offsetY = (height - drawHeight) / 2f
        val mirror =
            desiredFacing != loaded.defaultFacing

        canvas.save()
        canvas.translate(offsetX, offsetY)
        canvas.scale(fitScale, fitScale)

        if (mirror) {
            canvas.translate(virtual, 0f)
            canvas.scale(-1f, 1f)
        }

        applyWholeActorMotion(
            canvas,
            loaded.entityId,
            animationName,
            progress,
            virtual
        )

        if (loaded.entityId == "knight") {
            loaded.cape?.let {
                drawCapeMesh(
                    canvas,
                    it,
                    animationName,
                    progress
                )
            }
        }

        drawBodyMesh(
            canvas,
            loaded.body,
            loaded.entityId,
            animationName,
            progress
        )

        if (loaded.entityId == "knight") {
            loaded.shield?.let {
                drawShield(
                    canvas,
                    it,
                    animationName,
                    progress
                )
            }
            loaded.sword?.let {
                drawSword(
                    canvas,
                    it,
                    animationName,
                    progress
                )
            }
        }

        canvas.restore()

        if (
            motionEnabled &&
            (
                animationName == "idle" ||
                    elapsed < duration
                )
        ) {
            postInvalidateOnAnimation()
        }
    }

    private fun durationFor(
        entityId: String,
        name: String
    ): Long {
        return when (name) {
            "attack" ->
                if (entityId == "dire_wolf") 500L
                else 560L
            "hit" -> 340L
            "death" -> 900L
            else -> 1800L
        }
    }

    private fun applyWholeActorMotion(
        canvas: Canvas,
        entityId: String,
        name: String,
        progress: Float,
        size: Float
    ) {
        val pulse =
            sin(progress * PI.toFloat())
                .coerceAtLeast(0f)

        when (name) {
            "attack" -> {
                val direction =
                    if (entityId == "dire_wolf") -1f
                    else 1f

                canvas.translate(
                    direction * 20f * pulse,
                    -4f * pulse
                )

                if (entityId == "knight") {
                    canvas.rotate(
                        direction * 2.5f * pulse,
                        size * 0.52f,
                        size * 0.52f
                    )
                }
            }

            "hit" -> {
                val direction =
                    if (entityId == "dire_wolf") 1f
                    else -1f
                canvas.translate(
                    direction * 18f * pulse,
                    2f * pulse
                )
            }

            "death" -> {
                val eased =
                    smoothStep(progress)
                val angle =
                    if (entityId == "dire_wolf") -11f
                    else 13f

                canvas.translate(
                    0f,
                    74f * eased
                )
                canvas.rotate(
                    angle * eased,
                    size * 0.52f,
                    size * 0.72f
                )
            }
        }
    }

    private fun drawBodyMesh(
        canvas: Canvas,
        bitmap: Bitmap,
        entityId: String,
        name: String,
        progress: Float
    ) {
        val meshW = 18
        val meshH = 18
        val vertices =
            FloatArray((meshW + 1) * (meshH + 1) * 2)

        val phase =
            progress * PI.toFloat() * 2f
        val pulse =
            sin(progress * PI.toFloat())
                .coerceAtLeast(0f)

        var index = 0

        for (row in 0..meshH) {
            val v = row.toFloat() / meshH
            for (column in 0..meshW) {
                val u = column.toFloat() / meshW
                var x = u * bitmap.width
                var y = v * bitmap.height

                if (entityId == "knight") {
                    val torso =
                        influence(
                            u,
                            v,
                            0.53f,
                            0.34f,
                            0.22f,
                            0.22f
                        )
                    val rightArm =
                        influence(
                            u,
                            v,
                            0.72f,
                            0.39f,
                            0.22f,
                            0.22f
                        )
                    val leftArm =
                        influence(
                            u,
                            v,
                            0.33f,
                            0.37f,
                            0.20f,
                            0.22f
                        )
                    val hips =
                        influence(
                            u,
                            v,
                            0.53f,
                            0.52f,
                            0.23f,
                            0.16f
                        )

                    when (name) {
                        "idle" -> {
                            val breathe = sin(phase)
                            y -=
                                breathe * 3.2f * torso
                            x +=
                                breathe * 1.6f *
                                    (rightArm - leftArm)
                            y +=
                                cos(phase) * 1.2f * hips
                        }

                        "attack" -> {
                            x +=
                                pulse *
                                    (
                                        8f * torso +
                                            26f * rightArm
                                        )
                            y -=
                                pulse *
                                    (
                                        3f * torso +
                                            8f * rightArm
                                        )
                            x -=
                                pulse * 3f * leftArm
                        }

                        "hit" -> {
                            x -=
                                pulse *
                                    (
                                        16f * torso +
                                            10f *
                                            (rightArm + leftArm)
                                        )
                        }

                        "death" -> {
                            val eased =
                                smoothStep(progress)
                            y +=
                                eased *
                                    (
                                        34f * torso +
                                            18f * hips
                                        )
                            x -=
                                eased * 9f * torso
                        }
                    }
                } else if (
                    entityId == "dire_wolf"
                ) {
                    val head =
                        influence(
                            u,
                            v,
                            0.22f,
                            0.36f,
                            0.21f,
                            0.22f
                        )
                    val chest =
                        influence(
                            u,
                            v,
                            0.39f,
                            0.49f,
                            0.23f,
                            0.25f
                        )
                    val rear =
                        influence(
                            u,
                            v,
                            0.63f,
                            0.51f,
                            0.23f,
                            0.24f
                        )
                    val tail =
                        influence(
                            u,
                            v,
                            0.77f,
                            0.37f,
                            0.24f,
                            0.25f
                        )
                    val frontLegs =
                        influence(
                            u,
                            v,
                            0.34f,
                            0.70f,
                            0.23f,
                            0.28f
                        )

                    when (name) {
                        "idle" -> {
                            val breathe = sin(phase)
                            y -=
                                breathe * 3.8f * chest
                            y +=
                                sin(
                                    phase +
                                        u * 3.2f
                                ) *
                                    4.8f * tail
                            x +=
                                cos(phase) *
                                    1.8f * head
                        }

                        "attack" -> {
                            x -=
                                pulse *
                                    (
                                        42f * head +
                                            20f * chest +
                                            14f * frontLegs
                                        )
                            y -=
                                pulse *
                                    (
                                        10f * head +
                                            4f * chest
                                        )
                            x +=
                                pulse * 8f * rear
                            y +=
                                pulse * 4f * tail
                        }

                        "hit" -> {
                            x +=
                                pulse *
                                    (
                                        22f * head +
                                            12f * chest
                                        )
                            y +=
                                pulse * 5f * head
                        }

                        "death" -> {
                            val eased =
                                smoothStep(progress)
                            y +=
                                eased *
                                    (
                                        26f * head +
                                            34f * chest +
                                            42f * rear
                                        )
                            x +=
                                eased * 10f * head
                        }
                    }
                }

                vertices[index++] = x
                vertices[index++] = y
            }
        }

        canvas.drawBitmapMesh(
            bitmap,
            meshW,
            meshH,
            vertices,
            0,
            null,
            0,
            paint
        )
    }

    private fun drawCapeMesh(
        canvas: Canvas,
        bitmap: Bitmap,
        name: String,
        progress: Float
    ) {
        val meshW = 12
        val meshH = 16
        val vertices =
            FloatArray((meshW + 1) * (meshH + 1) * 2)
        val phase =
            progress * PI.toFloat() * 2f
        val pulse =
            sin(progress * PI.toFloat())
                .coerceAtLeast(0f)

        var index = 0

        for (row in 0..meshH) {
            val v = row.toFloat() / meshH

            for (column in 0..meshW) {
                val u = column.toFloat() / meshW
                var x = u * bitmap.width
                var y = v * bitmap.height
                val freeEdge =
                    ((v - 0.18f) / 0.82f)
                        .coerceIn(0f, 1f)

                x +=
                    sin(
                        phase +
                            v * 3.6f +
                            u * 1.2f
                    ) *
                        5.5f *
                        freeEdge

                y +=
                    cos(
                        phase * 0.72f +
                            u * 2.4f
                    ) *
                        2.4f *
                        freeEdge

                if (name == "attack") {
                    x -= pulse * 16f * freeEdge
                    y += pulse * 4f * freeEdge
                }

                if (name == "hit") {
                    x += pulse * 11f * freeEdge
                }

                if (name == "death") {
                    y +=
                        smoothStep(progress) *
                            26f *
                            freeEdge
                }

                vertices[index++] = x
                vertices[index++] = y
            }
        }

        // The cape asset was generated independently from the body,
        // so place it by its shoulder clasp instead of stretching it
        // across the entire actor canvas.
        canvas.save()
        canvas.translate(735f, 245f)
        canvas.scale(0.60f, 0.60f)
        canvas.translate(
            -bitmap.width * 0.82f,
            -bitmap.height * 0.14f
        )
        canvas.drawBitmapMesh(
            bitmap,
            meshW,
            meshH,
            vertices,
            0,
            null,
            0,
            paint
        )
        canvas.restore()
    }

    private fun drawSword(
        canvas: Canvas,
        bitmap: Bitmap,
        name: String,
        progress: Float
    ) {
        val pulse =
            sin(progress * PI.toFloat())
                .coerceAtLeast(0f)

        val actionRotation = when (name) {
            "attack" -> -72f * pulse
            "hit" -> 7f * pulse
            "death" ->
                34f * smoothStep(progress)
            else -> 0f
        }

        // Anchor the grip to the Knight's right hand. The independent
        // sword illustration intentionally gets a narrower X scale so
        // its oversized generated crossguard does not cover the torso.
        canvas.save()
        canvas.translate(1035f, 535f)
        canvas.rotate(-18f + actionRotation)
        canvas.scale(0.22f, 0.34f)
        canvas.translate(
            -bitmap.width * 0.50f,
            -bitmap.height * 0.18f
        )
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        canvas.restore()
    }

    private fun drawShield(
        canvas: Canvas,
        bitmap: Bitmap,
        name: String,
        progress: Float
    ) {
        val pulse =
            sin(progress * PI.toFloat())
                .coerceAtLeast(0f)

        val rotation = when (name) {
            "attack" -> 7f * pulse
            "hit" -> -10f * pulse
            "death" ->
                -22f * smoothStep(progress)
            else -> 0f
        }

        // Keep the shield as a rigid front layer and attach it to the
        // left forearm instead of rendering it at full actor size.
        canvas.save()
        canvas.translate(425f, 515f)
        canvas.rotate(-8f + rotation)
        canvas.scale(0.24f, 0.24f)
        canvas.translate(
            -bitmap.width * 0.50f,
            -bitmap.height * 0.50f
        )
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        canvas.restore()
    }

    private fun influence(
        u: Float,
        v: Float,
        cx: Float,
        cy: Float,
        rx: Float,
        ry: Float
    ): Float {
        val dx = (u - cx) / rx
        val dy = (v - cy) / ry
        return exp(
            -(dx * dx + dy * dy) * 1.7f
        )
    }

    private fun smoothStep(value: Float): Float {
        val t = value.coerceIn(0f, 1f)
        return t * t * (3f - 2f * t)
    }
}
