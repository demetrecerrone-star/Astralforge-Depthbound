package com.demetrecerrone.astralforge

import android.content.Context

data class PortraitOption(
    val id: String,
    val label: String,
    val category: String
)

object PortraitStore {
    private const val PREFS = "astralforge_portrait_selection"
    private const val KEY_SELECTED = "selected_portrait"
    const val DEFAULT_ID = "skeleton"

    private val options = listOf(
        PortraitOption("skeleton", "Skeleton", "Monster"),
        PortraitOption("warrior_m", "Warrior", "Male"),
        PortraitOption("mage_m", "Arcanist", "Male"),
        PortraitOption("elf_m", "Elf", "Male"),
        PortraitOption("ranger_f", "Ranger", "Female"),
        PortraitOption("sorceress_f", "Sorceress", "Female"),
        PortraitOption("knight_f", "Knight", "Female"),
        PortraitOption("orc", "Orc", "Monster"),
        PortraitOption("goblin", "Goblin", "Monster"),
        PortraitOption("demon", "Demon", "Monster"),
        PortraitOption("wolfkin", "Wolfkin", "Monster"),
        PortraitOption("lich", "Lich", "Monster"),
        PortraitOption("dragonkin", "Dragonkin", "Monster"),
        PortraitOption("masked", "Masked", "Other")
    )

    fun all(): List<PortraitOption> = options

    fun get(id: String): PortraitOption? = options.firstOrNull { it.id == id }

    fun selectedId(context: Context): String {
        val saved = AccountScopedStorage.preferences(context, PREFS)
            .getString(KEY_SELECTED, DEFAULT_ID)
            ?: DEFAULT_ID
        return if (get(saved) != null) saved else DEFAULT_ID
    }

    fun select(context: Context, id: String): Boolean {
        if (get(id) == null) return false
        AccountScopedStorage.preferences(context, PREFS)
            .edit()
            .putString(KEY_SELECTED, id)
            .apply()
        return true
    }
}
