package com.example.credittrackph.theme

import androidx.compose.ui.graphics.Color

// ── Brand Palette — Emerald ──
val Emerald900 = Color(0xFF064E3B)
val Emerald700 = Color(0xFF047857)
val Emerald600 = Color(0xFF059669)
val Emerald500 = Color(0xFF10B981)
val Emerald400 = Color(0xFF34D399)
val Emerald300 = Color(0xFF6EE7B7)

// ── Accent — Gold ──
val Gold400 = Color(0xFFFBBF24)
val Gold300 = Color(0xFFFCD34D)

// ── Surface Scale — Deep Slate (replaces Midnight Indigo) ──
val Slate950 = Color(0xFF020617)   // Deepest bg — near-black
val Slate900 = Color(0xFF0F172A)   // Card / surface base
val Slate800 = Color(0xFF1E293B)   // Elevated surfaces
val Slate700 = Color(0xFF334155)   // Borders, dividers
val Slate600 = Color(0xFF475569)   // Muted elements
val Slate500 = Color(0xFF64748B)   // Secondary text

// Backward-compat aliases (keeps old references compiling during migration)
val Surface950 = Slate950
val Surface900 = Slate900
val Surface850 = Slate900
val Surface800 = Slate800
val Surface700 = Slate700
val Surface600 = Slate600

// ── Status Colors ──
val RedAlert     = Color(0xFFEF4444)
val RedSoft      = Color(0xFFFEE2E2)
val YellowWarn   = Color(0xFFF59E0B)
val GreenSuccess = Color(0xFF22C55E)
val GreenSoft    = Color(0xFFDCFCE7)

// ── Navigation ──
val BottomNavSelected   = Emerald500
val BottomNavUnselected = Slate500

// ── Card Preset Colors (dark card visuals) ──
val CardColorPresets = listOf(
    Color(0xFF1E3A5F), // Navy Blue
    Color(0xFF0F172A), // Slate Midnight
    Color(0xFF064E3B), // Emerald Dark
    Color(0xFF1E1B4B), // Indigo Dark
    Color(0xFF4A1942), // Purple Dark
    Color(0xFF7F1D1D), // Dark Red
    Color(0xFF1C1917), // Near Black
    Color(0xFF0C4A6E), // Sky Dark
    Color(0xFF713F12), // Bronze
    Color(0xFF134E4A), // Teal Dark
)

// ── Reactive Color Functions ──

@androidx.compose.runtime.Composable
fun appBackgroundColor(): Color =
    if (LocalIsDarkTheme.current) Slate950 else Color(0xFFF8FAFC)

@androidx.compose.runtime.Composable
fun appSurfaceColor(): Color =
    if (LocalIsDarkTheme.current) Slate900 else Color(0xFFFFFFFF)

@androidx.compose.runtime.Composable
fun appCardColor(): Color =
    if (LocalIsDarkTheme.current) Slate800 else Color(0xFFFFFFFF)

@androidx.compose.runtime.Composable
fun appTextColor(): Color =
    if (LocalIsDarkTheme.current) Color.White else Color(0xFF0F172A)

@androidx.compose.runtime.Composable
fun appTextSubColor(): Color =
    if (LocalIsDarkTheme.current) Color.White.copy(alpha = 0.55f) else Slate500

@androidx.compose.runtime.Composable
fun appPrimaryColor(): Color =
    if (LocalIsDarkTheme.current) Emerald500 else Color(0xFF0284C7)

@androidx.compose.runtime.Composable
fun appAccentColor(): Color =
    if (LocalIsDarkTheme.current) Emerald400 else Color(0xFF0EA5E9)

@androidx.compose.runtime.Composable
fun appAccentDarkColor(): Color =
    if (LocalIsDarkTheme.current) Emerald700 else Color(0xFF0369A1)

@androidx.compose.runtime.Composable
fun appSuccessColor(): Color =
    if (LocalIsDarkTheme.current) GreenSuccess else Color(0xFF0284C7)

@androidx.compose.runtime.Composable
fun appSoftSuccessColor(): Color =
    if (LocalIsDarkTheme.current) Emerald500.copy(alpha = 0.14f) else Color(0xFF0284C7).copy(alpha = 0.10f)

@androidx.compose.runtime.Composable
fun appFabContainerColor(): Color =
    if (LocalIsDarkTheme.current) Emerald500 else Color(0xFF0284C7)

@androidx.compose.runtime.Composable
fun appBorderColor(): Color =
    if (LocalIsDarkTheme.current) Slate700.copy(alpha = 0.65f) else Color(0xFFE2E8F0)

@androidx.compose.runtime.Composable
fun appIndicatorGlowColor(): Color =
    if (LocalIsDarkTheme.current) Emerald500.copy(alpha = 0.18f) else Color(0xFF0284C7).copy(alpha = 0.14f)

@androidx.compose.runtime.Composable
fun appOnAccentColor(): Color = Color.White
