package com.example.myapplication1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.myapplication1.ui.MainScreen
import com.example.myapplication1.ui.theme.MyApplication1Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        updateWidgetOnAppStart()

        enableEdgeToEdge()
        setContent {
            MyApplication1Theme {
                MainScreen()
            }
        }
    }

    private fun updateWidgetOnAppStart() {
        androidx.work.WorkManager.getInstance(this)
            .enqueue(
                androidx.work.OneTimeWorkRequestBuilder<com.example.myapplication1.widget.WidgetUpdateWorker>()
                    .build()
            )
    }
}
