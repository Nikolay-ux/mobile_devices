package com.example.myapplication1.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object GameData {
    var speed by mutableIntStateOf(1)
    var maxBugCount by mutableIntStateOf(5)
    var bonusInterval by mutableIntStateOf(20)
    var duration by mutableIntStateOf(120)
}
