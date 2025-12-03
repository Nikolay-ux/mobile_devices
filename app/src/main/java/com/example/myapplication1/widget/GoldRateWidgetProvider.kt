package com.example.myapplication1.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import com.example.myapplication1.MainActivity
import com.example.myapplication1.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class GoldRateWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        GlobalScope.launch(Dispatchers.IO) {
            updateDataAndWidgets(context)
        }

        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        Log.d("GoldWidget", "Первый виджет добавлен")
        GlobalScope.launch(Dispatchers.IO) {
            updateDataAndWidgets(context)
        }
    }

    private suspend fun updateDataAndWidgets(context: Context) {
        try {
            val workManager = androidx.work.WorkManager.getInstance(context)
            val workRequest = androidx.work.OneTimeWorkRequestBuilder<WidgetUpdateWorker>().build()
            workManager.enqueue(workRequest).result.get()

        } catch (e: Exception) {
            Log.e("GoldWidget", "Ошибка при запуске воркера", e)
        }
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {

            val prefs = context.getSharedPreferences("gold_rate_widget", Context.MODE_PRIVATE)
            var buyPrice = prefs.getString("buy_price", null)


            if (buyPrice == null) {
                buyPrice = "Загрузка..."
            } else {
                try {
                    val priceNum = buyPrice.toDouble()
                    buyPrice = String.format("%.2f", priceNum)
                } catch (e: Exception) {
                    Log.e("GoldWidget", "Ошибка форматирования цены", e)
                }
            }

            val views = RemoteViews(context.packageName, R.layout.widget_gold_rate)

            val displayText = if (buyPrice == "Загрузка...") buyPrice else "$buyPrice ₽/г"
            views.setTextViewText(R.id.widget_gold_price, displayText)

            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)

            Log.d("GoldWidget", "Виджет обновлен: $displayText")
        }
    }
}