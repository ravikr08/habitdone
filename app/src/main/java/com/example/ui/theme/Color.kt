package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Backgrounds & Surfaces (Near-black / Deep Obsidian / Charcoal)
val DarkBackground = Color(0xFF0B0C12)
val DarkSurface = Color(0xFF12141F)
val DarkSurfaceElevated = Color(0xFF181B28)
val DarkSurfaceCard = Color(0xFF141624)
val DarkSurfaceBorder = Color(0xFF24273B)

// Reference App Accents
val ReferenceNeonGreen = Color(0xFF2EE886)
val ReferenceLilac = Color(0xFFC084FC) // "Morning Stretch" in reference
val ReferenceMint = Color(0xFF4ADE80)  // "Gym Session" in reference
val ReferenceBlue = Color(0xFF818CF8)  // "Quit Smoking" in reference
val ReferenceOrange = Color(0xFFFB923C) // "Drink Water" in reference
val ReferenceCoral = Color(0xFFF87171)

// General Accents
val NeonCyan = Color(0xFF00E5FF)
val NeonEmerald = ReferenceNeonGreen
val NeonOrange = ReferenceOrange
val NeonPurple = ReferenceLilac
val NeonPink = Color(0xFFFF4081)
val NeonYellow = Color(0xFFFFD600)
val NeonBlue = ReferenceBlue
val DarkStreakFlame = Color(0xFFFF7A00)

// Text
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFF9DA3BD)
val TextTertiary = Color(0xFF5D637E)

// Heatmap Cell States
val HeatmapCellEmpty = Color(0xFF1E2132)
val HeatmapCellFuture = Color(0xFF12131D)
val HeatmapCellBorder = Color(0xFF2A2E44)
val HeatmapTodayBorder = Color(0xFFFFFFFF)

// Palette for habit creation
val HabitColorPalette = listOf(
    "#C084FC", // Reference Lilac
    "#4ADE80", // Reference Mint
    "#818CF8", // Reference Indigo/Blue
    "#FB923C", // Reference Orange
    "#00E5FF", // Vibrant Cyan
    "#F87171", // Coral Red
    "#FFD600", // Gold Yellow
    "#FF4081", // Neon Pink
    "#2EE886", // Bright Emerald
    "#38BDF8"  // Sky Blue
)

val HabitEmojiPresets = listOf(
    "🧘", "🏋️", "💊", "💧", "💻", "🎧", "📚", "🏃",
    "🚭", "✍️", "🎯", "🍎", "⚡", "🛌", "🚶", "🌱"
)
