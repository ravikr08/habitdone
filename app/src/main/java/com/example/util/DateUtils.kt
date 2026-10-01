package com.example.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

object DateUtils {
    val ISO_FORMATTER: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val DISPLAY_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.getDefault())
    private val SHORT_DISPLAY_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())

    fun today(): LocalDate = LocalDate.now()

    fun todayIso(): String = today().format(ISO_FORMATTER)

    fun toIso(date: LocalDate): String = date.format(ISO_FORMATTER)

    fun fromIso(iso: String): LocalDate = LocalDate.parse(iso, ISO_FORMATTER)

    fun formatHeaderDate(date: LocalDate = today()): String {
        return date.format(DISPLAY_FORMATTER)
    }

    fun formatShortDate(date: LocalDate): String {
        return date.format(SHORT_DISPLAY_FORMATTER)
    }

    /**
     * Generates a list of consecutive ISO dates covering the last [daysCount] days up to [endDate].
     * Defaults to the last 90 days up to today.
     * Returned from oldest to newest.
     */
    fun getLastNDays(daysCount: Int = 90, endDate: LocalDate = today()): List<LocalDate> {
        val startDate = endDate.minusDays((daysCount - 1).toLong())
        val days = mutableListOf<LocalDate>()
        var cur = startDate
        while (!cur.isAfter(endDate)) {
            days.add(cur)
            cur = cur.plusDays(1)
        }
        return days
    }

    /**
     * Day of week from 0 (Monday) to 6 (Sunday)
     */
    fun dayOfWeekMonToSun(date: LocalDate): Int {
        return date.dayOfWeek.value - 1 // 1..7 -> 0..6
    }
}
