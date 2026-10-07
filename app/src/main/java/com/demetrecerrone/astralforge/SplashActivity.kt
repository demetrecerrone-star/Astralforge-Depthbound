package com.demetrecerrone.astralforge

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.media.AudioTrack
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.VideoView

class SplashActivity : Activity() {

    private var leaving = false
    private var videoView: VideoView? = null
    private var introSound: AudioTrack? = null

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
        val videoFile = runCatching {
            EmbeddedDabskyVideo.materialize(this)
        }.getOrNull()

        if (videoFile == null) {
            root.postDelayed(
                { leaveSplash(root, true) },
                1200L
            )
            return
        }

        val video = VideoView(this).apply {
            setBackgroundColor(Color.BLACK)
            setVideoURI(Uri.fromFile(videoFile))
            setOnPreparedListener { mediaPlayer ->
                mediaPlayer.isLooping = false
                mediaPlayer.setVolume(0f, 0f)
                poster.visibility = View.GONE
                introSound = DabskyIntroSound.play()
                start()
            }
            setOnCompletionListener {
                leaveSplash(root, true)
            }
            setOnErrorListener { _, _, _ ->
                visibility = View.GONE
                poster.visibility = View.VISIBLE
                stopIntroSound()
                root.postDelayed(
                    { leaveSplash(root, true) },
                    1000L
                )
                true
            }
        }

        videoView = video
        root.addView(
            video,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )
    }

    private fun leaveSplash(
        root: View,
        animate: Boolean
    ) {
        if (leaving) return
        leaving = true

        videoView?.stopPlayback()
        stopIntroSound()

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

    private fun stopIntroSound() {
        val sound = introSound ?: return
        introSound = null
        runCatching { sound.stop() }
        runCatching { sound.release() }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            FullscreenUi.apply(this)
        }
    }

    override fun onDestroy() {
        videoView?.stopPlayback()
        videoView = null
        stopIntroSound()
        super.onDestroy()
    }
}
