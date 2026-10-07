# Skeletal Rig Pipeline

Astral Forge is moving from frame-by-frame battle actors to layered 2D skeletal actors.

## Current roster

Characters:
- Knight (male)
- Barbarian (male)
- Rogue (female)
- Mage (female)
- Ranger (female)
- Paladin (female)
- Necromancer (male)
- Dark Knight (male)

Monsters:
- Blue Slime
- Goblin Raider
- Skeleton Warrior
- Dire Wolf
- Astral Warden (dungeon boss)

## Pipeline

Each source rigging sheet is split into transparent art components. The pipeline preserves every extracted component and creates a draft semantic mapping to a standard rig.

Each processed entity contains:

- source_sheet.png
- parts/raw/*.png
- parts/semantic/*.png
- parts/index.json
- rig.json
- animations/idle.json
- animations/attack.json
- animations/hit.json
- animations/death.json
- rig_review.png

The first pass is intentionally marked `draft-auto`. Before an entity is used in the APK, review its semantic part choices and tune pivots/anchors.

## Rig families

- `biped`: characters, goblin, skeleton, Astral Warden
- `quadruped`: Dire Wolf
- `slime`: Blue Slime

Do not convert these actors back into frame sheets. Animation should be driven by bones/transforms so armor, weapons, scale, and timing can be changed without regenerating every animation frame.
