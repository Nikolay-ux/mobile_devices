package com.example.myapplication1.database

import androidx.room.*
import com.example.myapplication1.model.HighScore

@Dao
interface HighScoreDao {
    @Query("SELECT * FROM high_scores ORDER BY score DESC, date ASC LIMIT :limit")
    suspend fun getTopScores(limit: Int): List<HighScore>

    @Query("""
        SELECT h1.* 
        FROM high_scores h1
        INNER JOIN (
            SELECT playerId, MAX(score) as maxScore 
            FROM high_scores 
            GROUP BY playerId
        ) h2 
        ON h1.playerId = h2.playerId AND h1.score = h2.maxScore
        ORDER BY h1.score DESC, h1.date ASC
    """)
    suspend fun getTopScoresPerPlayer(): List<HighScore>

    @Query("SELECT * FROM high_scores WHERE playerId = :playerId ORDER BY score DESC, date ASC")
    suspend fun getScoresByPlayer(playerId: Long): List<HighScore>

    @Query("SELECT * FROM high_scores WHERE playerId = :playerId ORDER BY score DESC, date ASC LIMIT 1")
    suspend fun getTopScoreByPlayer(playerId: Long): HighScore?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScore(score: HighScore)

    @Delete
    suspend fun deleteScore(score: HighScore)
}