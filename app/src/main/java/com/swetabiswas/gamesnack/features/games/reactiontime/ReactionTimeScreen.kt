package com.swetabiswas.gamesnack.features.games.reactiontime

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.swetabiswas.gamesnack.core.theme.*

@Composable
fun ReactionTimeScreen(
    navController: NavController,
    viewModel: ReactionTimeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    // Background color based on game state
    val bgColor by animateColorAsState(
        targetValue = when (state.state) {
            ReactionState.IDLE      -> DeepPurple
            ReactionState.WAITING   -> Color(0xFF1A0D2E)
            ReactionState.TAP_NOW   -> Color(0xFF003D1A)
            ReactionState.TOO_EARLY -> Color(0xFF3D0000)
            ReactionState.RESULT    -> DeepPurple
        },
        animationSpec = tween(300),
        label = "bgColor"
    )

    // Pulse animation when state is TAP_NOW
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue   = 1f,
        targetValue    = 1.08f,
        animationSpec  = infiniteRepeatable(
            animation  = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .clickable(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.onScreenTap()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        // Back button (top-left)
        IconButton(
            onClick   = { navController.popBackStack() },
            modifier  = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = TextPrimary
            )
        }

        // Reset button (top-right)
        if (state.state != ReactionState.IDLE) {
            IconButton(
                onClick  = { viewModel.reset() },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = TextPrimary)
            }
        }

        // Best time indicator (top center)
        if (state.bestTimeMs > 0) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp),
                shape    = RoundedCornerShape(20.dp),
                color    = Color(0x40FFFFFF)
            ) {
                Text(
                    text     = "⚡ Best: ${state.bestTimeMs}ms",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    style    = MaterialTheme.typography.labelLarge.copy(color = NeonYellow)
                )
            }
        }

        // Main content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier            = Modifier.fillMaxWidth()
        ) {
            when (state.state) {
                ReactionState.IDLE -> IdleContent()
                ReactionState.WAITING -> WaitingContent()
                ReactionState.TAP_NOW -> TapNowContent(scale = pulseScale)
                ReactionState.TOO_EARLY -> TooEarlyContent()
                ReactionState.RESULT -> ResultContent(
                    reactionMs = state.reactionTimeMs,
                    bestMs     = state.bestTimeMs,
                    isNewBest  = state.isNewBest,
                    attempts   = state.attemptCount
                )
            }
        }
    }
}

// ── State-specific content ────────────────────────────────────────────────────

@Composable
private fun IdleContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("⚡", fontSize = 80.sp)
        Text(
            text      = "Reaction Time",
            style     = MaterialTheme.typography.displaySmall.copy(
                color      = TextPrimary,
                fontWeight = FontWeight.ExtraBold
            )
        )
        Text(
            text      = "Tap anywhere when the screen turns green",
            style     = MaterialTheme.typography.bodyLarge.copy(color = TextSecondary),
            textAlign = TextAlign.Center,
            modifier  = Modifier.padding(horizontal = 32.dp)
        )
        Spacer(Modifier.height(16.dp))
        StartButton()
    }
}

@Composable
private fun WaitingContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Blinking dot
        val blinkAlpha by rememberInfiniteTransition(label = "blink").animateFloat(
            initialValue  = 0.3f,
            targetValue   = 1f,
            animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
            label         = "blink_alpha"
        )
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(NeonRed.copy(alpha = blinkAlpha), CircleShape)
        )
        Text(
            text      = "Wait...",
            style     = MaterialTheme.typography.displaySmall.copy(
                color      = TextPrimary.copy(alpha = 0.9f),
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text      = "Don't tap yet!",
            style     = MaterialTheme.typography.bodyLarge.copy(color = TextSecondary)
        )
    }
}

@Composable
private fun TapNowContent(scale: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier            = Modifier.scale(scale)
    ) {
        Text("👆", fontSize = 72.sp)
        Text(
            text      = "TAP NOW!",
            style     = MaterialTheme.typography.displayLarge.copy(
                color          = NeonGreen,
                fontWeight     = FontWeight.ExtraBold,
                letterSpacing  = 4.sp
            )
        )
    }
}

