package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CompletionState
import com.example.data.model.DailyLog
import com.example.data.model.Habit
import com.example.data.model.HabitType
import com.example.data.model.SubHabit
import com.example.data.model.SubHabitLog
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun HabitCard(
    habit: Habit,
    subHabits: List<SubHabit>,
    subHabitLogs: List<SubHabitLog>,
    todayLog: DailyLog?,
    recentLogs: List<DailyLog>,
    streakDays: Int,
    onCardClick: () -> Unit,
    onToggleClick: () -> Unit,
    onToggleSubHabit: ((Long) -> Unit)? = null,
    onEditClick: (() -> Unit)? = null,
    onArchiveClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val habitColor = runCatching { Color(android.graphics.Color.parseColor(habit.colorHex)) }
        .getOrDefault(MaterialTheme.colorScheme.primary)

    val completionState = todayLog?.completionState ?: CompletionState.INCOMPLETE
    var showMenu by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }

    val animatedBorderColor by animateColorAsState(
        targetValue = when (completionState) {
            CompletionState.COMPLETED -> habitColor.copy(alpha = 0.65f)
            CompletionState.PARTIAL -> habitColor.copy(alpha = 0.45f)
            CompletionState.INCOMPLETE -> habitColor.copy(alpha = 0.25f)
        },
        animationSpec = spring(),
        label = "borderColor"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(1.5.dp, animatedBorderColor, RoundedCornerShape(24.dp))
            .clickable(onClick = onCardClick)
            .testTag("habit_card_${habit.id}"),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurfaceCard
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row: Avatar, Title & Streak, and Right Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar Circle
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(habitColor.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = habit.emoji,
                        fontSize = 24.sp
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Title & Streak Subtitle
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 17.sp
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🔥 Streak: $streakDays",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (streakDays > 0) habitColor else TextSecondary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )

                        if (habit.type == HabitType.PARENT && subHabits.isNotEmpty()) {
                            Text(
                                text = " · ${subHabits.size} steps",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextTertiary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Squircle Check Button (Reference Style)
                ReferenceCompletionButton(
                    state = completionState,
                    accentColor = habitColor,
                    onClick = onToggleClick,
                    modifier = Modifier.testTag("habit_toggle_button_${habit.id}")
                )

                Spacer(modifier = Modifier.width(4.dp))

                // Overflow Menu
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = TextTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(DarkSurfaceElevated)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit Habit") },
                            onClick = {
                                showMenu = false
                                onEditClick?.invoke() ?: onCardClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (habit.archived) "Unarchive" else "Archive") },
                            onClick = {
                                showMenu = false
                                onArchiveClick?.invoke()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = Color(0xFFFF5252)) },
                            onClick = {
                                showMenu = false
                                onDeleteClick?.invoke()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Prominent Full-Width Heatmap (Major visual feature as in reference)
            HeatmapView(
                logs = recentLogs,
                accentColor = habitColor,
                weeksCount = 18,
                cellSize = 13.5.dp,
                cellSpacing = 3.dp,
                showDayLabels = true,
                showMonthLabels = true,
                showLegend = false,
                modifier = Modifier.fillMaxWidth()
            )

            // Sub-habits quick drawer for Parent Habits
            if (habit.type == HabitType.PARENT && subHabits.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurfaceElevated.copy(alpha = 0.6f))
                        .clickable { isExpanded = !isExpanded }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val completedSubCount = subHabitLogs.count { it.isCompleted }
                    Text(
                        text = "Sub-habits: $completedSubCount of ${subHabits.size} completed",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (completedSubCount == subHabits.size) habitColor else TextSecondary
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = TextTertiary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                AnimatedVisibility(visible = isExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val subLogsMap = subHabitLogs.associateBy { it.subHabitId }
                        subHabits.forEach { subHabit ->
                            val isCompleted = subLogsMap[subHabit.id]?.isCompleted == true
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurfaceElevated)
                                    .clickable { onToggleSubHabit?.invoke(subHabit.id) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isCompleted,
                                    onCheckedChange = { onToggleSubHabit?.invoke(subHabit.id) },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = habitColor,
                                        checkmarkColor = Color.Black,
                                        uncheckedColor = TextTertiary
                                    ),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = subHabit.name,
                                    fontSize = 13.sp,
                                    color = if (isCompleted) TextPrimary else TextSecondary,
                                    fontWeight = if (isCompleted) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReferenceCompletionButton(
    state: CompletionState,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonShape = RoundedCornerShape(14.dp)

    Box(
        modifier = modifier
            .size(width = 46.dp, height = 44.dp)
            .clip(buttonShape)
            .then(
                when (state) {
                    CompletionState.COMPLETED -> Modifier.background(accentColor)
                    CompletionState.PARTIAL -> Modifier
                        .background(accentColor.copy(alpha = 0.25f))
                        .border(1.5.dp, accentColor, buttonShape)
                    CompletionState.INCOMPLETE -> Modifier
                        .background(Color(0xFF1E2132))
                        .border(1.dp, DarkSurfaceBorder, buttonShape)
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        when (state) {
            CompletionState.COMPLETED -> {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            CompletionState.PARTIAL -> {
                Text(
                    text = "◐",
                    color = accentColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            CompletionState.INCOMPLETE -> {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .border(1.5.dp, TextTertiary, RoundedCornerShape(4.dp))
                )
            }
        }
    }
}
