package com.swetabiswas.gamesnack.core.navigation

sealed class Screen(val route: String) {
    object Splash        : Screen("splash")
    object Home          : Screen("home")
    object TicTacToe     : Screen("tictactoe")
    object SpeedMath     : Screen("speedmath")
    object ReactionTime  : Screen("reactiontime")
    object BottleSpinner : Screen("bottlespinner")
    object Profile       : Screen("profile")
    object Settings      : Screen("settings")
}
