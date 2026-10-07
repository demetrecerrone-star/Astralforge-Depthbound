package com.demetrecerrone.astralforge

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.VideoView

class SplashActivity : Activity() {

    private var leaving = false
    private var videoView: VideoView? = null

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
            val video = VideoView(this).apply {
                setBackgroundColor(Color.TRANSPARENT)
                setVideoURI(
                    Uri.parse(
                        "android.resource://" +
                            packageName +
                            "/" +
                            R.raw.dabsky_intro
                    )
                )
                setOnPreparedListener { mediaPlayer ->
                    mediaPlayer.isLooping = false
                    mediaPlayer.setVolume(0.86f, 0.86f)
                    poster.visibility = View.GONE
                    start()
                }
                setOnCompletionListener {
                    leaveSplash(root, true)
                }
                setOnErrorListener { _, _, _ ->
                    visibility = View.GONE
                    poster.visibility = View.VISIBLE
                    root.postDelayed(
                        { leaveSplash(root, true) },
                        1100L
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

    private fun leaveSplash(
        root: View,
        animate: Boolean
    ) {
        if (leaving) return
        leaving = true

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

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            FullscreenUi.apply(this)
        }
    }

    override fun onDestroy() {
        videoView?.stopPlayback()
        videoView = null
        super.onDestroy()
    }
}
