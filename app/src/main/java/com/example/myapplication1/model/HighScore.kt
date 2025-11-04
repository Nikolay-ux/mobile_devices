package com.example.myapplication1.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "high_scores",
    foreignKeys = [
        ForeignKey(
            entity = Player::class,
            parentColumns = ["playerId"],
            childColumns = ["playerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["playerId"])]
)
data class HighScore(
    @PrimaryKey(autoGenerate = true)
    val scoreId: Long = 0,
    val playerId: Long,
    val score: Int,
    val date: Long = System.currentTimeMillis()
)
