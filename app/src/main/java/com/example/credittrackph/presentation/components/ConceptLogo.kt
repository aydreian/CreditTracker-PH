package com.example.credittrackph.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.credittrackph.theme.SoraFontFamily

/**
 * Concept 03 Modern Monogram Logo from Figma ("GEMINI USE THIS").
 * Renders the geometric ligature fusing letters 'C' and 'T' on an Indigo Iris (#4F46E5) badge.
 */
@Composable
fun ConceptLogo(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val s = this.size.width
        val scale = s / 48f

        // 1. Outer Iris Badge (#4F46E5)
        drawRoundRect(
            color = Color(0xFF4F46E5),
            size = Size(s, s),
            cornerRadius = CornerRadius(12f * scale, 12f * scale)
        )

        // 2. White C-boundary square with rounded stroke
        val strokeWidth = 5.5f * scale
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(10f * scale, 10f * scale),
            size = Size(28f * scale, 28f * scale),
            cornerRadius = CornerRadius(4f * scale, 4f * scale),
            style = Stroke(width = strokeWidth)
        )

        // 3. Neon Coral (#FF6B6B) T-ligature
        val coral = Color(0xFFFF6B6B)
        // Horizontal crossbar
        drawRoundRect(
            color = coral,
            topLeft = Offset(7f * scale, 14f * scale),
            size = Size(20f * scale, 6f * scale),
            cornerRadius = CornerRadius(1.5f * scale, 1.5f * scale)
        )
        // Vertical stem
        drawRoundRect(
            color = coral,
            topLeft = Offset(7f * scale, 14f * scale),
            size = Size(6f * scale, 17f * scale),
            cornerRadius = CornerRadius(1.5f * scale, 1.5f * scale)
        )
    }
}

/**
 * Concept 03 Primary Horizontal Lockup: Monogram Logo + "CreditTracker" + "PH"
 */
@Composable
fun ConceptBrandLockup(
    modifier: Modifier = Modifier,
    logoSize: Dp = 32.dp,
    textColor: Color = Color.White
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        ConceptLogo(size = logoSize)
        Spacer(modifier = Modifier.width(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "CreditTracker",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = textColor,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "PH",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 17.sp,
                color = Color(0xFFFF6B6B),
                letterSpacing = (-0.5).sp
            )
        }
    }
}
