package com.example.faceit.data.local

import androidx.room.TypeConverter
import com.example.faceit.model.MatchResult

class Converters {
    @TypeConverter
    fun fromMatchResult(value: MatchResult): String = value.name

    @TypeConverter
    fun toMatchResult(value: String): MatchResult = MatchResult.valueOf(value)
}
