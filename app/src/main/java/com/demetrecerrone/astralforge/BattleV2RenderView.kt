package com.demetrecerrone.astralforge

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.Choreographer
import android.view.SurfaceHolder
import android.view.SurfaceView
import kotlin.math.min

class BattleV2RenderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : SurfaceView(context, attrs),
    SurfaceHolder.Callback,
    Choreographer.FrameCallback {

    private data class ActorState(
        var sprites: EntitySpriteStore.EntitySpriteSet? = null,
        var animation: BattleV2Animation = BattleV2Animation.IDLE,
        var animationStartMs: Long = 0L
    )

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val player = ActorState()
    private val enemy = ActorState()

    private var running = false
    private var surfaceWidth = 1
    private var surfaceHeight = 1
    private var lastFrameNanos = 0L
    private var targetFrameNanos = 16_666_667L

    private var backgroundSource: Bitmap? = null
    private var backgroundScaled: Bitmap? = null

    init {
        holder.addCallback(this)
        isFocusable = false
        isClickable = false
    }

    fun setPreferredFps(value: String, batterySaver: Boolean) {
        targetFrameNanos =
            if (batterySaver || value == "30") {
                33_333_333L
            } else {
                16_666_667L
            }
    }

    fun setActors(
        playerSprites: EntitySpriteStore.EntitySpriteSet,
        enemySprites: EntitySpriteStore.EntitySpriteSet
    ) {
        player.sprites = playerSprites
        enemy.sprites = enemySprites
        player.animation = BattleV2Animation.IDLE
        enemy.animation = BattleV2Animation.IDLE
        player.animationStartMs = System.currentTimeMillis()
        enemy.animationStartMs = System.currentTimeMillis()
        renderOnce()
    }

    fun playPlayer(animation: BattleV2Animation) {
        player.animation = animation
        player.animationStartMs = System.currentTimeMillis()
    }

    fun playEnemy(animation: BattleV2Animation) {
        enemy.animation = animation
        enemy.animationStartMs = System.currentTimeMillis()
    }

    fun resetActors() {
        playPlayer(BattleV2Animation.IDLE)
        playEnemy(BattleV2Animation.IDLE)
    }

    fun release() {
        running = false
        Choreographer.getInstance().removeFrameCallback(this)
        backgroundScaled?.recycle()
        backgroundScaled = null
        backgroundSource?.recycle()
        backgroundSource = null
    }

    override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
        ensureBackground()
        running = true
        Choreographer.getInstance().postFrameCallback(this)
    }

    override fun surfaceChanged(
        surfaceHolder: SurfaceHolder,
        format: Int,
        width: Int,
        height: Int
    ) {
        surfaceWidth = width.coerceAtLeast(1)
        surfaceHeight = height.coerceAtLeast(1)
        rebuildScaledBackground()
        renderOnce()
    }

    override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {
        running = false
        Choreographer.getInstance().removeFrameCallback(this)
    }

    override fun doFrame(frameTimeNanos: Long) {
        if (!running) return

        if (
            lastFrameNanos == 0L ||
            frameTimeNanos - lastFrameNanos >= targetFrameNanos
        ) {
            lastFrameNanos = frameTimeNanos
            renderOnce()
        }

        if (running) {
            Choreographer.getInstance().postFrameCallback(this)
        }
    }

    private fun renderOnce() {
        if (!holder.surface.isValid) return

        val canvas = runCatching { holder.lockCanvas() }.getOrNull()
            ?: return

        try {
            drawFrame(canvas)
        } finally {
            runCatching { holder.unlockCanvasAndPost(canvas) }
        }
    }

    private fun drawFrame(canvas: Canvas) {
        val w = canvas.width.toFloat()
        val h = canvas.height.toFloat()

        val bg = backgroundScaled
        if (bg != null && !bg.isRecycled) {
            canvas.drawBitmap(bg, 0f, 0f, paint)
        } else {
            paint.shader =
                LinearGradient(
                    0f,
                    0f,
                    0f,
                    h,
                    intArrayOf(
                        Color.rgb(8, 9, 25),
                        Color.rgb(18, 12, 35),
                        Color.rgb(4, 5, 14)
                    ),
                    null,
                    Shader.TileMode.CLAMP
                )
            canvas.drawRect(0f, 0f, w, h, paint)
            paint.shader = null
        }

        paint.color = Color.argb(88, 4, 3, 18)
        canvas.drawRect(0f, 0f, w, h, paint)

        shadowPaint.color = Color.argb(125, 0, 0, 0)
        canvas.drawOval(
            RectF(
                w * 0.12f,
                h * 0.67f,
                w * 0.47f,
                h * 0.79f
            ),
            shadowPaint
        )
        canvas.drawOval(
            RectF(
                w * 0.54f,
                h * 0.65f,
                w * 0.89f,
                h * 0.78f
            ),
            shadowPaint
        )

        drawActor(
            canvas = canvas,
            actor = player,
            centerX = w * 0.30f,
            baselineY = h * 0.73f,
            maxWidth = w * 0.31f,
            maxHeight = h * 0.47f
        )

        drawActor(
            canvas = canvas,
            actor = enemy,
            centerX = w * 0.70f,
            baselineY = h * 0.71f,
            maxWidth = w * 0.31f,
            maxHeight = h * 0.45f
        )

        paint.color = Color.argb(190, 220, 199, 255)
        paint.textSize = min(w, h) * 0.025f
        paint.textAlign = Paint.Align.CENTER
        paint.isFakeBoldText = true
        canvas.drawText(
            "BATTLE V2 PREVIEW",
            w * 0.5f,
            h * 0.11f,
            paint
        )
        paint.isFakeBoldText = false
    }

    private fun drawActor(
        canvas: Canvas,
        actor: ActorState,
        centerX: Float,
        baselineY: Float,
        maxWidth: Float,
        maxHeight: Float
    ) {
        val bitmap = currentFrame(actor) ?: return
        if (bitmap.width <= 0 || bitmap.height <= 0) return

        val scale =
            min(
                maxWidth / bitmap.width.toFloat(),
                maxHeight / bitmap.height.toFloat()
            )

        val drawWidth = bitmap.width * scale
        val drawHeight = bitmap.height * scale

        val destination =
            RectF(
                centerX - drawWidth / 2f,
                baselineY - drawHeight,
                centerX + drawWidth / 2f,
                baselineY
            )

        canvas.drawBitmap(bitmap, null, destination, paint)
    }

    private fun currentFrame(actor: ActorState): Bitmap? {
        val sprites = actor.sprites ?: return null
        val animation = actor.animation
        val spec = BattleV2AnimationTimeline.spec(animation)

        if (
            animation != BattleV2Animation.IDLE &&
            spec.returnToIdle &&
            System.currentTimeMillis() - actor.animationStartMs >=
                spec.durationMs
        ) {
            actor.animation = BattleV2Animation.IDLE
            actor.animationStartMs = System.currentTimeMillis()
        }

        val frames =
            when (actor.animation) {
                BattleV2Animation.ATTACK -> sprites.attackFrames
                BattleV2Animation.HIT -> sprites.hitFrames
                BattleV2Animation.DEATH -> sprites.deathFrames
                BattleV2Animation.IDLE,
                BattleV2Animation.VICTORY -> emptyList()
            }

        if (frames.isEmpty()) return sprites.idle

        val elapsed =
            (System.currentTimeMillis() - actor.animationStartMs)
                .coerceAtLeast(0L)

        val frameMs =
            BattleV2AnimationTimeline
                .spec(actor.animation)
                .frameMs
                .coerceAtLeast(1L)

        val index =
            (elapsed / frameMs)
                .toInt()
                .coerceIn(0, frames.lastIndex)

        return frames[index]
    }

    private fun ensureBackground() {
        if (backgroundSource != null) return

        backgroundSource =
            runCatching {
                BitmapFactory.decodeResource(
                    resources,
                    R.drawable.file_000000003c3881f5b27141213085d016
                )
            }.getOrNull()

        rebuildScaledBackground()
    }

    private fun rebuildScaledBackground() {
        val source = backgroundSource ?: return
        if (surfaceWidth <= 1 || surfaceHeight <= 1) return

        backgroundScaled?.recycle()
        backgroundScaled =
            runCatching {
                Bitmap.createScaledBitmap(
                    source,
                    surfaceWidth,
                    surfaceHeight,
                    true
                )
            }.getOrNull()
    }
}
