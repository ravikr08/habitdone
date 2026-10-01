package com.example.ui.edit

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.HabitType
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.HabitColorPalette
import com.example.ui.theme.HabitEmojiPresets
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditHabitScreen(
    habitId: Long?,
    onNavigateBack: () -> Unit,
    onSaved: (Long) -> Unit,
    viewModel: AddEditHabitViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    LaunchedEffect(habitId) {
        viewModel.loadHabit(habitId)
    }

    BackHandler {
        onNavigateBack()
    }

    val uiState by viewModel.uiState.collectAsState()
    val isEditMode = uiState.habitId != null && uiState.habitId!! > 0
    val selectedColor = runCatching { Color(android.graphics.Color.parseColor(uiState.colorHex)) }
        .getOrDefault(NeonCyan)

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground),
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditMode) "Edit Habit" else "New Habit",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Live Preview Card
            HabitPreviewBanner(
                name = uiState.name,
                emoji = uiState.emoji,
                color = selectedColor,
                type = uiState.type,
                subHabitCount = uiState.subHabitDrafts.size
            )

            // Name Input
            Column {
                Text(
                    text = "Habit Name",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = { viewModel.setName(it) },
                    placeholder = { Text("e.g., Morning DSA & System Design", color = TextTertiary) },
                    singleLine = true,
                    isError = uiState.error != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("habit_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = selectedColor,
                        unfocusedBorderColor = DarkSurfaceBorder,
                        focusedContainerColor = DarkSurfaceCard,
                        unfocusedContainerColor = DarkSurfaceCard,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                if (uiState.error != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = uiState.error!!, color = Color(0xFFFF5252), fontSize = 12.sp)
                }
            }

            // Description Input
            Column {
                Text(
                    text = "Description (Optional)",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = { viewModel.setDescription(it) },
                    placeholder = { Text("Add motivating notes or daily reminders...", color = TextTertiary) },
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("habit_description_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = selectedColor,
                        unfocusedBorderColor = DarkSurfaceBorder,
                        focusedContainerColor = DarkSurfaceCard,
                        unfocusedContainerColor = DarkSurfaceCard,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Habit Type Selector (Single vs Parent)
            Column {
                Text(
                    text = "Habit Structure",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TypeSelectionCard(
                        title = "Single Habit",
                        subtitle = "One single checkmark per day",
                        isSelected = uiState.type == HabitType.SINGLE,
                        accentColor = selectedColor,
                        onClick = { viewModel.setType(HabitType.SINGLE) },
                        modifier = Modifier.weight(1f)
                    )
                    TypeSelectionCard(
                        title = "Parent Routine",
                        subtitle = "Contains sub-habits with auto-parent sync",
                        isSelected = uiState.type == HabitType.PARENT,
                        accentColor = selectedColor,
                        onClick = { viewModel.setType(HabitType.PARENT) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // If Parent Routine: Sub-habits builder
            if (uiState.type == HabitType.PARENT) {
                SubHabitsBuilderSection(
                    drafts = uiState.subHabitDrafts,
                    accentColor = selectedColor,
                    onAddSubHabit = { viewModel.addSubHabit() },
                    onUpdateSubHabit = { idx, name -> viewModel.updateSubHabitName(idx, name) },
                    onRemoveSubHabit = { idx -> viewModel.removeSubHabit(idx) }
                )
            }

            // Icon & Emoji Picker
            Column {
                Text(
                    text = "Icon / Emoji",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (emoji in HabitEmojiPresets) {
                        val isSelected = uiState.emoji == emoji
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) selectedColor.copy(alpha = 0.2f) else DarkSurfaceElevated)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) selectedColor else DarkSurfaceBorder,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.setEmoji(emoji) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 22.sp)
                        }
                    }
                }
            }

            // Accent Color Swatches
            Column {
                Text(
                    text = "Accent Color",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    for (colorHex in HabitColorPalette) {
                        val color = runCatching { Color(android.graphics.Color.parseColor(colorHex)) }
                            .getOrDefault(NeonCyan)
                        val isSelected = uiState.colorHex.equals(colorHex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) Color.White else DarkSurfaceBorder,
                                    shape = CircleShape
                                )
                                .clickable { viewModel.setColorHex(colorHex) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Save Button
            Button(
                onClick = { viewModel.save(onSaved) },
                enabled = !uiState.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_habit_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = selectedColor,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
                } else {
                    Text(
                        text = if (isEditMode) "Save Changes" else "Create Habit",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HabitPreviewBanner(
    name: String,
    emoji: String,
    color: Color,
    type: HabitType,
    subHabitCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(color.copy(alpha = 0.18f))
                    .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 26.sp)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = name.ifBlank { "Habit Name Preview" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (name.isBlank()) TextTertiary else TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                val subtitle = if (type == HabitType.PARENT) "Parent Routine · $subHabitCount sub-habits" else "Single Habit"
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = color
                )
            }
        }
    }
}

@Composable
private fun TypeSelectionCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) accentColor else DarkSurfaceBorder,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) accentColor.copy(alpha = 0.1f) else DarkSurfaceElevated
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) accentColor else TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
private fun SubHabitsBuilderSection(
    drafts: List<SubHabitDraft>,
    accentColor: Color,
    onAddSubHabit: () -> Unit,
    onUpdateSubHabit: (Int, String) -> Unit,
    onRemoveSubHabit: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        shape = RoundedCornerShape(16.dp),
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
                Column {
                    Text(
                        text = "Sub-Habits (Steps)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Parent auto-completes when all steps are done",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                IconButton(
                    onClick = onAddSubHabit,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Step",
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                drafts.forEachIndexed { index, draft ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = draft.name,
                            onValueChange = { onUpdateSubHabit(index, it) },
                            placeholder = { Text("Sub-habit #${index + 1}", color = TextTertiary) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("sub_habit_input_$index"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = accentColor,
                                unfocusedBorderColor = DarkSurfaceBorder,
                                focusedContainerColor = DarkSurfaceElevated,
                                unfocusedContainerColor = DarkSurfaceElevated,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = { onRemoveSubHabit(index) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove Step",
                                tint = Color(0xFFFF5252),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
