package com.example.myapplication1.widget

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

class WidgetUpdateWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d("GoldWidget", "=== WidgetUpdateWorker START ===")

        try {
            val goldPrice = fetchGoldPriceDirectly()

            if (goldPrice != null) {

                val prefs = applicationContext.getSharedPreferences(
                    "gold_rate_widget",
                    Context.MODE_PRIVATE
                )

                prefs.edit()
                    .putString("buy_price", goldPrice.toString())
                    .putString("date", "Обновлено: сегодня")
                    .apply()

                Log.d("GoldWidget", "Данные сохранены: $goldPrice")

                updateAllWidgets(applicationContext)

                Result.success()
            } else {
                Log.d("GoldWidget", "Не удалось получить цену")
                saveTestData()
                Result.failure()
            }
        } catch (e: Exception) {
            Log.e("GoldWidget", "Ошибка при получении данных", e)
            saveTestData()
            Result.failure()
        }
    }

    private fun fetchGoldPriceDirectly(): Double? {
        return try {
            val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            val calendar = java.util.Calendar.getInstance()

            for (daysBack in 1..7) {
                calendar.time = java.util.Date()
                calendar.add(java.util.Calendar.DAY_OF_YEAR, -daysBack)
                val targetDate = dateFormat.format(calendar.time)

                val url = "https://www.cbr.ru/scripts/xml_metall.asp?date_req1=$targetDate&date_req2=$targetDate"

                val connection = URL(url).openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                if (connection.responseCode == 200) {
                    val inputStream = connection.inputStream
                    val xmlContent = inputStream.bufferedReader().use { it.readText() }

                    val price = parseGoldPriceFromXml(xmlContent)
                    if (price != null) {
                        return price
                    }
                }

                connection.disconnect()
            }
            null
        } catch (e: Exception) {
            Log.e("GoldWidget", "Ошибка при прямом запросе", e)
            null
        }
    }

    private fun parseGoldPriceFromXml(xml: String): Double? {
        return try {
            val buyPattern = "<Buy>([^<]+)</Buy>".toRegex()
            val codePattern = "Code=\"([^\"]+)\"".toRegex()

            val records = xml.split("</Record>")

            for (record in records) {
                val codeMatch = codePattern.find(record)
                if (codeMatch?.groupValues?.get(1) == "1") {
                    val buyMatch = buyPattern.find(record)
                    val priceStr = buyMatch?.groupValues?.get(1)

                    priceStr?.let {
                        val cleanPrice = it.replace(",", ".")
                        return cleanPrice.toDoubleOrNull()
                    }
                }
            }

            null
        } catch (e: Exception) {
            Log.e("GoldWidget", "Ошибка парсинга XML", e)
            null
        }
    }

    private fun saveTestData() {
        try {
            val prefs = applicationContext.getSharedPreferences("gold_rate_widget", Context.MODE_PRIVATE)
            val testPrice = 10000

            prefs.edit()
                .putString("buy_price", testPrice.toString())
                .putString("date", "Тестовые данные")
                .apply()

            updateAllWidgets(applicationContext)
        } catch (e: Exception) {
            Log.e("GoldWidget", "Ошибка при сохранении тестовых данных", e)
        }
    }

    private fun updateAllWidgets(context: Context) {
        try {
            val appWidgetManager = android.appwidget.AppWidgetManager.getInstance(context)
            val widgetIds = appWidgetManager.getAppWidgetIds(
                android.content.ComponentName(context, GoldRateWidgetProvider::class.java)
            )

            widgetIds.forEach { widgetId ->
                GoldRateWidgetProvider.updateAppWidget(context, appWidgetManager, widgetId)
            }
        } catch (e: Exception) {
            Log.e("GoldWidget", "Ошибка при обновлении виджетов", e)
        }
    }
}