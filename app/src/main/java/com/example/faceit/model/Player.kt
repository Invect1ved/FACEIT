package com.example.faceit.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "players")
data class Player(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nickname: String,
    val elo: Int,
    val level: Int,
    val faceitUrl: String,
    val comment: String
)
