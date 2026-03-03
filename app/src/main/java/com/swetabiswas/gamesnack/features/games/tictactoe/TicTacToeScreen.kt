package com.swetabiswas.gamesnack.features.games.tictactoe

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.swetabiswas.gamesnack.core.theme.*

@Composable
fun TicTacToeScreen(
    navController: NavController,
    viewModel: TicTacToeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DeepPurple, Color(0xFF0D0721))))
    ) {
        when (state.gameState) {
            TttGameState.MODE_SELECTION -> {
                IconButton(
                    onClick  = { navController.popBackStack() },
                    modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextPrimary)
                }
                ModeSelectionScreen(onSelectMode = { viewModel.selectMode(it) })
            }
            TttGameState.PLAYING, TttGameState.GAME_OVER -> {
                TttGameScreen(
                    state    = state,
                    onCell   = { viewModel.onCellClick(it) },
                    onReset  = { viewModel.resetRound() },
                    onBack   = { viewModel.backToModeSelection() }
                )
            }
        }
    }
}

// ── Mode Selection ────────────────────────────────────────────────────────────
@Composable
private fun ModeSelectionScreen(onSelectMode: (GameMode) -> Unit) {
    Column(
        modifier            = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("❌⭕", fontSize = 72.sp)
        Spacer(Modifier.height(12.dp))
        Text(
            "Tic Tac Toe",
            style = MaterialTheme.typography.displaySmall.copy(
                color = TextPrimary, fontWeight = FontWeight.ExtraBold
            )
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Choose your game mode",
            style = MaterialTheme.typography.bodyLarge.copy(color = TextSecondary)
        )
        Spacer(Modifier.height(40.dp))

        ModeButton("👫  2 Player", "Play with a friend", NeonPurple) {
            onSelectMode(GameMode.TWO_PLAYER)
        }
        Spacer(Modifier.height(12.dp))
        ModeButton("🤖  AI – Easy", "Beat the rookie AI", NeonGreen) {
            onSelectMode(GameMode.AI_EASY)
        }
        Spacer(Modifier.height(12.dp))
        ModeButton("🧠  AI – Hard", "Can you beat minimax?", NeonRed) {
            onSelectMode(GameMode.AI_HARD)
        }
    }
}

@Composable
private fun ModeButton(
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        onClick  = onClick,
        modifier = Modifier
            .padding(horizontal = 32.dp)
            .fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color, CircleShape)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium.copy(
                    color = TextPrimary, fontWeight = FontWeight.Bold))
                Text(subtitle, style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
            }
        }
    }
}

// ── Game Screen ───────────────────────────────────────────────────────────────
@Composable
private fun TttGameScreen(
    state: TicTacToeUiState,
    onCell: (Int) -> Unit,
    onReset: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier            = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Back + Mode label
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextPrimary)
            }
            Text(
                modeName(state.mode),
                style    = MaterialTheme.typography.titleMedium.copy(color = TextSecondary),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.width(48.dp)) // balance the back button
        }

        Spacer(Modifier.height(8.dp))

        // Scoreboard
        ScoreBoard(
            scoreX = state.scoreX,
            scoreO = state.scoreO,
            draws  = state.draws
        )

        Spacer(Modifier.height(16.dp))

        // Turn indicator
        AnimatedContent(
            targetState = state.winner,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "status"
        ) { winner ->
            if (winner == null) {
                if (state.isAiThinking) {
                    Text(
                        "🤖 AI is thinking...",
                        style = MaterialTheme.typography.titleMedium.copy(color = TextSecondary)
                    )
                } else {
                    val playerSymbol = if (state.currentPlayer == "X") "❌" else "⭕"
                    Text(
                        "$playerSymbol Player ${state.currentPlayer}'s turn",
                        style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    )
                }
            } else {
                val msg = when (winner) {
                    "Draw" -> "🤝 It's a Draw!"
                    "X"    -> "❌ Player X Wins!"
                    else   -> if (state.mode == GameMode.TWO_PLAYER) "⭕ Player O Wins!" else "🤖 AI Wins!"
                }
                Text(
                    msg,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = when (winner) {
                            "X"    -> NeonPurple
                            "O"    -> NeonPink
                            else   -> NeonYellow
                        },
                        fontWeight = FontWeight.ExtraBold
                    )
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // Board
        TttBoard(
            board       = state.board,
            winningLine = state.winningLine,
            onCell      = onCell
        )

        Spacer(Modifier.height(24.dp))

        // Buttons
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onReset,
                shape   = RoundedCornerShape(12.dp),
                colors  = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                modifier = Modifier.weight(1f)
            ) {
                Text("New Round", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}

// ── Board ─────────────────────────────────────────────────────────────────────
@Composable
private fun TttBoard(
    board: List<String>,
    winningLine: List<Int>?,
    onCell: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(8.dp)
    ) {
        // Grid lines
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val stroke = Stroke(width = 4f, cap = StrokeCap.Round)
            val color = Color(0xFF3D2E70)
            // Vertical
            drawLine(color, Offset(w / 3, 0f), Offset(w / 3, h), strokeWidth = 4f)
            drawLine(color, Offset(2 * w / 3, 0f), Offset(2 * w / 3, h), strokeWidth = 4f)
            // Horizontal
            drawLine(color, Offset(0f, h / 3), Offset(w, h / 3), strokeWidth = 4f)
            drawLine(color, Offset(0f, 2 * h / 3), Offset(w, 2 * h / 3), strokeWidth = 4f)
        }

        // Cells
        Column(modifier = Modifier.fillMaxSize()) {
            for (row in 0..2) {
                Row(modifier = Modifier.weight(1f)) {
                    for (col in 0..2) {
                        val index = row * 3 + col
                        TttCell(
                            value       = board[index],
                            isWinning   = winningLine?.contains(index) == true,
                            onClick     = { onCell(index) },
                            modifier    = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TttCell(
    value: String,
    isWinning: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var appeared by remember { mutableStateOf(value.isEmpty()) }

    val scale by animateFloatAsState(
        targetValue   = if (!appeared || value.isEmpty()) 1f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label         = "cellScale"
    )

    Box(
        modifier        = modifier
            .fillMaxSize()
            .clickable(enabled = value.isEmpty()) { onClick() }
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = value.isNotEmpty(),
            enter   = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn()
        ) {
            val bg = when {
                isWinning && value == "X" -> NeonPurple.copy(alpha = 0.25f)
                isWinning && value == "O" -> NeonPink.copy(alpha = 0.25f)
                else -> Color.Transparent
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(bg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text     = if (value == "X") "❌" else "⭕",
                    fontSize = 44.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ── Scoreboard ────────────────────────────────────────────────────────────────
@Composable
private fun ScoreBoard(scoreX: Int, scoreO: Int, draws: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            ScoreItemTtt("❌ X", scoreX, NeonPurple)
            ScoreItemTtt("🤝", draws, NeonYellow)
            ScoreItemTtt("⭕ O", scoreO, NeonPink)
        }
    }
}

@Composable
private fun ScoreItemTtt(label: String, value: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "$value",
            style = MaterialTheme.typography.displaySmall.copy(
                color = color, fontWeight = FontWeight.ExtraBold
            )
        )
        Text(label, style = MaterialTheme.typography.labelMedium.copy(color = TextSecondary))
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────
private fun modeName(mode: GameMode) = when (mode) {
    GameMode.TWO_PLAYER -> "2 Player"
    GameMode.AI_EASY    -> "AI – Easy"
    GameMode.AI_HARD    -> "AI – Hard"
}
