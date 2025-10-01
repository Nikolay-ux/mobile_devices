package com.example.myapplication1.utils

import com.example.myapplication1.R
import java.util.Calendar

object ZodiacUtil {
    fun getSign(day: Int, month: Int): String {
        return when (month) {
            0 -> if (day < 20) "Козерог" else "Водолей"
            1 -> if (day < 19) "Водолей" else "Рыбы"
            2 -> if (day < 21) "Рыбы" else "Овен"
            3 -> if (day < 20) "Овен" else "Телец"
            4 -> if (day < 21) "Телец" else "Близнецы"
            5 -> if (day < 21) "Близнецы" else "Рак"
            6 -> if (day < 23) "Рак" else "Лев"
            7 -> if (day < 23) "Лев" else "Дева"
            8 -> if (day < 23) "Дева" else "Весы"
            9 -> if (day < 23) "Весы" else "Скорпион"
            10 -> if (day < 22) "Скорпион" else "Стрелец"
            11 -> if (day < 22) "Стрелец" else "Козерог"
            else -> "Не, не бывает"
        }
    }

    fun getRes(zodiacSign: String): Int {
        return when (zodiacSign) {
            "Овен" -> R.drawable.aries
            "Телец" -> R.drawable.taurus
            "Близнецы" -> R.drawable.gemini
            "Рак" -> R.drawable.cancer
            "Лев" -> R.drawable.leo
            "Дева" -> R.drawable.virgo
            "Весы" -> R.drawable.libra
            "Скорпион" -> R.drawable.scorpio
            "Стрелец" -> R.drawable.sagittarius
            "Козерог" -> R.drawable.capricorn
            "Водолей" -> R.drawable.aquarius
            "Рыбы" -> R.drawable.pisces
            else -> R.drawable.ic_launcher_foreground
        }
    }

    fun getDate(calendar: Calendar): String {
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH) + 1
        val year = calendar.get(Calendar.YEAR)
        return  "$day.$month.$year"
    }
}