package com.example.ui.detail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.CompletionState
import com.example.data.model.Habit
import com.example.data.model.HabitType
import com.example.domain.HabitStreakStats
import com.example.ui.components.HeatmapView
import com.example.ui.components.StatusBadge
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(
    habitId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    viewModel: HabitDetailViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    LaunchedEffect(habitId) {
        viewModel.loadHabit(habitId)
    }

    BackHandler {
        onNavigateBack()
    }

    val uiState by viewModel.uiState.collectAsState()
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    when (val state = uiState) {
        is HabitDetailUiState.Loading -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(DarkBackground),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = NeonCyan)
            }
        }
        is HabitDetailUiState.NotFound -> {
            Scaffold(
                containerColor = DarkBackground,
                topBar = {
                    TopAppBar(
                        title = { Text("Habit Not Found") },
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
                    )
                }
            ) { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("This habit could not be found or was deleted.", color = TextSecondary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onNavigateBack,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black)
                        ) {
                            Text("Return to Dashboard")
                        }
                    }
                }
            }
        }
        is HabitDetailUiState.Success -> {
            val habit = state.habit
            val habitColor = runCatching { Color(android.graphics.Color.parseColor(habit.colorHex)) }
                .getOrDefault(NeonCyan)

            if (showDeleteConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmDialog = false },
                    title = { Text("Delete Habit", color = TextPrimary) },
                    text = {
                        Text(
                            "Are you sure you want to delete '${habit.name}'? All history and sub-habits will be permanently removed.",
                            color = TextSecondary
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showDeleteConfirmDialog = false
                                viewModel.deleteHabit(onComplete = onNavigateBack)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF5252),
                                contentColor = Color.White
                            ),
                            modifier = Modifier.testTag("confirm_delete_button")
                        ) {
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteConfirmDialog = false }) {
                            Text("Cancel", color = TextSecondary)
                        }
                    },
                    containerColor = DarkSurfaceElevated
                )
            }

            Scaffold(
                modifier = modifier
                    .fillMaxSize()
                    .background(DarkBackground),
                containerColor = DarkBackground,
                topBar = {
                    TopAppBar(
                        title = { Text(habit.name, maxLines = 1, fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = TextPrimary
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { onNavigateToEdit(habit.id) }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Habit", tint = TextPrimary)
                            }
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "More Options", tint = TextPrimary)
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                modifier = Modifier.background(DarkSurfaceElevated)
                            ) {
                                DropdownMenuItem(
                                    text = { Text(if (habit.archived) "Unarchive Habit" else "Archive Habit") },
                                    leadingIcon = {
                                        Icon(
                                            if (habit.archived) Icons.Default.Unarchive else Icons.Default.Archive,
                                            contentDescription = null
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        viewModel.setArchived(!habit.archived) {}
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete Habit", color = Color(0xFFFF5252)) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFF5252))
                                    },
                                    onClick = {
                                        showMenu = false
                                        showDeleteConfirmDialog = true
                                    }
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = DarkBackground,
                            titleContentColor = TextPrimary
                        )
                    )
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Habit Header Card
                    HabitHeaderCard(
                        habit = habit,
                        habitColor = habitColor
                    )

                    // Today's Status & Action Card
                    TodayStatusCard(
                        habit = habit,
                        habitColor = habitColor,
                        state = state.todayLog?.completionState ?: CompletionState.INCOMPLETE,
                        subHabitsCount = state.subHabitsWithLogs.size,
                        onToggle = { viewModel.toggleTodayHabit() }
                    )

                    // Sub-habits Section (If Parent Habit)
                    if (habit.type == HabitType.PARENT) {
                        SubHabitsChecklistSection(
                            subHabitsWithLogs = state.subHabitsWithLogs,
                            habitColor = habitColor,
                            onToggleSubHabit = { viewModel.toggleSubHabit(it) }
                        )
                    }

                    // 90-Day Heatmap Section
                    HeatmapSection(
                        logs = state.logs90Days,
                        accentColor = habitColor
                    )

                    // Statistics Row
                    StatisticsSection(stats = state.stats)

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun HabitHeaderCard(
    habit: Habit,
    habitColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, habitColor.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(habitColor.copy(alpha = 0.16f))
                    .border(1.5.dp, habitColor.copy(alpha = 0.4f), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = habit.emoji, fontSize = 32.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                if (habit.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = habit.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val typeLabel = if (habit.type == HabitType.PARENT) "Parent Routine" else "Single Habit"
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkSurfaceElevated)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = typeLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NeonCyan
                        )
                    }
                    if (habit.archived) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF3E2723))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Archived",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFFFB74D)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TodayStatusCard(
    habit: Habit,
    habitColor: Color,
    state: CompletionState,
    subHabitsCount: Int,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Today's Status",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                StatusBadge(
                    state = state,
                    accentColor = habitColor
                )
            }

            val buttonText = when (state) {
                CompletionState.COMPLETED -> "✓ Completed"
                CompletionState.PARTIAL -> "Finish All (${subHabitsCount})"
                CompletionState.INCOMPLETE -> "Check as Done"
            }
            val buttonBg = when (state) {
                CompletionState.COMPLETED -> habitColor
                CompletionState.PARTIAL -> Color(0xFFFFB300)
                CompletionState.INCOMPLETE -> habitColor.copy(alpha = 0.15f)
            }
            val buttonTextColor = when (state) {
                CompletionState.COMPLETED -> Color.Black
                CompletionState.PARTIAL -> Color.Black
                CompletionState.INCOMPLETE -> habitColor
            }

            Button(
                onClick = onToggle,
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonBg,
                    contentColor = buttonTextColor
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("detail_toggle_today_button")
            ) {
                Text(
                    text = buttonText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun SubHabitsChecklistSection(
    subHabitsWithLogs: List<Pair<com.example.data.model.SubHabit, Boolean>>,
    habitColor: Color,
    onToggleSubHabit: (Long) -> Unit
) {
    val completedCount = subHabitsWithLogs.count { it.second }
    val totalCount = subHabitsWithLogs.size

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Sub-Habits",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "$completedCount of $totalCount done",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (completedCount == totalCount && totalCount > 0) NeonEmerald else TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for ((subHabit, isDone) in subHabitsWithLogs) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDone) habitColor.copy(alpha = 0.12f) else DarkSurfaceElevated)
                            .border(
                                1.dp,
                                if (isDone) habitColor.copy(alpha = 0.4f) else DarkSurfaceBorder,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { onToggleSubHabit(subHabit.id) }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .testTag("sub_habit_row_${subHabit.id}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isDone,
                            onCheckedChange = { onToggleSubHabit(subHabit.id) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = habitColor,
                                checkmarkColor = Color.Black,
                                uncheckedColor = TextTertiary
                            ),
                            modifier = Modifier.testTag("sub_habit_checkbox_${subHabit.id}")
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = subHabit.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isDone) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isDone) TextPrimary else TextSecondary,
                            modifier = Modifier.weight(1f)
                        )

                        if (isDone) {
                            Text(
                                text = "Done",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = habitColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeatmapSection(
    logs: List<com.example.data.model.DailyLog>,
    accentColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "90-Day Calendar Heatmap",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Scroll horizontally to inspect recent activity",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            HeatmapView(
                logs = logs,
                accentColor = accentColor,
                weeksCount = 18,
                cellSize = 15.dp,
                cellSpacing = 3.dp,
                showDayLabels = true,
                showMonthLabels = true,
                showLegend = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun StatisticsSection(stats: HabitStreakStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Performance & Streaks",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Current Streak",
                    value = "${stats.currentStreak}d",
                    icon = "🔥",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Longest Streak",
                    value = "${stats.longestStreak}d",
                    icon = "🏆",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "30-Day Rate",
                    value = "${stats.rate30Days.toInt()}%",
                    icon = "📈",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "90-Day Rate",
                    value = "${stats.rate90Days.toInt()}%",
                    icon = "🎯",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = icon, fontSize = 14.sp)
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
        }
    }
}
