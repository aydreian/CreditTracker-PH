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
    primary = Emerald700,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1FAE5),
    onPrimaryContainer = Emerald900,
    secondary = Color(0xFFD97706),
    onSecondary = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = Surface900,
    surface = Color.White,
    onSurface = Surface900,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Surface600,
    outline = Color(0xFFCBD5E1),
    error = RedAlert,
    onError = Color.White,
)

@Composable
fun CreditTrackPHTheme(
    darkTheme: Boolean = true, // Default dark
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
