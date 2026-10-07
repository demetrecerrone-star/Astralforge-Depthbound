package com.demetrecerrone.astralforge

import android.content.Context

data class GameSettings(
    val music: Boolean = true,
    val musicVolume: Int = 45,
    val soundEffects: Boolean = true,
    val soundEffectsVolume: Int = 70,
    val vibration: Boolean = true,
    val battleEffects: Boolean = true,
    val damageNumbers: Boolean = true,
    val reducedMotion: Boolean = false,
    val notifications: Boolean = true,
    val visualQuality: String = "HIGH",
    val particleDensity: Int = 75,
    val batterySaver: Boolean = false,
    val fpsPreference: String = "SYSTEM"
)

object GameSettingsStore {
    private const val PREFS = "astralforge_game_settings"
    private val validQualities = setOf("LOW", "MEDIUM", "HIGH")
    private val validFps = setOf("SYSTEM", "30", "60")

    fun defaults(): GameSettings = GameSettings()

    fun load(context: Context): GameSettings {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val quality = p.getString("visualQuality", "HIGH") ?: "HIGH"
        val fps = p.getString("fpsPreference", "SYSTEM") ?: "SYSTEM"

        return GameSettings(
            music = p.getBoolean("music", true),
            musicVolume = p.getInt("musicVolume", 45).coerceIn(0, 100),
            soundEffects = p.getBoolean("soundEffects", true),
            soundEffectsVolume = p.getInt("soundEffectsVolume", 70).coerceIn(0, 100),
            vibration = p.getBoolean("vibration", true),
            battleEffects = p.getBoolean("battleEffects", true),
            damageNumbers = p.getBoolean("damageNumbers", true),
            reducedMotion = p.getBoolean("reducedMotion", false),
            notifications = p.getBoolean("notifications", true),
            visualQuality = if (quality in validQualities) quality else "HIGH",
            particleDensity = p.getInt("particleDensity", 75).coerceIn(0, 100),
            batterySaver = p.getBoolean("batterySaver", false),
            fpsPreference = if (fps in validFps) fps else "SYSTEM"
        )
    }

    fun save(context: Context, settings: GameSettings) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean("music", settings.music)
            .putInt("musicVolume", settings.musicVolume.coerceIn(0, 100))
            .putBoolean("soundEffects", settings.soundEffects)
            .putInt("soundEffectsVolume", settings.soundEffectsVolume.coerceIn(0, 100))
            .putBoolean("vibration", settings.vibration)
            .putBoolean("battleEffects", settings.battleEffects)
            .putBoolean("damageNumbers", settings.damageNumbers)
            .putBoolean("reducedMotion", settings.reducedMotion)
            .putBoolean("notifications", settings.notifications)
            .putString(
                "visualQuality",
                if (settings.visualQuality in validQualities) settings.visualQuality else "HIGH"
            )
            .putInt("particleDensity", settings.particleDensity.coerceIn(0, 100))
            .putBoolean("batterySaver", settings.batterySaver)
            .putString(
                "fpsPreference",
                if (settings.fpsPreference in validFps) settings.fpsPreference else "SYSTEM"
            )
            .apply()
    }

    fun reset(context: Context): GameSettings {
        val defaults = defaults()
        save(context, defaults)
        return defaults
    }
}
