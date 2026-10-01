package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sub_habit_logs",
    foreignKeys = [
        ForeignKey(
            entity = SubHabit::class,
            parentColumns = ["id"],
            childColumns = ["subHabitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["subHabitId", "date"], unique = true),
        Index(value = ["subHabitId"]),
        Index(value = ["parentHabitId", "date"])
    ]
)
data class SubHabitLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subHabitId: Long,
    val parentHabitId: Long,
    val date: String, // "yyyy-MM-dd"
    val isCompleted: Boolean = false
)
