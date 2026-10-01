package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CompletionState
import com.example.data.model.DailyLog
import com.example.ui.theme.HeatmapCellBorder
import com.example.ui.theme.HeatmapCellEmpty
import com.example.ui.theme.HeatmapCellFuture
import com.example.ui.theme.HeatmapTodayBorder
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.util.DateUtils
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HeatmapView(
    logs: List<DailyLog>,
    accentColor: Color,
    modifier: Modifier = Modifier,
    weeksCount: Int = 18, // Dense weekly columns (~18 weeks = ~126 days, matching reference)
    cellSize: Dp = 14.dp,
    cellSpacing: Dp = 3.dp,
    showDayLabels: Boolean = true,
    showMonthLabels: Boolean = true,
    showLegend: Boolean = false,
    onDateSelected: ((LocalDate, CompletionState) -> Unit)? = null
) {
    val today = remember { DateUtils.today() }
    val logsMap = remember(logs) { logs.associate { it.date to it.completionState } }

    // Align calendar so the current week is the last column
    val currentWeekSunday = remember(today) {
        today.plusDays((6 - DateUtils.dayOfWeekMonToSun(today)).toLong())
    }
    // Start week: weeksCount - 1 weeks prior
    val calendarStart = remember(currentWeekSunday, weeksCount) {
        currentWeekSunday.minusWeeks((weeksCount - 1).toLong()).minusDays(6)
    }

    // Build list of weeks (each week has 7 days from Mon to Sun)
    val weeks = remember(calendarStart, currentWeekSunday) {
        val weekList = mutableListOf<List<LocalDate>>()
        var curDate = calendarStart
        while (!curDate.isAfter(currentWeekSunday)) {
            val week = (0..6).map { curDate.plusDays(it.toLong()) }
            weekList.add(week)
            curDate = curDate.plusWeeks(1)
        }
        weekList
    }

    // Month label for each week column (show month name if this week starts a new month)
    val monthLabels = remember(weeks) {
        val monthFormatter = DateTimeFormatter.ofPattern("MMM", Locale.getDefault())
        var lastMonth = -1
        weeks.map { week ->
            // Check if any day in the week is the 1st of the month, or first week
            val firstDay = week.first()
            if (firstDay.monthValue != lastMonth) {
                lastMonth = firstDay.monthValue
                firstDay.format(monthFormatter)
            } else {
                null
            }
        }
    }

    val scrollState = rememberScrollState()
    var selectedDateInfo by remember { mutableStateOf<Pair<LocalDate, CompletionState>?>(null) }

    // Scroll to latest days (right side) on first load
    LaunchedEffect(weeks) {
        scrollState.scrollTo(scrollState.maxValue)
    }

    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.Top) {
            // Day-of-week labels column on the left (M, W, F, S)
            if (showDayLabels) {
                Column(
                    modifier = Modifier
                        .padding(end = 6.dp)
                        .padding(top = if (showMonthLabels) 18.dp else 0.dp),
                    verticalArrangement = Arrangement.spacedBy(cellSpacing)
                ) {
                    val labels = listOf("M", "", "W", "", "F", "", "S")
                    for (label in labels) {
                        Box(
                            modifier = Modifier.size(width = 12.dp, height = cellSize),
                            contentAlignment = Alignment.Center
                        ) {
                            if (label.isNotEmpty()) {
                                Text(
                                    text = label,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextTertiary
                                )
                            }
                        }
                    }
                }
            }

            // Horizontally scrollable grid of weeks
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(cellSpacing)
            ) {
                weeks.forEachIndexed { colIdx, week ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(cellSpacing)
                    ) {
                        // Month label above column if present
                        if (showMonthLabels) {
                            val monthName = monthLabels.getOrNull(colIdx)
                            Box(
                                modifier = Modifier
                                    .height(18.dp)
                                    .width(cellSize + 8.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (monthName != null) {
                                    Text(
                                        text = monthName,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextTertiary,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        // 7 days for this week
                        for (date in week) {
                            val isFuture = date.isAfter(today)
                            val isToday = (date == today)
                            val dateIso = DateUtils.toIso(date)
                            val state = logsMap[dateIso] ?: CompletionState.INCOMPLETE

                            HeatmapCell(
                                date = date,
                                state = state,
                                isToday = isToday,
                                isFuture = isFuture,
                                accentColor = accentColor,
                                cellSize = cellSize,
                                onClick = {
                                    if (!isFuture) {
                                        selectedDateInfo = date to state
                                        onDateSelected?.invoke(date, state)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // Selected Date Feedback banner if tapped
        selectedDateInfo?.let { (date, state) ->
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E2132))
                    .border(1.dp, HeatmapCellBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val stateDesc = when (state) {
                    CompletionState.COMPLETED -> "✓ Completed"
                    CompletionState.PARTIAL -> "◐ Partial completion"
                    CompletionState.INCOMPLETE -> "○ Incomplete"
                }
                Text(
                    text = "${DateUtils.formatShortDate(date)}: $stateDesc",
                    fontSize = 11.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Legend if requested
        if (showLegend) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.padding(start = if (showDayLabels) 18.dp else 0.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                LegendItem(color = HeatmapCellEmpty, label = "Incomplete")
                LegendItem(color = accentColor.copy(alpha = 0.5f), label = "Partial")
                LegendItem(color = accentColor, label = "Completed")
                LegendItem(
                    color = HeatmapCellEmpty,
                    borderColor = HeatmapTodayBorder,
                    label = "Today"
                )
            }
        }
    }
}

@Composable
private fun HeatmapCell(
    date: LocalDate,
    state: CompletionState,
    isToday: Boolean,
    isFuture: Boolean,
    accentColor: Color,
    cellSize: Dp,
    onClick: () -> Unit
) {
    val cellShape = RoundedCornerShape(3.5.dp)

    val (cellBg, cellBorder) = when {
        isFuture -> HeatmapCellFuture to null
        state == CompletionState.COMPLETED -> accentColor to null
        state == CompletionState.PARTIAL -> accentColor.copy(alpha = 0.45f) to HeatmapCellBorder
        else -> HeatmapCellEmpty to null
    }

    val finalBorder = when {
        isToday -> HeatmapTodayBorder
        cellBorder != null -> cellBorder
        else -> null
    }

    Box(
        modifier = Modifier
            .size(cellSize)
            .clip(cellShape)
            .then(
                if (finalBorder != null) {
                    Modifier.border(
                        width = if (isToday) 1.5.dp else 1.dp,
                        color = finalBorder,
                        shape = cellShape
                    )
                } else Modifier
            )
            .background(cellBg)
            .clickable(enabled = !isFuture, onClick = onClick)
    )
}

@Composable
private fun LegendItem(
    color: Color,
    label: String,
    borderColor: Color? = null
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(2.5.dp))
                .then(
                    if (borderColor != null) {
                        Modifier.border(1.dp, borderColor, RoundedCornerShape(2.5.dp))
                    } else Modifier
                )
                .background(color)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextSecondary
        )
    }
}
