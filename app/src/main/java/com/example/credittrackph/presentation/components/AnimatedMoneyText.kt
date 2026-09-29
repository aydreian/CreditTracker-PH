package com.example.credittrackph.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.credittrackph.theme.SoraFontFamily

/**
 * Animated money text that counts up from 0 to [amount] on first render and on
 * [isVisible] changes. Uses tabular number rendering to prevent width jitter.
 *
 * Usage:
 *   AnimatedMoneyText(amount = netAvailable, isVisible = isBalanceVisible, color = Color.White)
 */
@Composable
fun AnimatedMoneyText(
    amount: Double,
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    prefix: String = "₱",
    decimals: Int = 2,
    hiddenText: String = "₱ •••••••",
    color: Color = Color.White,
    style: TextStyle = TextStyle(
        fontFamily    = SoraFontFamily,
        fontWeight    = FontWeight.Bold,
        fontSize      = 32.sp,
        letterSpacing = (-0.5).sp,
    )
) {
    val animatedAmount by animateFloatAsState(
        targetValue    = if (isVisible) amount.toFloat() else 0f,
        animationSpec  = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label          = "money_count_up"
    )

    val displayText = if (isVisible) {
        val fmt = if (decimals == 0) "%,.0f" else "%,.${decimals}f"
        "$prefix${fmt.format(animatedAmount.toDouble())}"
    } else {
        hiddenText
    }

    Text(
        text     = displayText,
        style    = style,
        color    = color,
        modifier = modifier
    )
}

/**
 * Compact money text for list rows and sub-cards (smaller, no count-up — instant toggle).
 */
@Composable
fun MoneyText(
    amount: Double,
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    prefix: String = "₱",
    decimals: Int = 2,
    hiddenText: String = "••••",
    color: Color = Color.White,
    style: TextStyle = TextStyle(
        fontFamily = SoraFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize   = 14.sp,
    )
) {
    val text = if (isVisible) {
        val fmt = if (decimals == 0) "%,.0f" else "%,.${decimals}f"
        "$prefix${fmt.format(amount)}"
    } else hiddenText

    Text(text = text, style = style, color = color, modifier = modifier)
}
