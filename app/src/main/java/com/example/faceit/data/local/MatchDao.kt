package com.example.faceit.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.faceit.model.Match
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchDao {
    @Query("SELECT * FROM matches WHERE playerId = :playerId ORDER BY date DESC")
    fun observeForPlayer(playerId: Long): Flow<List<Match>>

    @Query("SELECT * FROM matches WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Match?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(match: Match): Long

    @Update
    suspend fun update(match: Match)

    @Delete
    suspend fun delete(match: Match)
}
