package com.dnavarro.sidequest.model

import kotlinx.serialization.Serializable
import kotlin.random.Random

/**
 * A one-time task. Completing it banks its XP into [AppState.renownXp]; the quest itself is
 * only kept around (greyed out) until it falls out of the last [Renown.COMPLETED_KEPT] completed.
 */
@Serializable
data class Quest(
    val id: String,
    val name: String,
    val description: String = "",
    val icon: SkillIcon = SkillIcon.SCROLL,
    val xp: Int = QuestDraft.DEFAULT_XP,
    /** Epoch millis when completed, or null while active. */
    val completedAt: Long? = null,
) {
    val isCompleted: Boolean get() = completedAt != null
}

/**
 * Quest XP feeds a single Renown pool. Every [XP_PER_RANK] XP is one Renown rank, and a rank
 * counts exactly like one skill level-up in every overall-level mode.
 */
object Renown {
    const val XP_PER_RANK = 100
    const val COMPLETED_KEPT = 10

    fun ranks(renownXp: Long): Long = renownXp.coerceAtLeast(0) / XP_PER_RANK
    fun progress(renownXp: Long): Int = (renownXp.coerceAtLeast(0) % XP_PER_RANK).toInt()
}

/** What the quest editor produces. */
data class QuestDraft(
    val name: String,
    val description: String,
    val icon: SkillIcon,
    val xp: Int,
) {
    companion object {
        const val DEFAULT_XP = 50

        fun from(quest: Quest) = QuestDraft(quest.name, quest.description, quest.icon, quest.xp)

        fun newQuest(random: Random = Random.Default) =
            QuestDraft(SillyQuests.names.random(random), "", SkillIcon.SCROLL, DEFAULT_XP)
    }
}

object SillyQuests {
    val names = listOf(
        "Slay the Laundry Pile", "Rescue the Lost Sock", "Tame the Inbox", "Raid the Grocery Keep",
        "Banish the Dust Bunnies", "Feed the Beasts", "Escort the Trash Cart", "Duel the Dishes",
    )

    val emptyMessages = listOf(
        "No dragons to slay at the moment.",
        "No maidens to save today.",
        "The tavern notice board is suspiciously empty.",
        "The kingdom is at peace… for now.",
        "Not a single goblin in sight.",
    )
}

object Sorting {
    /** Highest level first, then A to Z. */
    fun skills(skills: List<Skill>): List<Skill> =
        skills.sortedWith(compareByDescending<Skill> { it.level }.thenBy(String.CASE_INSENSITIVE_ORDER) { it.name }.thenBy { it.id })

    /** Active quests: biggest XP reward first, then A to Z. */
    fun activeQuests(quests: List<Quest>): List<Quest> =
        quests.filterNot { it.isCompleted }
            .sortedWith(compareByDescending<Quest> { it.xp }.thenBy(String.CASE_INSENSITIVE_ORDER) { it.name }.thenBy { it.id })

    /** Completed quests: most recently completed first. */
    fun completedQuests(quests: List<Quest>): List<Quest> =
        quests.filter { it.isCompleted }.sortedWith(compareByDescending<Quest> { it.completedAt }.thenBy { it.id })
}
