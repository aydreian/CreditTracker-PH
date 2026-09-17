package com.example.credittrackph.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Emerald500,
    onPrimary = Surface950,
    primaryContainer = Emerald900,
    onPrimaryContainer = Emerald300,
    secondary = Gold400,
    onSecondary = Surface950,
    secondaryContainer = Color(0xFF3D2F00),
    onSecondaryContainer = Gold300,
    background = Surface950,
    onBackground = Color.White,
    surface = Surface900,
    onSurface = Color.White,
    surfaceVariant = Surface800,
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Surface700,
    error = RedAlert,
    onError = Color.White,
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0284C7), // Financial Sky Blue
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = Color(0xFFD97706),
    onSecondary = Color.White,
    background = Color(0xFFF8FAFC), // Clean crisp white / light slate
    onBackground = Color(0xFF0F172A), // Dark slate typography
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF64748B),
    outline = Color(0xFFE2E8F0),
    error = RedAlert,
    onError = Color.White,
)

val LocalIsDarkTheme = androidx.compose.runtime.compositionLocalOf { true }

@Composable
fun CreditTrackPHTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    androidx.compose.runtime.CompositionLocalProvider(LocalIsDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
