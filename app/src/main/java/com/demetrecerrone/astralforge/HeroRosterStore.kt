package com.demetrecerrone.astralforge

import android.content.Context

data class HeroDefinition(
    val id: String,
    val displayName: String,
    val heroClass: String,
    val role: String,
    val affinity: String,
    val rarity: String,
    val rarityStars: Int,
    val powerOffset: Int,
    val skillName: String,
    val skillText: String,
    val lore: String
)

object HeroRosterStore {
    private const val PREFS = "astralforge_hero_roster"
    private const val KEY_ACTIVE = "active_hero_id"
    private const val KEY_FAVORITES = "favorite_hero_ids"
    private const val DEFAULT_HERO = "knight"

    private val heroes = listOf(
        HeroDefinition(
            id = "knight",
            displayName = "Auren Vale",
            heroClass = "Knight",
            role = "Vanguard",
            affinity = "Astral",
            rarity = "Epic",
            rarityStars = 4,
            powerOffset = 0,
            skillName = "Starbreaker Guard",
            skillText = "Raises a radiant guard before answering with a heavy counterstrike.",
            lore = "A disciplined citadel swordsman who refuses to yield ground to the Depth."
        ),
        HeroDefinition(
            id = "paladin",
            displayName = "Seraphine Lux",
            heroClass = "Paladin",
            role = "Guardian",
            affinity = "Radiant",
            rarity = "Legendary",
            rarityStars = 5,
            powerOffset = 45,
            skillName = "Dawn Aegis",
            skillText = "Channels astral light into a crushing shield strike and protective ward.",
            lore = "A sworn protector whose oath burns brightest where the darkness is deepest."
        ),
        HeroDefinition(
            id = "rogue",
            displayName = "Nyra Vex",
            heroClass = "Rogue",
            role = "Assassin",
            affinity = "Umbral",
            rarity = "Epic",
            rarityStars = 4,
            powerOffset = 25,
            skillName = "Veilstep",
            skillText = "Slips through the veil, striking a vulnerable point before the enemy can react.",
            lore = "A silent delver who treats every shadow as a doorway and every opening as a weapon."
        ),
        HeroDefinition(
            id = "mage",
            displayName = "Vaelis Ember",
            heroClass = "Mage",
            role = "Burst Mage",
            affinity = "Arcane",
            rarity = "Legendary",
            rarityStars = 5,
            powerOffset = 50,
            skillName = "Astral Rupture",
            skillText = "Compresses raw astral energy into a volatile crystal burst.",
            lore = "A brilliant battle mage fascinated by the impossible energies bleeding from the Depth."
        ),
        HeroDefinition(
            id = "barbarian",
            displayName = "Kael Draven",
            heroClass = "Barbarian",
            role = "Bruiser",
            affinity = "Ember",
            rarity = "Epic",
            rarityStars = 4,
            powerOffset = 35,
            skillName = "Rift Cleaver",
            skillText = "Turns pain into momentum, driving a brutal cleaving blow through the front line.",
            lore = "A relentless pit fighter who found a greater arena beneath the Astralforge."
        ),
        HeroDefinition(
            id = "necromancer",
            displayName = "Mireth Noct",
            heroClass = "Necromancer",
            role = "Controller",
            affinity = "Void",
            rarity = "Legendary",
            rarityStars = 5,
            powerOffset = 55,
            skillName = "Grave Orbit",
            skillText = "Binds fractured souls into an orbiting curse that weakens and punishes enemies.",
            lore = "A forbidden scholar who studies the dead so the living can survive what waits below."
        ),
        HeroDefinition(
            id = "ranger",
            displayName = "Elowen Skye",
            heroClass = "Ranger",
            role = "Marksman",
            affinity = "Gale",
            rarity = "Epic",
            rarityStars = 4,
            powerOffset = 20,
            skillName = "Comet Volley",
            skillText = "Marks a target and releases a precise volley charged with astral wind.",
            lore = "A horizon scout whose arrows have mapped paths through ruins no expedition returned from."
        ),
        HeroDefinition(
            id = "dark_knight",
            displayName = "Mordren Ash",
            heroClass = "Dark Knight",
            role = "Juggernaut",
            affinity = "Eclipse",
            rarity = "Legendary",
            rarityStars = 5,
            powerOffset = 60,
            skillName = "Eclipse Brand",
            skillText = "Brands an enemy with voidfire, then detonates the mark with a crushing greatblade strike.",
            lore = "A fallen order's last champion, carrying a cursed power he has chosen to turn against the Depth."
        )
    )

    fun allHeroes(): List<HeroDefinition> = heroes

    fun hero(id: String): HeroDefinition? =
        heroes.firstOrNull { it.id == id }

    fun activeHeroId(context: Context): String {
        val saved = AccountScopedStorage.preferences(context, PREFS)
            .getString(KEY_ACTIVE, DEFAULT_HERO)
            ?: DEFAULT_HERO
        return if (hero(saved) != null) saved else DEFAULT_HERO
    }

    fun activeHero(context: Context): HeroDefinition =
        hero(activeHeroId(context)) ?: heroes.first()

    fun setActiveHero(context: Context, heroId: String): Boolean {
        if (hero(heroId) == null) return false
        AccountScopedStorage.preferences(context, PREFS)
            .edit()
            .putString(KEY_ACTIVE, heroId)
            .apply()
        return true
    }

    fun favorites(context: Context): Set<String> {
        return AccountScopedStorage.preferences(context, PREFS)
            .getStringSet(KEY_FAVORITES, emptySet())
            ?.toSet()
            ?: emptySet()
    }

    fun isFavorite(context: Context, heroId: String): Boolean =
        heroId in favorites(context)

    fun toggleFavorite(context: Context, heroId: String): Boolean {
        if (hero(heroId) == null) return false
        val next = favorites(context).toMutableSet()
        val nowFavorite = if (heroId in next) {
            next.remove(heroId)
            false
        } else {
            next.add(heroId)
            true
        }
        AccountScopedStorage.preferences(context, PREFS)
            .edit()
            .putStringSet(KEY_FAVORITES, next)
            .apply()
        return nowFavorite
    }

    fun heroPower(hero: HeroDefinition, progress: PlayerProgress): Int =
        (progress.powerRating + hero.powerOffset).coerceAtLeast(1)
}
