package com.demetrecerrone.astralforge

import java.util.Locale

object BattleActorFactory {

    fun playerRigId(playerClass: String): String {
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

    fun enemyRigId(depth: Int): String {
        return when (((depth.coerceAtLeast(1) - 1) % 5)) {
            0 -> "blue_slime"
            1 -> "goblin_raider"
            2 -> "skeleton_warrior"
            3 -> "dire_wolf"
            else -> "dungeon_boss"
        }
    }

    fun enemyDisplayName(entityId: String): String {
        return when (entityId) {
            "blue_slime" -> "Blue Slime"
            "goblin_raider" -> "Goblin Raider"
            "skeleton_warrior" -> "Skeleton Warrior"
            "dire_wolf" -> "Dire Wolf"
            "dungeon_boss" -> "Astral Warden"
            else -> "Depth Creature"
        }
    }

    fun playerFacing(): String = "right"

    fun enemyFacing(): String = "left"
}
