package com.demetrecerrone.astralforge

import android.content.Context
import android.media.MediaPlayer

object AppMusicManager {

    private var player: MediaPlayer? = null

    @Synchronized
    fun sync(context: Context) {
        val settings = GameSettingsStore.load(context)
        if (!settings.music) {
            pause()
            return
        }

        if (player == null) {
            player = MediaPlayer.create(
                context.applicationContext,
                R.raw.hub_theme
            )?.apply {
                isLooping = true
            }
        }

        applyVolume(settings.musicVolume)

        player?.let {
            if (!it.isPlaying) {
                it.start()
            }
        }
    }

    @Synchronized
    fun setVolume(percent: Int) {
        applyVolume(percent)
    }

    private fun applyVolume(percent: Int) {
        val value = percent.coerceIn(0, 100) / 100f
        player?.setVolume(value, value)
    }

    @Synchronized
    fun pause() {
        player?.let {
            if (it.isPlaying) {
                it.pause()
            }
        }
    }

    @Synchronized
    fun release() {
        player?.release()
        player = null
    }
}
