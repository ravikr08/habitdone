package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val emoji: String = "⚡",
    val colorHex: String = "#00E5FF",
    val type: HabitType = HabitType.SINGLE,
    val createdAt: Long = System.currentTimeMillis(),
    val archived: Boolean = false
)
