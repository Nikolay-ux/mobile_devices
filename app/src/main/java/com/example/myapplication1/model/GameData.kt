package com.example.myapplication1.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object GameData {
    var speed by mutableIntStateOf(5)
    var maxBugCount by mutableIntStateOf(10)
    var bonusInterval by mutableIntStateOf(30)
    var duration by mutableIntStateOf(60)

    var score by mutableIntStateOf(0)
    var misses by mutableIntStateOf(0)
    var timeLeft by mutableFloatStateOf(duration.toFloat())
    var gameActive by mutableStateOf(false)

    var currentPlayerId by mutableLongStateOf(-1L)
    var currentPlayerName by mutableStateOf("")
    var currentPlayerDifficulty by mutableIntStateOf(0)
}
