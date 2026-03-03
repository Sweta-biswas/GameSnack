package com.swetabiswas.gamesnack.features.games.bottlespinner

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
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
fun BottleSpinnerScreen(
    navController: NavController,
    viewModel: BottleSpinnerViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Animate the rotation
    val animatedRotation by animateFloatAsState(
        targetValue   = state.targetDegrees,
        animationSpec = tween(
            durationMillis = 3000,
            easing         = FastOutSlowInEasing
        ),
        label = "bottleRotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DeepPurple, Color(0xFF0D0721))))
    ) {
        Column(
            modifier            = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextPrimary)
                }
                Text(
                    "Bottle Spinner",
                    style    = MaterialTheme.typography.titleLarge.copy(
                        color = TextPrimary, fontWeight = FontWeight.ExtraBold
                    ),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                IconButton(onClick = { viewModel.toggleAddPlayer() }) {
                    Icon(Icons.Default.Add, "Add Player", tint = NeonPink)
                }
            }

            Spacer(Modifier.height(8.dp))

            // Mode selector
            ModeSelector(
                current   = state.mode,
                onSelect  = { viewModel.setMode(it) }
            )

            Spacer(Modifier.height(16.dp))

            // Players list
            PlayersRow(
                players  = state.players,
                selected = state.selectedPlayer,
                onRemove = { viewModel.removePlayer(it) }
            )

            Spacer(Modifier.height(20.dp))

            // Bottle + spin button
            Box(
                modifier        = Modifier
                    .size(220.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFF2D1A5A), Color(0xFF1A1035))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Spinning bottle emoji
                Text(
                    "🍾",
                    fontSize = 80.sp,
                    modifier = Modifier.rotate(animatedRotation)
                )
            }

            Spacer(Modifier.height(24.dp))

            // Spin button
            Button(
                onClick  = { viewModel.spin() },
                enabled  = !state.isSpinning && state.players.size >= 2,
                shape    = RoundedCornerShape(50),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = Brush.horizontalGradient(
                        listOf(BottleGradientStart, BottleGradientEnd)
                    ).let { NeonPink },
                    disabledContainerColor = CardDark
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 32.dp)
            ) {
                Text(
                    if (state.isSpinning) "Spinning..." else "🎡 Spin!",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color      = TextPrimary
                    )
                )
            }

            // Result card
            state.selectedPlayer?.let { winner ->
                Spacer(Modifier.height(24.dp))
                ResultCard(
                    player = winner,
                    mode   = state.mode,
                    prompt = state.prompt
                )
            }

            // Add player dialog
            if (state.showAddPlayer) {
                Spacer(Modifier.height(16.dp))
                AddPlayerCard(
                    name     = state.newPlayerName,
                    onChange = { viewModel.setNewPlayerName(it) },
                    onAdd    = { viewModel.addPlayer() },
                    onDismiss = { viewModel.toggleAddPlayer() }
                )
            }

            Spacer(Modifier.height(16.dp))

            // Spin count
            if (state.spinCount > 0) {
                Text(
                    "Total spins: ${state.spinCount}",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }
        }
    }
}

// ── Mode Selector ─────────────────────────────────────────────────────────────
@Composable
private fun ModeSelector(current: SpinnerMode, onSelect: (SpinnerMode) -> Unit) {
    val modes = SpinnerMode.entries.toList()
    val labels = mapOf(
        SpinnerMode.TRUTH     to "🤔 Truth",
        SpinnerMode.DARE      to "😈 Dare",
        SpinnerMode.CHALLENGE to "🏆 Challenge"
    )

    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        modes.forEach { mode ->
            val selected = mode == current
            Surface(
                onClick   = { onSelect(mode) },
                modifier  = Modifier.weight(1f),
                shape     = RoundedCornerShape(12.dp),
                color     = if (selected) NeonPink else CardDark
            ) {
                Text(
                    labels[mode] ?: "",
                    modifier  = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                    textAlign = TextAlign.Center,
                    style     = MaterialTheme.typography.labelLarge.copy(
                        color      = if (selected) TextPrimary else TextSecondary,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                )
            }
        }
    }
}

// ── Players Row ───────────────────────────────────────────────────────────────
@Composable
private fun PlayersRow(
    players: List<String>,
    selected: String?,
    onRemove: (String) -> Unit
) {
    Row(
        modifier              = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        players.forEach { player ->
            val isSelected = player == selected
            Surface(
                shape = RoundedCornerShape(50),
                color = if (isSelected) NeonPurple else CardDark
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        player,
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = if (isSelected) TextPrimary else TextSecondary
                        )
                    )
                    if (players.size > 2) {
                        IconButton(
                            onClick  = { onRemove(player) },
                            modifier = Modifier.size(16.dp)
                        ) {
                            Icon(
                                Icons.Default.Close, "Remove",
                                tint     = if (isSelected) TextPrimary else TextDisabled,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Result Card ───────────────────────────────────────────────────────────────
@Composable
private fun ResultCard(player: String, mode: SpinnerMode, prompt: String?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(20.dp),
        colors   = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Column(
            modifier            = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "🎯 $player",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = NeonPink, fontWeight = FontWeight.ExtraBold
                )
            )
            val modeLabel = when (mode) {
                SpinnerMode.TRUTH     -> "has to answer a Truth!"
                SpinnerMode.DARE      -> "has a Dare!"
                SpinnerMode.CHALLENGE -> "faces a Challenge!"
            }
            Text(
                modeLabel,
                style = MaterialTheme.typography.titleMedium.copy(color = TextSecondary)
            )
            prompt?.let {
                HorizontalDivider(color = Color(0xFF3D2E70))
                Text(
                    it,
                    style     = MaterialTheme.typography.bodyLarge.copy(
                        color = TextPrimary, fontWeight = FontWeight.SemiBold
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ── Add Player ────────────────────────────────────────────────────────────────
@Composable
private fun AddPlayerCard(
    name: String,
    onChange: (String) -> Unit,
    onAdd: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Add Player",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = TextPrimary, fontWeight = FontWeight.Bold
                )
            )
            OutlinedTextField(
                value         = name,
                onValueChange = onChange,
                placeholder   = { Text("Player name", color = TextDisabled) },
                modifier      = Modifier.fillMaxWidth(),
                shape         = RoundedCornerShape(12.dp),
                singleLine    = true,
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedTextColor   = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = NeonPink,
                    unfocusedBorderColor = TextDisabled,
                    cursorColor        = NeonPink
                )
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick  = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape    = RoundedCornerShape(12.dp)
                ) { Text("Cancel") }
                Button(
                    onClick  = onAdd,
                    modifier = Modifier.weight(1f),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = NeonPink)
                ) { Text("Add", fontWeight = FontWeight.Bold) }
            }
        }
    }
}
