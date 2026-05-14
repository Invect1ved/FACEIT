package com.example.faceit.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
@Entity(
    tableName = "matches",
    foreignKeys = [
        ForeignKey(
            entity = Player::class,
            parentColumns = ["id"],
            childColumns = ["playerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("playerId")]
)
data class Match(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val playerId: Long,
    val mapName: String,
    val result: MatchResult,
    val kills: Int,
    val deaths: Int,
    val date: Long
)
