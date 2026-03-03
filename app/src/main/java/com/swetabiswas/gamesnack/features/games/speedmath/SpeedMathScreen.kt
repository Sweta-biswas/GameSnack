package com.swetabiswas.gamesnack.features.games.speedmath

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.swetabiswas.gamesnack.core.theme.*

@Composable
fun SpeedMathScreen(
    navController: NavController,
    viewModel: SpeedMathViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DeepPurple, Color(0xFF0D0721))))
    ) {
        when (state.gameState) {
            MathGameState.IDLE -> {
                // Back button
                IconButton(
                    onClick  = { navController.popBackStack() },
                    modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextPrimary)
                }
                MathIdleScreen(
                    bestScore = state.bestScore,
                    onStart   = { viewModel.startGame() }
                )
            }
            MathGameState.PLAYING -> {
                MathPlayingScreen(
                    state    = state,
                    onKey    = { viewModel.onKeyPress(it) }
                )
            }
            MathGameState.GAME_OVER -> {
                MathGameOverScreen(
                    score     = state.score,
                    bestScore = state.bestScore,
                    correct   = state.correctCount,
                    wrong     = state.wrongCount,
                    onReplay  = { viewModel.startGame() },
                    onHome    = { navController.popBackStack() }
                )
            }
        }
    }
}

// ── Idle ──────────────────────────────────────────────────────────────────────
@Composable
private fun MathIdleScreen(bestScore: Int, onStart: () -> Unit) {
    Column(
        modifier            = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🧮", fontSize = 80.sp)
        Spacer(Modifier.height(12.dp))
        Text(
            "Speed Math",
            style = MaterialTheme.typography.displaySmall.copy(
                color      = TextPrimary,
                fontWeight = FontWeight.ExtraBold
            )
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Solve as many as you can in 60 seconds!",
            style     = MaterialTheme.typography.bodyLarge.copy(color = TextSecondary),
            textAlign = TextAlign.Center,
            modifier  = Modifier.padding(horizontal = 32.dp)
        )
        Spacer(Modifier.height(8.dp))
        if (bestScore > 0) {
            Surface(shape = RoundedCornerShape(20.dp), color = Color(0x40FFE44D)) {
                Text(
                    "⭐ Best: $bestScore",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style    = MaterialTheme.typography.labelLarge.copy(color = NeonYellow)
                )
            }
        }
        Spacer(Modifier.height(40.dp))
        Button(
            onClick  = onStart,
            shape    = RoundedCornerShape(16.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = NeonBlue),
            modifier = Modifier.padding(horizontal = 48.dp).fillMaxWidth()
        ) {
            Text(
                "Start Game",
                style    = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold, color = DeepPurple
                ),
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}

// ── Playing ───────────────────────────────────────────────────────────────────
@Composable
private fun MathPlayingScreen(
    state: SpeedMathUiState,
    onKey: (String) -> Unit
) {
    val shakeOffset by animateFloatAsState(
        targetValue   = if (state.isWrongAnswer) 16f else 0f,
        animationSpec = if (state.isWrongAnswer)
            spring(dampingRatio = Spring.DampingRatioHighBouncy)
        else
            spring(),
        label = "shake"
    )

    Column(
        modifier            = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))

        // Timer + Score row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            // Score chip
            Surface(shape = RoundedCornerShape(12.dp), color = Color(0x40FFFFFF)) {
                Column(
                    modifier            = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Score", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
                    Text(
                        "${state.score}",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = NeonYellow, fontWeight = FontWeight.ExtraBold
                        )
                    )
                }
            }

            // Timer circle
            CircularTimer(timeLeft = state.timeLeftSeconds)

            // Stats chip
            Surface(shape = RoundedCornerShape(12.dp), color = Color(0x40FFFFFF)) {
                Column(
                    modifier            = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Correct", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
                    Text(
                        "${state.correctCount}",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = NeonGreen, fontWeight = FontWeight.ExtraBold
                        )
                    )
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        // Question card (with shake on wrong)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset(x = shakeOffset.dp),
            shape  = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (state.isWrongAnswer) NeonRed.copy(alpha = 0.3f) else CardDark
            )
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp)) {
                Text(
                    text  = "${state.question.a}  ${state.question.operator}  ${state.question.b}  =  ?",
                    style = MaterialTheme.typography.displayMedium.copy(
                        color      = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Input display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x30FFFFFF))
                .padding(horizontal = 24.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text  = if (state.input.isEmpty()) "?" else state.input,
                style = MaterialTheme.typography.displaySmall.copy(
                    color      = if (state.isWrongAnswer) NeonRed else NeonYellow,
                    fontWeight = FontWeight.ExtraBold
                )
            )
        }

        Spacer(Modifier.height(20.dp))

        // Numpad
        MathNumPad(onKey = onKey)
    }
}

