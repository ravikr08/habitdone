package com.example.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.HabitApplication
import com.example.MainActivity
import com.example.data.model.CompletionState
import com.example.domain.StreakCalculator
import com.example.util.DateUtils
import java.time.LocalDate

class HabitGlanceWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

    companion object {
        val habitIdKey = ActionParameters.Key<Long>("habit_id")
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val app = (context.applicationContext as? HabitApplication) ?: HabitApplication.instance
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
        val habitId = HabitWidgetPrefs.getHabitId(context, appWidgetId)

        val habit = habitId?.let { app.repository.getHabitById(it) }
        val today = DateUtils.today()
        val todayIso = DateUtils.todayIso()
        val todayLog = habit?.let { app.repository.getDailyLog(it.id, todayIso) }

        // Fetch logs for up to 120 days for dense widget heatmap
        val pastLogs = habit?.let {
            app.repository.getDailyLogsBetween(
                it.id,
                DateUtils.toIso(today.minusDays(120)),
                todayIso
            )
        } ?: emptyList()

        val streakStats = StreakCalculator.calculate(pastLogs, today)
        val logsMap = pastLogs.associate { it.date to it.completionState }

        provideContent {
            val size = LocalSize.current
            val isWide = size.width >= 220.dp
            val numColumns = if (isWide) 15 else 8
            val cellSize = if (isWide) 11.5.dp else 12.dp
            val cellSpacing = 2.5.dp

            val habitColor = habit?.let {
                runCatching { Color(android.graphics.Color.parseColor(it.colorHex)) }
                    .getOrDefault(Color(0xFFC084FC))
            } ?: Color(0xFFC084FC)

            val widgetBg = ColorProvider(Color(0xFF131522))
            val textWhite = ColorProvider(Color(0xFFFFFFFF))
            val textMuted = ColorProvider(Color(0xFF9DA3BD))
            val emptyCellColor = ColorProvider(Color(0xFF1E2132))
            val futureCellColor = ColorProvider(Color(0xFF12131D))
            val completedColor = ColorProvider(habitColor)
            val partialColor = ColorProvider(habitColor.copy(alpha = 0.5f))
            val todayHighlightColor = ColorProvider(Color(0xFFFFFFFF))

            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(widgetBg)
                    .cornerRadius(22.dp)
                    .padding(14.dp)
            ) {
                if (habit == null) {
                    // Unconfigured state
                    val appWidgetIdExtraKey = ActionParameters.Key<Int>(AppWidgetManager.EXTRA_APPWIDGET_ID)
                    Column(
                        modifier = GlanceModifier
                            .fillMaxSize()
                            .clickable(
                                actionStartActivity<HabitWidgetConfigActivity>(
                                    actionParametersOf(appWidgetIdExtraKey to appWidgetId)
                                )
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "⚡ HabitDone",
                            style = TextStyle(
                                color = textWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = GlanceModifier.height(4.dp))
                        Text(
                            text = "Tap to configure habit",
                            style = TextStyle(
                                color = ColorProvider(Color(0xFF2EE886)),
                                fontSize = 12.sp
                            )
                        )
                    }
                } else {
                    val habitExtraKey = ActionParameters.Key<Long>(MainActivity.EXTRA_HABIT_ID)
                    val isCompletedToday = todayLog?.completionState == CompletionState.COMPLETED
                    val isPartialToday = todayLog?.completionState == CompletionState.PARTIAL

                    // Deep-link intent for tapping widget body
                    Column(
                        modifier = GlanceModifier
                            .fillMaxSize()
                            .clickable(
                                actionStartActivity<MainActivity>(
                                    actionParametersOf(habitExtraKey to habit.id)
                                )
                            )
                    ) {
                        // Header Row: Title & Streak on Left, Squircle Button on Right
                        Row(
                            modifier = GlanceModifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = GlanceModifier.defaultWeight()) {
                                Text(
                                    text = habit.name,
                                    maxLines = 1,
                                    style = TextStyle(
                                        color = textWhite,
                                        fontSize = if (isWide) 16.sp else 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = GlanceModifier.height(2.dp))
                                val streakText = "🔥 Streak: ${streakStats.currentStreak} days"
                                Text(
                                    text = streakText,
                                    style = TextStyle(
                                        color = ColorProvider(habitColor),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }

                            Spacer(modifier = GlanceModifier.width(8.dp))

                            // Reference Squircle Completion Button
                            val buttonBg = when {
                                isCompletedToday -> ColorProvider(habitColor)
                                isPartialToday -> ColorProvider(habitColor.copy(alpha = 0.35f))
                                else -> ColorProvider(Color(0xFF1E2132))
                            }
                            val buttonText = when {
                                isCompletedToday -> "✓"
                                isPartialToday -> "◐"
                                else -> "○"
                            }
                            val buttonTextColor = when {
                                isCompletedToday -> ColorProvider(Color(0xFFFFFFFF))
                                isPartialToday -> ColorProvider(habitColor)
                                else -> ColorProvider(Color(0xFF8E95AA))
                            }

                            Box(
                                modifier = GlanceModifier
                                    .size(38.dp)
                                    .background(buttonBg)
                                    .cornerRadius(12.dp)
                                    .clickable(
                                        actionRunCallback<HabitWidgetToggleActionCallback>(
                                            actionParametersOf(habitIdKey to habit.id)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = buttonText,
                                    style = TextStyle(
                                        color = buttonTextColor,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        Spacer(modifier = GlanceModifier.height(10.dp))

                        // Dense 7-Row Heatmap Grid (Matching Reference Image 2)
                        // Align so current week is the rightmost column
                        val currentWeekSunday = today.plusDays((6 - DateUtils.dayOfWeekMonToSun(today)).toLong())
                        val calendarStart = currentWeekSunday.minusWeeks((numColumns - 1).toLong()).minusDays(6)

                        Row(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .defaultWeight(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (col in 0 until numColumns) {
                                val weekStart = calendarStart.plusWeeks(col.toLong())
                                Column(
                                    modifier = GlanceModifier.defaultWeight(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    for (dayIdx in 0..6) {
                                        val cellDate = weekStart.plusDays(dayIdx.toLong())
                                        val isCellToday = (cellDate == today)
                                        val isCellFuture = cellDate.isAfter(today)
                                        val dateIso = DateUtils.toIso(cellDate)
                                        val cellState = logsMap[dateIso] ?: CompletionState.INCOMPLETE

                                        val cellColor = when {
                                            isCellFuture -> futureCellColor
                                            cellState == CompletionState.COMPLETED -> completedColor
                                            cellState == CompletionState.PARTIAL -> partialColor
                                            else -> emptyCellColor
                                        }

                                        if (isCellToday) {
                                            // Today's cell highlighted with white outer frame
                                            Box(
                                                modifier = GlanceModifier
                                                    .size(cellSize)
                                                    .background(todayHighlightColor)
                                                    .cornerRadius(2.5.dp)
                                                    .padding(1.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Box(
                                                    modifier = GlanceModifier
                                                        .fillMaxSize()
                                                        .background(cellColor)
                                                        .cornerRadius(1.5.dp)
                                                ) {}
                                            }
                                        } else {
                                            Box(
                                                modifier = GlanceModifier
                                                    .size(cellSize)
                                                    .background(cellColor)
                                                    .cornerRadius(2.dp)
                                            ) {}
                                        }

                                        if (dayIdx < 6) {
                                            Spacer(modifier = GlanceModifier.height(cellSpacing))
                                        }
                                    }
                                }

                                if (col < numColumns - 1) {
                                    Spacer(modifier = GlanceModifier.width(cellSpacing))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
