package com.example.credittrackph.presentation.screen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.credittrackph.data.db.entity.CardEntity
import com.example.credittrackph.data.model.CardType
import com.example.credittrackph.presentation.components.BankLogo
import com.example.credittrackph.presentation.components.CardNetworkLogo
import com.example.credittrackph.presentation.viewmodel.CardViewModel
import com.example.credittrackph.theme.*

@Composable
fun WalletScreen(
    onCardClick: (CardEntity) -> Unit,
    onAddCard: () -> Unit,
    cardViewModel: CardViewModel = hiltViewModel()
) {
    val cards by cardViewModel.allCards.collectAsState()
    val totalCreditLimit by cardViewModel.totalCreditLimit.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(appBackgroundColor())
    ) {
        // ── Header ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "CREDITTRACK PH",
                    color = appPrimaryColor(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Text(
                    "Wallet",
                    color = appTextColor(),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            IconButton(
                onClick = onAddCard,
                modifier = Modifier
                    .size(44.dp)
                    .background(appFabContainerColor(), CircleShape)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Card", tint = appOnAccentColor())
            }
        }

        // Card count summary
        Text(
            "${cards.size} card${if (cards.size != 1) "s" else ""} • Total Limit: ₱%,.0f".format(totalCreditLimit),
            color = appTextSubColor(),
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(Modifier.height(16.dp))

        if (cards.isEmpty()) {
            // ── Empty State ──
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("💳", fontSize = 64.sp)
                    Spacer(Modifier.height(16.dp))
                    Text("No cards yet", color = appTextColor(), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text(
                        "Tap + to add your first credit card",
                        color = appTextSubColor(),
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = onAddCard,
                        colors = ButtonDefaults.buttonColors(containerColor = appAccentColor())
                    ) {
                        Icon(Icons.Default.Add, null, tint = appOnAccentColor())
                        Spacer(Modifier.width(8.dp))
                        Text("Add Card", color = appOnAccentColor(), fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // ── 2-Column Card Grid ──
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(cards, key = { it.id }) { card ->
                    WalletCardItem(
                        card = card,
                        modifier = Modifier.animateItem(),
                        onClick = { onCardClick(card) }
                    )
                }
            }
        }
    }
}

// ── Wallet Card Item (compact grid card, Agila Finance-inspired) ──

@Composable
fun WalletCardItem(card: CardEntity, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val cardColor = Color(card.cardColorArgb)
    val lighterColor = cardColor.copy(alpha = 0.7f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.75f)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(cardColor, lighterColor, cardColor.copy(alpha = 0.85f))
                    )
                )
        ) {
            // Decorative circles
            Box(
                Modifier
                    .size(100.dp)
                    .offset(x = 100.dp, y = (-20).dp)
                    .background(Color.White.copy(alpha = 0.06f), CircleShape)
            )
            Box(
                Modifier
                    .size(80.dp)
                    .offset(x = 120.dp, y = 60.dp)
                    .background(Color.White.copy(alpha = 0.04f), CircleShape)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top row: Bank logo + menu
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BankLogo(card.bank)
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color.White.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.MoreHoriz,
                            contentDescription = "Options",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Chip / SIM icon
                Box(
                    modifier = Modifier
                        .size(32.dp, 24.dp)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFFFD700),
                                    Color(0xFFFFA500),
                                    Color(0xFFFFD700)
                                )
                            ),
                            RoundedCornerShape(4.dp)
                        )
                )

                // Balance section
                Column {
                    Text(
                        card.label,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "BALANCE",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp
                    )
                    Text(
                        "₱%,.0f".format(card.creditLimit),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                // Bottom row: card dots + type badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "•••• ${card.lastFourDigits}",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                    CardNetworkLogo(card.cardType)
                }
            }
        }
    }
}
