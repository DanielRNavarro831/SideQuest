package com.dnavarro.sidequest.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OverallLevelTest {
    private fun skills(vararg levels: Int) =
        levels.mapIndexed { i, lvl -> Skill(id = "$i", name = "S$i", level = lvl) }

    @Test fun `average rounds down`() {
        val r = OverallLevelCalculator.calculate(skills(3, 4, 4), OverallLevelSettings(LevelMode.AVERAGE))
        assertEquals(3, r.level) // 11 / 3 = 3.67
        assertNull(r.progress)
    }

    @Test fun `sum adds all levels`() {
        val r = OverallLevelCalculator.calculate(skills(3, 4, 4), OverallLevelSettings(LevelMode.SUM))
        assertEquals(11, r.level)
    }

    @Test fun `every x counts level ups not starting levels`() {
        // Five brand-new skills have no level-ups yet.
        val fresh = OverallLevelCalculator.calculate(skills(1, 1, 1, 1, 1), OverallLevelSettings(LevelMode.EVERY_X, 10))
        assertEquals(1, fresh.level)
        assertEquals(0, fresh.progress)

        // 4 + 9 + 1 = 14 level-ups -> level 2, 4 of 10 toward the next.
        val r = OverallLevelCalculator.calculate(skills(5, 10, 2), OverallLevelSettings(LevelMode.EVERY_X, 10))
        assertEquals(2, r.level)
        assertEquals(4, r.progress)
        assertEquals(10, r.progressGoal)
    }

    @Test fun `changing x recalculates`() {
        val r = OverallLevelCalculator.calculate(skills(5, 10, 2), OverallLevelSettings(LevelMode.EVERY_X, 3))
        assertEquals(5, r.level)  // 1 + 14 / 3
        assertEquals(2, r.progress)
    }

    @Test fun `x of 1 or lower is rejected`() {
        val state = AppState()
        assertEquals(state, state.withLevelSettings(OverallLevelSettings(LevelMode.EVERY_X, 1)))
        assertEquals(state, state.withLevelSettings(OverallLevelSettings(LevelMode.EVERY_X, 0)))
    }

    @Test fun `default mode is every 10`() {
        assertEquals(OverallLevelSettings(LevelMode.EVERY_X, 10), Profile().levelSettings)
    }
}
