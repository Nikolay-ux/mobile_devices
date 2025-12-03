package com.example.myapplication1.repository

import com.example.myapplication1.api.ApiClient
import com.example.myapplication1.model.MetalRate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class MetalRepository {

    private val apiService = ApiClient.cbrApiService

    suspend fun getCurrentGoldRate(): MetalRate? = withContext(Dispatchers.IO) {
        try {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val currentDate = Calendar.getInstance()

            val today = dateFormat.format(currentDate.time)

            val response = apiService.getMetalRates(today, today)

            if (response.isSuccessful) {
                val cbrResponse = response.body()

                val goldRecord = cbrResponse?.records?.find { it.code == "1" }

                goldRecord?.let { record ->
                    return@withContext MetalRate(
                        code = record.code,
                        name = "Золото",
                        date = record.date,
                        buyPrice = record.buy.toDoubleOrNull() ?: 0.0,
                        sellPrice = record.sell.toDoubleOrNull() ?: 0.0
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }

    suspend fun getAllMetalRates(): List<MetalRate> = withContext(Dispatchers.IO) {
        try {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val currentDate = Calendar.getInstance()
            val today = dateFormat.format(currentDate.time)

            val response = apiService.getMetalRates(today, today)

            if (response.isSuccessful) {
                val cbrResponse = response.body()

                return@withContext cbrResponse?.records?.mapNotNull { record ->
                    val metalName = when (record.code) {
                        "1" -> "Золото"
                        "2" -> "Серебро"
                        "3" -> "Платина"
                        "4" -> "Палладий"
                        else -> "Неизвестный металл"
                    }

                    MetalRate(
                        code = record.code,
                        name = metalName,
                        date = record.date,
                        buyPrice = record.buy.toDoubleOrNull() ?: 0.0,
                        sellPrice = record.sell.toDoubleOrNull() ?: 0.0
                    )
                } ?: emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext emptyList()
    }
}