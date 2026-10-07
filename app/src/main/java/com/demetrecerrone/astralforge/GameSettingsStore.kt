package com.demetrecerrone.astralforge

import android.content.Context

data class GameSettings(
    val music: Boolean = true,
    val soundEffects: Boolean = true,
    val vibration: Boolean = true,
    val battleEffects: Boolean = true,
    val damageNumbers: Boolean = true,
    val reducedMotion: Boolean = false,
    val notifications: Boolean = true
)

object GameSettingsStore {
    private const val PREFS = "astralforge_game_settings"

    fun load(context: Context): GameSettings {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return GameSettings(
            music = p.getBoolean("music", true),
            soundEffects = p.getBoolean("soundEffects", true),
            vibration = p.getBoolean("vibration", true),
            battleEffects = p.getBoolean("battleEffects", true),
            damageNumbers = p.getBoolean("damageNumbers", true),
            reducedMotion = p.getBoolean("reducedMotion", false),
            notifications = p.getBoolean("notifications", true)
        )
    }

    fun save(context: Context, settings: GameSettings) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean("music", settings.music)
            .putBoolean("soundEffects", settings.soundEffects)
            .putBoolean("vibration", settings.vibration)
            .putBoolean("battleEffects", settings.battleEffects)
            .putBoolean("damageNumbers", settings.damageNumbers)
            .putBoolean("reducedMotion", settings.reducedMotion)
            .putBoolean("notifications", settings.notifications)
            .apply()
    }
}
