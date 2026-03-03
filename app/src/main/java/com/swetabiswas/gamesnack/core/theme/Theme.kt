package com.swetabiswas.gamesnack.core.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/** Exposed so any composable can know if dark mode is active. */
val LocalDarkTheme = compositionLocalOf { true }

private val DarkColorScheme = darkColorScheme(
    primary              = NeonPurple,
    onPrimary            = TextPrimary,
    primaryContainer     = CardDark,
    onPrimaryContainer   = TextPrimary,
    secondary            = NeonPink,
    onSecondary          = TextPrimary,
    secondaryContainer   = SurfaceDark,
    onSecondaryContainer = TextSecondary,
    tertiary             = NeonGreen,
    onTertiary           = DeepPurple,
    background           = DeepPurple,
    onBackground         = TextPrimary,
    surface              = CardDark,
    onSurface            = TextPrimary,
    surfaceVariant       = CardDarkAlt,
    onSurfaceVariant     = TextSecondary,
    outline              = TextDisabled,
    error                = NeonRed,
    onError              = TextPrimary
)

private val LightColorScheme = lightColorScheme(
    primary              = NeonPurple,
    onPrimary            = TextPrimary,
    primaryContainer     = LightSurface,
    onPrimaryContainer   = LightTextPrimary,
    secondary            = NeonPink,
    onSecondary          = TextPrimary,
    secondaryContainer   = LightSurface,
    onSecondaryContainer = LightTextSecondary,
    tertiary             = NeonBlue,
    onTertiary           = TextPrimary,
    background           = LightBackground,
    onBackground         = LightTextPrimary,
    surface              = LightCard,
    onSurface            = LightTextPrimary,
    surfaceVariant       = LightSurface,
    onSurfaceVariant     = LightTextSecondary,
    outline              = LightTextSecondary,
    error                = NeonRed,
    onError              = TextPrimary
)

@Composable
fun GameSnackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = GameSnackTypography,
            content     = content
        )
    }
}
