package com.example.credittrackph.presentation.screen

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.credittrackph.data.db.entity.ExpenseEntity
import com.example.credittrackph.data.model.ExpenseCategory
import com.example.credittrackph.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SummaryCardDialog(
    expenses: List<ExpenseEntity>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val currentMonthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())

    val totalSpend = expenses.sumOf { it.monthlyAmortization }
    val count = expenses.size

    val topCategory = ExpenseCategory.entries.mapNotNull { cat ->
        val sum = expenses.filter { it.category == cat }.sumOf { it.monthlyAmortization }
        if (sum > 0) Pair(cat, sum) else null
    }.maxByOrNull { it.second }

    val shareText = buildString {
        appendLine("📊 CreditTrack PH — $currentMonthName Summary 🇵🇭")
        appendLine("━━━━━━━━━━━━━━━━━━━━━")
        appendLine("💳 Total Spent: ₱%,.2f".format(totalSpend))
        appendLine("🔢 Transactions: $count")
        if (topCategory != null) {
            val pct = (topCategory.second / totalSpend) * 100
            appendLine("🏆 Top Spend: ${topCategory.first.emoji} ${topCategory.first.displayName} (₱%,.2f • %.0f%%)".format(topCategory.second, pct))
        }
        appendLine("━━━━━━━━━━━━━━━━━━━━━")
        appendLine("✨ Track your PH credit cards smartly with CreditTrack PH")
        appendLine("Built with ❤️ by aydreian • github.com/aydreian")
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .clip(RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = appCardColor()),
            border = BorderStroke(1.dp, appBorderColor())
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Monthly Spend Card ✨",
                        color = appTextColor(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = appTextSubColor())
                    }
                }

                Spacer(Modifier.height(12.dp))

                // The Graphic Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                colors = if (LocalIsDarkTheme.current) {
                                    listOf(
                                        Emerald900,
                                        Emerald700,
                                        Surface900
                                    )
                                } else {
                                    listOf(
                                        Color(0xFF0369A1),
                                        Color(0xFF0284C7),
                                        Color(0xFF0C4A6E)
                                    )
                                }
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "CREDITTRACK PH 🇵🇭",
                                color = if (LocalIsDarkTheme.current) Emerald400 else Color(0xFFBAE6FD),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            )
                            Surface(
                                color = Color.White.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    currentMonthName,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                "Total Monthly Spend",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                            Text(
                                "₱%,.2f".format(totalSpend),
                                color = Color.White,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        HorizontalDivider(color = Color.White.copy(alpha = 0.15f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Transactions", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                                Text("$count items", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                            if (topCategory != null) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Top Category", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                                    Text(
                                        "${topCategory.first.emoji} ${topCategory.first.displayName}",
                                        color = if (LocalIsDarkTheme.current) Emerald400 else Color(0xFFBAE6FD),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Built with ❤️ by aydreian • github.com/aydreian",
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))

                // Share Button
                Button(
                    onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "Share Monthly Summary")
                        context.startActivity(shareIntent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = appAccentColor()),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = appOnAccentColor())
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Share to GCash / Friends / Stories 🚀",
                        color = appOnAccentColor(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
