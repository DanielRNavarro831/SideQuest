package com.dnavarro.sidequest.model

/** Something worth a bit of fanfare, smallest first. */
sealed interface Celebration {
    data class QuestCompleted(val questName: String, val xp: Int) : Celebration
    data class SkillLevelUp(val skillId: String, val skillName: String, val icon: SkillIcon, val from: Int, val to: Int) : Celebration
    data class RenownUp(val from: Long, val to: Long) : Celebration
    data class HeroLevelUp(val character: CharacterClass, val from: Int, val to: Int) : Celebration
}

object Celebrations {
    /** What got better going from [old] to [new], ordered small → medium → big. */
    fun between(old: AppState, new: AppState): List<Celebration> {
        val out = mutableListOf<Celebration>()
        for (quest in new.quests) {
            val before = old.quest(quest.id) ?: continue
            if (quest.isCompleted && !before.isCompleted) out += Celebration.QuestCompleted(quest.name, quest.xp)
        }
        for (skill in new.skills) {
            val before = old.skill(skill.id) ?: continue
            if (skill.level > before.level) {
                out += Celebration.SkillLevelUp(skill.id, skill.name, skill.icon, before.level, skill.level)
            }
        }
        val oldRenown = Renown.ranks(old.renownXp)
        val newRenown = Renown.ranks(new.renownXp)
        if (newRenown > oldRenown) out += Celebration.RenownUp(oldRenown, newRenown)
        // Changing how the level is calculated isn't an achievement.
        if (new.profile.levelSettings == old.profile.levelSettings) {
            val from = old.overallLevel.level
            val to = new.overallLevel.level
            if (to > from) out += Celebration.HeroLevelUp(new.profile.character, from, to)
        }
        return out
    }

    /**
     * Adds [incoming] to a waiting [queue], folding repeats of the same thing into the last
     * matching entry (e.g. tapping +XP quickly gives one "Lv 2 → 5" banner, not three).
     */
    fun enqueue(queue: List<Celebration>, incoming: List<Celebration>): List<Celebration> {
        val out = queue.toMutableList()
        for (c in incoming) {
            val i = out.indexOfLast { it.sameKindAs(c) }
            if (i >= 0 && c !is Celebration.QuestCompleted) out[i] = out[i].mergedWith(c) else out += c
        }
        return out
    }

    private fun Celebration.sameKindAs(other: Celebration): Boolean = when (this) {
        is Celebration.SkillLevelUp -> other is Celebration.SkillLevelUp && other.skillId == skillId
        is Celebration.RenownUp -> other is Celebration.RenownUp
        is Celebration.HeroLevelUp -> other is Celebration.HeroLevelUp
        is Celebration.QuestCompleted -> false
    }

    private fun Celebration.mergedWith(later: Celebration): Celebration = when {
        this is Celebration.SkillLevelUp && later is Celebration.SkillLevelUp -> copy(to = later.to, skillName = later.skillName, icon = later.icon)
        this is Celebration.RenownUp && later is Celebration.RenownUp -> copy(to = later.to)
        this is Celebration.HeroLevelUp && later is Celebration.HeroLevelUp -> copy(to = later.to, character = later.character)
        else -> later
    }
}
