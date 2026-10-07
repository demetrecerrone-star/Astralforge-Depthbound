package com.demetrecerrone.astralforge

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.util.Locale

object EntitySpriteStore {

    data class EntitySpriteSet(
        val id: String,
        val displayName: String,
        val idle: Bitmap?,
        val attackFrames: List<Bitmap>,
        val hitFrames: List<Bitmap>,
        val deathFrames: List<Bitmap>
    )

    private val bitmapCache = mutableMapOf<String, Bitmap>()

    fun characterIdForClass(playerClass: String): String {
        val value = playerClass.lowercase(Locale.US)
        return when {
            "dark knight" in value -> "dark_knight"
            "necromancer" in value -> "necromancer"
            "paladin" in value -> "paladin"
            "ranger" in value || "archer" in value -> "ranger"
            "mage" in value || "wizard" in value || "sorcer" in value -> "mage"
            "rogue" in value || "assassin" in value -> "rogue"
            "barbar" in value || "berserk" in value -> "barbarian"
            else -> "knight"
        }
    }

    fun monsterIdForDepth(depth: Int): String {
        return when (((depth.coerceAtLeast(1) - 1) % 5)) {
            0 -> "blue_slime"
            1 -> "goblin_raider"
            2 -> "skeleton_warrior"
            3 -> "dire_wolf"
            else -> "dungeon_boss"
        }
    }

    fun isBossDepth(depth: Int): Boolean =
        depth.coerceAtLeast(1) % 5 == 0

    fun loadCharacter(context: Context, id: String): EntitySpriteSet {
        val display = when (id) {
            "barbarian" -> "Barbarian"
            "rogue" -> "Rogue"
            "mage" -> "Crystal Mage"
            "ranger" -> "Ranger"
            "paladin" -> "Paladin"
            "necromancer" -> "Necromancer"
            "dark_knight" -> "Dark Knight"
            else -> "Knight"
        }
        return loadEntity(context, "characters", id, display, 8)
    }

    fun loadMonster(context: Context, id: String): EntitySpriteSet {
        val display = when (id) {
            "blue_slime" -> "Blue Slime"
            "goblin_raider" -> "Goblin Raider"
            "skeleton_warrior" -> "Skeleton Warrior"
            "dire_wolf" -> "Dire Wolf"
            "dungeon_boss" -> "Depth Warden"
            else -> "Depth Creature"
        }
        return loadEntity(context, "monsters", id, display, 4)
    }

    private fun loadEntity(
        context: Context,
        category: String,
        id: String,
        displayName: String,
        actionCount: Int
    ): EntitySpriteSet {
        val base = "astral_sprites/" + category + "/" + id
        return EntitySpriteSet(
            id = id,
            displayName = displayName,
            idle = loadBitmap(context, base + "/battle_idle.png"),
            attackFrames = loadSequence(context, base + "/attack", "attack", actionCount),
            hitFrames = loadSequence(context, base + "/hit", "hit", actionCount),
            deathFrames = loadSequence(context, base + "/death", "death", actionCount)
        )
    }

    private fun loadSequence(
        context: Context,
        folder: String,
        prefix: String,
        count: Int
    ): List<Bitmap> {
        return (1..count).mapNotNull { index ->
            loadBitmap(
                context,
                folder + "/" + prefix + "_" +
                    index.toString().padStart(2, '0') + ".png"
            )
        }
    }

    @Synchronized
    private fun loadBitmap(context: Context, path: String): Bitmap? {
        bitmapCache[path]?.let { return it }
        return runCatching {
            context.assets.open(path).use { input ->
                BitmapFactory.decodeStream(input)
            }
        }.getOrNull()?.also {
            bitmapCache[path] = it
        }
    }
}
