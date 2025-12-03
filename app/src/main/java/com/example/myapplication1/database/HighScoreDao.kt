package com.example.myapplication1.database

import androidx.room.*
import com.example.myapplication1.model.HighScore

@Dao
interface HighScoreDao {
    @Query("SELECT * FROM high_scores ORDER BY score DESC")
    suspend fun getAllScores(): List<HighScore>

    @Query("""
        SELECT hs1.* FROM high_scores hs1
        WHERE hs1.score = (
            SELECT MAX(hs2.score) 
            FROM high_scores hs2 
            WHERE hs2.playerId = hs1.playerId
        )
        GROUP BY hs1.playerId
        ORDER BY hs1.score DESC
    """)
    suspend fun getTopScoresPerPlayer(): List<HighScore>

    @Query("SELECT * FROM high_scores WHERE playerId = :playerId")
    suspend fun getScoresByPlayer(playerId: Long): List<HighScore>

    @Query("SELECT * FROM high_scores WHERE playerId = :playerId ORDER BY score DESC LIMIT 1")
    suspend fun getTopScoreByPlayer(playerId: Long): HighScore?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateScore(score: HighScore)

    @Transaction
    suspend fun insertScoreIfHigher(score: HighScore): Boolean {
        val existingScore = getTopScoreByPlayer(score.playerId)

        return if (existingScore == null || score.score > existingScore.score) {
            insertOrUpdateScore(score)
            true
        } else {
            false
        }
    }

    @Delete
    suspend fun deleteScore(score: HighScore)
}