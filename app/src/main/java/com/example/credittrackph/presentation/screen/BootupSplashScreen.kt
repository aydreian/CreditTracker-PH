package com.example.credittrackph.presentation.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.example.credittrackph.presentation.components.ConceptLogo
import com.example.credittrackph.theme.SoraFontFamily

/**
 * Bootup Splash Animation Sequence based on Figma Concept 03 & 04 / Wireframe Group 4.
 *
 * Sequence:
 * Frame 1: "Credit" (Centered)
 * Frame 2: "CreditTracker" (Centered)
 * Frame 3: "CreditTracker" (Animates left) + "PH" (Neon Coral #FF6B6B fades in)
 * Frame 4 & 5: Logo appears above, text smoothly pans down
 * Frame 6: Subtitle "Track your credit. Own your progress." fades in
 */
@Composable
fun BootupSplashScreen(
    modifier: Modifier = Modifier,
    onAnimationFinished: () -> Unit
) {
    var step by remember { mutableIntStateOf(1) }

    LaunchedEffect(Unit) {
        delay(400) // Frame 1: "Credit" centered
        step = 2   // Frame 2: "Tracker" expands from center
        delay(600)
        step = 3   // Frame 3: "PH" expands in Neon Coral, text shifts left
        delay(650)
        step = 4   // Frame 4 & 5: Logo appears and text pans down
        delay(700)
        step = 5   // Frame 5: Logo elements full reveal
        delay(500)
        step = 6   // Frame 6: Subtitle fades in
        delay(1400) // Hold state for user reading
        onAnimationFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF020617)), // Slate950 — matches new dark theme background
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
        ) {
            // Frame 4 & 5: Monogram Logo from Concept 03
            AnimatedVisibility(
                visible = step >= 4,
                enter = fadeIn(tween(400)) + scaleIn(
                    initialScale = 0.5f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    ConceptLogo(size = 56.dp)
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // Typography Lockup: Credit + Tracker + PH (Perfect mathematical alignment)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Credit",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    color = Color.White,
                    letterSpacing = (-0.5).sp
                )

                AnimatedVisibility(
                    visible = step >= 2,
                    enter = fadeIn(tween(350)) + expandHorizontally(
                        expandFrom = Alignment.Start,
                        animationSpec = tween(400, easing = FastOutSlowInEasing)
                    )
                ) {
                    Text(
                        text = "Tracker",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        color = Color.White,
                        letterSpacing = (-0.5).sp
                    )
                }

                AnimatedVisibility(
                    visible = step >= 3,
                    enter = fadeIn(tween(350)) + expandHorizontally(
                        expandFrom = Alignment.Start,
                        animationSpec = tween(400, easing = FastOutSlowInEasing)
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PH",
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 30.sp,
                            color = Color(0xFFFF6B6B), // Neon Coral from Figma Concept 03
                            letterSpacing = (-0.5).sp
                        )
                    }
                }
            }

            // Frame 6: Textholder / Subtitle from Figma Wireframe
            AnimatedVisibility(
                visible = step >= 6,
                enter = fadeIn(tween(600)) + slideInVertically(
                    initialOffsetY = { 20 },
                    animationSpec = tween(600, easing = FastOutSlowInEasing)
                )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Track your credit. Own your progress.",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.72f),
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}
