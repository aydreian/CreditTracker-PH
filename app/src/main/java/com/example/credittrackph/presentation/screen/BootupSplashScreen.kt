package com.example.credittrackph.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.example.credittrackph.theme.appBackgroundColor
import com.example.credittrackph.theme.appTextColor

@Composable
fun BootupSplashScreen(onAnimationFinished: () -> Unit) {
    // Animation states mapping to the 6 frames
    var step by remember { mutableIntStateOf(1) }

    // Text offsets and alphas
    val creditOffset by animateDpAsState(
        targetValue = if (step >= 3) (-24).dp else 0.dp,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "creditOffset"
    )
    val containerOffsetY by animateDpAsState(
        targetValue = if (step >= 4) 40.dp else 0.dp,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "containerOffsetY"
    )

    LaunchedEffect(Unit) {
        delay(300) // Frame 1: "Credit"
        step = 2
        delay(600) // Frame 2: "CreditTracker"
        step = 3
        delay(600) // Frame 3: "CreditTracker" moves left, "PH" fades in
        step = 4
        delay(600) // Frame 4: Panning down
        step = 5
        delay(800) // Frame 5: Logo appears
        step = 6
        delay(1200) // Frame 6: Subtitle fades in
        delay(1500) // Hold for reading
        onAnimationFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1B4B)), // Forced deep indigo for splash
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.offset(y = containerOffsetY)
        ) {
            // Logo placeholder / Concept 3 Logo
            AnimatedVisibility(
                visible = step >= 5,
                enter = fadeIn(tween(800)) + scaleIn(initialScale = 0.8f)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color.White.copy(alpha = 0.1f), shape = androidx.compose.foundation.shape.CircleShape)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "CT",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Main Typographic Lockup
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(contentAlignment = Alignment.CenterEnd, modifier = Modifier.width(180.dp).offset(x = creditOffset)) {
                    Row {
                        Text(
                            text = "Credit",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-1).sp
                        )
                        AnimatedVisibility(visible = step >= 2) {
                            Text(
                                text = "Tracker",
                                color = Color.White,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-1).sp
                            )
                        }
                    }
                }
                AnimatedVisibility(visible = step >= 3, enter = fadeIn(tween(600))) {
                    Text(
                        text = "PH",
                        color = Color(0xFF34D399), // Emerald accent
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-1).sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Subtitle
            AnimatedVisibility(
                visible = step >= 6,
                enter = fadeIn(tween(1000))
            ) {
                Text(
                    text = "Clear direction for your financial future.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
