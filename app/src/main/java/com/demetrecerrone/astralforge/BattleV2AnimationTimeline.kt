package com.demetrecerrone.astralforge

enum class BattleV2Animation {
    IDLE,
    ATTACK,
    HIT,
    DEATH,
    VICTORY
}

data class BattleV2AnimationSpec(
    val durationMs: Long,
    val impactMs: Long? = null,
    val frameMs: Long = 80L,
    val returnToIdle: Boolean = true
)

object BattleV2AnimationTimeline {
    fun spec(animation: BattleV2Animation): BattleV2AnimationSpec =
        when (animation) {
            BattleV2Animation.IDLE ->
                BattleV2AnimationSpec(
                    durationMs = Long.MAX_VALUE,
                    frameMs = 180L,
                    returnToIdle = false
                )

            BattleV2Animation.ATTACK ->
                BattleV2AnimationSpec(
                    durationMs = 640L,
                    impactMs = 340L,
                    frameMs = 80L
                )

            BattleV2Animation.HIT ->
                BattleV2AnimationSpec(
                    durationMs = 360L,
                    frameMs = 90L
                )

            BattleV2Animation.DEATH ->
                BattleV2AnimationSpec(
                    durationMs = 900L,
                    frameMs = 112L,
                    returnToIdle = false
                )

            BattleV2Animation.VICTORY ->
                BattleV2AnimationSpec(
                    durationMs = 1200L,
                    frameMs = 150L,
                    returnToIdle = false
                )
        }
}
