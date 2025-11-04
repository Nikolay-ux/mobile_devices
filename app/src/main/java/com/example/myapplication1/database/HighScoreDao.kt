package com.example.myapplication1.database

import androidx.room.*
import com.example.myapplication1.model.HighScore
import kotlinx.coroutines.flow.Flow

@Dao
interface HighScoreDao {
    @Query("SELECT * FROM high_scores ORDER BY score DESC, date ASC LIMIT :limit")
    suspend fun getTopScores(limit: Int): List<HighScore>

    @Query("SELECT * FROM high_scores WHERE playerId = :playerId ORDER BY score DESC, date ASC")
    suspend fun getScoresByPlayer(playerId: Long): List<HighScore>

    @Query("SELECT * FROM high_scores WHERE playerId = :playerId ORDER BY score DESC, date ASC LIMIT 1")
    suspend fun getTopScoreByPlayer(playerId: Long): HighScore?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScore(score: HighScore)

    @Delete
    suspend fun deleteScore(score: HighScore)
}