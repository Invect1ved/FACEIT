package com.example.faceit.data.repository

import com.example.faceit.model.Match
import com.example.faceit.model.Player
import kotlinx.coroutines.flow.Flow

interface PlayerRepository {
    fun observePlayers(): Flow<List<Player>>
    fun observePlayer(playerId: Long): Flow<Player?>
    fun observeMatches(playerId: Long): Flow<List<Match>>
    suspend fun insertPlayer(player: Player): Long
    suspend fun updatePlayer(player: Player)
    suspend fun deletePlayer(player: Player)
    suspend fun insertMatch(match: Match): Long
    suspend fun updateMatch(match: Match)
    suspend fun getMatchById(id: Long): Match?
    suspend fun deleteMatch(match: Match)
}
