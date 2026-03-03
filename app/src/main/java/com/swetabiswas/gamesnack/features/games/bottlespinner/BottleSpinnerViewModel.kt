package com.swetabiswas.gamesnack.features.games.bottlespinner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swetabiswas.gamesnack.data.repository.ProfileRepository
import com.swetabiswas.gamesnack.data.repository.ScoreRepository
import com.swetabiswas.gamesnack.gamification.XPSystem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

enum class SpinnerMode { TRUTH, DARE, CHALLENGE }

data class BottleUiState(
    val players: List<String>      = listOf("Alice", "Bob", "Charlie", "Dave"),
    val selectedPlayer: String?    = null,
    val rotationDegrees: Float     = 0f,
    val targetDegrees: Float       = 0f,
    val isSpinning: Boolean        = false,
    val mode: SpinnerMode          = SpinnerMode.TRUTH,
    val prompt: String?            = null,
    val spinCount: Int             = 0,
    val showAddPlayer: Boolean     = false,
    val newPlayerName: String      = ""
)

@HiltViewModel
class BottleSpinnerViewModel @Inject constructor(
    private val scoreRepository: ScoreRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BottleUiState())
    val uiState: StateFlow<BottleUiState> = _uiState.asStateFlow()

    fun spin() {
        if (_uiState.value.isSpinning || _uiState.value.players.size < 2) return

        val state = _uiState.value
        val extraSpins = Random.nextInt(3, 8).toFloat() * 360f
        val targetAngle = state.rotationDegrees + extraSpins + Random.nextFloat() * 360f

        _uiState.update {
            it.copy(
                isSpinning    = true,
                targetDegrees = targetAngle,
                selectedPlayer = null,
                prompt         = null
            )
        }

        viewModelScope.launch {
            // Simulate spin duration
            delay(3000L)

            // Determine winner based on final angle
            val finalAngle = targetAngle % 360f
            val sectorSize = 360f / state.players.size
            val idx = ((finalAngle / sectorSize).toInt()) % state.players.size
            val winner = state.players[idx]
            val newCount = state.spinCount + 1

            _uiState.update {
                it.copy(
                    isSpinning     = false,
                    rotationDegrees = targetAngle,
                    selectedPlayer  = winner,
                    prompt          = pickPrompt(it.mode),
                    spinCount       = newCount
                )
            }

            scoreRepository.recordScore("bottle", newCount)
            profileRepository.awardXp(XPSystem.XP_BOTTLE_SPIN)
        }
    }

    fun setMode(mode: SpinnerMode) {
        _uiState.update { it.copy(mode = mode) }
    }

    fun addPlayer() {
        val name = _uiState.value.newPlayerName.trim()
        if (name.isNotEmpty() && !_uiState.value.players.contains(name)) {
            _uiState.update {
                it.copy(
                    players       = it.players + name,
                    newPlayerName  = "",
                    showAddPlayer  = false
                )
            }
        }
    }

    fun removePlayer(name: String) {
        val players = _uiState.value.players.filter { it != name }
        if (players.size >= 2) {
            _uiState.update { it.copy(players = players) }
        }
    }

    fun setNewPlayerName(name: String) {
        _uiState.update { it.copy(newPlayerName = name) }
    }

    fun toggleAddPlayer() {
        _uiState.update { it.copy(showAddPlayer = !it.showAddPlayer) }
    }

    private fun pickPrompt(mode: SpinnerMode): String {
        val truths = listOf(
            "What's the most embarrassing thing you've done?",
            "Who do you have a crush on?",
            "What's your biggest fear?",
            "Have you ever lied to a friend?",
            "What's the worst thing you've eaten?",
            "What's a secret you've never told anyone?"
        )
        val dares = listOf(
            "Do 20 push-ups right now!",
            "Speak in an accent for 2 minutes",
            "Text someone random and say Hi!",
            "Do your best impression of someone here",
            "Eat something spicy without water",
            "Hold a plank for 30 seconds"
        )
        val challenges = listOf(
            "Say the alphabet backwards in 10 seconds",
            "Name 10 countries in 15 seconds",
            "Do a handstand for 5 seconds",
            "Sing the chorus of a popular song",
            "Count to 50 in another language",
            "Solve: 17 × 13 = ?"
        )
        return when (mode) {
            SpinnerMode.TRUTH     -> truths.random()
            SpinnerMode.DARE      -> dares.random()
            SpinnerMode.CHALLENGE -> challenges.random()
        }
    }
}
