package com.dnavarro.sidequest.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class SkillTest {
    private fun skill(threshold: Int = 3) = Skill(id = "s", name = "Dishes", xpThreshold = threshold)

    @Test fun `new skill starts at level 1 with 0 xp`() {
        val s = skill()
        assertEquals(1, s.level)
        assertEquals(0, s.xp)
    }

    @Test fun `reaching the threshold levels up and resets the bar`() {
        val s = skill().addXp().addXp().addXp()
        assertEquals(2, s.level)
        assertEquals(0, s.xp)
        assertEquals(3, s.lifetimeXp)
    }

    @Test fun `large xp gains can level up several times`() {
        val s = skill(threshold = 1000).addXp(2500)
        assertEquals(3, s.level)
        assertEquals(500, s.xp)
    }

    @Test fun `minus undoes xp and can drop a level`() {
        val s = skill().addXp(3).removeXp()
        assertEquals(1, s.level)
        assertEquals(2, s.xp)
        assertEquals(2, s.lifetimeXp)
    }

    @Test fun `minus never goes below level 1 with 0 xp`() {
        val s = skill().removeXp()
        assertEquals(1, s.level)
        assertEquals(0, s.xp)
        assertEquals(0, s.lifetimeXp)
    }

    @Test fun `changing threshold recalculates from lifetime xp`() {
        val s = skill(threshold = 3).addXp(10) // level 4, xp 1
        val r = s.withThreshold(4)
        assertEquals(3, r.level) // 1 + 10 / 4
        assertEquals(2, r.xp)    // 10 % 4
        assertEquals(10, r.lifetimeXp)
    }

    @Test fun `silly names fit the skill name rules`() {
        SillyNames.all.forEach {
            assertTrue(it, it.length <= Limits.SKILL_NAME_MAX)
            assertEquals(it, TextRules.cleanName(it, Limits.SKILL_NAME_MAX))
        }
        assertTrue(SkillDraft.newSkill(Random(1)).name in SillyNames.all)
    }
}
