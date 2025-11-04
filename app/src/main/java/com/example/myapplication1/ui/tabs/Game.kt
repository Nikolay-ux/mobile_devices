package com.example.myapplication1.ui.tabs

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.myapplication1.R
import com.example.myapplication1.model.GameData
import kotlinx.coroutines.delay
import kotlin.random.Random
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import com.example.myapplication1.model.HighScore
import com.example.myapplication1.repository.GameRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class Bug(
    val id: Int,
    val initialX: Float,
    val initialY: Float,
    var dx: Float,
    var dy: Float,
    val size: Int,
    val imageRes: Int
)

@SuppressLint("ConfigurationScreenWidthHeight", "DefaultLocale")
@Composable
fun Game() {
    Log.d("GameTab", "GameTab recomposed")
    val context = LocalContext.current
    val repository = remember { GameRepository.getInstance(context) }
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val screenHeightDp = configuration.screenHeightDp

    val screenWidthPx = with(density) { (screenWidthDp - 50).dp.toPx() }
    val topPaddingDp = 0.dp
    val topPaddingPx = with(density) { topPaddingDp.toPx() }

    val bottomPaddingDp = 230.dp
    val bottomPaddingPx = with(density) { bottomPaddingDp.toPx() }

    val gameAreaHeightPx = with(density) { screenHeightDp.dp.toPx() } - topPaddingPx - bottomPaddingPx

    val bugs = remember { mutableStateListOf<Bug>() }
    var score by GameData::score
    var misses by GameData::misses
    var timeLeft by GameData::timeLeft
    var gameActive by GameData::gameActive

    val bugImages = remember { listOf(R.drawable.bug1, R.drawable.bug2, R.drawable.bug3) }

    val timeProgress by animateFloatAsState(
        targetValue = if (GameData.duration > 0) timeLeft / GameData.duration else 0f,
        label = "TimeProgress"
    )

    fun createBug(): Bug {
        val sizeDp = 30 + Random.nextInt(20)
        val sizePx = with(density) { sizeDp.dp.toPx() }
        val speedFactor = (GameData.speed / 5.0f).coerceAtLeast(0.5f)
        val maxSpeed = 5f * speedFactor

        val x = Random.nextFloat() * (screenWidthPx - sizePx)
        val y = topPaddingPx + Random.nextFloat() * (gameAreaHeightPx - sizePx)

        return Bug(
            id = Random.nextInt(),
            initialX = x,
            initialY = y,
            dx = (Random.nextFloat() * 2 * maxSpeed - maxSpeed),
            dy = (Random.nextFloat() * 2 * maxSpeed - maxSpeed),
            size = sizeDp,
            imageRes = bugImages.random()
        )
    }

    val bugPositions = remember { mutableMapOf<Int, Pair<Float, Float>>() }

    LaunchedEffect(gameActive) {
        if (gameActive) {
            val startTime = System.currentTimeMillis()
            val totalTimeMs = GameData.duration * 1000L
            while (gameActive && (System.currentTimeMillis() - startTime) < totalTimeMs) {
                val elapsedMs = (System.currentTimeMillis() - startTime).coerceAtMost(totalTimeMs)
                timeLeft = ((totalTimeMs - elapsedMs) / 1000.0f).coerceAtLeast(0f)
                delay(100)
            }
            if (gameActive) {
                gameActive = false
            }
        }
    }

    LaunchedEffect(gameActive) {
        val targetTime = 1000L / 16
        while (gameActive) {
            bugs.forEach { bug ->
                val currentX = bugPositions[bug.id]?.first ?: bug.initialX
                val currentY = bugPositions[bug.id]?.second ?: bug.initialY
                val sizePx = with(density) { bug.size.dp.toPx() }

                var newX = currentX + bug.dx
                var newY = currentY + bug.dy

                if (newX < 0) {
                    newX = 0f
                    bug.dx = -bug.dx
                } else if (newX + sizePx > screenWidthPx) {
                    newX = screenWidthPx - sizePx
                    bug.dx = -bug.dx
                }

                if (newY < topPaddingPx) {
                    newY = topPaddingPx
                    bug.dy = -bug.dy
                } else if (newY + sizePx > topPaddingPx + gameAreaHeightPx) {
                    newY = topPaddingPx + gameAreaHeightPx - sizePx
                    bug.dy = -bug.dy
                }

                bugPositions[bug.id] = Pair(newX, newY)
            }
            delay(targetTime)
        }
    }

    LaunchedEffect(gameActive) {
        while (gameActive) {
            if (bugs.size < GameData.maxBugCount) {
                val newBug = createBug()
                bugs.add(newBug)
                bugPositions[newBug.id] = Pair(newBug.initialX, newBug.initialY)
            }
            delay(1000L)
        }
    }

    LaunchedEffect(gameActive) {
        while (true) {
            if (!gameActive && bugs.isNotEmpty()) {
                bugs.clear()
                bugPositions.clear()

                if (GameData.currentPlayerId != -1L && score > 0) {
                    val newScore = HighScore(
                        playerId = GameData.currentPlayerId,
                        score = score
                    )
                    CoroutineScope(Dispatchers.IO).launch {
                        repository.insertScore(newScore)
                    }
                }
            }
            delay(500)
        }
    }

    LaunchedEffect(gameActive) {
        while (true) {
            if (!gameActive && bugs.isNotEmpty()) {
                bugs.clear()
                bugPositions.clear()
            }
            delay(500)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (GameData.currentPlayerId != -1L) {
            Text(
                text = "Игрок: ${GameData.currentPlayerName}",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Text(
                text = "Игрок не выбран",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.error
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Счёт: $score", style = MaterialTheme.typography.titleMedium)
            Text(text = "Промахи: $misses", style = MaterialTheme.typography.titleMedium)
            Text(text = "Время: ${String.format("%.1f", timeLeft)}", style = MaterialTheme.typography.titleMedium)
        }

        LinearProgressIndicator(
            progress = { timeProgress },
            modifier = Modifier.fillMaxWidth(),
            color = ProgressIndicatorDefaults.linearColor,
            trackColor = ProgressIndicatorDefaults.linearTrackColor,
            strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { offset ->
                            if (gameActive) {
                                val tapX = offset.x
                                val tapY = offset.y
                                var hit = false

                                for (i in bugs.size - 1 downTo 0) {
                                    val bug = bugs[i]
                                    val (bugX, bugY) = bugPositions[bug.id] ?: continue
                                    val sizePx = with(density) { bug.size.dp.toPx() }
                                    val hitBoxPadding = 50.dp
                                    val hitBoxPaddingPx = with(density) { hitBoxPadding.toPx() }

                                    if (tapX >= bugX - hitBoxPaddingPx && tapX <= bugX + sizePx + hitBoxPaddingPx &&
                                        tapY >= bugY - hitBoxPaddingPx && tapY <= bugY + sizePx + hitBoxPaddingPx
                                    ) {
                                        bugs.removeAt(i)
                                        bugPositions.remove(bug.id)
                                        score += 10
                                        hit = true
                                        break
                                    }
                                }
                                if (!hit) {
                                    misses += 1
                                    if (score > 0) score -= 5
                                }
                            }
                        }
                    )
                },
            contentAlignment = Alignment.TopStart
        ) {
            bugs.forEach { bug ->
                val (currentX, currentY) = bugPositions[bug.id] ?: Pair(bug.initialX, bug.initialY)

                val imageModifier = remember(currentX, currentY, bug.dx, bug.dy) {
                    Modifier
                        .size(bug.size.dp)
                        .graphicsLayer(
                            translationX = currentX,
                            translationY = currentY,
                            rotationZ = (kotlin.math.atan2(
                                bug.dy,
                                bug.dx
                            ) * 180 / kotlin.math.PI).toFloat()
                        )
                }

                Image(
                    painter = painterResource(id = bug.imageRes),
                    contentDescription = "Жук",
                    modifier = imageModifier
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    if (!gameActive) {
                        gameActive = true
                        timeLeft = GameData.duration.toFloat()
                        score = 0
                        misses = 0
                        bugs.forEach { bug ->
                            val sizePx = with(density) { bug.size.dp.toPx() }
                            val clampedX = bug.initialX.coerceIn(0f, screenWidthPx - sizePx)
                            val clampedY = bug.initialY.coerceIn(topPaddingPx, topPaddingPx + gameAreaHeightPx - sizePx)
                            bugPositions[bug.id] = Pair(clampedX, clampedY)
                        }
                    }
                },
                enabled = !gameActive,
                modifier = Modifier.weight(1f)
            ) {
                Text("Старт")
            }
            Button(
                onClick = {
                    gameActive = false
                },
                enabled = gameActive,
                modifier = Modifier.weight(1f)
            ) {
                Text("Стоп")
            }
        }
    }
}
