package com.example.credittrackph.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.credittrackph.theme.appCardColor

/**
 * Skeleton loading shimmer block. Alpha pulses between [minAlpha] and [maxAlpha]
 * to signal that content is loading without showing a spinner.
 *
 * Usage:
 *   ShimmerBox(Modifier.fillMaxWidth().height(60.dp))
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 12.dp,
    minAlpha: Float = 0.25f,
    maxAlpha: Float = 0.65f
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val shimmerAlpha by transition.animateFloat(
        initialValue  = minAlpha,
        targetValue   = maxAlpha,
        animationSpec = infiniteRepeatable(
            animation  = tween(durationMillis = 880, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_alpha"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(appCardColor().copy(alpha = shimmerAlpha))
    )
}
