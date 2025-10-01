package com.example.myapplication1

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.CalendarView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.viewinterop.AndroidView
import com.example.myapplication1.model.Student
import com.example.myapplication1.ui.theme.MyApplication1Theme
import com.example.myapplication1.utils.ZodiacUtil
import java.util.Calendar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplication1Theme {
                RegForm()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegForm() {
    var student by remember { mutableStateOf(Student()) }
    val scrollState = rememberScrollState()
    val courses = listOf("1 курс", "2 курс", "3 курс", "4 курс")
    var expanded by remember { mutableStateOf(false) }
    var selectedCourse by remember { mutableStateOf("Выберите курс") }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    val context = LocalContext.current
    var showResults by remember { mutableStateOf(false) }

    val zodiacImageRes = ZodiacUtil.getRes(student.zodiac)

    fun updateDate(calendar: Calendar) {
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH)
        val zodiacSign = ZodiacUtil.getSign(day, month)
        val formattedDate = ZodiacUtil.getDate(calendar)

        student = student.copy(
            birthday = formattedDate,
            zodiac = zodiacSign
        )
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
                selectedDate = newDate
                updateDate(newDate)
            },
            year, month, day
        )

        datePickerDialog.show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Регистрация",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        OutlinedTextField(
            value = student.name,
            onValueChange = { student = student.copy(name = it)},
            modifier = Modifier.fillMaxWidth(),
            label = { Text("ФИО")}
        )
        Text("Пол:", style = MaterialTheme.typography.bodyLarge)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = student.sex == "Мужской",
                    onClick = { student = student.copy(sex = "Мужской") }
                )
                Text("Мужской")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = student.sex == "Женский",
                    onClick = { student = student.copy(sex = "Женский") }
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
                Text(selectedCourse)
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                courses.forEach { course ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                course,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        onClick = {
                            selectedCourse = course
                            expanded = false
                        }
                    )
                }
            }
        }
        Text("Уровень сложности: ${student.difficult}/10",
            style = MaterialTheme.typography.bodyLarge)
        Slider(
            value = student.difficult.toFloat(),
            onValueChange = {
                student = student.copy(difficult = it.toInt())
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
            text = "Выбрана дата: ${student.birthday}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Знак зодиака: ${student.zodiac}",
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
                    text = student.zodiac,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Image(
                    painter = painterResource(id = zodiacImageRes),
                    contentDescription = "Знак зодиака: ${student.zodiac}",
                    modifier = Modifier
                        .size(120.dp)
                        .padding(8.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }
        }

        Button(
            onClick = {
                student = student.copy(course = courses.indexOf(selectedCourse) + 1)
                showResults = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Зарегистрироваться", style = MaterialTheme.typography.bodyLarge)
        }

        if (showResults) {
            StudentResults(student = student, zodiacImageRes = zodiacImageRes)
        }
    }
}

@Composable
fun StudentResults(student: Student, zodiacImageRes: Int) {
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

            Text("ФИО: ${student.name}", modifier = Modifier.align(Alignment.Start))
            Text("Пол: ${student.sex}", modifier = Modifier.align(Alignment.Start))
            Text("Курс: ${student.course}", modifier = Modifier.align(Alignment.Start))
            Text("Уровень сложности: ${student.difficult}/10", modifier = Modifier.align(Alignment.Start))
            Text("Дата рождения: ${student.birthday}", modifier = Modifier.align(Alignment.Start))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("♈ Знак зодиака: ${student.zodiac}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold)

                Image(
                    painter = painterResource(id = zodiacImageRes),
                    contentDescription = "Знак зодиака: ${student.zodiac}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .size(80.dp)
                        .padding(8.dp)
                )
            }
        }
    }
}

fun updateZodiacSign(date: Calendar, currentStudent: Student, onUpdate: (Student) -> Unit) {
    val day = date.get(Calendar.DAY_OF_MONTH)
    val month = date.get(Calendar.MONTH)
    val zodiacSign = ZodiacUtil.getSign(day, month)
    val formattedDate = ZodiacUtil.getDate(date)

    onUpdate(currentStudent.copy(
        birthday = formattedDate,
        zodiac = zodiacSign
    ))
}

@Composable
fun MyApplication1Theme(content: @Composable () -> Unit) {
    MaterialTheme(
        content = content
    )
}
