package com.example.data.backup

import android.content.Context
import android.net.Uri
import com.example.data.model.CompletionState
import com.example.data.model.DailyLog
import com.example.data.model.Habit
import com.example.data.model.HabitType
import com.example.data.model.SubHabit
import com.example.data.model.SubHabitLog
import com.example.data.repository.HabitRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.time.Instant

sealed class BackupResult {
    data class Success(val message: String, val habitsCount: Int) : BackupResult()
    data class Error(val error: String) : BackupResult()
}

class HabitBackupManager(private val repository: HabitRepository) {

    companion object {
        const val CURRENT_FORMAT_VERSION = 1
    }

    suspend fun exportToJson(context: Context, targetUri: Uri): BackupResult = withContext(Dispatchers.IO) {
        try {
            val habits = repository.getAllHabits()
            val subHabits = repository.getAllSubHabits()
            val dailyLogs = repository.getAllDailyLogs()
            val subHabitLogs = repository.getAllSubHabitLogs()

            val rootJson = JSONObject()
            rootJson.put("formatVersion", CURRENT_FORMAT_VERSION)
            rootJson.put("exportedAt", Instant.now().toString())

            // Habits
            val habitsArray = JSONArray()
            for (h in habits) {
                val obj = JSONObject()
                obj.put("id", h.id)
                obj.put("name", h.name)
                obj.put("description", h.description)
                obj.put("emoji", h.emoji)
                obj.put("colorHex", h.colorHex)
                obj.put("type", h.type.name)
                obj.put("createdAt", h.createdAt)
                obj.put("archived", h.archived)
                habitsArray.put(obj)
            }
            rootJson.put("habits", habitsArray)

            // SubHabits
            val subHabitsArray = JSONArray()
            for (sh in subHabits) {
                val obj = JSONObject()
                obj.put("id", sh.id)
                obj.put("parentHabitId", sh.parentHabitId)
                obj.put("name", sh.name)
                obj.put("sortOrder", sh.sortOrder)
                obj.put("createdAt", sh.createdAt)
                obj.put("archived", sh.archived)
                subHabitsArray.put(obj)
            }
            rootJson.put("subHabits", subHabitsArray)

            // DailyLogs
            val dailyLogsArray = JSONArray()
            for (log in dailyLogs) {
                val obj = JSONObject()
                obj.put("id", log.id)
                obj.put("habitId", log.habitId)
                obj.put("date", log.date)
                obj.put("completionState", log.completionState.name)
                dailyLogsArray.put(obj)
            }
            rootJson.put("dailyLogs", dailyLogsArray)

            // SubHabitLogs
            val subHabitLogsArray = JSONArray()
            for (shl in subHabitLogs) {
                val obj = JSONObject()
                obj.put("id", shl.id)
                obj.put("subHabitId", shl.subHabitId)
                obj.put("parentHabitId", shl.parentHabitId)
                obj.put("date", shl.date)
                obj.put("isCompleted", shl.isCompleted)
                subHabitLogsArray.put(obj)
            }
            rootJson.put("subHabitLogs", subHabitLogsArray)

            context.contentResolver.openOutputStream(targetUri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(rootJson.toString(2))
                }
            } ?: return@withContext BackupResult.Error("Failed to open output file for writing.")

            BackupResult.Success("Data exported successfully (${habits.size} habits).", habits.size)
        } catch (e: Exception) {
            BackupResult.Error(e.localizedMessage ?: "Unknown export error")
        }
    }

    suspend fun importFromJson(context: Context, sourceUri: Uri): BackupResult = withContext(Dispatchers.IO) {
        try {
            val content = context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    reader.readText()
                }
            } ?: return@withContext BackupResult.Error("Could not open input backup file.")

            val rootJson = try {
                JSONObject(content)
            } catch (e: Exception) {
                return@withContext BackupResult.Error("Invalid JSON structure: ${e.message}")
            }

            // 1. Validate version
            if (!rootJson.has("formatVersion")) {
                return@withContext BackupResult.Error("Missing 'formatVersion' in backup file.")
            }
            val version = rootJson.getInt("formatVersion")
            if (version != CURRENT_FORMAT_VERSION) {
                return@withContext BackupResult.Error("Unsupported backup format version: $version (expected $CURRENT_FORMAT_VERSION).")
            }

            // 2. Parse Habits
            val habitsArray = rootJson.optJSONArray("habits")
                ?: return@withContext BackupResult.Error("Missing 'habits' array in backup file.")
            val habits = mutableListOf<Habit>()
            val habitIds = mutableSetOf<Long>()

            for (i in 0 until habitsArray.length()) {
                val obj = habitsArray.getJSONObject(i)
                val id = obj.getLong("id")
                habitIds.add(id)
                val habit = Habit(
                    id = id,
                    name = obj.getString("name"),
                    description = obj.optString("description", ""),
                    emoji = obj.optString("emoji", "⚡"),
                    colorHex = obj.optString("colorHex", "#00E5FF"),
                    type = runCatching { HabitType.valueOf(obj.getString("type")) }.getOrDefault(HabitType.SINGLE),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                    archived = obj.optBoolean("archived", false)
                )
                habits.add(habit)
            }

            // 3. Parse SubHabits and validate relationships
            val subHabitsArray = rootJson.optJSONArray("subHabits") ?: JSONArray()
            val subHabits = mutableListOf<SubHabit>()
            val subHabitIds = mutableSetOf<Long>()

            for (i in 0 until subHabitsArray.length()) {
                val obj = subHabitsArray.getJSONObject(i)
                val id = obj.getLong("id")
                val parentId = obj.getLong("parentHabitId")

                if (!habitIds.contains(parentId)) {
                    return@withContext BackupResult.Error(
                        "Integrity error: Sub-habit '${obj.optString("name")}' references missing parent habit id $parentId"
                    )
                }

                subHabitIds.add(id)
                subHabits.add(
                    SubHabit(
                        id = id,
                        parentHabitId = parentId,
                        name = obj.getString("name"),
                        sortOrder = obj.optInt("sortOrder", 0),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        archived = obj.optBoolean("archived", false)
                    )
                )
            }

            // 4. Parse DailyLogs and validate relationships
            val dailyLogsArray = rootJson.optJSONArray("dailyLogs") ?: JSONArray()
            val dailyLogs = mutableListOf<DailyLog>()

            for (i in 0 until dailyLogsArray.length()) {
                val obj = dailyLogsArray.getJSONObject(i)
                val habitId = obj.getLong("habitId")
                if (!habitIds.contains(habitId)) {
                    return@withContext BackupResult.Error(
                        "Integrity error: Daily log references missing habit id $habitId"
                    )
                }
                dailyLogs.add(
                    DailyLog(
                        id = obj.optLong("id", 0L),
                        habitId = habitId,
                        date = obj.getString("date"),
                        completionState = runCatching {
                            CompletionState.valueOf(obj.getString("completionState"))
                        }.getOrDefault(CompletionState.INCOMPLETE)
                    )
                )
            }

            // 5. Parse SubHabitLogs and validate relationships
            val subHabitLogsArray = rootJson.optJSONArray("subHabitLogs") ?: JSONArray()
            val subHabitLogs = mutableListOf<SubHabitLog>()

            for (i in 0 until subHabitLogsArray.length()) {
                val obj = subHabitLogsArray.getJSONObject(i)
                val subHabitId = obj.getLong("subHabitId")
                val parentId = obj.getLong("parentHabitId")

                if (!subHabitIds.contains(subHabitId)) {
                    return@withContext BackupResult.Error(
                        "Integrity error: Sub-habit log references missing sub-habit id $subHabitId"
                    )
                }
                if (!habitIds.contains(parentId)) {
                    return@withContext BackupResult.Error(
                        "Integrity error: Sub-habit log references missing parent habit id $parentId"
                    )
                }

                subHabitLogs.add(
                    SubHabitLog(
                        id = obj.optLong("id", 0L),
                        subHabitId = subHabitId,
                        parentHabitId = parentId,
                        date = obj.getString("date"),
                        isCompleted = obj.getBoolean("isCompleted")
                    )
                )
            }

            // 6. Restore atomically into Room
            repository.restoreDatabase(habits, subHabits, dailyLogs, subHabitLogs)

            BackupResult.Success("Successfully restored ${habits.size} habits and ${dailyLogs.size} logs.", habits.size)
        } catch (e: Exception) {
            BackupResult.Error("Failed to import backup: ${e.localizedMessage}")
        }
    }
}
