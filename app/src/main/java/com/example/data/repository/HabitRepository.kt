package com.example.data.repository

import com.example.data.local.HabitDao
import com.example.data.model.DailyLog
import com.example.data.model.Habit
import com.example.data.model.SubHabit
import com.example.data.model.SubHabitLog
import kotlinx.coroutines.flow.Flow

class HabitRepository(private val habitDao: HabitDao) {

    val allHabits: Flow<List<Habit>> = habitDao.getAllHabitsFlow()
    val activeHabits: Flow<List<Habit>> = habitDao.getActiveHabitsFlow()
    val allDailyLogs: Flow<List<DailyLog>> = habitDao.getAllDailyLogsFlow()
    val allSubHabits: Flow<List<SubHabit>> = habitDao.getAllSubHabitsFlow()
    val allSubHabitLogs: Flow<List<SubHabitLog>> = habitDao.getAllSubHabitLogsFlow()

    fun getHabitByIdFlow(id: Long): Flow<Habit?> = habitDao.getHabitByIdFlow(id)

    suspend fun getHabitById(id: Long): Habit? = habitDao.getHabitById(id)

    suspend fun getAllHabits(): List<Habit> = habitDao.getAllHabits()

    suspend fun insertHabit(habit: Habit): Long = habitDao.insertHabit(habit)

    suspend fun updateHabit(habit: Habit) = habitDao.updateHabit(habit)

    suspend fun deleteHabit(habit: Habit) = habitDao.deleteHabit(habit)

    suspend fun deleteHabitById(id: Long) = habitDao.deleteHabitById(id)

    // Sub-habits
    fun getSubHabitsForParentFlow(parentId: Long): Flow<List<SubHabit>> =
        habitDao.getSubHabitsForParentFlow(parentId)

    suspend fun getSubHabitsForParent(parentId: Long): List<SubHabit> =
        habitDao.getSubHabitsForParent(parentId)

    suspend fun getAllSubHabits(): List<SubHabit> = habitDao.getAllSubHabits()

    suspend fun insertSubHabit(subHabit: SubHabit): Long = habitDao.insertSubHabit(subHabit)

    suspend fun insertSubHabits(subHabits: List<SubHabit>): List<Long> =
        habitDao.insertSubHabits(subHabits)

    suspend fun updateSubHabit(subHabit: SubHabit) = habitDao.updateSubHabit(subHabit)

    suspend fun deleteSubHabit(subHabit: SubHabit) = habitDao.deleteSubHabit(subHabit)

    suspend fun deleteSubHabitsForParent(parentId: Long) =
        habitDao.deleteSubHabitsForParent(parentId)

    // Daily Logs
    fun getDailyLogsForHabitFlow(habitId: Long): Flow<List<DailyLog>> =
        habitDao.getDailyLogsForHabitFlow(habitId)

    suspend fun getDailyLogsForHabit(habitId: Long): List<DailyLog> =
        habitDao.getDailyLogsForHabit(habitId)

    fun getDailyLogsBetweenFlow(habitId: Long, startDate: String, endDate: String): Flow<List<DailyLog>> =
        habitDao.getDailyLogsBetweenFlow(habitId, startDate, endDate)

    suspend fun getDailyLogsBetween(habitId: Long, startDate: String, endDate: String): List<DailyLog> =
        habitDao.getDailyLogsBetween(habitId, startDate, endDate)

    fun getDailyLogsForDateFlow(date: String): Flow<List<DailyLog>> =
        habitDao.getDailyLogsForDateFlow(date)

    suspend fun getDailyLogsForDate(date: String): List<DailyLog> =
        habitDao.getDailyLogsForDate(date)

    suspend fun getDailyLog(habitId: Long, date: String): DailyLog? =
        habitDao.getDailyLog(habitId, date)

    suspend fun getAllDailyLogs(): List<DailyLog> = habitDao.getAllDailyLogs()

    suspend fun upsertDailyLog(log: DailyLog): Long = habitDao.upsertDailyLog(log)

    suspend fun deleteDailyLog(habitId: Long, date: String) =
        habitDao.deleteDailyLog(habitId, date)

    // Sub-Habit Logs
    fun getSubHabitLogsForParentDateFlow(parentId: Long, date: String): Flow<List<SubHabitLog>> =
        habitDao.getSubHabitLogsForParentDateFlow(parentId, date)

    suspend fun getSubHabitLogsForParentDate(parentId: Long, date: String): List<SubHabitLog> =
        habitDao.getSubHabitLogsForParentDate(parentId, date)

    suspend fun getSubHabitLog(subHabitId: Long, date: String): SubHabitLog? =
        habitDao.getSubHabitLog(subHabitId, date)

    suspend fun getAllSubHabitLogs(): List<SubHabitLog> = habitDao.getAllSubHabitLogs()

    suspend fun upsertSubHabitLog(log: SubHabitLog): Long = habitDao.upsertSubHabitLog(log)

    suspend fun upsertSubHabitLogs(logs: List<SubHabitLog>): List<Long> =
        habitDao.upsertSubHabitLogs(logs)

    suspend fun restoreDatabase(
        habits: List<Habit>,
        subHabits: List<SubHabit>,
        dailyLogs: List<DailyLog>,
        subHabitLogs: List<SubHabitLog>
    ) = habitDao.restoreDatabase(habits, subHabits, dailyLogs, subHabitLogs)
}
