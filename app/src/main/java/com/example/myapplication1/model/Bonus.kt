package com.example.myapplication1.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object Bonus {
    var isActive by mutableStateOf(false)
    var x by mutableFloatStateOf(0f)
    var y by mutableFloatStateOf(0f)
    var timeLeft by mutableFloatStateOf(0f)
    var duration by mutableFloatStateOf(5f)
}