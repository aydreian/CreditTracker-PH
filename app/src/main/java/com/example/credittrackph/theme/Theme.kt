package com.example.credittrackph.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF4F46E5), // Indigo Iris (Figma Concept 03)
    onPrimary = Color.White,
    primaryContainer = Color(0xFF312D6B),
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = Color(0xFFFF6B6B), // Neon Coral (Figma Concept 03)
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF4C1D1D),
    onSecondaryContainer = Color(0xFFFFD1D1),
    background = Color(0xFF1E1B4B), // Midnight (Figma Concept 03/04)
    onBackground = Color.White,
    surface = Color(0xFF26225B), // Deep Midnight Surface
    onSurface = Color.White,
    surfaceVariant = Color(0xFF332E74),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF433E8E),
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
