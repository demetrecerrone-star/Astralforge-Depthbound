package com.demetrecerrone.astralforge

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

internal object DabskyIntroSound {
    fun play(): AudioTrack? = runCatching {
        val sampleRate = 22_050
        val duration = 4.5
        val frameCount = (sampleRate * duration).toInt()
        val samples = ShortArray(frameCount)

        for (index in samples.indices) {
            val t = index.toDouble() / sampleRate.toDouble()

            val fadeIn = (t / 0.55).coerceIn(0.0, 1.0)
            val fadeOut = ((duration - t) / 0.50).coerceIn(0.0, 1.0)
            val envelope = fadeIn * fadeOut

            var value =
                0.050 * sin(2.0 * PI * 55.0 * t) +
                0.020 * sin(2.0 * PI * 82.4 * t)

            if (t < 1.5) {
                value +=
                    0.025 * (t / 1.5) *
                    sin(2.0 * PI * (110.0 + 38.0 * t) * t)
            }

            val impact = t - 1.43
            if (impact >= 0.0) {
                value +=
                    0.29 * exp(-impact * 5.2) *
                    sin(2.0 * PI * 58.0 * impact)
            }

            val shimmer = t - 2.15
            if (shimmer >= 0.0) {
                value +=
                    0.026 * exp(-shimmer * 1.7) *
                    sin(2.0 * PI * 440.0 * shimmer)
            }

            val normalized =
                (value * envelope).coerceIn(-1.0, 1.0)

            samples[index] =
                (normalized * Short.MAX_VALUE)
                    .toInt()
                    .toShort()
        }

        @Suppress("DEPRECATION")
        AudioTrack(
            AudioManager.STREAM_MUSIC,
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            samples.size * 2,
            AudioTrack.MODE_STATIC
        ).apply {
            write(samples, 0, samples.size)
            setVolume(0.42f)
            play()
        }
    }.getOrNull()
}
