package com.example.credittrackph.theme

import androidx.compose.ui.graphics.Color

// Primary brand colors - deep emerald/teal premium dark palette
val Emerald900 = Color(0xFF064E3B)
val Emerald700 = Color(0xFF047857)
val Emerald500 = Color(0xFF10B981)
val Emerald400 = Color(0xFF34D399)
val Emerald300 = Color(0xFF6EE7B7)

// Accent - gold for premium feel
val Gold400 = Color(0xFFFBBF24)
val Gold300 = Color(0xFFFCD34D)

// Surface colors - deep dark theme
val Surface950 = Color(0xFF030712)
val Surface900 = Color(0xFF0F172A)
val Surface850 = Color(0xFF111827)
val Surface800 = Color(0xFF1E293B)
val Surface700 = Color(0xFF334155)
val Surface600 = Color(0xFF475569)

// Status colors
val RedAlert = Color(0xFFEF4444)
val RedSoft = Color(0xFFFEE2E2)
val YellowWarn = Color(0xFFF59E0B)
val GreenSuccess = Color(0xFF22C55E)
val GreenSoft = Color(0xFFDCFCE7)

// Bottom navigation
val BottomNavSelected = Color(0xFF10B981)
val BottomNavUnselected = Color(0xFF64748B)

// Card preset colors
val CardColorPresets = listOf(
    Color(0xFF1E3A5F), // Navy Blue
    Color(0xFF1A1A2E), // Midnight
    Color(0xFF064E3B), // Emerald Dark
    Color(0xFF1E1B4B), // Indigo Dark
    Color(0xFF4A1942), // Purple Dark
    Color(0xFF7F1D1D), // Dark Red
    Color(0xFF1C1917), // Near Black
    Color(0xFF0C4A6E), // Sky Dark
    Color(0xFF713F12), // Bronze
    Color(0xFF134E4A), // Teal Dark
)

@androidx.compose.runtime.Composable
fun appBackgroundColor(): Color = if (LocalIsDarkTheme.current) Surface950 else Color(0xFFF8FAFC)

@androidx.compose.runtime.Composable
fun appSurfaceColor(): Color = if (LocalIsDarkTheme.current) Surface900 else Color(0xFFFFFFFF)

@androidx.compose.runtime.Composable
fun appCardColor(): Color = if (LocalIsDarkTheme.current) Surface800 else Color(0xFFFFFFFF)

@androidx.compose.runtime.Composable
fun appTextColor(): Color = if (LocalIsDarkTheme.current) Color.White else Color(0xFF0F172A)

@androidx.compose.runtime.Composable
fun appTextSubColor(): Color = if (LocalIsDarkTheme.current) Color.White.copy(alpha = 0.6f) else Color(0xFF64748B)

@androidx.compose.runtime.Composable
fun appPrimaryColor(): Color = if (LocalIsDarkTheme.current) Emerald400 else Color(0xFF0284C7)

@androidx.compose.runtime.Composable
fun appAccentColor(): Color = if (LocalIsDarkTheme.current) Emerald500 else Color(0xFF0EA5E9)

@androidx.compose.runtime.Composable
fun appAccentDarkColor(): Color = if (LocalIsDarkTheme.current) Emerald700 else Color(0xFF0369A1)

@androidx.compose.runtime.Composable
fun appSuccessColor(): Color = if (LocalIsDarkTheme.current) GreenSuccess else Color(0xFF0284C7)

@androidx.compose.runtime.Composable
fun appSoftSuccessColor(): Color = if (LocalIsDarkTheme.current) GreenSuccess.copy(alpha = 0.15f) else Color(0xFF0284C7).copy(alpha = 0.12f)

@androidx.compose.runtime.Composable
fun appFabContainerColor(): Color = if (LocalIsDarkTheme.current) Emerald500 else Color(0xFF0284C7)

@androidx.compose.runtime.Composable
fun appBorderColor(): Color = if (LocalIsDarkTheme.current) Surface700.copy(alpha = 0.4f) else Color(0xFFE2E8F0)

@androidx.compose.runtime.Composable
fun appIndicatorGlowColor(): Color = if (LocalIsDarkTheme.current) Emerald500.copy(alpha = 0.22f) else Color(0xFF0284C7).copy(alpha = 0.16f)

@androidx.compose.runtime.Composable
fun appOnAccentColor(): Color = if (LocalIsDarkTheme.current) Surface950 else Color.White

