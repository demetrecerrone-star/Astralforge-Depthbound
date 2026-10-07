package com.demetrecerrone.astralforge

import android.content.Context
import android.media.MediaPlayer

object AppMusicManager {

    private var player: MediaPlayer? = null

    @Synchronized
    fun sync(context: Context) {
        val enabled = GameSettingsStore.load(context).music
        if (!enabled) {
            pause()
            return
        }

        if (player == null) {
            player = MediaPlayer.create(
                context.applicationContext,
                R.raw.hub_theme
            )?.apply {
                isLooping = true
                setVolume(0.45f, 0.45f)
            }
        }

        player?.let {
            if (!it.isPlaying) {
                it.start()
            }
        }
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