@Composable
private fun TooEarlyContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("🚫", fontSize = 72.sp)
        Text(
            text      = "Too Early!",
            style     = MaterialTheme.typography.displaySmall.copy(
                color      = NeonRed,
                fontWeight = FontWeight.ExtraBold
            )
        )
        Text(
            text      = "You need to wait for the green screen",
            style     = MaterialTheme.typography.bodyLarge.copy(color = TextSecondary),
            textAlign = TextAlign.Center,
            modifier  = Modifier.padding(horizontal = 32.dp)
        )
        Spacer(Modifier.height(8.dp))
        TryAgainButton()
    }
}

@Composable
private fun ResultContent(
    reactionMs: Long,
    bestMs: Long,
    isNewBest: Boolean,
    attempts: Int
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(if (isNewBest) "🏆" else "⚡", fontSize = 74.sp)

        // Animated reaction time
        var showMs by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(100)
            showMs = true
        }
        AnimatedVisibility(
            visible = showMs,
            enter   = fadeIn(tween(400)) + scaleIn(tween(400, easing = OvershootInterpolatorEasing))
        ) {
            Text(
                text  = "${reactionMs}ms",
                style = MaterialTheme.typography.displayLarge.copy(
                    color      = if (isNewBest) NeonYellow else TextPrimary,
                    fontWeight = FontWeight.ExtraBold
                )
            )
        }

        Text(
            text  = reactionRating(reactionMs),
            style = MaterialTheme.typography.headlineMedium.copy(color = TextSecondary)
        )

        if (isNewBest) {
            Surface(
                shape = RoundedCornerShape(50),
                color = Color(0xFF1A3D00)
            ) {
                Text(
                    text     = "🎉 New Personal Best!",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style    = MaterialTheme.typography.labelLarge.copy(color = NeonGreen)
                )
            }
        }

        Text(
            text  = "Attempts: $attempts",
            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
        )

        Spacer(Modifier.height(8.dp))

        TryAgainButton()
    }
}

// ── Helper composables ────────────────────────────────────────────────────────

@Composable
private fun StartButton() {
    Button(
        onClick = { /* handled by screen tap */ },
        enabled = false,
        colors  = ButtonDefaults.buttonColors(
            containerColor = NeonGreen,
            contentColor   = DeepPurple,
            disabledContainerColor = NeonGreen.copy(alpha = 0.8f),
            disabledContentColor   = DeepPurple
        ),
        shape   = RoundedCornerShape(16.dp),
        modifier = Modifier.padding(horizontal = 32.dp).fillMaxWidth()
    ) {
        Text(
            text     = "Tap anywhere to start",
            style    = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(vertical = 4.dp)
        )
    }
}

@Composable
private fun TryAgainButton() {
    Button(
        onClick = { /* handled by game state machine */ },
        enabled = false,
        colors  = ButtonDefaults.buttonColors(
            disabledContainerColor = NeonPurple,
            disabledContentColor   = TextPrimary
        ),
        shape   = RoundedCornerShape(16.dp),
        modifier = Modifier.padding(horizontal = 32.dp).fillMaxWidth()
    ) {
        Text(
            text     = "Tap anywhere to try again",
            style    = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 4.dp)
        )
    }
}

// ── Utilities ─────────────────────────────────────────────────────────────────

private fun reactionRating(ms: Long): String = when {
    ms < 150  -> "⚡ Lightning Fast!"
    ms < 200  -> "🔥 Excellent!"
    ms < 250  -> "😎 Great!"
    ms < 350  -> "👍 Good"
    ms < 500  -> "😐 Average"
    else      -> "🐢 Keep practicing!"
}

// Custom easing for overshoot spring feel
private val OvershootInterpolatorEasing = Easing { fraction ->
    val tension = 3.0f
    val scaledTension = tension + 1
    ((scaledTension * fraction - tension) * fraction + 1) * fraction
}
