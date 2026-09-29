package com.example.credittrackph.theme

import androidx.compose.animation.core.*

/**
 * Centralized animation specs for CreditTrack PH.
 * Using these consistently across all screens gives the app its "silky smooth" feel.
 */
object Motion {

    // ── Standard card / page transitions ──
    val EnterSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness    = Spring.StiffnessMediumLow
    )
    val ExitTween = tween<Float>(
        durationMillis = 220,
        easing         = FastOutLinearInEasing
    )

    // Dp variants for offset animations
    val EnterSpringDp = spring<androidx.compose.ui.unit.Dp>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness    = Spring.StiffnessMediumLow
    )

    // ── Micro-interactions (button press, icon toggle) ──
    val MicroSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness    = Spring.StiffnessMedium
    )

    // ── Currency / count-up animation ──
    val CounterTween = tween<Float>(
        durationMillis = 900,
        easing         = FastOutSlowInEasing
    )

    // ── FAB rotation and menu reveal ──
    val FabTween = tween<Float>(
        durationMillis = 280,
        easing         = FastOutSlowInEasing
    )

    // ── Nav indicator glide ──
    val NavIndicatorTween = tween<androidx.compose.ui.unit.Dp>(
        durationMillis = 340,
        easing         = FastOutSlowInEasing
    )

    // ── List stagger ──
    const val StaggerBaseMs = 55
    const val StaggerMax    = 6 // cap stagger at 6 items so it doesn't drag

    // ── Duration constants (ms) ──
    const val Short  = 180
    const val Medium = 320
    const val Long   = 480
}
