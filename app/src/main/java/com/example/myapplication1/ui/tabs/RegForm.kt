package com.example.myapplication1.ui.tabs

import android.app.DatePickerDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication1.model.GameData
import com.example.myapplication1.model.Player
import com.example.myapplication1.model.Student.birthday
import com.example.myapplication1.model.Student.course
import com.example.myapplication1.model.Student.difficult
import com.example.myapplication1.model.Student.name
import com.example.myapplication1.model.Student.sex
import com.example.myapplication1.model.Student.zodiac
import com.example.myapplication1.repository.GameRepository
import com.example.myapplication1.utils.ZodiacUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegForm() {
    val context = LocalContext.current
    val repository = remember { GameRepository.getInstance(context) }

    var players by remember { mutableStateOf<List<Player>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    val scrollState = rememberScrollState()
    val courses = listOf("1 курс", "2 курс", "3 курс", "4 курс")
    var expanded by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    var showResults by remember { mutableStateOf(false) }

    val zodiacImageRes = ZodiacUtil.getRes(zodiac)

    val isFormValid = remember(name, sex, course, birthday) {
        name.isNotBlank() &&
        sex.isNotBlank() &&
        course != -1 &&
        birthday.isNotBlank()
    }

    LaunchedEffect(Unit) {
        loading = true
        players = withContext(Dispatchers.IO) { repository.getAllPlayers() }
        loading = false
    }

    fun updateDate(calendar: Calendar) {
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH)
        val zodiacSign = ZodiacUtil.getSign(day, month)
        val formattedDate = ZodiacUtil.getDate(calendar)
        birthday = formattedDate
        zodiac = zodiacSign
    }

    fun showDatePicker() {
        val year = selectedDate.get(Calendar.YEAR)
        val month = selectedDate.get(Calendar.MONTH)
        val day = selectedDate.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                val newDate = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay)
                }

                val today = Calendar.getInstance()
                today.set(Calendar.HOUR_OF_DAY, 0)
                today.set(Calendar.MINUTE, 0)
                today.set(Calendar.SECOND, 0)
                today.set(Calendar.MILLISECOND, 0)

                newDate.set(Calendar.HOUR_OF_DAY, 0)
                newDate.set(Calendar.MINUTE, 0)
                newDate.set(Calendar.SECOND, 0)
                newDate.set(Calendar.MILLISECOND, 0)

                if (newDate.timeInMillis <= today.timeInMillis) {
                    selectedDate = newDate
                    updateDate(newDate)
                }
            },
            year, month, day
        )

        val today = Calendar.getInstance()
        datePickerDialog.datePicker.maxDate = today.timeInMillis

        val minDate = Calendar.getInstance()
        minDate.add(Calendar.YEAR, -100)
        datePickerDialog.datePicker.minDate = minDate.timeInMillis

        datePickerDialog.show()
    }

    fun loadPlayerData(player: Player) {
        name = player.name
        sex = player.sex
        course = player.course
        difficult = player.difficult
        birthday = player.birthday
        zodiac = player.zodiac
        showResults = true

        GameData.currentPlayerId = player.playerId
        GameData.currentPlayerName = player.name
        GameData.currentPlayerDifficulty = player.difficult
    }

    fun savePlayer() {
        val newPlayer = Player(
            name = name,
            sex = sex,
            course = course,
            difficult = difficult,
            birthday = birthday,
            zodiac = zodiac
        )
        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
            val id = repository.insertPlayer(newPlayer)
            GameData.currentPlayerId = id
            GameData.currentPlayerName = newPlayer.name
            GameData.currentPlayerDifficulty = newPlayer.difficult

            val updatedPlayers = repository.getAllPlayers()
            withContext(Dispatchers.Main) {
                players = updatedPlayers
            }
        }

    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Регистрация / Выбор игрока") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (loading) {
                Text("Загрузка игроков...")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                        .heightIn(max = 200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
                                Button(
                                    onClick = { loadPlayerData(player) }
                                ) {
                                    Text("Выбрать")
                                }
                            },
                            modifier = Modifier.fillParentMaxWidth()
                        )
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(30.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Регистрация",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("ФИО") }
                )
                Text("Пол:", style = MaterialTheme.typography.bodyLarge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = sex == "Мужской",
                            onClick = { sex = "Мужской" }
                        )
                        Text("Мужской")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = sex == "Женский",
                            onClick = { sex = "Женский" }
                        )
                        Text("Женский")
                    }
                }

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            if (course == -1) "Выберите курс" else courses[course - 1]
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        courses.forEachIndexed { index, courseName ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        courseName,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                },
                                onClick = {
                                    course = index + 1
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Text(
                    "Уровень сложности: ${difficult}/10",
                    style = MaterialTheme.typography.bodyLarge
                )
                Slider(
                    value = difficult.toFloat(),
                    onValueChange = {
                        difficult = it.toInt()
                    },
                    valueRange = 1f..10f,
                    steps = 8,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Нуб")
                    Text("Нормис")
                    Text("Легенда")
                }
                Text("Дата рождения:", style = MaterialTheme.typography.bodyLarge)

                OutlinedButton(
                    onClick = { showDatePicker() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Выбрать дату рождения")
                }

                Text(
                    text = "Выбрана дата: $birthday",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Знак зодиака: $zodiac",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Знак зодиака:",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = zodiac,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Image(
                            painter = painterResource(id = zodiacImageRes),
                            contentDescription = "Знак зодиака: $zodiac",
                            modifier = Modifier
                                .size(120.dp)
                                .padding(8.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                    }
                }

                Button(
                    onClick = {
                        if (isFormValid) {
                            savePlayer()
                            showResults = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = isFormValid
                ) {
                    Text(
                        if (isFormValid) "Зарегистрироваться" else "Заполните все поля",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                if (!isFormValid) {
                    Text(
                        text = "⚠️ Заполните все обязательные поля для регистрации",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }

                if (showResults) {
                    StudentResults(zodiacImageRes = zodiacImageRes)
                }
            }
        }
    }
}

@Composable
fun StudentResults(zodiacImageRes: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Регистрация завершена!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text("ФИО: $name", modifier = Modifier.align(Alignment.Start))
            Text("Пол: $sex", modifier = Modifier.align(Alignment.Start))
            Text("Курс: $course", modifier = Modifier.align(Alignment.Start))
            Text("Уровень сложности: $difficult/10", modifier = Modifier.align(Alignment.Start))
            Text("Дата рождения: $birthday", modifier = Modifier.align(Alignment.Start))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("♈ Знак зодиака: $zodiac",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold)

                Image(
                    painter = painterResource(id = zodiacImageRes),
                    contentDescription = "Знак зодиака: $zodiac",
                    modifier = Modifier
                        .fillMaxWidth()
                        .size(80.dp)
                        .padding(8.dp)
                )
            }
        }
    }
}
