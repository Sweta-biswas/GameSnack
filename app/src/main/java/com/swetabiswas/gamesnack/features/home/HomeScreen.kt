package com.swetabiswas.gamesnack.features.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.swetabiswas.gamesnack.R
import com.swetabiswas.gamesnack.core.navigation.Screen
import com.swetabiswas.gamesnack.core.sound.SoundManager
import com.swetabiswas.gamesnack.core.sound.SoundType
import com.swetabiswas.gamesnack.core.theme.*
import com.swetabiswas.gamesnack.core.theme.LocalDarkTheme
import com.swetabiswas.gamesnack.data.local.entity.BadgeEntity

// ── Data model for game cards ─────────────────────────────────────────────────
data class GameCardInfo(
    val id: String,
    val imageRes: Int,
    val route: String,
    val xpRequired: Int = 0
)

val GAMES = listOf(
    GameCardInfo("ttt",      R.drawable.tictactoe,    Screen.TicTacToe.route),
    GameCardInfo("math",     R.drawable.speedmath,    Screen.SpeedMath.route),
    GameCardInfo("reaction", R.drawable.reactiontime, Screen.ReactionTime.route),
    GameCardInfo("bottle",   R.drawable.bottlespinner,Screen.BottleSpinner.route)
)

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel(),
    settingsViewModel: com.swetabiswas.gamesnack.features.settings.SettingsViewModel = hiltViewModel()
) {
    val state   by viewModel.uiState.collectAsStateWithLifecycle()
    val prefs   by settingsViewModel.prefs.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val sound   = remember { SoundManager(context.applicationContext) }
    val isDark  = LocalDarkTheme.current

    val bgBrush = if (isDark)
        Brush.verticalGradient(listOf(Color(0xFF0A0618), Color(0xFF12082A), Color(0xFF0D0520)))
    else
        Brush.verticalGradient(listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.surfaceVariant))

    Box(
        modifier = Modifier.fillMaxSize().background(bgBrush)
    ) {
        // Decorative glowing orbs in background
        if (isDark) {
            GlowOrb(color = NeonPurple.copy(alpha = 0.12f), size = 280.dp, modifier = Modifier.offset((-60).dp, (-40).dp))
            GlowOrb(color = NeonPink.copy(alpha = 0.08f),   size = 220.dp, modifier = Modifier.align(Alignment.TopEnd).offset(40.dp, 80.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ── Premium Top Bar ───────────────────────────────────────────
            PremiumTopBar(
                playerName = state.profile.name.ifBlank { "Champion" },
                level      = state.profile.level,
                totalXp    = state.profile.totalXp,
                streak     = state.profile.dailyStreak,
                onSettings = {
                    sound.play(SoundType.CLICK, prefs.isSoundEnabled)
                    navController.navigate(Screen.Settings.route)
                },
                isDark = isDark
            )

            Spacer(Modifier.height(4.dp))

            // ── XP Progress Bar (slim, professional) ─────────────────────
            XpProgressSection(
                level         = state.profile.level,
                levelProgress = state.levelProgress,
                xpWithin      = state.xpWithinLevel,
                xpToNext      = state.xpToNext
            )

            Spacer(Modifier.height(28.dp))

            // ── Games Section ─────────────────────────────────────────────
            SectionHeader(emoji = "🎮", title = "Games")
            Spacer(Modifier.height(16.dp))

            GameGrid(
                games  = GAMES,
                userXp = state.profile.totalXp,
                onPlay = { route ->
                    sound.play(SoundType.TAP, prefs.isSoundEnabled)
                    navController.navigate(route)
                }
            )

            // ── Badges Section ────────────────────────────────────────────
            if (state.badges.isNotEmpty()) {
                Spacer(Modifier.height(32.dp))
                SectionHeader(emoji = "🏆", title = "Achievements")
                Spacer(Modifier.height(16.dp))
                BadgesGrid(badges = state.badges)
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

// ── Decorative Glow Orb ───────────────────────────────────────────────────────
@Composable
private fun GlowOrb(color: Color, size: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size)
            .blur(80.dp)
            .background(color, CircleShape)
    )
}

// ── Premium Top Bar ───────────────────────────────────────────────────────────
@Composable
private fun PremiumTopBar(
    playerName: String,
    level: Int,
    totalXp: Int,
    streak: Int,
    onSettings: () -> Unit,
    isDark: Boolean
) {
    val avatarBg = Brush.linearGradient(listOf(NeonPurple, NeonPink))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: Avatar + greeting
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar circle with level ring
            Box(contentAlignment = Alignment.Center) {
                // Outer ring (animated)
                val ringAngle by rememberInfiniteTransition(label = "ring").animateFloat(
                    initialValue  = 0f,
                    targetValue   = 360f,
                    animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing)),
                    label         = "ringAngle"
                )
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .drawBehind {
                            drawCircle(
                                brush  = Brush.sweepGradient(listOf(NeonPurple, NeonPink, Color.Transparent)),
                                style  = Stroke(width = 2.5.dp.toPx()),
                                radius = size.minDimension / 2f
                            )
                        }
                )
                // Avatar fill
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(avatarBg, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text  = playerName.firstOrNull()?.uppercaseChar()?.toString() ?: "G",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color      = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                }
                // Level badge on bottom-right
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(18.dp)
                        .background(NeonYellow, CircleShape)
                        .border(1.5.dp, Color(0xFF0A0618), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text  = "$level",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color      = Color.Black,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize   = 8.sp
                        )
                    )
                }
            }

            // Greeting + name
            Column {
                Text(
                    text  = "Welcome back,",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = if (isDark) TextSecondary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                )
                Text(
                    text  = playerName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color      = if (isDark) TextPrimary else MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
            }
        }

        // Right: streak + settings
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Streak pill
            if (streak > 0) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0x33FF6B35)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Whatshot,
                            contentDescription = null,
                            tint     = NeonOrange,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text  = "$streak",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color      = NeonOrange,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                    }
                }
            }

            // Settings button
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(
                        if (isDark) Color(0x22FFFFFF) else Color(0x11000000),
                        CircleShape
                    )
                    .clip(CircleShape)
                    .clickable { onSettings() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint     = if (isDark) TextSecondary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ── XP Progress Section ───────────────────────────────────────────────────────
@Composable
private fun XpProgressSection(
    level: Int,
    levelProgress: Float,
    xpWithin: Int,
    xpToNext: Int
) {
    val isDark = LocalDarkTheme.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        // Level label row
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            // Level pill
            Surface(
                shape = RoundedCornerShape(50),
                color = NeonPurple.copy(alpha = 0.2f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(text = "⭐", fontSize = 11.sp)
                    Text(
                        text  = "LEVEL $level",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color      = NeonPurple,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    )
                }
            }

            // XP counter
            Text(
                text  = "$xpWithin / $xpToNext XP",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (isDark) TextSecondary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
                    fontWeight = FontWeight.SemiBold
                )
            )
        }

        Spacer(Modifier.height(8.dp))

        // Slim glowing XP bar
        GlowingXpBar(progress = levelProgress)
    }
}

