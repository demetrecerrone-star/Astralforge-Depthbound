package com.demetrecerrone.astralforge

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

object AppHaptics {

    fun tap(context: Context) {
        if (!GameSettingsStore.load(context).vibration) return

        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    18L,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(18L)
        }
    }
}
