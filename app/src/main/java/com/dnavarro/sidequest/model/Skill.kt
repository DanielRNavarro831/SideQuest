package com.dnavarro.sidequest.model

import kotlinx.serialization.Serializable
import kotlin.random.Random

@Serializable
enum class SkillIcon {
    FOOTSTEPS, SWORD, SPELLBOOK, SCROLL, DISHES, BROOM, D20,
    TRAINING_DUMMY, SHIELD, DRAGON, BOW_AND_ARROW, QUILL_AND_PARCHMENT, POTION,
    ARMOR, HEART, LUTE, SKULL, TREASURE_CHEST, TREE, TURKEY_LEG,
}

/**
 * A trackable skill. Invariant for normal skills: lifetimeXp == (level - 1) * xpThreshold + xp.
 * The step skill breaks that on purpose: its daily XP is wiped at midnight but levels are kept.
 */
@Serializable
data class Skill(
    val id: String,
    val name: String,
    val description: String = "",
    val icon: SkillIcon = SkillIcon.SWORD,
    val xpThreshold: Int = DEFAULT_THRESHOLD,
    val level: Int = 1,
    val xp: Int = 0,
    val lifetimeXp: Long = 0,
    val isStepSkill: Boolean = false,
) {
    fun addXp(amount: Long = 1): Skill {
        if (amount <= 0) return this
        val pool = xp + amount
        return copy(
            level = level + (pool / xpThreshold).toInt(),
            xp = (pool % xpThreshold).toInt(),
            lifetimeXp = lifetimeXp + amount,
        )
    }

    /** Undo one XP. Can drop a level, but never below level 1 with 0 XP. */
    fun removeXp(): Skill = when {
        xp > 0 -> copy(xp = xp - 1, lifetimeXp = (lifetimeXp - 1).coerceAtLeast(0))
        level > 1 -> copy(
            level = level - 1,
            xp = xpThreshold - 1,
            lifetimeXp = (lifetimeXp - 1).coerceAtLeast(0),
        )
        else -> this
    }

    /** Level = 1 + lifetimeXp / threshold, remainder becomes the current XP. */
    fun withThreshold(newThreshold: Int): Skill {
        require(newThreshold in Limits.THRESHOLD_MIN..Limits.THRESHOLD_MAX)
        return copy(
            xpThreshold = newThreshold,
            level = (1 + lifetimeXp / newThreshold).toInt(),
            xp = (lifetimeXp % newThreshold).toInt(),
        )
    }

    fun resetDailyXp(): Skill = copy(xp = 0)

    companion object {
        const val DEFAULT_THRESHOLD = 3
        const val DAILY_ADVENTURE_ID = "daily-adventure"
        const val STEPS_PER_LEVEL = 1000

        fun dailyAdventure() = Skill(
            id = DAILY_ADVENTURE_ID,
            name = "Daily Adventure",
            description = "Walk around on your daily adventure.",
            icon = SkillIcon.FOOTSTEPS,
            xpThreshold = STEPS_PER_LEVEL,
            isStepSkill = true,
        )
    }
}

/** What the skill editor produces. */
data class SkillDraft(
    val name: String,
    val description: String,
    val icon: SkillIcon,
    val xpThreshold: Int,
) {
    companion object {
        fun from(skill: Skill) = SkillDraft(skill.name, skill.description, skill.icon, skill.xpThreshold)

        fun newSkill(random: Random = Random.Default) =
            SkillDraft(SillyNames.pick(random), "", SkillIcon.SWORD, Skill.DEFAULT_THRESHOLD)
    }
}

object SillyNames {
    val all = listOf(
        "Dragon Slaying", "Rescue Maidens", "Goblin Taming", "Potion Brewing",
        "Treasure Hunting", "Slime Wrangling", "Mimic Spotting", "Tavern Brawling",
        "Quest Dodging", "Loot Hoarding", "Wizard Napping", "Cape Swooshing",
        "Bard Karaoke", "Troll Tickling", "Gnome Juggling", "Owlbear Hugging",
    )

    fun pick(random: Random = Random.Default): String = all.random(random)
}
