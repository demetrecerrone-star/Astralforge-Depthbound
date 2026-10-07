package com.demetrecerrone.astralforge

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.os.Bundle
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import kotlin.math.max

class SplashActivity : Activity() {

    private var leaving = false
    private var mediaPlayer: MediaPlayer? = null
    private var textureView: TextureView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val settings = runCatching {
            GameSettingsStore.load(this)
        }.getOrElse {
            GameSettingsStore.defaults()
        }

        val motionEnabled =
            !settings.reducedMotion && !settings.batterySaver

        val root = FrameLayout(this).apply {
            setBackgroundColor(Color.BLACK)
            isClickable = true
            isFocusable = true
            clipChildren = true
        }

        val poster = ImageView(this).apply {
            setImageResource(R.drawable.dabsky_splash)
            scaleType = ImageView.ScaleType.CENTER_CROP
            contentDescription = "DABSKY"
        }
        root.addView(
            poster,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        if (motionEnabled) {
            startCinematicIntro(root, poster)
        } else {
            root.postDelayed(
                { leaveSplash(root, false) },
                1200L
            )
        }

        root.setOnClickListener {
            leaveSplash(root, true)
        }

        setContentView(root)
        FullscreenUi.apply(this)
    }

    private fun startCinematicIntro(
        root: FrameLayout,
        poster: ImageView
    ) {
        val texture = TextureView(this).apply {
            alpha = 0f
            isOpaque = false
        }
        textureView = texture

        root.addView(
            texture,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        texture.surfaceTextureListener =
            object : TextureView.SurfaceTextureListener {
                override fun onSurfaceTextureAvailable(
                    surfaceTexture: SurfaceTexture,
                    width: Int,
                    height: Int
                ) {
                    preparePlayer(
                        root,
                        poster,
                        texture,
                        surfaceTexture
                    )
                }

                override fun onSurfaceTextureSizeChanged(
                    surfaceTexture: SurfaceTexture,
                    width: Int,
                    height: Int
                ) {
                    mediaPlayer?.let { player ->
                        applyCenterCrop(
                            texture,
                            player.videoWidth,
                            player.videoHeight
                        )
                    }
                }

                override fun onSurfaceTextureDestroyed(
                    surfaceTexture: SurfaceTexture
                ): Boolean {
                    releasePlayer()
                    return true
                }

                override fun onSurfaceTextureUpdated(
                    surfaceTexture: SurfaceTexture
                ) = Unit
            }
    }

    private fun preparePlayer(
        root: FrameLayout,
        poster: ImageView,
        texture: TextureView,
        surfaceTexture: SurfaceTexture
    ) {
        releasePlayer()

        val player = MediaPlayer()
        mediaPlayer = player

        runCatching {
            resources.openRawResourceFd(R.raw.dabsky_intro).use { afd ->
                player.setDataSource(
                    afd.fileDescriptor,
                    afd.startOffset,
                    afd.length
                )
            }

            val surface = Surface(surfaceTexture)
            player.setSurface(surface)
            surface.release()

            player.isLooping = false
            player.setVolume(1f, 1f)

            player.setOnVideoSizeChangedListener {
                    mediaPlayer,
                    videoWidth,
                    videoHeight ->
                applyCenterCrop(
                    texture,
                    videoWidth,
                    videoHeight
                )
            }

            player.setOnPreparedListener { prepared ->
                applyCenterCrop(
                    texture,
                    prepared.videoWidth,
                    prepared.videoHeight
                )
                prepared.start()
            }

            player.setOnInfoListener { _, what, _ ->
                if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                    texture.alpha = 1f
                    poster.visibility = View.GONE
                }
                false
            }

            player.setOnCompletionListener {
                leaveSplash(root, true)
            }

            player.setOnErrorListener { _, _, _ ->
                showPosterFallback(root, poster, texture)
                true
            }

            player.prepareAsync()
        }.onFailure {
            showPosterFallback(root, poster, texture)
        }
    }

    private fun applyCenterCrop(
        texture: TextureView,
        videoWidth: Int,
        videoHeight: Int
    ) {
        if (videoWidth <= 0 || videoHeight <= 0) return

        texture.post {
            val viewWidth = texture.width.toFloat()
            val viewHeight = texture.height.toFloat()
            if (viewWidth <= 0f || viewHeight <= 0f) return@post

            val scale = max(
                viewWidth / videoWidth.toFloat(),
                viewHeight / videoHeight.toFloat()
            )

            val scaledWidth = videoWidth * scale
            val scaledHeight = videoHeight * scale
            val scaleX = scaledWidth / viewWidth
            val scaleY = scaledHeight / viewHeight

            texture.setTransform(
                Matrix().apply {
                    setScale(
                        scaleX,
                        scaleY,
                        viewWidth / 2f,
                        viewHeight / 2f
                    )
                }
            )
        }
    }

    private fun showPosterFallback(
        root: FrameLayout,
        poster: ImageView,
        texture: TextureView
    ) {
        releasePlayer()
        texture.visibility = View.GONE
        poster.visibility = View.VISIBLE
        root.postDelayed(
            { leaveSplash(root, true) },
            1200L
        )
    }

    private fun leaveSplash(
        root: View,
        animate: Boolean
    ) {
        if (leaving) return
        leaving = true

        releasePlayer()

        fun openLogin() {
            startActivity(
                Intent(
                    this@SplashActivity,
                    MainActivity::class.java
                )
            )
            finish()
        }

        if (!animate) {
            openLogin()
            return
        }

        root.animate()
            .alpha(0f)
            .setDuration(300L)
            .withEndAction { openLogin() }
            .start()
    }

    private fun releasePlayer() {
        val player = mediaPlayer ?: return
        mediaPlayer = null
        runCatching { player.stop() }
        runCatching { player.reset() }
        runCatching { player.release() }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            FullscreenUi.apply(this)
        }
    }

    override fun onDestroy() {
        releasePlayer()
        textureView = null
        super.onDestroy()
    }
}
