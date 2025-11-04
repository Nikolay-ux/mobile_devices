package com.example.myapplication1.database

import androidx.room.*
import com.example.myapplication1.model.Player

@Dao
interface PlayerDao {
    @Query("SELECT * FROM players ORDER BY name ASC")
    suspend fun getAllPlayers(): List<Player>

    @Query("SELECT * FROM players WHERE playerId = :playerId")
    suspend fun getPlayerById(playerId: Long): Player?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayer(player: Player): Long

    @Update
    suspend fun updatePlayer(player: Player)

    @Delete
    suspend fun deletePlayer(player: Player)
}