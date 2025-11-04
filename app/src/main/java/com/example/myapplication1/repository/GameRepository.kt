package com.example.myapplication1.repository

import android.content.Context
import com.example.myapplication1.database.AppDatabase
import com.example.myapplication1.model.Player
import com.example.myapplication1.model.HighScore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GameRepository private constructor(private val database: AppDatabase) {

    private val playerDao = database.playerDao()
    private val highScoreDao = database.highScoreDao()

    suspend fun getAllPlayers(): List<Player> = withContext(Dispatchers.IO) {
        playerDao.getAllPlayers()
    }

    suspend fun getPlayerById(playerId: Long): Player? = withContext(Dispatchers.IO) {
        playerDao.getPlayerById(playerId)
    }

    suspend fun insertPlayer(player: Player): Long = withContext(Dispatchers.IO) {
        playerDao.insertPlayer(player)
    }

    suspend fun getTopScores(limit: Int): List<HighScore> = withContext(Dispatchers.IO) {
        highScoreDao.getTopScores(limit)
    }

    suspend fun getTopScoreByPlayer(playerId: Long): HighScore? = withContext(Dispatchers.IO) {
        highScoreDao.getTopScoreByPlayer(playerId)
    }

    suspend fun insertScore(score: HighScore) = withContext(Dispatchers.IO) {
        highScoreDao.insertScore(score)
    }

    companion object {
        @Volatile
        private var INSTANCE: GameRepository? = null

        fun getInstance(context: Context): GameRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = GameRepository(AppDatabase.getDatabase(context))
                INSTANCE = instance
                instance
            }
        }
    }
}