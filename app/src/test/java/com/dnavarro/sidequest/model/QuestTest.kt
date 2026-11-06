package com.dnavarro.sidequest.model

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class QuestTest {
    private fun draft(name: String, xp: Int) = QuestDraft(name, "", SkillIcon.SCROLL, xp)

    private fun skills(vararg levels: Int) =
        levels.mapIndexed { i, lvl -> Skill(id = "$i", name = "S$i", level = lvl) }

    // ---- Renown and overall level ----

    @Test fun `renown ranks count as level-ups in every mode`() {
        val s = skills(3, 4, 4) // 8 level-ups, sum 11
        assertEquals(5, OverallLevelCalculator.calculate(s, OverallLevelSettings(LevelMode.AVERAGE), 4).level) // (11 + 4) / 3
        assertEquals(15, OverallLevelCalculator.calculate(s, OverallLevelSettings(LevelMode.SUM), 4).level)
        val everyX = OverallLevelCalculator.calculate(s, OverallLevelSettings(LevelMode.EVERY_X, 10), 4)
        assertEquals(2, everyX.level) // 12 level-ups
        assertEquals(2, everyX.progress)
    }

    @Test fun `only whole renown ranks count`() {
        assertEquals(0, Renown.ranks(99))
        assertEquals(1, Renown.ranks(100))
        assertEquals(1, Renown.ranks(199))
        assertEquals(99, Renown.progress(199))
        val state = AppState(renownXp = 250)
        // Daily Adventure at level 1, every 10: 2 ranks = 2 level-ups.
        assertEquals(2, state.overallLevel.progress)
    }

    // ---- Quest lifecycle ----

    @Test fun `completing banks xp and undoing takes it back`() {
        val s = AppState().addQuest("q", draft("Dishes", 150))
        val done = s.completeQuest("q", nowMillis = 1000)
        assertEquals(150, done.renownXp)
        assertEquals(1000L, done.quest("q")!!.completedAt)
        assertEquals(done, done.completeQuest("q", 2000)) // can't complete twice

        val undone = done.uncompleteQuest("q")
        assertEquals(0, undone.renownXp)
        assertNull(undone.quest("q")!!.completedAt)
    }

    @Test fun `deleting a completed quest keeps its renown`() {
        val s = AppState().addQuest("q", draft("Dishes", 150)).completeQuest("q", 1).deleteQuest("q")
        assertEquals(150, s.renownXp)
        assertTrue(s.quests.isEmpty())
    }

    @Test fun `completed quest xp is locked but other fields can change`() {
        val s = AppState().addQuest("q", draft("Dishes", 150)).completeQuest("q", 1)
            .editQuest("q", QuestDraft("Big Dishes", "sink", SkillIcon.DISHES, 9000))
        val q = s.quest("q")!!
        assertEquals("Big Dishes", q.name)
        assertEquals(150, q.xp)
        assertEquals(150, s.uncompleteQuest("q").let { 150 - it.renownXp })
    }

    @Test fun `xp reward is clamped and blank names are rejected`() {
        val s = AppState()
            .addQuest("a", draft("Huge", 50_000))
            .addQuest("b", draft("Tiny", 0))
            .addQuest("c", draft("   ", 10))
        assertEquals(9999, s.quest("a")!!.xp)
        assertEquals(1, s.quest("b")!!.xp)
        assertNull(s.quest("c"))
    }

    @Test fun `only the last 10 completed quests are kept`() {
        var s = AppState()
        for (i in 1..12) s = s.addQuest("q$i", draft("Quest $i", 10))
        for (i in 1..11) s = s.completeQuest("q$i", nowMillis = i.toLong())
        assertEquals(10, s.completedQuests.size)
        assertNull(s.quest("q1")) // oldest completed is gone
        assertEquals(listOf("q12"), s.activeQuests.map { it.id })
        assertEquals(110, s.renownXp) // its XP is still banked
    }

    // ---- Sorting ----

    @Test fun `skills sort by level then name`() {
        val s = listOf(
            Skill("1", "beta", level = 2),
            Skill("2", "Alpha", level = 2),
            Skill("3", "Zed", level = 5),
            Skill("4", "aardvark", level = 1),
        )
        assertEquals(listOf("Zed", "Alpha", "beta", "aardvark"), Sorting.skills(s).map { it.name })
    }

    @Test fun `active quests sort by xp then name, completed by most recent`() {
        var s = AppState()
            .addQuest("1", draft("Bravo", 50))
            .addQuest("2", draft("alpha", 50))
            .addQuest("3", draft("Charlie", 500))
            .addQuest("4", draft("Old", 10))
            .addQuest("5", draft("New", 10))
        s = s.completeQuest("4", 100).completeQuest("5", 200)
        assertEquals(listOf("Charlie", "alpha", "Bravo"), s.activeQuests.map { it.name })
        assertEquals(listOf("New", "Old"), s.completedQuests.map { it.name })
    }

    // ---- Saving ----

    @Test fun `old saves without quests still load`() {
        val json = Json { ignoreUnknownKeys = true }
        val old = """{"profile":{"name":"Hero"},"skills":[]}"""
        val s = json.decodeFromString(AppState.serializer(), old)
        assertTrue(s.quests.isEmpty())
        assertEquals(0, s.renownXp)
    }

    @Test fun `quests survive a save round trip`() {
        val json = Json { encodeDefaults = true }
        val s = AppState().addQuest("q", draft("Dishes", 75)).completeQuest("q", 5)
        assertEquals(s, json.decodeFromString(AppState.serializer(), json.encodeToString(AppState.serializer(), s)))
    }
}
