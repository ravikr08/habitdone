package com.example.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.HabitApplication
import com.example.data.model.CompletionState
import com.example.data.model.DailyLog
import com.example.data.model.Habit
import com.example.data.model.SubHabit
import com.example.data.model.SubHabitLog
import com.example.domain.StreakCalculator
import com.example.util.DateUtils
import com.example.widget.HabitGlanceWidget
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

data class WeekDayCapsule(
    val dayName: String,
    val date: LocalDate,
    val completedCount: Int,
    val totalCount: Int,
    val isToday: Boolean
)

data class HabitItemUiState(
    val habit: Habit,
    val subHabits: List<SubHabit>,
    val subHabitLogs: List<SubHabitLog>,
    val todayLog: DailyLog?,
    val recentLogs: List<DailyLog>,
    val streakDays: Int
)

data class HomeUiState(
    val todayHeader: String = "",
    val weekCapsules: List<WeekDayCapsule> = emptyList(),
    val items: List<HabitItemUiState> = emptyList(),
    val selectedCategory: String = "All",
    val totalCount: Int = 0,
    val completedCount: Int = 0,
    val showArchived: Boolean = false,
    val isLoading: Boolean = false
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as HabitApplication
    private val repository = app.repository
    private val completionUseCase = app.completionUseCase

    private val _showArchived = MutableStateFlow(false)
    private val _selectedCategory = MutableStateFlow("All")

    val uiState: StateFlow<HomeUiState> = combine(
        repository.allHabits,
        repository.allDailyLogs,
        repository.allSubHabits,
        repository.allSubHabitLogs,
        _showArchived
    ) { allHabits: List<Habit>, allLogs: List<DailyLog>, allSubHabits: List<SubHabit>, allSubLogs: List<SubHabitLog>, showArchived: Boolean ->
        val category = _selectedCategory.value
        val today = DateUtils.today()
        val todayIso = DateUtils.todayIso()
        val ninetyDaysAgoIso = DateUtils.toIso(today.minusDays(120))

        // Week capsules: last 6 days + today
        val dayFormatter = DateTimeFormatter.ofPattern("EEE", Locale.getDefault())
        val activeHabits = allHabits.filter { !it.archived }
        val logsByDate = allLogs.groupBy { it.date }

        val weekCapsules = (6 downTo 0).map { daysAgo ->
            val date = today.minusDays(daysAgo.toLong())
            val dateIso = DateUtils.toIso(date)
            val dateLogs = logsByDate[dateIso] ?: emptyList()
            val completedCount = activeHabits.count { habit ->
                dateLogs.any { it.habitId == habit.id && it.completionState == CompletionState.COMPLETED }
            }
            val isToday = daysAgo == 0
            val dayName = if (isToday) "Today" else date.format(dayFormatter)
            WeekDayCapsule(
                dayName = dayName,
                date = date,
                completedCount = completedCount,
                totalCount = activeHabits.size,
                isToday = isToday
            )
        }

        val filteredHabits = allHabits.filter { habit ->
            if (showArchived) habit.archived
            else !habit.archived
        }

        val subHabitsByParent = allSubHabits.groupBy { it.parentHabitId }
        val todaySubLogsByParent = allSubLogs.filter { it.date == todayIso }.groupBy { it.parentHabitId }
        val logsByHabit = allLogs.groupBy { it.habitId }

        val items = filteredHabits.map { habit ->
            val habitLogs = logsByHabit[habit.id] ?: emptyList()
            val todayLog = habitLogs.find { it.date == todayIso }
            val recentLogs = habitLogs.filter { it.date >= ninetyDaysAgoIso }
            val stats = StreakCalculator.calculate(habitLogs, today)
            val subHabits = subHabitsByParent[habit.id] ?: emptyList()
            val subLogs = todaySubLogsByParent[habit.id] ?: emptyList()

            HabitItemUiState(
                habit = habit,
                subHabits = subHabits,
                subHabitLogs = subLogs,
                todayLog = todayLog,
                recentLogs = recentLogs,
                streakDays = stats.currentStreak
            )
        }

        val completedToday = activeHabits.count { habit ->
            logsByHabit[habit.id]?.any { it.date == todayIso && it.completionState == CompletionState.COMPLETED } == true
        }

        val headerFormatter = DateTimeFormatter.ofPattern("EEEE, d MMM", Locale.getDefault())
        val formattedToday = "Today, " + today.format(DateTimeFormatter.ofPattern("d MMM", Locale.getDefault()))

        HomeUiState(
            todayHeader = formattedToday,
            weekCapsules = weekCapsules,
            items = items,
            selectedCategory = category,
            totalCount = activeHabits.size,
            completedCount = completedToday,
            showArchived = showArchived,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isLoading = true)
    )

    fun toggleHabit(habitId: Long) {
        viewModelScope.launch {
            completionUseCase.toggleHabit(habitId, DateUtils.todayIso())
            runCatching {
                HabitGlanceWidget().updateAll(getApplication())
            }
        }
    }

    fun toggleSubHabit(subHabitId: Long, parentHabitId: Long) {
        viewModelScope.launch {
            completionUseCase.toggleSubHabit(subHabitId, parentHabitId, DateUtils.todayIso())
            runCatching {
                HabitGlanceWidget().updateAll(getApplication())
            }
        }
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
        if (category == "Archived") {
            _showArchived.value = true
        } else {
            _showArchived.value = false
        }
    }

    fun setArchived(habitId: Long, archived: Boolean) {
        viewModelScope.launch {
            val habit = repository.getHabitById(habitId) ?: return@launch
            repository.updateHabit(habit.copy(archived = archived))
            runCatching {
                HabitGlanceWidget().updateAll(getApplication())
            }
        }
    }

    fun deleteHabit(habitId: Long) {
        viewModelScope.launch {
            repository.deleteHabitById(habitId)
            runCatching {
                HabitGlanceWidget().updateAll(getApplication())
            }
        }
    }
}
