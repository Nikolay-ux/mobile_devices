package com.example.myapplication1.ui.tabs

import android.content.Context
import android.text.Html
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.myapplication1.R
import java.io.InputStream

@Composable
fun Rules() {
    val context = LocalContext.current
    val htmlContent = remember { loadHtmlFromResources(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Правила игры",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            modifier = Modifier.fillMaxSize(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            AndroidView(
                factory = { context ->
                    TextView(context).apply {
                        movementMethod = LinkMovementMethod.getInstance()
                        setPadding(32, 24, 32, 24)
                    }
                },
                update = { textView ->
                    textView.text = parseHtml(htmlContent)
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

private fun loadHtmlFromResources(context: Context): String {
    return try {
        val inputStream: InputStream = context.resources.openRawResource(R.raw.rules)
        inputStream.bufferedReader().use { it.readText() }
    } catch (e: Exception) {
        """
        <h1>Правила игры</h1>
        <p>Файл с правилами не найден.</p>
        """.trimIndent()
    }
}

private fun parseHtml(html: String): Spanned {
    return Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT)
}