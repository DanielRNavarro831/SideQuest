package com.dnavarro.sidequest.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CelebrationTest {
    private val skillDraft = SkillDraft("Dishes", "", SkillIcon.DISHES, 1)

    @Test fun `gaining xp without a level up celebrates nothing`() {
        val s = AppState().addSkill("d", skillDraft.copy(xpThreshold = 3))
        assertTrue(Celebrations.between(s, s.gainXp("d")).isEmpty())
    }

    @Test fun `skill level up is celebrated`() {
        val s = AppState().addSkill("d", skillDraft)
        val c = Celebrations.between(s, s.gainXp("d"))
        assertEquals(listOf(Celebration.SkillLevelUp("d", "Dishes", SkillIcon.DISHES, 1, 2)), c)
    }

    @Test fun `big quest celebrates quest, renown and hero level in that order`() {
        val s = AppState()
            .withLevelSettings(OverallLevelSettings(LevelMode.EVERY_X, 2))
            .addQuest("q", QuestDraft("Big One", "", SkillIcon.SCROLL, 250))
        val c = Celebrations.between(s, s.completeQuest("q", 1))
        assertEquals(
            listOf(
                Celebration.QuestCompleted("Big One", 250),
                Celebration.RenownUp(0, 2),
                Celebration.HeroLevelUp(CharacterClass.WARRIOR, 1, 2),
            ),
            c,
        )
    }

    @Test fun `changing level mode is not a hero level up`() {
        val s = AppState().addSkill("d", skillDraft).gainXp("d").gainXp("d")
        val switched = s.withLevelSettings(OverallLevelSettings(LevelMode.SUM))
        assertTrue(switched.overallLevel.level > s.overallLevel.level)
        assertTrue(Celebrations.between(s, switched).isEmpty())
    }

    @Test fun `undoing things celebrates nothing`() {
        val s = AppState().addSkill("d", skillDraft).gainXp("d")
            .addQuest("q", QuestDraft("Q", "", SkillIcon.SCROLL, 100)).completeQuest("q", 1)
        assertTrue(Celebrations.between(s, s.loseXp("d").uncompleteQuest("q")).isEmpty())
    }

    @Test fun `repeated level ups fold into one entry`() {
        val q = Celebrations.enqueue(
            listOf(Celebration.SkillLevelUp("d", "Dishes", SkillIcon.DISHES, 1, 2)),
            listOf(Celebration.SkillLevelUp("d", "Dishes", SkillIcon.DISHES, 2, 3), Celebration.RenownUp(0, 1)),
        )
        val q2 = Celebrations.enqueue(q, listOf(Celebration.RenownUp(1, 2), Celebration.QuestCompleted("A", 5)))
        assertEquals(
            listOf(
                Celebration.SkillLevelUp("d", "Dishes", SkillIcon.DISHES, 1, 3),
                Celebration.RenownUp(0, 2),
                Celebration.QuestCompleted("A", 5),
            ),
            q2,
        )
    }
}
