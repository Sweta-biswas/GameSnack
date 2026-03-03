package com.swetabiswas.gamesnack.features.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.swetabiswas.gamesnack.R
import com.swetabiswas.gamesnack.core.navigation.Screen
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {

    // Animate the splash image fading + scaling in
    val imageAlpha by animateFloatAsState(
        targetValue   = 1f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label         = "splashAlpha"
    )
    val imageScale by animateFloatAsState(
        targetValue   = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessLow
        ),
        label = "splashScale"
    )

    // Infinite rotating loader color animation
    val loaderAlpha by rememberInfiniteTransition(label = "loader").animateFloat(
        initialValue   = 0.7f,
        targetValue    = 1f,
        animationSpec  = infiniteRepeatable(
            animation  = tween(700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "loaderPulse"
    )

    // Navigate to Home after 3 seconds
    LaunchedEffect(Unit) {
        delay(3000L)
        navController.navigate(Screen.Home.route) {
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // ── Full-screen splash image ──────────────────────────────────────
        Image(
            painter            = painterResource(id = R.drawable.splashscreen),
            contentDescription = "GameSnack Splash",
            contentScale       = ContentScale.Crop,
            modifier           = Modifier
                .fillMaxSize()
                .alpha(imageAlpha)
                .scale(imageScale)
        )

        // ── Loader at the bottom centre ───────────────────────────────────
        Column(
            modifier            = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 72.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                modifier        = Modifier
                    .size(52.dp)
                    .alpha(loaderAlpha),
                color           = Color(0xFFFFE44D),          // Neon yellow to match branding
                trackColor      = Color.White.copy(alpha = 0.25f),
                strokeWidth     = 5.dp,
                strokeCap       = StrokeCap.Round
            )
        }
    }
}
