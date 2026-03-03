package com.swetabiswas.gamesnack.features.games.tictactoe

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

enum class GameMode { TWO_PLAYER, AI_EASY, AI_HARD }
enum class TttGameState { MODE_SELECTION, PLAYING, GAME_OVER }

data class TicTacToeUiState(
    val gameState   : TttGameState = TttGameState.MODE_SELECTION,
    val mode        : GameMode     = GameMode.TWO_PLAYER,
    val board       : List<String> = List(9) { "" },
    val currentPlayer: String      = "X",
    val winner      : String?      = null,      // "X", "O", "Draw"
    val winningLine : List<Int>?   = null,
    val scoreX      : Int          = 0,
    val scoreO      : Int          = 0,
    val draws       : Int          = 0,
    val gamesPlayed : Int          = 0,
    val bestScore   : Int          = 0,         // TTT wins (X wins in AI mode)
    val isAiThinking: Boolean      = false
)

@HiltViewModel
class TicTacToeViewModel @Inject constructor(
    private val scoreRepository: ScoreRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TicTacToeUiState())
    val uiState: StateFlow<TicTacToeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            scoreRepository.getScoreForGame("ttt").collectLatest { score ->
                _uiState.update { it.copy(bestScore = score?.bestScore ?: 0) }
            }
        }
    }

    fun selectMode(mode: GameMode) {
        _uiState.update {
            it.copy(
                gameState    = TttGameState.PLAYING,
                mode         = mode,
                board        = List(9) { "" },
                currentPlayer = "X",
                winner       = null,
                winningLine  = null,
                scoreX       = 0,
                scoreO       = 0,
                draws        = 0,
                gamesPlayed  = 0
            )
        }
    }

    fun onCellClick(index: Int) {
        val state = _uiState.value
        if (state.board[index] != "" || state.winner != null || state.isAiThinking) return
        if (state.gameState != TttGameState.PLAYING) return

        makeMove(index, state.currentPlayer)
    }

    private fun makeMove(index: Int, player: String) {
        val state = _uiState.value
        val newBoard = state.board.toMutableList().also { it[index] = player }
        val winner   = TicTacToeAI.checkWinner(newBoard)
        val wLine    = TicTacToeAI.winningLine(newBoard)
        val nextPlayer = if (player == "X") "O" else "X"

        _uiState.update {
            it.copy(
                board         = newBoard,
                currentPlayer = nextPlayer,
                winner        = winner,
                winningLine   = wLine
            )
        }

        if (winner != null) {
            onGameEnd(winner, newBoard)
        } else if (isAiTurn()) {
            triggerAiMove()
        }
    }

    private fun isAiTurn(): Boolean {
        val state = _uiState.value
        return (state.mode == GameMode.AI_EASY || state.mode == GameMode.AI_HARD) &&
               state.currentPlayer == "O" &&
               state.winner == null
    }

    private fun triggerAiMove() {
        viewModelScope.launch {
            _uiState.update { it.copy(isAiThinking = true) }
            delay(500) // Human-feel delay

            val state = _uiState.value
            val idx = when (state.mode) {
                GameMode.AI_EASY -> TicTacToeAI.easyMove(state.board)
                GameMode.AI_HARD -> TicTacToeAI.hardMove(state.board)
                else             -> return@launch
            }

            _uiState.update { it.copy(isAiThinking = false) }
            if (idx >= 0) makeMove(idx, "O")
        }
    }

    private fun onGameEnd(winner: String, board: List<String>) {
        val isAiMode = _uiState.value.mode != GameMode.TWO_PLAYER
        when (winner) {
            "X" -> {
                val newScoreX = _uiState.value.scoreX + 1
                val newPlayed = _uiState.value.gamesPlayed + 1
                _uiState.update { it.copy(scoreX = newScoreX, gamesPlayed = newPlayed) }

                viewModelScope.launch {
                    scoreRepository.recordScore("ttt", newScoreX)
                    profileRepository.awardXp(XPSystem.XP_TTT_WIN)
                }
            }
            "O" -> {
                _uiState.update {
                    it.copy(scoreO = it.scoreO + 1, gamesPlayed = it.gamesPlayed + 1)
                }
                if (!isAiMode) {
                    viewModelScope.launch { profileRepository.awardXp(XPSystem.XP_TTT_WIN) }
                }
            }
            "Draw" -> {
                _uiState.update {
                    it.copy(draws = it.draws + 1, gamesPlayed = it.gamesPlayed + 1)
                }
                viewModelScope.launch { profileRepository.awardXp(XPSystem.XP_TTT_DRAW) }
            }
        }
    }

    fun resetRound() {
        _uiState.update {
            it.copy(
                board        = List(9) { "" },
                currentPlayer = "X",
                winner       = null,
                winningLine  = null,
                isAiThinking = false
            )
        }
    }

    fun backToModeSelection() {
        _uiState.update { TicTacToeUiState(bestScore = it.bestScore) }
    }
}
