package com.example.faceit.data.repository

import com.example.faceit.data.local.MatchDao
import com.example.faceit.data.local.PlayerDao
import com.example.faceit.model.Match
import com.example.faceit.model.Player
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerRepositoryImpl @Inject constructor(
    private val playerDao: PlayerDao,
    private val matchDao: MatchDao
) : PlayerRepository {

    override fun observePlayers(): Flow<List<Player>> = playerDao.observeAll()

    override fun observePlayer(playerId: Long): Flow<Player?> = playerDao.observeById(playerId)

    override fun observeMatches(playerId: Long): Flow<List<Match>> =
        matchDao.observeForPlayer(playerId)

    override suspend fun insertPlayer(player: Player): Long = playerDao.insert(player)

    override suspend fun updatePlayer(player: Player) {
        playerDao.update(player)
    }

    override suspend fun deletePlayer(player: Player) {
        playerDao.delete(player)
    }

    override suspend fun insertMatch(match: Match): Long = matchDao.insert(match)

    override suspend fun updateMatch(match: Match) {
        matchDao.update(match)
    }

    override suspend fun getMatchById(id: Long): Match? = matchDao.getById(id)

    override suspend fun deleteMatch(match: Match) {
        matchDao.delete(match)
    }
}
