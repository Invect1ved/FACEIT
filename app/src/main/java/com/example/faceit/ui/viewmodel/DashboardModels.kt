package com.example.faceit.ui.viewmodel

import com.example.faceit.model.MatchResult

data class MatchHistoryRow(
    val mapName: String,
    val result: MatchResult,
    val kills: Int,
    val deaths: Int,
    val dateMillis: Long,
    val eloDelta: Int,
    val eloAfter: Int
)
