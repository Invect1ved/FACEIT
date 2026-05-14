package com.example.faceit.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.faceit.model.Player
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {
    @Query("SELECT * FROM players WHERE nickname = :nickname COLLATE NOCASE LIMIT 1")
    suspend fun getByNickname(nickname: String): Player?

    @Query("SELECT * FROM players ORDER BY nickname COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<Player>>

    @Query("SELECT * FROM players WHERE id = :id")
    fun observeById(id: Long): Flow<Player?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(player: Player): Long

    @Update
    suspend fun update(player: Player)

    @Delete
    suspend fun delete(player: Player)
}
