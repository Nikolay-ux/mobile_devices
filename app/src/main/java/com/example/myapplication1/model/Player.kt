package com.example.myapplication1.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "players")
data class Player(
    @PrimaryKey(autoGenerate = true)
    val playerId: Long = 0,
    val name: String,
    val sex: String,
    val course: Int,
    val difficult: Int,
    val birthday: String,
    val zodiac: String
)
