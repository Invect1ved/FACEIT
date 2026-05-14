package com.example.faceit.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

@Serializable
data class PlayerDetailRoute(val playerId: Long)

@Serializable
data class PlayerFormRoute(val playerId: Long = 0L)

@Serializable
data class AddMatchRoute(
    val playerId: Long,
    val matchId: Long = 0L
)
