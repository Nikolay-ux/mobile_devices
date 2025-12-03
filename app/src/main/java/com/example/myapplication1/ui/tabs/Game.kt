package com.example.myapplication1.ui.tabs

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
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
import androidx.compose.runtime.collectAsState
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
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import com.example.myapplication1.viewmodel.GameViewModel
import org.koin.androidx.compose.koinViewModel
import kotlin.math.PI
import kotlin.math.atan2

@SuppressLint("ConfigurationScreenWidthHeight", "DefaultLocale")
@Composable
fun Game(
    viewModel: GameViewModel = koinViewModel()
) {
    Log.d("GameTab", "GameTab recomposed")

    val context = LocalContext.current
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val screenHeightDp = configuration.screenHeightDp

    val screenWidthPx = with(density) { (screenWidthDp - 50).dp.toPx() }
    val screenHeightPx = with(density) { (screenHeightDp - 50).dp.toPx() }
    val topPaddingDp = 0.dp
    val topPaddingPx = with(density) { topPaddingDp.toPx() }
    val bottomPaddingDp = 230.dp
    val bottomPaddingPx = with(density) { bottomPaddingDp.toPx() }
    val gameAreaHeightPx = screenHeightPx - topPaddingPx - bottomPaddingPx

    val gameState by viewModel.gameState.collectAsState()
    val timeProgress by animateFloatAsState(
        targetValue = if (GameData.duration > 0) gameState.timeLeft / GameData.duration else 0f,
        label = "TimeProgress"
    )

    LaunchedEffect(screenWidthPx, screenHeightPx, topPaddingPx, gameAreaHeightPx) {
        viewModel.initScreenDimensions(
            screenWidthPx = screenWidthPx,
            screenHeightPx = screenHeightPx,
            topPaddingPx = topPaddingPx,
            bottomPaddingPx = bottomPaddingPx,
            gameAreaHeightPx = gameAreaHeightPx,
            density = density.density
        )
    }

    var sensorManager by remember { mutableStateOf<SensorManager?>(null) }
    var accelerometer by remember { mutableStateOf<Sensor?>(null) }

    val sensorListener = remember {
        object : SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                        val accX = it.values[0]
                        val accY = it.values[1]

                        viewModel.updateAcceleration(accX, accY)
                    }
                }
            }
        }
    }

    DisposableEffect(context) {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        accelerometer?.let { accSensor ->
            sensorManager?.registerListener(sensorListener, accSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
        onDispose {
            sensorManager?.unregisterListener(sensorListener)
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
            Text(text = "Счёт: ${gameState.score}", style = MaterialTheme.typography.titleMedium)
            Text(text = "Промахи: ${gameState.misses}", style = MaterialTheme.typography.titleMedium)
            Text(text = "Время: ${String.format("%.1f", gameState.timeLeft)}", style = MaterialTheme.typography.titleMedium)
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
                            if (gameState.gameActive && GameData.currentPlayerId != -1L) {
                                val prefs = context.getSharedPreferences(
                                    "gold_rate_widget",
                                    Context.MODE_PRIVATE
                                )
                                val goldRateStr = prefs.getString("buy_price", "0")
                                val goldRate = goldRateStr?.toDoubleOrNull()?.toInt() ?: 0

                                viewModel.handleTap(offset.x, offset.y, goldRate, context)
                            }
                        }
                    )
                },
            contentAlignment = Alignment.TopStart
        ) {
            gameState.bugs.forEach { bug ->
                val absoluteX = bug.relativeX * screenWidthPx
                val absoluteY = topPaddingPx + (bug.relativeY * gameAreaHeightPx)

                Image(
                    painter = painterResource(id = bug.imageRes),
                    contentDescription = "Жук",
                    modifier = Modifier
                        .size(bug.size.dp)
                        .graphicsLayer(
                            translationX = absoluteX,
                            translationY = absoluteY,
                            rotationZ = (atan2(bug.dy, bug.dx) * 180 / PI).toFloat()
                        )
                )
            }

            gameState.goldBug?.let { bug ->
                val absoluteX = bug.relativeX * screenWidthPx
                val absoluteY = topPaddingPx + (bug.relativeY * gameAreaHeightPx)

                Image(
                    painter = painterResource(id = bug.imageRes),
                    contentDescription = "Золотой таракан",
                    modifier = Modifier
                        .size(bug.size.dp)
                        .graphicsLayer(
                            translationX = absoluteX,
                            translationY = absoluteY
                        )
                )
            }

            if (gameState.bonusActive) {
                val absoluteX = gameState.bonusRelativeX * screenWidthPx
                val absoluteY = topPaddingPx + (gameState.bonusRelativeY * gameAreaHeightPx)

                Image(
                    painter = painterResource(id = R.drawable.bonus_icon),
                    contentDescription = "Бонус",
                    modifier = Modifier
                        .size(40.dp)
                        .graphicsLayer(
                            translationX = absoluteX,
                            translationY = absoluteY
                        )
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    if (!gameState.gameActive) {
                        viewModel.startGame()
                    }
                },
                enabled = !gameState.gameActive,
                modifier = Modifier.weight(1f)
            ) {
                Text("Старт")
            }
            Button(
                onClick = {
                    if (gameState.gameActive) {
                        viewModel.stopGame()
                    }
                },
                enabled = gameState.gameActive,
                modifier = Modifier.weight(1f)
            ) {
                Text("Стоп")
            }
        }
    }
}