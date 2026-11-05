package com.dnavarro.sidequest.model

import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class StepTracking(
    /** Last raw value of the phone's step counter (steps since the phone booted). */
    val lastCounterValue: Long? = null,
    /** Boot count when lastCounterValue was read, so a reboot can be detected reliably. */
    val lastBootCount: Int? = null,
    /** ISO date (yyyy-MM-dd) that Daily Adventure's XP currently belongs to. */
    val currentDay: String? = null,
    val permissionPrompted: Boolean = false,
)

/** Everything the app saves. All operations are pure so they can be unit tested. */
@Serializable
data class AppState(
    val profile: Profile = Profile(),
    val skills: List<Skill> = listOf(Skill.dailyAdventure()),
    val steps: StepTracking = StepTracking(),
) {
    val overallLevel: OverallLevel
        get() = OverallLevelCalculator.calculate(skills, profile.levelSettings)

    fun skill(id: String): Skill? = skills.firstOrNull { it.id == id }

    fun ensureDefaultSkill(): AppState =
        if (skills.any { it.isStepSkill }) this
        else copy(skills = listOf(Skill.dailyAdventure()) + skills)

    // ---- Profile ----

    fun withName(raw: String): AppState {
        val name = TextRules.cleanName(raw, Limits.NAME_MAX)
        return if (name.isEmpty()) this else copy(profile = profile.copy(name = name))
    }

    fun withTitle(raw: String): AppState =
        copy(profile = profile.copy(title = TextRules.cleanName(raw, Limits.TITLE_MAX)))

    fun withCharacter(character: CharacterClass): AppState =
        copy(profile = profile.copy(character = character))

    fun withLevelSettings(settings: OverallLevelSettings): AppState {
        if (settings.mode == LevelMode.EVERY_X && settings.x < Limits.X_MIN) return this
        val x = settings.x.coerceIn(Limits.X_MIN, Limits.X_MAX)
        return copy(profile = profile.copy(levelSettings = settings.copy(x = x)))
    }

    // ---- Skills ----

    fun addSkill(id: String, draft: SkillDraft): AppState {
        val name = TextRules.cleanName(draft.name, Limits.SKILL_NAME_MAX)
        if (name.isEmpty() || skills.any { it.id == id }) return this
        val skill = Skill(
            id = id,
            name = name,
            description = TextRules.cleanDescription(draft.description),
            icon = draft.icon,
            xpThreshold = draft.xpThreshold.coerceIn(Limits.THRESHOLD_MIN, Limits.THRESHOLD_MAX),
        )
        return copy(skills = skills + skill)
    }

    fun editSkill(id: String, draft: SkillDraft): AppState = updateEditable(id) { skill ->
        val name = TextRules.cleanName(draft.name, Limits.SKILL_NAME_MAX)
        if (name.isEmpty()) return@updateEditable skill
        val edited = skill.copy(
            name = name,
            description = TextRules.cleanDescription(draft.description),
            icon = draft.icon,
        )
        val threshold = draft.xpThreshold.coerceIn(Limits.THRESHOLD_MIN, Limits.THRESHOLD_MAX)
        if (threshold != skill.xpThreshold) edited.withThreshold(threshold) else edited
    }

    fun deleteSkill(id: String): AppState =
        copy(skills = skills.filterNot { it.id == id && !it.isStepSkill })

    fun gainXp(id: String): AppState = updateEditable(id) { it.addXp(1) }

    fun loseXp(id: String): AppState = updateEditable(id) { it.removeXp() }

    /** The step skill can't be edited, deleted or manually adjusted. */
    private inline fun updateEditable(id: String, transform: (Skill) -> Skill): AppState =
        copy(skills = skills.map { if (it.id == id && !it.isStepSkill) transform(it) else it })

    // ---- Steps ----

    /** Wipes Daily Adventure's XP (not its level) the first time it's seen on a new day. */
    fun rollOverDay(today: LocalDate): AppState {
        val key = today.toString()
        if (steps.currentDay == key) return this
        return copy(
            skills = skills.map { if (it.isStepSkill) it.resetDailyXp() else it },
            steps = steps.copy(currentDay = key),
        )
    }

    /**
     * Applies a new reading from the phone's step counter. Steps since the last reading count
     * toward today; the very first reading only sets the baseline.
     */
    fun recordStepCounter(counterValue: Long, bootCount: Int?, today: LocalDate): AppState {
        val rolled = rollOverDay(today)
        val tracking = rolled.steps
        val newSteps = StepMath.stepsSince(
            previousValue = tracking.lastCounterValue,
            previousBoot = tracking.lastBootCount,
            value = counterValue,
            boot = bootCount,
        )
        val updatedSkills =
            if (newSteps > 0) rolled.skills.map { if (it.isStepSkill) it.addXp(newSteps) else it }
            else rolled.skills
        return rolled.copy(
            skills = updatedSkills,
            steps = tracking.copy(
                lastCounterValue = counterValue,
                lastBootCount = bootCount ?: tracking.lastBootCount,
            ),
        )
    }

    fun markPermissionPrompted(): AppState = copy(steps = steps.copy(permissionPrompted = true))
}

object StepMath {
    fun stepsSince(previousValue: Long?, previousBoot: Int?, value: Long, boot: Int?): Long = when {
        previousValue == null -> 0
        // The counter restarts at 0 on reboot, so everything it shows is new.
        boot != null && previousBoot != null && boot != previousBoot -> value
        value >= previousValue -> value - previousValue
        else -> value
    }
}
