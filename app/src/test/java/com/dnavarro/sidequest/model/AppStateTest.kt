package com.dnavarro.sidequest.model

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class AppStateTest {
    private val day1 = LocalDate.of(2026, 11, 5)
    private val day2 = day1.plusDays(1)
    private val stepId = Skill.DAILY_ADVENTURE_ID

    private fun AppState.steps() = skill(stepId)!!

    @Test fun `new state has the daily adventure skill`() {
        val s = AppState().steps()
        assertEquals("Daily Adventure", s.name)
        assertEquals(1000, s.xpThreshold)
        assertEquals(SkillIcon.FOOTSTEPS, s.icon)
        assertTrue(s.isStepSkill)
    }

    @Test fun `daily adventure cannot be deleted edited or bumped`() {
        val draft = SkillDraft("Renamed", "", SkillIcon.SWORD, 5)
        val s = AppState().deleteSkill(stepId).editSkill(stepId, draft).gainXp(stepId).loseXp(stepId)
        assertEquals(AppState(), s)
    }

    @Test fun `normal skills can be added edited and deleted`() {
        var s = AppState().addSkill("a", SkillDraft("Dishes", "Scrub <b>", SkillIcon.DISHES, 3))
        assertEquals("Scrub b", s.skill("a")!!.description)
        s = s.gainXp("a").gainXp("a").gainXp("a").gainXp("a")
        assertEquals(2, s.skill("a")!!.level)
        s = s.editSkill("a", SkillDraft("Laundry", "", SkillIcon.BROOM, 2))
        val edited = s.skill("a")!!
        assertEquals("Laundry", edited.name)
        assertEquals(3, edited.level) // 1 + 4 / 2
        assertEquals(0, edited.xp)
        s = s.deleteSkill("a")
        assertEquals(1, s.skills.size)
    }

    @Test fun `blank skill names are refused`() {
        val s = AppState().addSkill("a", SkillDraft("  ", "", SkillIcon.SWORD, 3))
        assertEquals(1, s.skills.size)
    }

    @Test fun `first step reading only sets the baseline`() {
        val s = AppState().recordStepCounter(5000, bootCount = 3, today = day1)
        assertEquals(0, s.steps().xp)
        assertEquals(5000L, s.steps.lastCounterValue)
    }

    @Test fun `steps add xp and level up every 1000`() {
        val s = AppState()
            .recordStepCounter(5000, 3, day1)
            .recordStepCounter(6500, 3, day1)
        assertEquals(2, s.steps().level)
        assertEquals(500, s.steps().xp)
        assertEquals(1500, s.steps().lifetimeXp)
    }

    @Test fun `midnight wipes xp but keeps the level`() {
        val s = AppState()
            .recordStepCounter(0, 3, day1)
            .recordStepCounter(1500, 3, day1)
            .rollOverDay(day2)
        assertEquals(2, s.steps().level)
        assertEquals(0, s.steps().xp)
    }

    @Test fun `steps after midnight count toward the new day`() {
        val s = AppState()
            .recordStepCounter(0, 3, day1)
            .recordStepCounter(900, 3, day1)
            .recordStepCounter(1200, 3, day2)
        assertEquals(1, s.steps().level)
        assertEquals(300, s.steps().xp)
    }

    @Test fun `a reboot restarts the counter without losing steps`() {
        val s = AppState()
            .recordStepCounter(8000, 3, day1)
            .recordStepCounter(800, 4, day1) // rebooted, walked 800
        assertEquals(800, s.steps().xp)
    }

    @Test fun `counter going backwards is treated as a reboot`() {
        assertEquals(200L, StepMath.stepsSince(8000, null, 200, null))
    }

    @Test fun `rolling over the same day does nothing`() {
        val s = AppState().rollOverDay(day1)
        assertEquals(s, s.rollOverDay(day1))
    }

    @Test fun `state survives a save and load`() {
        val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
        val s = AppState()
            .withName("Ada")
            .addSkill("a", SkillDraft("Dishes", "", SkillIcon.DISHES, 3))
            .recordStepCounter(10, 1, day1)
        val back = json.decodeFromString(AppState.serializer(), json.encodeToString(AppState.serializer(), s))
        assertEquals(s, back)
    }

    @Test fun `missing default skill is restored`() {
        val s = AppState(skills = emptyList()).ensureDefaultSkill()
        assertNotNull(s.skill(stepId))
    }
}
