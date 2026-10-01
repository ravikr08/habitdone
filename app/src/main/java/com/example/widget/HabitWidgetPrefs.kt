package com.example.widget

import android.content.Context
import android.content.SharedPreferences

object HabitWidgetPrefs {
    private const val PREFS_NAME = "habit_done_widget_prefs"
    private const val KEY_PREFIX_WIDGET = "widget_habit_"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveHabitId(context: Context, appWidgetId: Int, habitId: Long) {
        getPrefs(context).edit().putLong("$KEY_PREFIX_WIDGET$appWidgetId", habitId).apply()
    }

    fun getHabitId(context: Context, appWidgetId: Int): Long? {
        val prefs = getPrefs(context)
        val key = "$KEY_PREFIX_WIDGET$appWidgetId"
        return if (prefs.contains(key)) prefs.getLong(key, -1L).takeIf { it != -1L } else null
    }

    fun removeWidget(context: Context, appWidgetId: Int) {
        getPrefs(context).edit().remove("$KEY_PREFIX_WIDGET$appWidgetId").apply()
    }

    fun getAllWidgetIdsForHabit(context: Context, habitId: Long): List<Int> {
        val prefs = getPrefs(context)
        val result = mutableListOf<Int>()
        for ((key, value) in prefs.all) {
            if (key.startsWith(KEY_PREFIX_WIDGET) && value == habitId) {
                val widgetIdStr = key.removePrefix(KEY_PREFIX_WIDGET)
                widgetIdStr.toIntOrNull()?.let { result.add(it) }
            }
        }
        return result
    }
}
