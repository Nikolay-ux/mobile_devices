package com.example.myapplication1.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication1.model.GameData
import com.example.myapplication1.model.HighScore
import com.example.myapplication1.repository.GameRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

data class Bug(
    val id: Int,
    var relativeX: Float,
    var relativeY: Float,
    var dx: Float,
    var dy: Float,
    val size: Int,
    val imageRes: Int,
    val isGold: Boolean = false
)

data class GameState(
    val bugs: List<Bug> = emptyList(),
    val goldBug: Bug? = null,
    val bonusActive: Boolean = false,
    val bonusRelativeX: Float = 0f,
    val bonusRelativeY: Float = 0f,
    val bonusTimeLeft: Float = 0f,
    val score: Int = 0,
    val misses: Int = 0,
    val timeLeft: Float = GameData.duration.toFloat(),
    val gameActive: Boolean = false,
    val goldBugSpawned: Boolean = false,
    val tiltEffectActive: Boolean = false,
    val tiltEffectTimeLeft: Float = 0f,
    val accelerationX: Float = 0f,
    val accelerationY: Float = 0f
)

class GameViewModel(
    private val repository: GameRepository
) : ViewModel() {

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private var screenWidthPx: Float = 0f
    private var screenHeightPx: Float = 0f
    private var topPaddingPx: Float = 0f
    private var bottomPaddingPx: Float = 0f
    private var gameAreaHeightPx: Float = 0f
    private var density: Float = 1f

    private var gameLoopJob: Job? = null
    private var bugSpawnJob: Job? = null
    private var goldBugJob: Job? = null
    private var bonusJob: Job? = null
    private var timerJob: Job? = null
    private var tiltEffectJob: Job? = null

    fun initScreenDimensions(
        screenWidthPx: Float,
        screenHeightPx: Float,
        topPaddingPx: Float,
        bottomPaddingPx: Float,
        gameAreaHeightPx: Float,
        density: Float
    ) {
        this.screenWidthPx = screenWidthPx
        this.screenHeightPx = screenHeightPx
        this.topPaddingPx = topPaddingPx
        this.bottomPaddingPx = bottomPaddingPx
        this.gameAreaHeightPx = gameAreaHeightPx
        this.density = density

        updateBugPositions()
    }

    fun updateAcceleration(accelerationX: Float, accelerationY: Float) {
        _gameState.update { it.copy(
            accelerationX = accelerationX,
            accelerationY = accelerationY
        ) }
    }

    private fun dpToPx(dp: Int): Float {
        return dp * density
    }

    private fun dpToRelativeWidth(dp: Int): Float {
        return dpToPx(dp) / screenWidthPx
    }

    private fun dpToRelativeHeight(dp: Int): Float {
        return dpToPx(dp) / gameAreaHeightPx
    }

    private fun updateBugPositions() {
        val currentState = _gameState.value
        val updatedBugs = currentState.bugs.map { bug ->
            val absoluteX = bug.relativeX * screenWidthPx
            val absoluteY = topPaddingPx + (bug.relativeY * gameAreaHeightPx)

            val clampedRelativeX = (absoluteX / screenWidthPx).coerceIn(0f, 1f)
            val clampedRelativeY = ((absoluteY - topPaddingPx) / gameAreaHeightPx).coerceIn(0f, 1f)

            bug.copy(
                relativeX = clampedRelativeX,
                relativeY = clampedRelativeY
            )
        }

        _gameState.update { it.copy(bugs = updatedBugs) }
    }

    private fun createBug(): Bug {
        val sizeDp = 30 + Random.nextInt(20)
        val speedFactor = (GameData.speed / 5.0f).coerceAtLeast(0.5f)
        val maxSpeed = 5f * speedFactor

        val relativeX = Random.nextFloat()
        val relativeY = Random.nextFloat()

        return Bug(
            id = Random.nextInt(),
            relativeX = relativeX,
            relativeY = relativeY,
            dx = (Random.nextFloat() * 2 * maxSpeed - maxSpeed) / 1000f,
            dy = (Random.nextFloat() * 2 * maxSpeed - maxSpeed) / 1000f,
            size = sizeDp,
            imageRes = when (Random.nextInt(3)) {
                0 -> com.example.myapplication1.R.drawable.bug1
                1 -> com.example.myapplication1.R.drawable.bug2
                else -> com.example.myapplication1.R.drawable.bug3
            }
        )
    }

    private fun createGoldBug(): Bug {
        val relativeX = Random.nextFloat()
        val relativeY = Random.nextFloat()

        return Bug(
            id = Random.nextInt(),
            relativeX = relativeX,
            relativeY = relativeY,
            dx = 0f,
            dy = 0f,
            size = 40,
            imageRes = com.example.myapplication1.R.drawable.gold_bug,
            isGold = true
        )
    }

    fun startGame() {
        if (_gameState.value.gameActive) return

        _gameState.update {
            it.copy(
                gameActive = true,
                score = 0,
                misses = 0,
                timeLeft = GameData.duration.toFloat(),
                bugs = emptyList(),
                goldBug = null,
                bonusActive = false,
                tiltEffectActive = false,
                tiltEffectTimeLeft = 0f
            )
        }

        startGameLoop()
        startBugSpawning()
        startGoldBugTimer()
        startBonusTimer()
        startTimer()
        startTiltEffectTimer()
    }

    fun stopGame() {
        _gameState.update { it.copy(gameActive = false) }

        gameLoopJob?.cancel()
        bugSpawnJob?.cancel()
        goldBugJob?.cancel()
        bonusJob?.cancel()
        timerJob?.cancel()
        tiltEffectJob?.cancel()

        saveHighScore()
    }

    fun handleTap(absoluteX: Float, absoluteY: Float, goldRate: Int, context: android.content.Context) {
        if (!_gameState.value.gameActive || GameData.currentPlayerId == -1L) return

        val relativeX = absoluteX / screenWidthPx
        val relativeY = (absoluteY - topPaddingPx) / gameAreaHeightPx

        var hit = false

        val hitBoxPaddingRelativeX = dpToRelativeWidth(50)
        val hitBoxPaddingRelativeY = dpToRelativeHeight(50)

        val currentGoldBug = _gameState.value.goldBug
        if (currentGoldBug != null) {
            val goldBugRelativeSizeX = dpToRelativeWidth(40)
            val goldBugRelativeSizeY = dpToRelativeHeight(40)

            val minX = currentGoldBug.relativeX - hitBoxPaddingRelativeX
            val maxX = currentGoldBug.relativeX + goldBugRelativeSizeX + hitBoxPaddingRelativeX
            val minY = currentGoldBug.relativeY - hitBoxPaddingRelativeY
            val maxY = currentGoldBug.relativeY + goldBugRelativeSizeY + hitBoxPaddingRelativeY

            if (relativeX >= minX && relativeX <= maxX &&
                relativeY >= minY && relativeY <= maxY) {

                _gameState.update { it.copy(
                    score = it.score + goldRate,
                    goldBug = null,
                    goldBugSpawned = false
                ) }

                hit = true
            }
        }

        if (!hit && _gameState.value.bonusActive) {
            val bonusRelativeSizeX = dpToRelativeWidth(40)
            val bonusRelativeSizeY = dpToRelativeHeight(40)
            val bonusX = _gameState.value.bonusRelativeX
            val bonusY = _gameState.value.bonusRelativeY

            if (relativeX >= bonusX && relativeX <= bonusX + bonusRelativeSizeX &&
                relativeY >= bonusY && relativeY <= bonusY + bonusRelativeSizeY) {

                _gameState.update { it.copy(
                    bonusActive = false,
                    score = it.score + 20,
                    tiltEffectActive = true,
                    tiltEffectTimeLeft = 5f
                ) }

                android.media.MediaPlayer.create(context, com.example.myapplication1.R.raw.bug_scream)?.start()

                hit = true
            }
        }

        if (!hit) {
            val bugs = _gameState.value.bugs.toMutableList()

            for (i in bugs.size - 1 downTo 0) {
                val bug = bugs[i]

                val bugRelativeSizeX = dpToRelativeWidth(bug.size)
                val bugRelativeSizeY = dpToRelativeHeight(bug.size)

                val minX = bug.relativeX - hitBoxPaddingRelativeX
                val maxX = bug.relativeX + bugRelativeSizeX + hitBoxPaddingRelativeX
                val minY = bug.relativeY - hitBoxPaddingRelativeY
                val maxY = bug.relativeY + bugRelativeSizeY + hitBoxPaddingRelativeY

                if (relativeX >= minX && relativeX <= maxX &&
                    relativeY >= minY && relativeY <= maxY) {

                    bugs.removeAt(i)
                    _gameState.update { it.copy(
                        bugs = bugs,
                        score = it.score + 10
                    ) }
                    hit = true
                    break
                }
            }
        }

        if (!hit) {
            _gameState.update {
                it.copy(
                    misses = it.misses + 1,
                    score = maxOf(0, it.score - 5)
                )
            }
        }
    }

    private fun startGameLoop() {
        gameLoopJob = viewModelScope.launch {
            while (_gameState.value.gameActive) {
                updateBugs()
                delay(16)
            }
        }
    }

    private fun updateBugs() {
        val currentState = _gameState.value
        val updatedBugs = currentState.bugs.map { bug ->
            var newRelativeX = bug.relativeX
            var newRelativeY = bug.relativeY
            var newDx = bug.dx
            var newDy = bug.dy

            if (currentState.tiltEffectActive) {
                val tiltFactor = 0.0001f
                newDx -= currentState.accelerationX * tiltFactor
                newDy += currentState.accelerationY * tiltFactor
            }

            newRelativeX += newDx
            newRelativeY += newDy

            if (newRelativeX < 0) {
                newRelativeX = 0f
                if (currentState.tiltEffectActive) {
                    newDx = 0f
                } else {
                    newDx = -newDx
                }
            } else if (newRelativeX > 1) {
                newRelativeX = 1f
                if (currentState.tiltEffectActive) {
                    newDx = 0f
                } else {
                    newDx = -newDx
                }
            }

            if (newRelativeY < 0) {
                newRelativeY = 0f
                if (currentState.tiltEffectActive) {
                    newDy = 0f
                } else {
                    newDy = -newDy
                }
            } else if (newRelativeY > 1) {
                newRelativeY = 1f
                if (currentState.tiltEffectActive) {
                    newDy = 0f
                } else {
                    newDy = -newDy
                }
            }

            bug.copy(
                relativeX = newRelativeX,
                relativeY = newRelativeY,
                dx = newDx,
                dy = newDy
            )
        }

        _gameState.update { it.copy(bugs = updatedBugs) }
    }

    private fun startBugSpawning() {
        bugSpawnJob = viewModelScope.launch {
            while (_gameState.value.gameActive) {
                if (_gameState.value.bugs.size < GameData.maxBugCount) {
                    val newBug = createBug()
                    _gameState.update { it.copy(bugs = it.bugs + newBug) }
                }
                delay(1000)
            }
        }
    }

    private fun startGoldBugTimer() {
        goldBugJob = viewModelScope.launch {
            while (_gameState.value.gameActive) {
                delay(20000)
                if (_gameState.value.gameActive && _gameState.value.goldBug == null) {
                    _gameState.update { it.copy(goldBug = createGoldBug()) }

                    launch {
                        delay(5000)
                        if (_gameState.value.gameActive && _gameState.value.goldBug != null) {
                            _gameState.update { it.copy(goldBug = null) }
                        }
                    }
                }
            }
        }
    }

    private fun startBonusTimer() {
        bonusJob = viewModelScope.launch {
            while (_gameState.value.gameActive) {
                delay(15000)
                if (_gameState.value.gameActive && !_gameState.value.bonusActive) {
                    _gameState.update {
                        it.copy(
                            bonusActive = true,
                            bonusRelativeX = Random.nextFloat() * 0.8f + 0.1f,
                            bonusRelativeY = Random.nextFloat() * 0.8f + 0.1f
                        )
                    }
                }
            }
        }
    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            val totalTimeMs = GameData.duration * 1000L

            while (_gameState.value.gameActive) {
                val elapsed = (System.currentTimeMillis() - startTime).coerceAtMost(totalTimeMs)
                val timeLeft = ((totalTimeMs - elapsed) / 1000f).coerceAtLeast(0f)

                _gameState.update { it.copy(timeLeft = timeLeft) }

                if (timeLeft <= 0) {
                    stopGame()
                    break
                }

                delay(100)
            }
        }
    }

    private fun startTiltEffectTimer() {
        tiltEffectJob = viewModelScope.launch {
            while (_gameState.value.gameActive) {
                delay(100)
                val currentState = _gameState.value
                if (currentState.tiltEffectActive && currentState.tiltEffectTimeLeft > 0) {
                    _gameState.update {
                        it.copy(tiltEffectTimeLeft = it.tiltEffectTimeLeft - 0.1f)
                    }

                    if (_gameState.value.tiltEffectTimeLeft <= 0) {
                        _gameState.update { it.copy(tiltEffectActive = false) }
                    }
                }
            }
        }
    }

    private fun saveHighScore() {
        val currentState = _gameState.value

        if (GameData.currentPlayerId != -1L && currentState.score > 0) {
            viewModelScope.launch(Dispatchers.IO) {
                val newScore = HighScore(
                    playerId = GameData.currentPlayerId,
                    score = currentState.score
                )
                repository.insertScoreIfHigher(newScore)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopGame()
    }
}