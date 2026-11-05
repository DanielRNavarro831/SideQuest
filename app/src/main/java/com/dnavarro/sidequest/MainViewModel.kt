package com.dnavarro.sidequest

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.dnavarro.sidequest.model.AppState
import com.dnavarro.sidequest.model.CharacterClass
import com.dnavarro.sidequest.model.OverallLevelSettings
import com.dnavarro.sidequest.model.SkillDraft
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import java.util.UUID

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = (app as SideQuestApp).repository
    val state: StateFlow<AppState> = repository.state

    fun setName(name: String) = repository.update { it.withName(name) }
    fun setTitle(title: String) = repository.update { it.withTitle(title) }
    fun setCharacter(character: CharacterClass) = repository.update { it.withCharacter(character) }
    fun setLevelSettings(settings: OverallLevelSettings) = repository.update { it.withLevelSettings(settings) }

    fun addSkill(draft: SkillDraft) = repository.update { it.addSkill(UUID.randomUUID().toString(), draft) }
    fun editSkill(id: String, draft: SkillDraft) = repository.update { it.editSkill(id, draft) }
    fun deleteSkill(id: String) = repository.update { it.deleteSkill(id) }
    fun gainXp(id: String) = repository.update { it.gainXp(id) }
    fun loseXp(id: String) = repository.update { it.loseXp(id) }

    fun onResume() = repository.update { it.rollOverDay(LocalDate.now()) }
    fun markPermissionPrompted() = repository.update { it.markPermissionPrompted() }
}