@Composable
private fun CircularTimer(timeLeft: Int) {
    val progress = timeLeft / 60f
    val timerColor = when {
        progress > 0.5f -> NeonGreen
        progress > 0.25f -> NeonYellow
        else -> NeonRed
    }

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(72.dp)) {
        CircularProgressIndicator(
            progress = { progress },
            modifier  = Modifier.fillMaxSize(),
            color     = timerColor,
            strokeWidth = 5.dp,
            trackColor  = Color(0xFF2A1F5C)
        )
        Text(
            text  = "$timeLeft",
            style = MaterialTheme.typography.titleLarge.copy(
                color      = timerColor,
                fontWeight = FontWeight.ExtraBold
            )
        )
    }
}

@Composable
private fun MathNumPad(onKey: (String) -> Unit) {
    val keys = listOf(
        listOf("7", "8", "9"),
        listOf("4", "5", "6"),
        listOf("1", "2", "3"),
        listOf("-", "0", "⌫")
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        keys.forEach { row ->
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { key ->
                    NumKey(
                        label    = key,
                        modifier = Modifier.weight(1f),
                        onClick  = { onKey(key) }
                    )
                }
            }
        }
        // Submit row
        Row(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick  = { onKey("✓") },
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = NeonGreen)
            ) {
                Text(
                    "✓ Submit",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold, color = DeepPurple
                    ),
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun NumKey(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick   = onClick,
        modifier  = modifier.height(52.dp),
        shape     = RoundedCornerShape(12.dp),
        color     = CardDark,
        tonalElevation = 4.dp
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                text  = label,
                style = MaterialTheme.typography.titleLarge.copy(
                    color      = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}

// ── Game Over ─────────────────────────────────────────────────────────────────
@Composable
private fun MathGameOverScreen(
    score: Int,
    bestScore: Int,
    correct: Int,
    wrong: Int,
    onReplay: () -> Unit,
    onHome: () -> Unit
) {
    val isNewBest = score >= bestScore && score > 0

    Column(
        modifier            = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(if (isNewBest) "🏆" else "🎮", fontSize = 72.sp)
        Spacer(Modifier.height(12.dp))
        Text(
            "Time's Up!",
            style = MaterialTheme.typography.displaySmall.copy(
                color = TextPrimary, fontWeight = FontWeight.ExtraBold
            )
        )
        Spacer(Modifier.height(16.dp))

        // Score card
        Card(
            modifier = Modifier.padding(horizontal = 32.dp).fillMaxWidth(),
            shape    = RoundedCornerShape(20.dp),
            colors   = CardDefaults.cardColors(containerColor = CardDark)
        ) {
            Column(
                modifier            = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "$score",
                    style = MaterialTheme.typography.displayLarge.copy(
                        color = NeonYellow, fontWeight = FontWeight.ExtraBold
                    )
                )
                Text("correct answers", style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary))

                HorizontalDivider(color = Color(0xFF3D2E70), modifier = Modifier.padding(vertical = 4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatItem(label = "✅ Correct", value = "$correct")
                    StatItem(label = "❌ Wrong",   value = "$wrong")
                    StatItem(label = "⭐ Best",    value = "$bestScore")
                }

                if (isNewBest) {
                    Surface(shape = RoundedCornerShape(50), color = Color(0xFF1A3D00)) {
                        Text(
                            "🎉 New Best Score!",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            style    = MaterialTheme.typography.labelLarge.copy(color = NeonGreen)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick  = onReplay,
            shape    = RoundedCornerShape(16.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = NeonPurple),
            modifier = Modifier.padding(horizontal = 48.dp).fillMaxWidth()
        ) {
            Text("Play Again", style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold), modifier = Modifier.padding(vertical = 4.dp))
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick  = onHome,
            shape    = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(horizontal = 48.dp).fillMaxWidth()
        ) {
            Text("Back to Home", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge.copy(
            color = TextPrimary, fontWeight = FontWeight.Bold))
        Text(label, style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
    }
}
