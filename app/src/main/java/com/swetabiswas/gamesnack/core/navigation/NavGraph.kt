package com.swetabiswas.gamesnack.core.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.swetabiswas.gamesnack.features.games.bottlespinner.BottleSpinnerScreen
import com.swetabiswas.gamesnack.features.games.reactiontime.ReactionTimeScreen
import com.swetabiswas.gamesnack.features.games.speedmath.SpeedMathScreen
import com.swetabiswas.gamesnack.features.games.tictactoe.TicTacToeScreen
import com.swetabiswas.gamesnack.features.home.HomeScreen
import com.swetabiswas.gamesnack.features.settings.SettingsScreen
import com.swetabiswas.gamesnack.features.splash.SplashScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController    = navController,
        startDestination = Screen.Splash.route,   // ← Splash is now the entry point
        enterTransition = {
            fadeIn(animationSpec = tween(300)) +
            slideInVertically(
                animationSpec  = tween(300),
                initialOffsetY = { it / 10 }
            )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(200))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(200)) +
            slideOutVertically(
                animationSpec = tween(300),
                targetOffsetY = { it / 10 }
            )
        }
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.TicTacToe.route) {
            TicTacToeScreen(navController = navController)
        }
        composable(Screen.SpeedMath.route) {
            SpeedMathScreen(navController = navController)
        }
        composable(Screen.ReactionTime.route) {
            ReactionTimeScreen(navController = navController)
        }
        composable(Screen.BottleSpinner.route) {
            BottleSpinnerScreen(navController = navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
    }
}
