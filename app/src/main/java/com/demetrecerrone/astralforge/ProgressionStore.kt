package com.demetrecerrone.astralforge

import android.content.Context
import kotlin.math.roundToInt

data class PlayerProgress(
    val level: Int = 1,
    val xp: Int = 0,
    val xpToNext: Int = 100,
    val depth: Int = 1,
    val gold: Int = 0,
    val astralShards: Int = 0,
    val energy: Int = 120,
    val maxEnergy: Int = 120,
    val rank: String = "E",
    val playerClass: String = "Unawakened",
    val title: String = "Depthbound Initiate",
    val strength: Int = 5,
    val vitality: Int = 5,
    val agility: Int = 5,
    val intelligence: Int = 5,
    val luck: Int = 5,
    val attributePoints: Int = 0
) {
    val maxHp: Int
        get() = 80 + vitality * 20 + level * 5

    val attack: Int
        get() = 8 + strength * 3 + agility

    val defense: Int
        get() = 5 + vitality * 2 + level

    val magicPower: Int
        get() = 5 + intelligence * 3

    val critChance: Int
        get() = (5 + luck * 0.8 + agility * 0.25).roundToInt().coerceAtMost(75)

    val powerRating: Int
        get() = maxHp / 5 + attack * 3 + defense * 2 + magicPower * 2 + critChance
}

object ProgressionStore {
    private const val PREFS = "astralforge_player_progress"

    fun load(context: Context): PlayerProgress {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return PlayerProgress(
            level = prefs.getInt("level", 1),
            xp = prefs.getInt("xp", 0),
            xpToNext = prefs.getInt("xpToNext", 100),
            depth = prefs.getInt("depth", 1),
            gold = prefs.getInt("gold", 0),
            astralShards = prefs.getInt("astralShards", 0),
            energy = prefs.getInt("energy", 120),
            maxEnergy = prefs.getInt("maxEnergy", 120),
            rank = prefs.getString("rank", "E") ?: "E",
            playerClass = prefs.getString("playerClass", "Unawakened") ?: "Unawakened",
            title = prefs.getString("title", "Depthbound Initiate") ?: "Depthbound Initiate",
            strength = prefs.getInt("strength", 5),
            vitality = prefs.getInt("vitality", 5),
            agility = prefs.getInt("agility", 5),
            intelligence = prefs.getInt("intelligence", 5),
            luck = prefs.getInt("luck", 5),
            attributePoints = prefs.getInt("attributePoints", 0)
        )
    }

    fun save(context: Context, value: PlayerProgress) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putInt("level", value.level)
            .putInt("xp", value.xp)
            .putInt("xpToNext", value.xpToNext)
            .putInt("depth", value.depth)
            .putInt("gold", value.gold)
            .putInt("astralShards", value.astralShards)
            .putInt("energy", value.energy)
            .putInt("maxEnergy", value.maxEnergy)
            .putString("rank", value.rank)
            .putString("playerClass", value.playerClass)
            .putString("title", value.title)
            .putInt("strength", value.strength)
            .putInt("vitality", value.vitality)
            .putInt("agility", value.agility)
            .putInt("intelligence", value.intelligence)
            .putInt("luck", value.luck)
            .putInt("attributePoints", value.attributePoints)
            .apply()
    }

    fun addXp(context: Context, amount: Int): PlayerProgress {
        var current = load(context)
        var xp = current.xp + amount.coerceAtLeast(0)
        var level = current.level
        var needed = current.xpToNext
        var points = current.attributePoints

        while (xp >= needed) {
            xp -= needed
            level += 1
            points += 5
            needed = 100 + (level - 1) * 40
        }

        current = current.copy(
            level = level,
            xp = xp,
            xpToNext = needed,
            attributePoints = points
        )
        save(context, current)
        return current
    }
}
