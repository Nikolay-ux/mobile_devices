package com.example.myapplication1.ui


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.example.myapplication1.ui.tabs.Authors
import com.example.myapplication1.ui.tabs.RegForm
import com.example.myapplication1.ui.tabs.Rules
import com.example.myapplication1.ui.tabs.Settings

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
                text = { Text("Регистрация") },
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 }
            )
            Tab(
                text = { Text("Правила") },
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 }
            )
            Tab(
                text = { Text("Авторы") },
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 }
            )
            Tab(
                text = { Text("Настройки") },
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 }
            )
        }

        when (selectedTab) {
            0 -> RegForm()
            1 -> Rules()
            2 -> Authors()
            3 -> Settings()
        }
    }
}