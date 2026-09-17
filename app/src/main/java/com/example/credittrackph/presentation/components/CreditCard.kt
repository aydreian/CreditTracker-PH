package com.example.credittrackph.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.credittrackph.data.db.entity.CardEntity
import com.example.credittrackph.data.model.CardType

@Composable
fun CreditCardView(
    card: CardEntity,
    totalOutstanding: Double,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val cardColor = Color(card.cardColorArgb)
    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            cardColor,
            cardColor.copy(alpha = 0.85f),
            Color.White.copy(alpha = 0.08f),
            cardColor.copy(alpha = 0.9f),
            cardColor
        )
    )

    Box(
        modifier = modifier
            .width(340.dp)
            .height(200.dp)
            .shadow(12.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(shimmerBrush)
            .clickable { onClick() }
            .padding(24.dp)
    ) {
        // Decorative circles
        Box(
            Modifier
                .size(160.dp)
                .offset(x = 200.dp, y = (-40).dp)
                .background(
                    Color.White.copy(alpha = 0.05f),
                    RoundedCornerShape(50)
                )
        )
        Box(
            Modifier
                .size(120.dp)
                .offset(x = 240.dp, y = 80.dp)
                .background(
                    Color.White.copy(alpha = 0.04f),
                    RoundedCornerShape(50)
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // Top row: Bank logo + Card network logo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BankLogo(card.bank)
                CardNetworkLogo(card.cardType)
            }

            Spacer(Modifier.weight(1f))

            // Card label
            Text(
                text = card.label,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(4.dp))

            // Masked card number
            Text(
                text = "••••  ••••  ••••  ${card.lastFourDigits}",
                color = Color.White,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                letterSpacing = 2.sp
            )

            Spacer(Modifier.height(12.dp))

            // Bottom row: outstanding balance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        "Outstanding",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 10.sp
                    )
                    Text(
                        "₱${String.format("%,.2f", totalOutstanding)}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Limit",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 10.sp
                    )
                    Text(
                        "₱${String.format("%,.0f", card.creditLimit)}",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun CardTypeBadge(cardType: CardType) {
    val (text, color) = when (cardType) {
        CardType.VISA -> "VISA" to Color(0xFF1A1F71)
        CardType.MASTERCARD -> "MC" to Color(0xFFEB001B)
        CardType.JCB -> "JCB" to Color(0xFF003087)
        CardType.AMEX -> "AMEX" to Color(0xFF2E77BC)
        CardType.OTHER -> "CARD" to Color.Gray
    }
    Box(
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}
