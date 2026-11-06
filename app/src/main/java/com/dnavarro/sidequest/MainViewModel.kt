package com.dnavarro.sidequest

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dnavarro.sidequest.model.AppState
import com.dnavarro.sidequest.model.Celebration
import com.dnavarro.sidequest.model.Celebrations
import com.dnavarro.sidequest.model.CharacterClass
import com.dnavarro.sidequest.model.OverallLevelSettings
import com.dnavarro.sidequest.model.QuestDraft
import com.dnavarro.sidequest.model.SkillDraft
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = (app as SideQuestApp).repository
    val state: StateFlow<AppState> = repository.state

    /** Fanfare waiting to play; the UI shows the first one and calls [celebrationDone]. */
    private val _celebrations = MutableStateFlow<List<Celebration>>(emptyList())
    val celebrations: StateFlow<List<Celebration>> = _celebrations.asStateFlow()

    init {
        // Daily Adventure levels up from walking, not from a button, so watch for that too.
        viewModelScope.launch {
            var previous = repository.state.value
            repository.state.collect { current ->
                val stepLevel = { s: AppState -> s.skills.firstOrNull { it.isStepSkill }?.level ?: 0 }
                if (stepLevel(current) > stepLevel(previous)) {
                    celebrate(Celebrations.between(previous, current).filterNot { it is Celebration.QuestCompleted })
                }
                previous = current
            }
        }
    }

    fun celebrationDone() = _celebrations.update { it.drop(1) }

    private fun celebrate(new: List<Celebration>) {
        if (new.isNotEmpty()) _celebrations.update { Celebrations.enqueue(it, new) }
    }

    /** Applies a change made by the player and queues any fanfare it earned. */
    private fun updateWithFanfare(transform: (AppState) -> AppState) {
        val before = repository.state.value
        repository.update(transform)
        celebrate(Celebrations.between(before, repository.state.value))
    }

    fun setName(name: String) = repository.update { it.withName(name) }
    fun setTitle(title: String) = repository.update { it.withTitle(title) }
    fun setCharacter(character: CharacterClass) = repository.update { it.withCharacter(character) }
    fun setSound(on: Boolean) = repository.update { it.withSound(on) }
    fun setLevelSettings(settings: OverallLevelSettings) = repository.update { it.withLevelSettings(settings) }

    fun addSkill(draft: SkillDraft) = repository.update { it.addSkill(UUID.randomUUID().toString(), draft) }
    fun editSkill(id: String, draft: SkillDraft) = repository.update { it.editSkill(id, draft) }
    fun deleteSkill(id: String) = repository.update { it.deleteSkill(id) }
    fun gainXp(id: String) = updateWithFanfare { it.gainXp(id) }
    fun loseXp(id: String) = repository.update { it.loseXp(id) }

    fun addQuest(draft: QuestDraft) = repository.update { it.addQuest(UUID.randomUUID().toString(), draft) }
    fun editQuest(id: String, draft: QuestDraft) = repository.update { it.editQuest(id, draft) }
    fun deleteQuest(id: String) = repository.update { it.deleteQuest(id) }
    fun completeQuest(id: String) = updateWithFanfare { it.completeQuest(id, System.currentTimeMillis()) }
    fun uncompleteQuest(id: String) = repository.update { it.uncompleteQuest(id) }

    fun onResume() = repository.update { it.rollOverDay(LocalDate.now()) }
    fun markPermissionPrompted() = repository.update { it.markPermissionPrompted() }
}
