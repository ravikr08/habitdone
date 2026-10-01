package com.example.data.local

import android.content.Context
import com.example.data.model.CompletionState
import com.example.data.model.DailyLog
import com.example.data.model.Habit
import com.example.data.model.HabitType
import com.example.data.model.SubHabit
import com.example.data.model.SubHabitLog
import com.example.util.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object InitialDataSeeder {

    private const val PREFS_NAME = "habitdone_init_prefs"
    private const val KEY_SEEDED = "initial_data_seeded"

    suspend fun seedIfNecessary(context: Context, dao: HabitDao) = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_SEEDED, false)) {
            return@withContext
        }

        // Check if database already has habits
        val existingHabits = dao.getAllHabits()
        if (existingHabits.isNotEmpty()) {
            prefs.edit().putBoolean(KEY_SEEDED, true).apply()
            return@withContext
        }

        val today = DateUtils.today()

        // 1. Morning Stretch (SINGLE)
        val dsaHabitId = dao.insertHabit(
            Habit(
                name = "Morning Stretch",
                description = "Daily 20 mins flexibility, yoga & mindfulness routine",
                emoji = "🧘",
                colorHex = "#C084FC", // Lilac Purple as in reference image
                type = HabitType.SINGLE,
                createdAt = System.currentTimeMillis() - (60L * 24 * 3600 * 1000)
            )
        )

        // 2. Gym Session (SINGLE)
        val workoutHabitId = dao.insertHabit(
            Habit(
                name = "Gym Session",
                description = "Strength training, calisthenics & progressive overload",
                emoji = "🏋️",
                colorHex = "#4ADE80", // Mint Green as in reference image
                type = HabitType.SINGLE,
                createdAt = System.currentTimeMillis() - (55L * 24 * 3600 * 1000)
            )
        )

        // 3. Tech Podcast Walk (SINGLE)
        val podcastHabitId = dao.insertHabit(
            Habit(
                name = "Tech Podcast Walk",
                description = "Evening walk while listening to engineering podcasts",
                emoji = "🎧",
                colorHex = "#818CF8", // Indigo/Blue as in reference image
                type = HabitType.SINGLE,
                createdAt = System.currentTimeMillis() - (50L * 24 * 3600 * 1000)
            )
        )

        // 4. Daily Supplements (PARENT)
        val supplementsHabitId = dao.insertHabit(
            Habit(
                name = "Daily Supplements",
                description = "Essential daily micronutrients and morning hydration",
                emoji = "💊",
                colorHex = "#FB923C", // Warm Orange as in reference image
                type = HabitType.PARENT,
                createdAt = System.currentTimeMillis() - (65L * 24 * 3600 * 1000)
            )
        )

        // Sub-habits for Daily Supplements: Vitamin D, Omega-3, Magnesium
        val vitDId = dao.insertSubHabit(
            SubHabit(
                parentHabitId = supplementsHabitId,
                name = "Vitamin D",
                sortOrder = 1
            )
        )
        val omegaId = dao.insertSubHabit(
            SubHabit(
                parentHabitId = supplementsHabitId,
                name = "Omega-3",
                sortOrder = 2
            )
        )
        val magId = dao.insertSubHabit(
            SubHabit(
                parentHabitId = supplementsHabitId,
                name = "Magnesium",
                sortOrder = 3
            )
        )

        // Seed 60 days of historical logs for rich, realistic grid patterns
        val logs = mutableListOf<DailyLog>()
        val subLogs = mutableListOf<SubHabitLog>()

        for (i in 1..60) {
            val date = today.minusDays(i.toLong())
            val dateIso = DateUtils.toIso(date)

            // Morning Stretch: High consistency (12+ day current streak)
            if (i <= 12 || (i % 7 != 2 && i % 5 != 0)) {
                logs.add(
                    DailyLog(
                        habitId = dsaHabitId,
                        date = dateIso,
                        completionState = CompletionState.COMPLETED
                    )
                )
            }

            // Gym Session: 4-5 days a week pattern
            if (i % 7 == 1 || i % 7 == 2 || i % 7 == 4 || i % 7 == 5) {
                logs.add(
                    DailyLog(
                        habitId = workoutHabitId,
                        date = dateIso,
                        completionState = CompletionState.COMPLETED
                    )
                )
            }

            // Tech Podcast Walk: 3-4 days a week
            if (i % 3 != 0) {
                logs.add(
                    DailyLog(
                        habitId = podcastHabitId,
                        date = dateIso,
                        completionState = CompletionState.COMPLETED
                    )
                )
            }

            // Daily Supplements:
            if (i == 3) {
                // Partial on day 3: Vit D and Omega-3 checked, Magnesium unchecked
                subLogs.add(SubHabitLog(subHabitId = vitDId, parentHabitId = supplementsHabitId, date = dateIso, isCompleted = true))
                subLogs.add(SubHabitLog(subHabitId = omegaId, parentHabitId = supplementsHabitId, date = dateIso, isCompleted = true))
                subLogs.add(SubHabitLog(subHabitId = magId, parentHabitId = supplementsHabitId, date = dateIso, isCompleted = false))
                logs.add(DailyLog(habitId = supplementsHabitId, date = dateIso, completionState = CompletionState.PARTIAL))
            } else if (i % 6 != 0) {
                // Completed
                subLogs.add(SubHabitLog(subHabitId = vitDId, parentHabitId = supplementsHabitId, date = dateIso, isCompleted = true))
                subLogs.add(SubHabitLog(subHabitId = omegaId, parentHabitId = supplementsHabitId, date = dateIso, isCompleted = true))
                subLogs.add(SubHabitLog(subHabitId = magId, parentHabitId = supplementsHabitId, date = dateIso, isCompleted = true))
                logs.add(DailyLog(habitId = supplementsHabitId, date = dateIso, completionState = CompletionState.COMPLETED))
            }
        }

        if (logs.isNotEmpty()) {
            dao.insertDailyLogs(logs)
        }
        if (subLogs.isNotEmpty()) {
            dao.upsertSubHabitLogs(subLogs)
        }

        prefs.edit().putBoolean(KEY_SEEDED, true).apply()
    }
}
