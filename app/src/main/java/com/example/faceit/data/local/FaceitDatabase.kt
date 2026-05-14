package com.example.faceit.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.faceit.model.Match
import com.example.faceit.model.Player

@Database(
    entities = [Player::class, Match::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FaceitDatabase : RoomDatabase() {
    abstract fun playerDao(): PlayerDao
    abstract fun matchDao(): MatchDao
}
