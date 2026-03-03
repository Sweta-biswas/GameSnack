package com.swetabiswas.gamesnack.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.swetabiswas.gamesnack.core.theme.*
import com.swetabiswas.gamesnack.core.theme.LocalDarkTheme

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val prefs by viewModel.prefs.collectAsStateWithLifecycle()
    val isDark = LocalDarkTheme.current
    val bgGradient = if (isDark)
        listOf(DeepPurple, Color(0xFF0D0721))
    else
        listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.surfaceVariant)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(bgGradient))
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // Top bar
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextPrimary)
                }
                Text(
                    "Settings",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = TextPrimary, fontWeight = FontWeight.ExtraBold
                    )
                )
            }

            Spacer(Modifier.height(24.dp))

            // Settings items
            SettingToggleItem(
                icon       = if (prefs.isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                title      = "Dark Mode",
                subtitle   = if (prefs.isDarkMode) "Currently dark" else "Currently light",
                checked    = prefs.isDarkMode,
                onToggle   = { viewModel.toggleDarkMode() }
            )

            Spacer(Modifier.height(12.dp))

            SettingToggleItem(
                icon       = if (prefs.isSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                title      = "Sound Effects",
                subtitle   = if (prefs.isSoundEnabled) "Sound is on" else "Sound is off",
                checked    = prefs.isSoundEnabled,
                onToggle   = { viewModel.toggleSound() }
            )

            Spacer(Modifier.height(32.dp))

            // Info section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(16.dp),
                colors   = CardDefaults.cardColors(containerColor = CardDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "GameSnack v1.0",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = TextPrimary, fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "An offline mini-games hub.\nAll data stored locally on your device.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(icon, contentDescription = null, tint = NeonPurple)
                Column {
                    Text(title, style = MaterialTheme.typography.titleSmall.copy(
                        color = TextPrimary, fontWeight = FontWeight.SemiBold
                    ))
                    Text(subtitle, style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary
                    ))
                }
            }
            Switch(
                checked         = checked,
                onCheckedChange = { onToggle() },
                colors          = SwitchDefaults.colors(
                    checkedThumbColor    = TextPrimary,
                    checkedTrackColor    = NeonPurple,
                    uncheckedThumbColor  = TextDisabled,
                    uncheckedTrackColor  = CardDarkAlt
                )
            )
        }
    }
}
