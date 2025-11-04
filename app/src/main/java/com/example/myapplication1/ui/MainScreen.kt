package com.example.myapplication1.ui


import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication1.ui.tabs.Authors
import com.example.myapplication1.ui.tabs.RegForm
import com.example.myapplication1.ui.tabs.Rules
import com.example.myapplication1.ui.tabs.Settings
import com.example.myapplication1.ui.tabs.Game
import com.example.myapplication1.ui.tabs.HighScores
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

@Composable
fun MainScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val scrollState = rememberScrollState()

    val pagerState = rememberPagerState(
        pageCount = { 6 },
        initialPage = 0
    )

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage to pagerState.isScrollInProgress }
            .distinctUntilChanged()
            .filter { (_, isScrollInProgress) -> !isScrollInProgress }
            .collect { (currentPage, _) ->
                if (currentPage != selectedTab) {
                    selectedTab = currentPage
                }
            }
    }

    LaunchedEffect(selectedTab) {
        if (pagerState.currentPage != selectedTab) {
            pagerState.animateScrollToPage(selectedTab)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .horizontalScroll(scrollState)
                .background(MaterialTheme.colorScheme.primaryContainer),
        ) {
            Tab(
                text = { Text("Игра") },
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                modifier = Modifier.padding(top = 30.dp)
                    .background(color = if (selectedTab == 0) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    })
            )
            Tab(
                text = { Text("Рекорды") },
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                modifier = Modifier.padding( top = 30.dp)
                    .background(color = if (selectedTab == 1) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    })
            )
            Tab(
                text = { Text("Регистрация") },
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                modifier = Modifier.padding( top = 30.dp)
                    .background(color = if (selectedTab == 2) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    })
            )
            Tab(
                text = { Text("Правила") },
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                modifier = Modifier.padding( top = 30.dp)
                    .background(color = if (selectedTab == 3) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    })
            )
            Tab(
                text = { Text("Авторы") },
                selected = selectedTab == 4,
                onClick = { selectedTab = 4 },
                modifier = Modifier.padding( top = 30.dp)
                    .background(color = if (selectedTab == 4) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    })
            )
            Tab(
                text = { Text("Настройки") },
                selected = selectedTab == 5,
                onClick = { selectedTab = 5 },
                modifier = Modifier.padding( top = 30.dp)
                    .background(color = if (selectedTab == 5) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    })
            )
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
        ) { page ->
            when (page) {
                0 -> Game()
                1 -> HighScores()
                2 -> RegForm()
                3 -> Rules()
                4 -> Authors()
                5 -> Settings()
            }
        }
    }
}