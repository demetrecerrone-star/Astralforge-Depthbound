package com.demetrecerrone.astralforge

import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.random.Random

data class BattleV2AttackOutcome(
    val damage: Int,
    val critical: Boolean,
    val targetHp: Int,
    val defeated: Boolean
)

data class BattleV2Snapshot(
    val playerHp: Int,
    val playerMaxHp: Int,
    val enemyHp: Int,
    val enemyMaxHp: Int,
    val finished: Boolean,
    val playerWon: Boolean
)

class BattleV2Controller(
    playerMaxHp: Int,
    private val playerAttack: Int,
    private val playerDefense: Int,
    private val playerCritChance: Int,
    private val depth: Int,
    private val random: Random = Random.Default
) {
    private val playerMaxHpValue = playerMaxHp.coerceAtLeast(1)
    private val enemyMaxHpValue =
        (120 + depth.coerceAtLeast(1) * 32)
            .coerceAtLeast(1)

    private val enemyAttack =
        10 + depth.coerceAtLeast(1) * 4

    private val enemyDefense =
        3 + depth.coerceAtLeast(1) * 2

    private var playerHp = playerMaxHpValue
    private var enemyHp = enemyMaxHpValue
    private var finished = false
    private var playerWon = false

    fun snapshot(): BattleV2Snapshot =
        BattleV2Snapshot(
            playerHp = playerHp,
            playerMaxHp = playerMaxHpValue,
            enemyHp = enemyHp,
            enemyMaxHp = enemyMaxHpValue,
            finished = finished,
            playerWon = playerWon
        )

    fun playerAttack(): BattleV2AttackOutcome {
        if (finished) {
            return BattleV2AttackOutcome(
                damage = 0,
                critical = false,
                targetHp = enemyHp,
                defeated = enemyHp <= 0
            )
        }

        val critical =
            random.nextInt(100) <
                playerCritChance.coerceIn(0, 75)

        val variance = random.nextInt(-2, 4)
        val base =
            max(
                1,
                playerAttack + variance - enemyDefense
            )

        val damage =
            if (critical) (base * 1.75f).roundToInt()
            else base

        enemyHp = (enemyHp - damage).coerceAtLeast(0)

        if (enemyHp <= 0) {
            finished = true
            playerWon = true
        }

        return BattleV2AttackOutcome(
            damage = damage,
            critical = critical,
            targetHp = enemyHp,
            defeated = enemyHp <= 0
        )
    }

    fun enemyAttack(): BattleV2AttackOutcome {
        if (finished) {
            return BattleV2AttackOutcome(
                damage = 0,
                critical = false,
                targetHp = playerHp,
                defeated = playerHp <= 0
            )
        }

        val critical = random.nextInt(100) < 8
        val variance = random.nextInt(-2, 3)
        val base =
            max(
                1,
                enemyAttack + variance - playerDefense
            )

        val damage =
            if (critical) (base * 1.5f).roundToInt()
            else base

        playerHp = (playerHp - damage).coerceAtLeast(0)

        if (playerHp <= 0) {
            finished = true
            playerWon = false
        }

        return BattleV2AttackOutcome(
            damage = damage,
            critical = critical,
            targetHp = playerHp,
            defeated = playerHp <= 0
        )
    }

    fun reset() {
        playerHp = playerMaxHpValue
        enemyHp = enemyMaxHpValue
        finished = false
        playerWon = false
    }
}
