package com.dnavarro.sidequest.model

import kotlinx.serialization.Serializable

@Serializable
enum class CharacterClass { WARRIOR, WIZARD, ARCHER, ROGUE, BARD }

@Serializable
enum class LevelMode { AVERAGE, SUM, EVERY_X }

@Serializable
data class OverallLevelSettings(
    val mode: LevelMode = LevelMode.EVERY_X,
    val x: Int = 10,
)

@Serializable
data class Profile(
    val name: String = "Hero",
    val title: String = "",
    val character: CharacterClass = CharacterClass.WARRIOR,
    val levelSettings: OverallLevelSettings = OverallLevelSettings(),
)

/** progress/progressGoal are only set for EVERY_X, which shows a progress bar. */
data class OverallLevel(
    val level: Int,
    val progress: Int? = null,
    val progressGoal: Int? = null,
)

object OverallLevelCalculator {
    fun calculate(skills: List<Skill>, settings: OverallLevelSettings): OverallLevel =
        when (settings.mode) {
            LevelMode.AVERAGE -> OverallLevel(
                if (skills.isEmpty()) 1 else (skills.sumOf { it.level.toLong() } / skills.size).toInt(),
            )
            LevelMode.SUM -> OverallLevel(skills.sumOf { it.level })
            LevelMode.EVERY_X -> {
                val x = settings.x.coerceAtLeast(Limits.X_MIN)
                // Skills start at level 1, so a skill's level-ups are level - 1.
                val levelUps = skills.sumOf { (it.level - 1).toLong() }
                OverallLevel(
                    level = (1 + levelUps / x).toInt(),
                    progress = (levelUps % x).toInt(),
                    progressGoal = x,
                )
            }
        }
}
