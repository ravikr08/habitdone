package com.example.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.updateAll
import com.example.HabitApplication
import com.example.util.DateUtils

class HabitWidgetToggleActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val habitId = parameters[HabitGlanceWidget.habitIdKey] ?: return
        val app = (context.applicationContext as? HabitApplication) ?: HabitApplication.instance
        app.completionUseCase.toggleHabit(habitId, DateUtils.todayIso())
        HabitGlanceWidget().updateAll(context)
    }
}
