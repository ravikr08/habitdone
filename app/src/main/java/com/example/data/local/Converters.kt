package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.CompletionState
import com.example.data.model.HabitType

class Converters {
    @TypeConverter
    fun fromHabitType(type: HabitType): String = type.name

    @TypeConverter
    fun toHabitType(value: String): HabitType = runCatching {
        HabitType.valueOf(value)
    }.getOrDefault(HabitType.SINGLE)

    @TypeConverter
    fun fromCompletionState(state: CompletionState): String = state.name

    @TypeConverter
    fun toCompletionState(value: String): CompletionState = runCatching {
        CompletionState.valueOf(value)
    }.getOrDefault(CompletionState.INCOMPLETE)
}
