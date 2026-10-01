package com.example.ui.edit

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.HabitApplication
import com.example.data.model.Habit
import com.example.data.model.HabitType
import com.example.data.model.SubHabit
import com.example.ui.theme.HabitColorPalette
import com.example.widget.HabitGlanceWidget
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SubHabitDraft(
    val id: Long = 0,
    val name: String = ""
)

data class AddEditUiState(
    val habitId: Long? = null,
    val name: String = "",
    val description: String = "",
    val emoji: String = "⚡",
    val colorHex: String = HabitColorPalette.first(),
    val type: HabitType = HabitType.SINGLE,
    val subHabitDrafts: List<SubHabitDraft> = emptyList(),
    val isSaving: Boolean = false,
    val error: String? = null
)

class AddEditHabitViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as HabitApplication
    private val repository = app.repository

    private val _uiState = MutableStateFlow(AddEditUiState())
    val uiState: StateFlow<AddEditUiState> = _uiState.asStateFlow()

    fun loadHabit(habitId: Long?) {
        if (habitId == null || habitId <= 0) {
            _uiState.update { AddEditUiState() }
            return
        }
        viewModelScope.launch {
            val habit = repository.getHabitById(habitId) ?: return@launch
            val subHabits = repository.getSubHabitsForParent(habitId)
            _uiState.update {
                AddEditUiState(
                    habitId = habit.id,
                    name = habit.name,
                    description = habit.description,
                    emoji = habit.emoji,
                    colorHex = habit.colorHex,
                    type = habit.type,
                    subHabitDrafts = subHabits.map { SubHabitDraft(id = it.id, name = it.name) }
                )
            }
        }
    }

    fun setName(name: String) = _uiState.update { it.copy(name = name, error = null) }
    fun setDescription(desc: String) = _uiState.update { it.copy(description = desc) }
    fun setEmoji(emoji: String) = _uiState.update { it.copy(emoji = emoji) }
    fun setColorHex(color: String) = _uiState.update { it.copy(colorHex = color) }
    fun setType(type: HabitType) = _uiState.update {
        val drafts = if (type == HabitType.PARENT && it.subHabitDrafts.isEmpty()) {
            listOf(SubHabitDraft(name = "Step 1"), SubHabitDraft(name = "Step 2"))
        } else it.subHabitDrafts
        it.copy(type = type, subHabitDrafts = drafts)
    }

    fun addSubHabit(name: String = "") {
        _uiState.update { state ->
            state.copy(subHabitDrafts = state.subHabitDrafts + SubHabitDraft(name = name))
        }
    }

    fun updateSubHabitName(index: Int, name: String) {
        _uiState.update { state ->
            val updated = state.subHabitDrafts.toMutableList()
            if (index in updated.indices) {
                updated[index] = updated[index].copy(name = name)
            }
            state.copy(subHabitDrafts = updated)
        }
    }

    fun removeSubHabit(index: Int) {
        _uiState.update { state ->
            val updated = state.subHabitDrafts.toMutableList()
            if (index in updated.indices) {
                updated.removeAt(index)
            }
            state.copy(subHabitDrafts = updated)
        }
    }

    fun save(onSuccess: (Long) -> Unit) {
        val currentState = _uiState.value
        val trimmedName = currentState.name.trim()
        if (trimmedName.isBlank()) {
            _uiState.update { it.copy(error = "Please enter a habit name.") }
            return
        }

        if (currentState.type == HabitType.PARENT) {
            val validSubHabits = currentState.subHabitDrafts.filter { it.name.trim().isNotBlank() }
            if (validSubHabits.isEmpty()) {
                _uiState.update { it.copy(error = "A parent habit must have at least one sub-habit.") }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val habitId: Long
            if (currentState.habitId == null || currentState.habitId <= 0) {
                // Insert new
                habitId = repository.insertHabit(
                    Habit(
                        name = trimmedName,
                        description = currentState.description.trim(),
                        emoji = currentState.emoji.ifBlank { "⚡" },
                        colorHex = currentState.colorHex,
                        type = currentState.type
                    )
                )
            } else {
                // Update existing
                habitId = currentState.habitId
                val existing = repository.getHabitById(habitId)
                repository.updateHabit(
                    Habit(
                        id = habitId,
                        name = trimmedName,
                        description = currentState.description.trim(),
                        emoji = currentState.emoji.ifBlank { "⚡" },
                        colorHex = currentState.colorHex,
                        type = currentState.type,
                        createdAt = existing?.createdAt ?: System.currentTimeMillis(),
                        archived = existing?.archived ?: false
                    )
                )
            }

            // If Parent, sync sub-habits
            if (currentState.type == HabitType.PARENT) {
                val currentInDb = repository.getSubHabitsForParent(habitId)
                val validDrafts = currentState.subHabitDrafts.filter { it.name.trim().isNotBlank() }
                val draftIds = validDrafts.map { it.id }.toSet()

                // Delete removed
                for (existing in currentInDb) {
                    if (!draftIds.contains(existing.id)) {
                        repository.deleteSubHabit(existing)
                    }
                }

                // Insert or update
                validDrafts.forEachIndexed { index, draft ->
                    if (draft.id > 0) {
                        repository.updateSubHabit(
                            SubHabit(
                                id = draft.id,
                                parentHabitId = habitId,
                                name = draft.name.trim(),
                                sortOrder = index + 1
                            )
                        )
                    } else {
                        repository.insertSubHabit(
                            SubHabit(
                                parentHabitId = habitId,
                                name = draft.name.trim(),
                                sortOrder = index + 1
                            )
                        )
                    }
                }
            } else {
                // If converted to single, remove any lingering sub-habits
                repository.deleteSubHabitsForParent(habitId)
            }

            // Refresh widgets
            runCatching {
                HabitGlanceWidget().updateAll(getApplication())
            }

            _uiState.update { it.copy(isSaving = false) }
            onSuccess(habitId)
        }
    }
}
