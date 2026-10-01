package com.example.ui.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.HabitApplication
import com.example.data.model.CompletionState
import com.example.data.model.DailyLog
import com.example.data.model.Habit
import com.example.data.model.HabitType
import com.example.data.model.SubHabit
import com.example.data.model.SubHabitLog
import com.example.domain.HabitStreakStats
import com.example.domain.StreakCalculator
import com.example.util.DateUtils
import com.example.widget.HabitGlanceWidget
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class HabitDetailUiState {
    data object Loading : HabitDetailUiState()
    data object NotFound : HabitDetailUiState()
    data class Success(
        val habit: Habit,
        val subHabitsWithLogs: List<Pair<SubHabit, Boolean>>,
        val todayLog: DailyLog?,
        val logs90Days: List<DailyLog>,
        val stats: HabitStreakStats
    ) : HabitDetailUiState()
}

class HabitDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as HabitApplication
    private val repository = app.repository
    private val completionUseCase = app.completionUseCase

    private val _uiState = MutableStateFlow<HabitDetailUiState>(HabitDetailUiState.Loading)
    val uiState: StateFlow<HabitDetailUiState> = _uiState.asStateFlow()

    private var currentHabitId: Long = -1L

    fun loadHabit(habitId: Long) {
        currentHabitId = habitId
        refreshData()
    }

    private fun refreshData() {
        if (currentHabitId == -1L) return
        viewModelScope.launch {
            val habit = repository.getHabitById(currentHabitId)
            if (habit == null) {
                _uiState.value = HabitDetailUiState.NotFound
                return@launch
            }

            val todayIso = DateUtils.todayIso()
            val today = DateUtils.today()
            val ninetyDaysAgoIso = DateUtils.toIso(today.minusDays(90))

            val allLogs = repository.getDailyLogsForHabit(habit.id)
            val todayLog = repository.getDailyLog(habit.id, todayIso)
            val logs90Days = allLogs.filter { it.date >= ninetyDaysAgoIso }
            val stats = StreakCalculator.calculate(allLogs, today)

            val subHabitsWithLogs = if (habit.type == HabitType.PARENT) {
                val subHabits = repository.getSubHabitsForParent(habit.id)
                val subLogs = repository.getSubHabitLogsForParentDate(habit.id, todayIso).associateBy { it.subHabitId }
                subHabits.map { sub ->
                    sub to (subLogs[sub.id]?.isCompleted == true)
                }
            } else {
                emptyList()
            }

            _uiState.value = HabitDetailUiState.Success(
                habit = habit,
                subHabitsWithLogs = subHabitsWithLogs,
                todayLog = todayLog,
                logs90Days = logs90Days,
                stats = stats
            )
        }
    }

    fun toggleTodayHabit() {
        if (currentHabitId == -1L) return
        viewModelScope.launch {
            completionUseCase.toggleHabit(currentHabitId, DateUtils.todayIso())
            refreshData()
            runCatching {
                HabitGlanceWidget().updateAll(getApplication())
            }
        }
    }

    fun toggleSubHabit(subHabitId: Long) {
        if (currentHabitId == -1L) return
        viewModelScope.launch {
            completionUseCase.toggleSubHabit(subHabitId, currentHabitId, DateUtils.todayIso())
            refreshData()
            runCatching {
                HabitGlanceWidget().updateAll(getApplication())
            }
        }
    }

    fun setArchived(archived: Boolean, onComplete: () -> Unit) {
        viewModelScope.launch {
            val habit = repository.getHabitById(currentHabitId) ?: return@launch
            repository.updateHabit(habit.copy(archived = archived))
            runCatching {
                HabitGlanceWidget().updateAll(getApplication())
            }
            onComplete()
        }
    }

    fun deleteHabit(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deleteHabitById(currentHabitId)
            runCatching {
                HabitGlanceWidget().updateAll(getApplication())
            }
            onComplete()
        }
    }
}
