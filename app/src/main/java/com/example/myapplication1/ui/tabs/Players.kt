package com.example.myapplication1.ui.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication1.model.GameData
import com.example.myapplication1.model.Player
import com.example.myapplication1.repository.GameRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Players() {
    val context = LocalContext.current
    val repository = remember { GameRepository.getInstance(context) }

    var players by remember { mutableStateOf<List<Player>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    var newName by remember { mutableStateOf("") }
    var newSex by remember { mutableStateOf("Мужской") }
    var newCourse by remember { mutableStateOf(1) }
    var newDifficult by remember { mutableStateOf(5) }
    var newBirthday by remember { mutableStateOf("01.01.2000") }
    var newZodiac by remember { mutableStateOf("Овен") }

    var selectedPlayerId by remember { mutableStateOf(GameData.currentPlayerId) }

    LaunchedEffect(Unit) {
        loading = true
        players = withContext(Dispatchers.IO) { repository.getAllPlayers() }
        loading = false
    }

    LaunchedEffect(selectedPlayerId) {
        if (selectedPlayerId != -1L) {
            val player = players.find { it.playerId == selectedPlayerId }
            if (player != null) {
                GameData.currentPlayerId = player.playerId
                GameData.currentPlayerName = player.name
                GameData.currentPlayerDifficulty = player.difficult
            }
        } else {
            GameData.currentPlayerId = -1L
            GameData.currentPlayerName = ""
            GameData.currentPlayerDifficulty = 0
        }
    }

    fun saveNewPlayer() {
        CoroutineScope(Dispatchers.IO).launch {
            val newPlayer = Player(
                name = newName,
                sex = newSex,
                course = newCourse,
                difficult = newDifficult,
                birthday = newBirthday,
                zodiac = newZodiac
            )
            val newPlayerId = repository.insertPlayer(newPlayer)
            players = repository.getAllPlayers()
            selectedPlayerId = newPlayerId
            newName = ""
            newSex = "Мужской"
            newCourse = 1
            newDifficult = 5
            newBirthday = "01.01.2000"
            newZodiac = "Овен"
        }
    }

    fun selectPlayer(playerId: Long) {
        selectedPlayerId = playerId
    }

    fun selectNewPlayer() {
        selectedPlayerId = -1L
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Выбор игрока") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Новый игрок", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Имя") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Пол: ")
                        RadioButton(
                            selected = newSex == "Мужской",
                            onClick = { newSex = "Мужской" }
                        )
                        Text("Мужской")
                        RadioButton(
                            selected = newSex == "Женский",
                            onClick = { newSex = "Женский" }
                        )
                        Text("Женский")
                    }

                    Button(
                        onClick = { saveNewPlayer() },
                        enabled = newName.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Создать игрока")
                    }
                }
            }

            if (loading) {
                Text("Загрузка...")
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        ListItem(
                            headlineContent = { Text("Новый игрок", fontWeight = FontWeight.Bold) },
                            supportingContent = { Text("Создать нового игрока") },
                            trailingContent = {
                                RadioButton(
                                    selected = selectedPlayerId == -1L,
                                    onClick = { selectNewPlayer() }
                                )
                            },
                            modifier = Modifier.fillParentMaxWidth()
                        )
                    }
                    items(players) { player ->
                        ListItem(
                            headlineContent = { Text(player.name) },
                            supportingContent = {
                                Column {
                                    Text("Уровень: ${player.difficult}/10")
                                    Text("Курс: ${player.course}")
                                }
                            },
                            trailingContent = {
                                RadioButton(
                                    selected = selectedPlayerId == player.playerId,
                                    onClick = { selectPlayer(player.playerId) }
                                )
                            },
                            modifier = Modifier.fillParentMaxWidth()
                        )
                    }
                }
            }
        }
    }
}