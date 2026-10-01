package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CompletionState
import com.example.ui.theme.DarkStreakFlame

@Composable
fun StatusBadge(
    state: CompletionState,
    accentColor: Color,
    modifier: Modifier = Modifier,
    customLabel: String? = null
) {
    val (label, bg, contentColor) = when (state) {
        CompletionState.COMPLETED -> Triple(
            customLabel ?: "Completed",
            accentColor.copy(alpha = 0.15f),
            accentColor
        )
        CompletionState.PARTIAL -> Triple(
            customLabel ?: "Partial",
            Color(0xFFFFB300).copy(alpha = 0.15f),
            Color(0xFFFFB300)
        )
        CompletionState.INCOMPLETE -> Triple(
            customLabel ?: "Incomplete",
            Color(0xFF262A40).copy(alpha = 0.5f),
            Color(0xFF8E95AA)
        )
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.dp, contentColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(contentColor)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = contentColor
        )
    }
}

@Composable
fun StreakBadge(
    streakDays: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(DarkStreakFlame.copy(alpha = 0.12f))
            .border(1.dp, DarkStreakFlame.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "🔥",
            fontSize = 11.sp
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = if (streakDays == 1) "1 day" else "$streakDays days",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = DarkStreakFlame
        )
    }
}
