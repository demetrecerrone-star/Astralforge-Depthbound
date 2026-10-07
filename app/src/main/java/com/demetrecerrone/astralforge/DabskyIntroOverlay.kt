package com.demetrecerrone.astralforge

import android.app.Activity
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import kotlin.math.max

object DabskyIntroOverlay {

    fun show(activity: Activity) {
        val host = activity.findViewById<ViewGroup>(android.R.id.content) ?: return

        val overlay = FrameLayout(activity).apply {
            setBackgroundColor(Color.BLACK)
            isClickable = true
            isFocusable = true
            alpha = 1f
        }

        val texture = TextureView(activity).apply {
            isOpaque = false
        }

        overlay.addView(
            texture,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        host.addView(
            overlay,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        var player: MediaPlayer? = null
        var finished = false

        fun removeOverlay() {
            if (finished) return
            finished = true

            val current = player
            player = null
            runCatching { current?.stop() }
            runCatching { current?.reset() }
            runCatching { current?.release() }

            overlay.animate()
                .alpha(0f)
                .setDuration(650L)
                .withEndAction {
                    (overlay.parent as? ViewGroup)?.removeView(overlay)
                }
                .start()
        }

        fun applyCenterCrop(
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

                val scaleX = (videoWidth * scale) / viewWidth
                val scaleY = (videoHeight * scale) / viewHeight

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

        overlay.setOnClickListener {
            removeOverlay()
        }

        texture.surfaceTextureListener =
            object : TextureView.SurfaceTextureListener {
                override fun onSurfaceTextureAvailable(
                    surfaceTexture: SurfaceTexture,
                    width: Int,
                    height: Int
                ) {
                    val mediaPlayer = MediaPlayer()
                    player = mediaPlayer

                    runCatching {
                        activity.resources
                            .openRawResourceFd(R.raw.dabsky_intro)
                            .use { afd ->
                                mediaPlayer.setDataSource(
                                    afd.fileDescriptor,
                                    afd.startOffset,
                                    afd.length
                                )
                            }

                        val surface = Surface(surfaceTexture)
                        mediaPlayer.setSurface(surface)
                        surface.release()

                        mediaPlayer.isLooping = false
                        mediaPlayer.setVolume(1f, 1f)

                        mediaPlayer.setOnVideoSizeChangedListener { _, w, h ->
                            applyCenterCrop(w, h)
                        }

                        mediaPlayer.setOnPreparedListener {
                            applyCenterCrop(
                                it.videoWidth,
                                it.videoHeight
                            )
                            it.start()
                        }

                        mediaPlayer.setOnCompletionListener {
                            removeOverlay()
                        }

                        mediaPlayer.setOnErrorListener { _, _, _ ->
                            removeOverlay()
                            true
                        }

                        mediaPlayer.prepareAsync()
                    }.onFailure {
                        removeOverlay()
                    }
                }

                override fun onSurfaceTextureSizeChanged(
                    surfaceTexture: SurfaceTexture,
                    width: Int,
                    height: Int
                ) {
                    player?.let {
                        applyCenterCrop(
                            it.videoWidth,
                            it.videoHeight
                        )
                    }
                }

                override fun onSurfaceTextureDestroyed(
                    surfaceTexture: SurfaceTexture
                ): Boolean {
                    val current = player
                    player = null
                    runCatching { current?.stop() }
                    runCatching { current?.reset() }
                    runCatching { current?.release() }
                    return true
                }

                override fun onSurfaceTextureUpdated(
                    surfaceTexture: SurfaceTexture
                ) = Unit
            }
    }
}
