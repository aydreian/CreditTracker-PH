package com.example.credittrackph.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.credittrackph.theme.Emerald500

/**
 * Animated pulsing status dot — replaces all emoji status indicators (📡, etc.).
 * The scale loops between [minScale] and [maxScale] using a smooth tween.
 */
@Composable
fun PulsingDot(
    modifier: Modifier = Modifier,
    color: Color = Emerald500,
    size: Dp = 8.dp,
    minScale: Float = 0.8f,
    maxScale: Float = 1.45f
) {
    val pulse = rememberInfiniteTransition(label = "pulsing_dot")
    val scale by pulse.animateFloat(
        initialValue  = minScale,
        targetValue   = maxScale,
        animationSpec = infiniteRepeatable(
            animation  = tween(durationMillis = 820, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_pulse_scale"
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .background(color, CircleShape)
    )
}

/**
 * Three-dot typing indicator for AI loading state.
 * Each dot is staggered by [staggerMs] for a wave effect.
 */
@Composable
fun TypingIndicator(
    modifier: Modifier = Modifier,
    dotColor: Color = Emerald500,
    dotSize: Dp = 7.dp,
    staggerMs: Int = 160
) {
    val transition = rememberInfiniteTransition(label = "typing_indicator")

    @Composable
    fun bounceDot(delayMs: Int): Float {
        val alpha by transition.animateFloat(
            initialValue  = 0.3f,
            targetValue   = 1f,
            animationSpec = infiniteRepeatable(
                animation  = tween(durationMillis = 600, delayMillis = delayMs, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "dot_alpha_$delayMs"
        )
        return alpha
    }

    val a1 = bounceDot(0)
    val a2 = bounceDot(staggerMs)
    val a3 = bounceDot(staggerMs * 2)

    Row(
        modifier          = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(dotSize).padding(1.dp).background(dotColor.copy(alpha = a1), CircleShape))
        Box(Modifier.size(dotSize).padding(1.dp).background(dotColor.copy(alpha = a2), CircleShape))
        Box(Modifier.size(dotSize).padding(1.dp).background(dotColor.copy(alpha = a3), CircleShape))
    }
}
