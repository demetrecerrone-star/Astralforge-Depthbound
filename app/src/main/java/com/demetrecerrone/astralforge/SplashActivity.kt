package com.demetrecerrone.astralforge

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.VideoView
import kotlin.math.max
import kotlin.math.roundToInt

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
        val video = CropVideoView(this).apply {
            setBackgroundColor(Color.BLACK)
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
                mediaPlayer.setVolume(1f, 1f)
                setSourceSize(
                    mediaPlayer.videoWidth,
                    mediaPlayer.videoHeight
                )
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
            ).apply {
                gravity = android.view.Gravity.CENTER
            }
        )
    }

    private fun leaveSplash(
        root: View,
        animate: Boolean
    ) {
        if (leaving) return
        leaving = true

        videoView?.stopPlayback()

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

    private class CropVideoView(
        context: Context
    ) : VideoView(context) {

        private var sourceWidth = 16
        private var sourceHeight = 9

        fun setSourceSize(width: Int, height: Int) {
            if (width <= 0 || height <= 0) return
            sourceWidth = width
            sourceHeight = height
            requestLayout()
        }

        override fun onMeasure(
            widthMeasureSpec: Int,
            heightMeasureSpec: Int
        ) {
            val parentWidth =
                View.MeasureSpec.getSize(widthMeasureSpec)
            val parentHeight =
                View.MeasureSpec.getSize(heightMeasureSpec)

            if (parentWidth <= 0 || parentHeight <= 0) {
                super.onMeasure(
                    widthMeasureSpec,
                    heightMeasureSpec
                )
                return
            }

            val scale = max(
                parentWidth.toFloat() / sourceWidth.toFloat(),
                parentHeight.toFloat() / sourceHeight.toFloat()
            )

            setMeasuredDimension(
                (sourceWidth * scale).roundToInt(),
                (sourceHeight * scale).roundToInt()
            )
        }
    }
}
