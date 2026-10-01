package com.example.domain

import com.example.data.model.CompletionState
import com.example.data.model.DailyLog
import com.example.data.model.HabitType
import com.example.data.model.SubHabitLog
import com.example.data.repository.HabitRepository
import com.example.util.DateUtils

class HabitCompletionUseCase(private val repository: HabitRepository) {

    suspend fun toggleHabit(habitId: Long, date: String = DateUtils.todayIso()): CompletionState {
        val habit = repository.getHabitById(habitId) ?: return CompletionState.INCOMPLETE
        return if (habit.type == HabitType.PARENT) {
            toggleParentHabit(habitId, date)
        } else {
            toggleSingleHabit(habitId, date)
        }
    }

    suspend fun toggleSingleHabit(habitId: Long, date: String = DateUtils.todayIso()): CompletionState {
        val currentLog = repository.getDailyLog(habitId, date)
        val newState = if (currentLog?.completionState == CompletionState.COMPLETED) {
            CompletionState.INCOMPLETE
        } else {
            CompletionState.COMPLETED
        }
        val logId = currentLog?.id ?: 0L
        repository.upsertDailyLog(
            DailyLog(
                id = logId,
                habitId = habitId,
                date = date,
                completionState = newState
            )
        )
        return newState
    }

    suspend fun toggleParentHabit(parentHabitId: Long, date: String = DateUtils.todayIso()): CompletionState {
        val currentParentLog = repository.getDailyLog(parentHabitId, date)
        val isCurrentlyCompleted = currentParentLog?.completionState == CompletionState.COMPLETED

        // If currently completed, uncheck parent -> all children become unchecked
        // If currently incomplete or partial, check parent -> all children become checked
        val targetChildrenCompleted = !isCurrentlyCompleted
        val targetParentState = if (targetChildrenCompleted) {
            CompletionState.COMPLETED
        } else {
            CompletionState.INCOMPLETE
        }

        // Update all active sub-habits
        val subHabits = repository.getSubHabitsForParent(parentHabitId)
        val existingSubLogs = repository.getSubHabitLogsForParentDate(parentHabitId, date).associateBy { it.subHabitId }

        val subLogsToSave = subHabits.map { subHabit ->
            val existing = existingSubLogs[subHabit.id]
            SubHabitLog(
                id = existing?.id ?: 0L,
                subHabitId = subHabit.id,
                parentHabitId = parentHabitId,
                date = date,
                isCompleted = targetChildrenCompleted
            )
        }
        if (subLogsToSave.isNotEmpty()) {
            repository.upsertSubHabitLogs(subLogsToSave)
        }

        // Update parent log
        val parentLogId = currentParentLog?.id ?: 0L
        repository.upsertDailyLog(
            DailyLog(
                id = parentLogId,
                habitId = parentHabitId,
                date = date,
                completionState = targetParentState
            )
        )

        return targetParentState
    }

    suspend fun toggleSubHabit(
        subHabitId: Long,
        parentHabitId: Long,
        date: String = DateUtils.todayIso()
    ): CompletionState {
        val currentSubLog = repository.getSubHabitLog(subHabitId, date)
        val newChildCompleted = !(currentSubLog?.isCompleted ?: false)
        return setSubHabitCompletion(subHabitId, parentHabitId, newChildCompleted, date)
    }

    suspend fun setSubHabitCompletion(
        subHabitId: Long,
        parentHabitId: Long,
        isCompleted: Boolean,
        date: String = DateUtils.todayIso()
    ): CompletionState {
        val currentSubLog = repository.getSubHabitLog(subHabitId, date)
        val subLogId = currentSubLog?.id ?: 0L
        repository.upsertSubHabitLog(
            SubHabitLog(
                id = subLogId,
                subHabitId = subHabitId,
                parentHabitId = parentHabitId,
                date = date,
                isCompleted = isCompleted
            )
        )

        // Evaluate parent state based on all active children
        val subHabits = repository.getSubHabitsForParent(parentHabitId)
        if (subHabits.isEmpty()) {
            return CompletionState.INCOMPLETE
        }

        val allSubLogs = repository.getSubHabitLogsForParentDate(parentHabitId, date).associateBy { it.subHabitId }
        val completedCount = subHabits.count { sub ->
            allSubLogs[sub.id]?.isCompleted == true
        }

        val parentState = when {
            completedCount == subHabits.size -> CompletionState.COMPLETED
            completedCount > 0 -> CompletionState.PARTIAL
            else -> CompletionState.INCOMPLETE
        }

        val currentParentLog = repository.getDailyLog(parentHabitId, date)
        val parentLogId = currentParentLog?.id ?: 0L
        repository.upsertDailyLog(
            DailyLog(
                id = parentLogId,
                habitId = parentHabitId,
                date = date,
                completionState = parentState
            )
        )

        return parentState
    }

    suspend fun getHabitCompletionState(habitId: Long, date: String = DateUtils.todayIso()): CompletionState {
        return repository.getDailyLog(habitId, date)?.completionState ?: CompletionState.INCOMPLETE
    }
}
