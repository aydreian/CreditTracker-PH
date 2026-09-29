package com.example.credittrackph.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary              = Emerald500,
    onPrimary            = Color(0xFF022C22),
    primaryContainer     = Color(0xFF064E3B),
    onPrimaryContainer   = Emerald300,
    secondary            = Gold400,
    onSecondary          = Color(0xFF1C1400),
    secondaryContainer   = Color(0xFF3D2C00),
    onSecondaryContainer = Gold300,
    background           = Slate950,
    onBackground         = Color.White,
    surface              = Slate900,
    onSurface            = Color.White,
    surfaceVariant       = Slate800,
    onSurfaceVariant     = Color(0xFFCBD5E1),
    outline              = Slate700,
    error                = RedAlert,
    onError              = Color.White,
)

private val LightColorScheme = lightColorScheme(
    primary              = Color(0xFF0284C7),
    onPrimary            = Color.White,
    primaryContainer     = Color(0xFFE0F2FE),
    onPrimaryContainer   = Color(0xFF0369A1),
    secondary            = Color(0xFFD97706),
    onSecondary          = Color.White,
    background           = Color(0xFFF8FAFC),
    onBackground         = Color(0xFF0F172A),
    surface              = Color.White,
    onSurface            = Color(0xFF0F172A),
    surfaceVariant       = Color(0xFFF1F5F9),
    onSurfaceVariant     = Color(0xFF64748B),
    outline              = Color(0xFFE2E8F0),
    error                = RedAlert,
    onError              = Color.White,
)

val LocalIsDarkTheme = androidx.compose.runtime.compositionLocalOf { true }

@Composable
fun CreditTrackPHTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    androidx.compose.runtime.CompositionLocalProvider(
        LocalIsDarkTheme provides darkTheme
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
