package com.example.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.HabitCard
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ReferenceNeonGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun HomeScreen(
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToAdd: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground),
        containerColor = DarkBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAdd,
                containerColor = ReferenceNeonGreen,
                contentColor = Color.Black,
                shape = CircleShape,
                modifier = Modifier
                    .size(56.dp)
                    .testTag("add_habit_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Habit",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Reference Style Top Bar: "Today, 4th Apr" + Icons
            ReferenceTopBar(
                todayHeader = uiState.todayHeader,
                onSettingsClick = onNavigateToSettings
            )

            // Day of Week Capsules Row (Sun, Mon, Tue... Today)
            DayCapsulesRow(
                capsules = uiState.weekCapsules,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Category Filter Pills Row (Fitness, Health, Morning, etc.)
            CategoryFiltersRow(
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = { viewModel.setCategory(it) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Habit Cards List (Large, prominent cards with rich heatmaps)
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ReferenceNeonGreen)
                }
            } else if (uiState.items.isEmpty()) {
                EmptyStateCard(
                    isArchived = uiState.showArchived,
                    onAddClick = onNavigateToAdd,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(
                        items = uiState.items,
                        key = { it.habit.id }
                    ) { item ->
                        HabitCard(
                            habit = item.habit,
                            subHabits = item.subHabits,
                            subHabitLogs = item.subHabitLogs,
                            todayLog = item.todayLog,
                            recentLogs = item.recentLogs,
                            streakDays = item.streakDays,
                            onCardClick = { onNavigateToDetail(item.habit.id) },
                            onToggleClick = { viewModel.toggleHabit(item.habit.id) },
                            onToggleSubHabit = { subId -> viewModel.toggleSubHabit(subId, item.habit.id) },
                            onArchiveClick = { viewModel.setArchived(item.habit.id, !item.habit.archived) },
                            onDeleteClick = { viewModel.deleteHabit(item.habit.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReferenceTopBar(
    todayHeader: String,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = todayHeader.ifBlank { "Today" },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            fontSize = 20.sp
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Insights",
                    tint = ReferenceNeonGreen,
                    modifier = Modifier.size(22.dp)
                )
            }

            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Default.CloudDone,
                    contentDescription = "Offline Storage Active",
                    tint = TextTertiary,
                    modifier = Modifier.size(22.dp)
                )
            }

            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier.testTag("settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = ReferenceNeonGreen,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun DayCapsulesRow(
    capsules: List<WeekDayCapsule>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (capsule in capsules) {
            DayCapsuleItem(capsule = capsule)
        }
    }
}

@Composable
private fun DayCapsuleItem(
    capsule: WeekDayCapsule
) {
    val isToday = capsule.isToday
    val shape = RoundedCornerShape(14.dp)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(44.dp)
    ) {
        Text(
            text = capsule.dayName,
            fontSize = 11.sp,
            color = if (isToday) ReferenceNeonGreen else TextTertiary,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .size(width = 44.dp, height = 36.dp)
                .clip(shape)
                .background(Color(0xFF141724))
                .then(
                    if (isToday) {
                        Modifier.border(2.dp, ReferenceNeonGreen, shape)
                    } else {
                        Modifier.border(1.dp, ReferenceNeonGreen.copy(alpha = 0.35f), shape)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            val fractionText = if (capsule.totalCount > 0) "${capsule.completedCount}/${capsule.totalCount}" else "-"
            Text(
                text = fractionText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isToday) TextPrimary else TextSecondary
            )
        }
    }
}

@Composable
private fun CategoryFiltersRow(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf(
        CategoryItem("All", null),
        CategoryItem("Fitness", Icons.Default.FitnessCenter),
        CategoryItem("Health", Icons.Default.Favorite),
        CategoryItem("Morning", Icons.Default.WbSunny),
        CategoryItem("Archived", null)
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (cat in categories) {
            val isSelected = selectedCategory == cat.name
            CategoryPill(
                item = cat,
                isSelected = isSelected,
                onClick = { onCategorySelected(cat.name) }
            )
        }
    }
}

private data class CategoryItem(
    val name: String,
    val icon: ImageVector?
)

@Composable
private fun CategoryPill(
    item: CategoryItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val pillShape = RoundedCornerShape(14.dp)

    Row(
        modifier = Modifier
            .clip(pillShape)
            .background(if (isSelected) ReferenceNeonGreen.copy(alpha = 0.12f) else DarkSurfaceElevated)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) ReferenceNeonGreen else DarkSurfaceBorder,
                shape = pillShape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (item.icon != null) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = if (isSelected) ReferenceNeonGreen else TextSecondary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }

        Text(
            text = item.name,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) ReferenceNeonGreen else TextSecondary
        )
    }
}

@Composable
private fun EmptyStateCard(
    isArchived: Boolean,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(DarkSurfaceElevated),
            contentAlignment = Alignment.Center
        ) {
            Text(text = if (isArchived) "📦" else "✨", fontSize = 34.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isArchived) "No Archived Habits" else "Build Beautiful Daily Grids",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (isArchived) {
                "Archived habits will appear here."
            } else {
                "Track your daily habits and watch your heatmaps light up."
            },
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = 24.dp),
            lineHeight = 18.sp
        )
    }
}
