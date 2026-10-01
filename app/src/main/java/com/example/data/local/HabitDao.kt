package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.DailyLog
import com.example.data.model.Habit
import com.example.data.model.SubHabit
import com.example.data.model.SubHabitLog
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    // --- Habits ---
    @Query("SELECT * FROM habits ORDER BY archived ASC, createdAt ASC")
    fun getAllHabitsFlow(): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE archived = 0 ORDER BY createdAt ASC")
    fun getActiveHabitsFlow(): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE id = :id LIMIT 1")
    suspend fun getHabitById(id: Long): Habit?

    @Query("SELECT * FROM habits WHERE id = :id LIMIT 1")
    fun getHabitByIdFlow(id: Long): Flow<Habit?>

    @Query("SELECT * FROM habits")
    suspend fun getAllHabits(): List<Habit>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabits(habits: List<Habit>): List<Long>

    @Update
    suspend fun updateHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteHabitById(id: Long)

    // --- Sub-Habits ---
    @Query("SELECT * FROM sub_habits WHERE parentHabitId = :parentHabitId AND archived = 0 ORDER BY sortOrder ASC, createdAt ASC")
    fun getSubHabitsForParentFlow(parentHabitId: Long): Flow<List<SubHabit>>

    @Query("SELECT * FROM sub_habits WHERE parentHabitId = :parentHabitId AND archived = 0 ORDER BY sortOrder ASC, createdAt ASC")
    suspend fun getSubHabitsForParent(parentHabitId: Long): List<SubHabit>

    @Query("SELECT * FROM sub_habits WHERE parentHabitId = :parentHabitId ORDER BY sortOrder ASC, createdAt ASC")
    suspend fun getAllSubHabitsForParentIncludingArchived(parentHabitId: Long): List<SubHabit>

    @Query("SELECT * FROM sub_habits")
    suspend fun getAllSubHabits(): List<SubHabit>

    @Query("SELECT * FROM sub_habits")
    fun getAllSubHabitsFlow(): Flow<List<SubHabit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubHabit(subHabit: SubHabit): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubHabits(subHabits: List<SubHabit>): List<Long>

    @Update
    suspend fun updateSubHabit(subHabit: SubHabit)

    @Delete
    suspend fun deleteSubHabit(subHabit: SubHabit)

    @Query("DELETE FROM sub_habits WHERE parentHabitId = :parentHabitId")
    suspend fun deleteSubHabitsForParent(parentHabitId: Long)

    // --- Daily Logs ---
    @Query("SELECT * FROM daily_logs WHERE habitId = :habitId AND date = :date LIMIT 1")
    suspend fun getDailyLog(habitId: Long, date: String): DailyLog?

    @Query("SELECT * FROM daily_logs WHERE habitId = :habitId ORDER BY date DESC")
    fun getDailyLogsForHabitFlow(habitId: Long): Flow<List<DailyLog>>

    @Query("SELECT * FROM daily_logs WHERE habitId = :habitId ORDER BY date DESC")
    suspend fun getDailyLogsForHabit(habitId: Long): List<DailyLog>

    @Query("SELECT * FROM daily_logs WHERE habitId = :habitId AND date >= :startDate AND date <= :endDate ORDER BY date ASC")
    fun getDailyLogsBetweenFlow(habitId: Long, startDate: String, endDate: String): Flow<List<DailyLog>>

    @Query("SELECT * FROM daily_logs WHERE habitId = :habitId AND date >= :startDate AND date <= :endDate ORDER BY date ASC")
    suspend fun getDailyLogsBetween(habitId: Long, startDate: String, endDate: String): List<DailyLog>

    @Query("SELECT * FROM daily_logs WHERE date = :date")
    fun getDailyLogsForDateFlow(date: String): Flow<List<DailyLog>>

    @Query("SELECT * FROM daily_logs WHERE date = :date")
    suspend fun getDailyLogsForDate(date: String): List<DailyLog>

    @Query("SELECT * FROM daily_logs")
    suspend fun getAllDailyLogs(): List<DailyLog>

    @Query("SELECT * FROM daily_logs")
    fun getAllDailyLogsFlow(): Flow<List<DailyLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDailyLog(log: DailyLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyLogs(logs: List<DailyLog>): List<Long>

    @Query("DELETE FROM daily_logs WHERE habitId = :habitId AND date = :date")
    suspend fun deleteDailyLog(habitId: Long, date: String)

    // --- Sub-Habit Logs ---
    @Query("SELECT * FROM sub_habit_logs WHERE subHabitId = :subHabitId AND date = :date LIMIT 1")
    suspend fun getSubHabitLog(subHabitId: Long, date: String): SubHabitLog?

    @Query("SELECT * FROM sub_habit_logs WHERE parentHabitId = :parentHabitId AND date = :date")
    fun getSubHabitLogsForParentDateFlow(parentHabitId: Long, date: String): Flow<List<SubHabitLog>>

    @Query("SELECT * FROM sub_habit_logs WHERE parentHabitId = :parentHabitId AND date = :date")
    suspend fun getSubHabitLogsForParentDate(parentHabitId: Long, date: String): List<SubHabitLog>

    @Query("SELECT * FROM sub_habit_logs WHERE parentHabitId = :parentHabitId")
    suspend fun getSubHabitLogsForParent(parentHabitId: Long): List<SubHabitLog>

    @Query("SELECT * FROM sub_habit_logs")
    suspend fun getAllSubHabitLogs(): List<SubHabitLog>

    @Query("SELECT * FROM sub_habit_logs")
    fun getAllSubHabitLogsFlow(): Flow<List<SubHabitLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSubHabitLog(log: SubHabitLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSubHabitLogs(logs: List<SubHabitLog>): List<Long>

    @Query("DELETE FROM sub_habit_logs WHERE subHabitId = :subHabitId AND date = :date")
    suspend fun deleteSubHabitLog(subHabitId: Long, date: String)

    @Query("DELETE FROM sub_habit_logs WHERE parentHabitId = :parentHabitId AND date = :date")
    suspend fun deleteSubHabitLogsForParentDate(parentHabitId: Long, date: String)

    // --- Cleanup / Restore ---
    @Query("DELETE FROM sub_habit_logs")
    suspend fun deleteAllSubHabitLogs()

    @Query("DELETE FROM daily_logs")
    suspend fun deleteAllDailyLogs()

    @Query("DELETE FROM sub_habits")
    suspend fun deleteAllSubHabits()

    @Query("DELETE FROM habits")
    suspend fun deleteAllHabits()

    @Transaction
    suspend fun restoreDatabase(
        habits: List<Habit>,
        subHabits: List<SubHabit>,
        dailyLogs: List<DailyLog>,
        subHabitLogs: List<SubHabitLog>
    ) {
        deleteAllSubHabitLogs()
        deleteAllDailyLogs()
        deleteAllSubHabits()
        deleteAllHabits()

        insertHabits(habits)
        insertSubHabits(subHabits)
        insertDailyLogs(dailyLogs)
        upsertSubHabitLogs(subHabitLogs)
    }
}
