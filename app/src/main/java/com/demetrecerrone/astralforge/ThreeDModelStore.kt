package com.demetrecerrone.astralforge

object ThreeDModelStore {

    private const val PROTOTYPE_HERO =
        "models/prototype_knight.glb.b64"

    private const val PROTOTYPE_ENEMY =
        "models/prototype_goblin.glb.b64"

    private val heroIds = setOf(
        "knight",
        "barbarian",
        "rogue",
        "mage",
        "ranger",
        "paladin",
        "necromancer",
        "dark_knight"
    )

    private val enemyIds = setOf(
        "blue_slime",
        "goblin_raider",
        "skeleton_warrior",
        "dire_wolf",
        "dungeon_boss"
    )

    fun modelAssetFor(entityId: String): String? {
        return when (entityId) {
            in heroIds -> PROTOTYPE_HERO
            in enemyIds -> PROTOTYPE_ENEMY
            else -> null
        }
    }
}
