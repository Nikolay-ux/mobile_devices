package com.example.myapplication1.ui.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication1.model.HighScore
import com.example.myapplication1.model.Player
import com.example.myapplication1.repository.GameRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HighScores() {
    val context = LocalContext.current
    val repository = remember { GameRepository.getInstance(context) }

    var topScores by remember { mutableStateOf<List<HighScore>>(emptyList()) }
    var playersMap by remember { mutableStateOf<Map<Long, Player>>(emptyMap()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        loading = true
        topScores = withContext(Dispatchers.IO) { repository.getTopScoresPerPlayer() }
        val allPlayers = withContext(Dispatchers.IO) { repository.getAllPlayers() }
        playersMap = allPlayers.associateBy { it.playerId }
        loading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Рекорды") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (loading) {
                Text("Загрузка рекордов...")
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(topScores) { score ->
                        val player = playersMap[score.playerId]
                        ScoreItem(score = score, player = player)
                    }
                }
            }
        }
    }
}

@Composable
fun ScoreItem(score: HighScore, player: Player?) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        ListItem(
            headlineContent = {
                Text(
                    "${player?.name ?: "Неизвестный"} - ${score.score} очков",
                    fontWeight = FontWeight.Bold
                )
            },
            supportingContent = {
                Column {
                    Text("Уровень: ${player?.difficult ?: "N/A"}")
                    Text("Дата: ${formatDate(score.date)}")
                }
            }
        )
    }
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}