@Composable
private fun GlowingXpBar(progress: Float) {
    val animatedProgress by animateFloatAsState(
        targetValue   = progress.coerceIn(0f, 1f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessLow
        ),
        label = "xpBar"
    )

    val glowAlpha by rememberInfiniteTransition(label = "glow").animateFloat(
        initialValue  = 0.5f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Reverse),
        label         = "glowPulse"
    )

    // Track: subtle groove
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(50))
            .background(Color(0x33FFFFFF))
    ) {
        // Fill
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .fillMaxHeight()
                .clip(RoundedCornerShape(50))
                .background(Brush.horizontalGradient(listOf(NeonPurple, NeonPink, NeonYellow.copy(alpha = 0.8f))))
        )

        // Glowing leading dot at the progress edge
        if (animatedProgress > 0.02f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .wrapContentWidth(Alignment.End)
            ) {
                Box(
                    modifier = Modifier
                        .offset(x = 4.dp)
                        .size(14.dp)
                        .background(
                            NeonPink.copy(alpha = glowAlpha),
                            CircleShape
                        )
                        .align(Alignment.CenterEnd)
                )
            }
        }
    }
}

// ── Section Header ────────────────────────────────────────────────────────────
@Composable
private fun SectionHeader(emoji: String, title: String) {
    val isDark = LocalDarkTheme.current
    Row(
        modifier          = Modifier.padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = emoji, fontSize = 20.sp)
        Text(
            text  = title.uppercase(),
            style = MaterialTheme.typography.titleSmall.copy(
                color         = if (isDark) TextPrimary else MaterialTheme.colorScheme.onBackground,
                fontWeight    = FontWeight.ExtraBold,
                letterSpacing = 2.sp
            )
        )
        // Decorative line
        HorizontalDivider(
            modifier  = Modifier.weight(1f),
            thickness = 1.dp,
            color     = if (isDark) Color(0x22FFFFFF) else Color(0x22000000)
        )
    }
}

