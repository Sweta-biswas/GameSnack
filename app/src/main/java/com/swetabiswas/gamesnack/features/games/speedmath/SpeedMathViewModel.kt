package com.swetabiswas.gamesnack.features.games.speedmath

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swetabiswas.gamesnack.data.repository.ProfileRepository
import com.swetabiswas.gamesnack.data.repository.ScoreRepository
import com.swetabiswas.gamesnack.gamification.XPSystem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import kotlin.random.Random

enum class MathGameState { IDLE, PLAYING, GAME_OVER }

data class MathQuestion(
    val a: Int,
    val b: Int,
    val operator: String,
    val answer: Int
)

data class SpeedMathUiState(
    val gameState: MathGameState = MathGameState.IDLE,
    val question: MathQuestion = MathQuestion(0, 0, "+", 0),
    val score: Int = 0,
    val bestScore: Int = 0,
    val timeLeftSeconds: Int = 60,
    val input: String = "",
    val isWrongAnswer: Boolean = false,
    val correctCount: Int = 0,
    val wrongCount: Int = 0
)

@HiltViewModel
class SpeedMathViewModel @Inject constructor(
    private val scoreRepository: ScoreRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SpeedMathUiState())
    val uiState: StateFlow<SpeedMathUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            scoreRepository.getScoreForGame("math").collectLatest { score ->
                _uiState.update { it.copy(bestScore = score?.bestScore ?: 0) }
            }
        }
    }

    fun startGame() {
        timerJob?.cancel()
        _uiState.update {
            it.copy(
                gameState    = MathGameState.PLAYING,
                score        = 0,
                timeLeftSeconds = 60,
                input        = "",
                correctCount = 0,
                wrongCount   = 0,
                isWrongAnswer = false,
                question     = generateQuestion(difficulty = 1)
            )
        }
        startTimer()
    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            while (_uiState.value.timeLeftSeconds > 0) {
                delay(1000L)
                _uiState.update { it.copy(timeLeftSeconds = it.timeLeftSeconds - 1) }
            }
            endGame()
        }
    }

    fun onKeyPress(key: String) {
        if (_uiState.value.gameState != MathGameState.PLAYING) return

        val currentInput = _uiState.value.input
        when (key) {
            "⌫" -> _uiState.update { it.copy(input = currentInput.dropLast(1)) }
            "✓" -> checkAnswer()
            "-" -> {
                // Allow negative sign only at start
                if (currentInput.isEmpty()) {
                    _uiState.update { it.copy(input = "-") }
                }
            }
            else -> {
                if (currentInput.length < 5) {
                    _uiState.update { it.copy(input = currentInput + key, isWrongAnswer = false) }
                }
            }
        }
    }

    private fun checkAnswer() {
        val state    = _uiState.value
        val input    = state.input.toLongOrNull()
        val expected = state.question.answer

        if (input == null) return

        if (input.toInt() == expected) {
            val newScore = state.score + 1
            val difficulty = (newScore / 5) + 1   // difficulty increases every 5 correct
            _uiState.update {
                it.copy(
                    score        = newScore,
                    input        = "",
                    correctCount = it.correctCount + 1,
                    isWrongAnswer = false,
                    question     = generateQuestion(difficulty)
                )
            }
        } else {
            _uiState.update {
                it.copy(isWrongAnswer = true, wrongCount = it.wrongCount + 1, input = "")
            }
            viewModelScope.launch {
                delay(500)
                _uiState.update { it.copy(isWrongAnswer = false) }
            }
        }
    }

    private fun endGame() {
        val finalScore = _uiState.value.score
        _uiState.update { it.copy(gameState = MathGameState.GAME_OVER) }

        viewModelScope.launch {
            scoreRepository.recordScore("math", finalScore)
            val xp = finalScore * XPSystem.XP_MATH_CORRECT
            profileRepository.awardXp(xp)
        }
    }

    fun resetGame() {
        timerJob?.cancel()
        _uiState.update {
            it.copy(
                gameState = MathGameState.IDLE,
                input     = "",
                isWrongAnswer = false
            )
        }
    }

    private fun generateQuestion(difficulty: Int): MathQuestion {
        val ops = when {
            difficulty <= 1 -> listOf("+", "-")
            difficulty <= 3 -> listOf("+", "-", "×")
            else            -> listOf("+", "-", "×")
        }
        val op = ops.random()
        val maxNum = when {
            difficulty <= 1 -> 10
            difficulty <= 2 -> 20
            difficulty <= 3 -> 50
            else            -> 99
        }

        val a = Random.nextInt(1, maxNum + 1)
        val b = Random.nextInt(1, maxNum + 1)

        return when (op) {
            "+"  -> MathQuestion(a, b, "+", a + b)
            "-"  -> {
                val bigger = maxOf(a, b)
                val smaller = minOf(a, b)
                MathQuestion(bigger, smaller, "-", bigger - smaller)
            }
            "×"  -> {
                val fa = Random.nextInt(2, 13)
                val fb = Random.nextInt(2, 13)
                MathQuestion(fa, fb, "×", fa * fb)
            }
            else -> MathQuestion(a, b, "+", a + b)
        }
    }
}
