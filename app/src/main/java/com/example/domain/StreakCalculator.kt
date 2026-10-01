package com.example.domain

import com.example.data.model.CompletionState
import com.example.data.model.DailyLog
import com.example.util.DateUtils
import java.time.LocalDate

data class HabitStreakStats(
    val currentStreak: Int,
    val longestStreak: Int,
    val totalCompletions: Int,
    val rate30Days: Float, // 0.0 .. 100.0
    val rate90Days: Float  // 0.0 .. 100.0
)

object StreakCalculator {

    fun calculate(logs: List<DailyLog>, today: LocalDate = DateUtils.today()): HabitStreakStats {
        val completedDates = logs
            .filter { it.completionState == CompletionState.COMPLETED }
            .mapNotNull {
                runCatching { DateUtils.fromIso(it.date) }.getOrNull()
            }
            .toSet()

        val totalCompletions = completedDates.size

        // Calculate current streak
        var currentStreak = 0
        val isTodayCompleted = completedDates.contains(today)
        val startDate = if (isTodayCompleted) today else today.minusDays(1)

        var cur = startDate
        while (completedDates.contains(cur)) {
            currentStreak++
            cur = cur.minusDays(1)
        }

        // Calculate longest streak
        var longestStreak = 0
        if (completedDates.isNotEmpty()) {
            val sortedDates = completedDates.sorted()
            var tempStreak = 0
            var prevDate: LocalDate? = null

            for (date in sortedDates) {
                if (prevDate == null) {
                    tempStreak = 1
                } else if (date == prevDate.plusDays(1)) {
                    tempStreak++
                } else {
                    if (tempStreak > longestStreak) longestStreak = tempStreak
                    tempStreak = 1
                }
                prevDate = date
            }
            if (tempStreak > longestStreak) longestStreak = tempStreak
        }

        // Calculate 30-day and 90-day completion rates
        var completedLast30 = 0
        for (i in 0 until 30) {
            val d = today.minusDays(i.toLong())
            if (completedDates.contains(d)) completedLast30++
        }
        val rate30Days = (completedLast30 / 30f) * 100f

        var completedLast90 = 0
        for (i in 0 until 90) {
            val d = today.minusDays(i.toLong())
            if (completedDates.contains(d)) completedLast90++
        }
        val rate90Days = (completedLast90 / 90f) * 100f

        return HabitStreakStats(
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            totalCompletions = totalCompletions,
            rate30Days = rate30Days,
            rate90Days = rate90Days
        )
    }
}