// ── Game Grid ─────────────────────────────────────────────────────────────────
@Composable
private fun GameGrid(
    games: List<GameCardInfo>,
    userXp: Int,
    onPlay: (String) -> Unit
) {
    Column(
        modifier            = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        games.chunked(2).forEachIndexed { rowIdx, rowGames ->
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                rowGames.forEachIndexed { colIdx, game ->
                    val delayMs = (rowIdx * 2 + colIdx) * 90
                    var visible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(delayMs.toLong())
                        visible = true
                    }
                    AnimatedVisibility(
                        visible  = visible,
                        enter    = fadeIn(tween(450)) + scaleIn(tween(450), initialScale = 0.88f),
                        modifier = Modifier.weight(1f)
                    ) {
                        GameCard(
                            game     = game,
                            isLocked = userXp < game.xpRequired,
                            onClick  = { onPlay(game.route) }
                        )
                    }
                }
                if (rowGames.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GameCard(
    game: GameCardInfo,
    isLocked: Boolean,
    onClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue   = if (pressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label         = "cardScale"
    )

    Card(
        onClick   = {
            if (!isLocked) {
                pressed = true
                onClick()
            }
        },
        modifier  = Modifier
            .aspectRatio(0.82f)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(elevation = 20.dp, shape = RoundedCornerShape(22.dp)),
        shape     = RoundedCornerShape(22.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (isLocked) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.linearGradient(listOf(Color(0xFF1C1532), Color(0xFF110C28)))),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🔒", fontSize = 40.sp)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text  = "${game.xpRequired} XP",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextDisabled)
                        )
                    }
                }
            } else {
                Image(
                    painter            = painterResource(id = game.imageRes),
                    contentDescription = null,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier.fillMaxSize()
                )

                // Subtle inner glow border
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(
                            width  = 1.5.dp,
                            brush  = Brush.linearGradient(listOf(Color.White.copy(alpha = 0.18f), Color.Transparent)),
                            shape  = RoundedCornerShape(22.dp)
                        )
                )
            }
        }
    }
}

// ── Badges Grid ───────────────────────────────────────────────────────────────
@Composable
private fun BadgesGrid(badges: List<BadgeEntity>) {
    val isDark = LocalDarkTheme.current

    Column(
        modifier            = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        badges.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { badge ->
                    BadgeCard(badge = badge, isDark = isDark, modifier = Modifier.weight(1f))
                }
                // Pad remaining slots
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun BadgeCard(
    badge: BadgeEntity,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    // Only animate the glow for earned badges — InfiniteTransition requires InfiniteRepeatableSpec
    val earnedGlowAlpha by rememberInfiniteTransition(label = "badgeGlow").animateFloat(
        initialValue  = 0.4f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(tween(1800), RepeatMode.Reverse),
        label         = "glow"
    )
    val glowAlpha = if (badge.earned) earnedGlowAlpha else 0.4f

    val cardBg = when {
        badge.earned && isDark -> Brush.linearGradient(listOf(Color(0xFF2A1B5E), Color(0xFF1E1040)))
        badge.earned           -> Brush.linearGradient(listOf(Color(0xFFEDE8FB), Color(0xFFF5F0FF)))
        isDark                 -> Brush.linearGradient(listOf(Color(0xFF14102A), Color(0xFF100D20)))
        else                   -> Brush.linearGradient(listOf(Color(0xFFEEEEEE), Color(0xFFF5F5F5)))
    }

    Box(
        modifier = modifier
            .aspectRatio(0.9f)
            .then(
                if (badge.earned) Modifier.shadow(
                    elevation    = 12.dp,
                    shape        = RoundedCornerShape(18.dp),
                    ambientColor = NeonPurple.copy(alpha = glowAlpha * 0.6f),
                    spotColor    = NeonPink.copy(alpha = glowAlpha * 0.4f)
                ) else Modifier
            )
            .clip(RoundedCornerShape(18.dp))
            .background(cardBg)
            .then(
                if (badge.earned) Modifier.border(
                    width  = 1.dp,
                    brush  = Brush.linearGradient(listOf(NeonPurple.copy(alpha = 0.5f), NeonPink.copy(alpha = 0.3f))),
                    shape  = RoundedCornerShape(18.dp)
                ) else Modifier.border(
                    width  = 1.dp,
                    color  = Color.White.copy(alpha = if (isDark) 0.05f else 0.3f),
                    shape  = RoundedCornerShape(18.dp)
                )
            )
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Emoji in circle
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (badge.earned) NeonPurple.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.04f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text     = if (badge.earned) badge.iconEmoji else "🔒",
                    fontSize = if (badge.earned) 20.sp else 16.sp
                )
            }

            Text(
                text      = badge.name,
                style     = MaterialTheme.typography.labelSmall.copy(
                    color      = if (badge.earned) {
                        if (isDark) TextPrimary else MaterialTheme.colorScheme.onBackground
                    } else TextDisabled,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center,
                maxLines  = 2,
                overflow  = TextOverflow.Ellipsis
            )

            if (badge.earned) {
                Text(
                    text  = "✓ Earned",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color    = NeonGreen,
                        fontSize = 8.sp
                    )
                )
            }
        }
    }
}
