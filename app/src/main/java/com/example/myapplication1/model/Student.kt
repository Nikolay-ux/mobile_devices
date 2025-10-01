package com.example.myapplication1.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object Student {
    var name by mutableStateOf("")
    var sex by mutableStateOf("")
    var course by mutableIntStateOf(-1)
    var difficult by mutableIntStateOf(5)
    var birthday by mutableStateOf("")
    var zodiac by mutableStateOf("Не выбрано")
}
