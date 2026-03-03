package com.swetabiswas.gamesnack.features.games.reactiontime

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

enum class ReactionState {
    IDLE,        // Ready to start
    WAITING,     // Waiting for the "tap!" signal
    TAP_NOW,     // Tap NOW! (color changed)
    TOO_EARLY,   // Tapped too early
    RESULT       // Showing result
}

data class ReactionUiState(
    val state: ReactionState = ReactionState.IDLE,
    val reactionTimeMs: Long = 0L,
    val bestTimeMs: Long = 0L,     // 0 = no record
    val attemptCount: Int = 0,
    val isNewBest: Boolean = false,
    val message: String = "Tap to Start"
)

@HiltViewModel
class ReactionTimeViewModel @Inject constructor(
    private val scoreRepository: ScoreRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReactionUiState())
    val uiState: StateFlow<ReactionUiState> = _uiState.asStateFlow()

    private var tapStartTime: Long = 0L
    private var waitJob: Job? = null

    init {
        loadBestTime()
    }

    private fun loadBestTime() {
        viewModelScope.launch {
            scoreRepository.getScoreForGame("reaction").collectLatest { score ->
                val best = score?.bestScore?.toLong() ?: 0L
                _uiState.update { it.copy(bestTimeMs = best) }
            }
        }
    }

    fun onScreenTap() {
        when (_uiState.value.state) {
            ReactionState.IDLE, ReactionState.RESULT, ReactionState.TOO_EARLY -> startWaiting()
            ReactionState.WAITING -> tooEarly()
            ReactionState.TAP_NOW -> recordReaction()
        }
    }

    private fun startWaiting() {
        waitJob?.cancel()
        val delayMs = Random.nextLong(1500, 5000)

        _uiState.update {
            it.copy(
                state   = ReactionState.WAITING,
                message = "Wait for it...",
                isNewBest = false
            )
        }

        waitJob = viewModelScope.launch {
            delay(delayMs)
            if (isActive) {
                tapStartTime = System.currentTimeMillis()
                _uiState.update {
                    it.copy(
                        state   = ReactionState.TAP_NOW,
                        message = "TAP NOW!"
                    )
                }
            }
        }
    }

    private fun tooEarly() {
        waitJob?.cancel()
        _uiState.update {
            it.copy(
                state   = ReactionState.TOO_EARLY,
                message = "Too early! Tap to try again"
            )
        }
    }

    private fun recordReaction() {
        val elapsed = System.currentTimeMillis() - tapStartTime
        val currentBest = _uiState.value.bestTimeMs
        val isNewBest = currentBest == 0L || elapsed < currentBest

        _uiState.update {
            it.copy(
                state          = ReactionState.RESULT,
                reactionTimeMs = elapsed,
                bestTimeMs     = if (isNewBest) elapsed else currentBest,
                attemptCount   = it.attemptCount + 1,
                isNewBest      = isNewBest,
                message        = if (isNewBest) "🎉 New Best!" else "Nice try!"
            )
        }

        // Save score and award XP
        viewModelScope.launch {
            scoreRepository.recordScore("reaction", elapsed.toInt())
            val xp = if (isNewBest) XPSystem.XP_REACTION_BEST else XPSystem.XP_REACTION_PLAY
            profileRepository.awardXp(xp)
        }
    }

    fun reset() {
        waitJob?.cancel()
        _uiState.update {
            it.copy(
                state   = ReactionState.IDLE,
                message = "Tap to Start"
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        waitJob?.cancel()
    }
}
