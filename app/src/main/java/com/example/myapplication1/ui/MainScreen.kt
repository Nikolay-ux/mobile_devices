package com.example.myapplication1.ui


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication1.ui.tabs.Authors
import com.example.myapplication1.ui.tabs.RegForm
import com.example.myapplication1.ui.tabs.Rules
import com.example.myapplication1.ui.tabs.Settings
import com.example.myapplication1.ui.tabs.Game
import com.example.myapplication1.ui.tabs.Players
import com.example.myapplication1.ui.tabs.HighScores

@Composable
fun MainScreen() {
    var selectedTab by remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Tab(
                text = { Text("Игра") },
                modifier = Modifier.padding(0.dp, 30.dp),
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 }
            )
//            Tab(
//                text = { Text("Игроки") },
//                selected = selectedTab == 1,
//                onClick = { selectedTab = 1 }
//            )
            Tab(
                text = { Text("Рекорды") },
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 }
            )
            Tab(
                text = { Text("Регистрация") },
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 }
            )
            Tab(
                text = { Text("Правила") },
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 }
            )
            Tab(
                text = { Text("Авторы") },
                selected = selectedTab == 4,
                onClick = { selectedTab = 4 }
            )
            Tab(
                text = { Text("Настройки") },
                selected = selectedTab == 5,
                onClick = { selectedTab = 5 }
            )
        }

        when (selectedTab) {
            0 -> Game()
//            1 -> Players()
            1 -> HighScores()
            2 -> RegForm()
            3 -> Rules()
            4 -> Authors()
            5 -> Settings()
        }
    }
}