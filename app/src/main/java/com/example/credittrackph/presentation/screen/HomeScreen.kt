package com.example.credittrackph.presentation.screen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.credittrackph.data.db.entity.CardEntity
import com.example.credittrackph.data.db.entity.ExpenseEntity
import com.example.credittrackph.presentation.viewmodel.CardViewModel
import com.example.credittrackph.presentation.viewmodel.ExpenseViewModel
import com.example.credittrackph.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    onViewAllTransactions: () -> Unit,
    onViewWallet: () -> Unit,
    onCardClick: (CardEntity) -> Unit,
    onAiClick: () -> Unit,
    isDarkTheme: Boolean = true,
    onToggleTheme: () -> Unit = {},
    cardViewModel: CardViewModel = hiltViewModel(),
    expenseViewModel: ExpenseViewModel = hiltViewModel(),
    profileViewModel: com.example.credittrackph.presentation.viewmodel.ProfileViewModel = hiltViewModel()
) {
    val totalCreditLimit by cardViewModel.totalCreditLimit.collectAsState()
    val totalOutstanding by cardViewModel.totalOutstanding.collectAsState()
    val netAvailable by cardViewModel.netAvailableCredit.collectAsState()
    val cardCount by cardViewModel.cardCount.collectAsState()
    val recentTransactions by expenseViewModel.recentTransactions.collectAsState()
    val upcomingDues by expenseViewModel.upcomingDues.collectAsState()
    val thisMonthTotal by expenseViewModel.thisMonthTotal.collectAsState()
    val overdueCount by expenseViewModel.overdueCount.collectAsState()
    val allProfiles by profileViewModel.allProfiles.collectAsState()

    var isBalanceVisible by remember { mutableStateOf(true) }
    var showNotificationsDialog by remember { mutableStateOf(false) }

    if (showNotificationsDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationsDialog = false },
            containerColor = appSurfaceColor(),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Emerald400)
                    Text("Notifications & Alerts", color = appTextColor(), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(
                        color = Emerald500.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("📡", fontSize = 20.sp)
                            Column {
                                Text("SMS Auto-Tracking Active", color = Emerald400, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Transactions from BDO, BPI, UnionBank, etc. are automatically recognized and recorded.", color = appTextSubColor(), fontSize = 11.sp)
                            }
                        }
                    }

                    Text(
                        "Upcoming Dues (Next 7 Days):",
                        color = appTextColor(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )

                    if (upcomingDues.isEmpty()) {
                        Text("🎉 No payments due within the next 7 days!", color = appTextSubColor(), fontSize = 12.sp)
                    } else {
                        val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
                        upcomingDues.take(4).forEach { due ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        due.merchantName,
                                        color = appTextColor(),
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text("Due: ${sdf.format(Date(due.dueDate))}", color = appTextSubColor(), fontSize = 11.sp)
                                }
                                Text("₱%,.2f".format(due.monthlyAmortization), color = RedAlert, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNotificationsDialog = false }) {
                    Text("Close", color = Emerald400, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(appBackgroundColor())
            .verticalScroll(rememberScrollState())
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
                    color = Emerald400,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Text(
                    "Good ${getTimeGreeting()}",
                    color = appTextColor(),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                // Theme Toggle Button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(if (isDarkTheme) Surface800 else Color(0xFFE2E8F0), CircleShape)
                        .clickable { onToggleTheme() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Toggle Theme",
                        tint = if (isDarkTheme) Gold400 else Color(0xFF0284C7),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // AI Helper Trigger
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Emerald500.copy(alpha = 0.2f), CircleShape)
                        .clickable { onAiClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = "AI Assistant", tint = Emerald400, modifier = Modifier.size(20.dp))
                }
                
                // Notification Center Trigger
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(if (isDarkTheme) Surface800 else Color(0xFFE2E8F0), CircleShape)
                        .clickable { showNotificationsDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Emerald400, modifier = Modifier.size(20.dp))
                }
            }
        }

        // ── Net Worth Card ──
        NetWorthCard(
            totalCreditLimit = totalCreditLimit,
            totalOutstanding = totalOutstanding,
            netAvailable = netAvailable,
            isVisible = isBalanceVisible,
            onToggleVisibility = { isBalanceVisible = !isBalanceVisible }
        )

        Spacer(Modifier.height(24.dp))

        // ── Quick Stats Row ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickStatItem(
                label = "Cards",
                value = "$cardCount",
                icon = Icons.Default.CreditCard,
                color = Emerald400,
                modifier = Modifier.weight(1f),
                onClick = onViewWallet
            )
            QuickStatItem(
                label = "This Month",
                value = if (isBalanceVisible) "₱%,.0f".format(thisMonthTotal) else "••••",
                icon = Icons.Default.CalendarMonth,
                color = Gold400,
                modifier = Modifier.weight(1f)
            )
            QuickStatItem(
                label = "Overdue",
                value = "$overdueCount",
                icon = Icons.Default.Warning,
                color = if (overdueCount > 0) RedAlert else GreenSuccess,
                modifier = Modifier.weight(1f)
            )
        }

        // ── Upcoming Dues ──
        if (upcomingDues.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))
            HomeSectionHeader(
                title = "⚠\uFE0F Upcoming Due",
                trailing = "${upcomingDues.size} items"
            )
            upcomingDues.take(3).forEach { expense ->
                val swipedBy = allProfiles.find { it.id == expense.profileId }?.name ?: "Unknown"
                UpcomingDueRow(expense = expense, isVisible = isBalanceVisible, swipedBy = swipedBy)
            }
        }

        // ── Recent Transactions ──
        if (recentTransactions.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))
            HomeSectionHeader(
                title = "Recent Transactions",
                trailing = "See All",
                onTrailingClick = onViewAllTransactions
            )
            recentTransactions.forEach { expense ->
                val swipedBy = allProfiles.find { it.id == expense.profileId }?.name ?: "Unknown"
                RecentTransactionRow(expense = expense, isVisible = isBalanceVisible, swipedBy = swipedBy)
            }
        }

        // ── Watermark Footer ──
        val uriHandler = LocalUriHandler.current
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .clickable {
                    try {
                        uriHandler.openUri("https://github.com/aydreian")
                    } catch (_: Exception) {}
                },
            color = appCardColor().copy(alpha = 0.5f),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, appSurfaceColor())
        ) {
            Row(
                modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Built with ❤️ by aydreian • github.com/aydreian",
                    color = appTextSubColor(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

// ── Net Worth Card ──

@Composable
private fun NetWorthCard(
    totalCreditLimit: Double,
    totalOutstanding: Double,
    netAvailable: Double,
    isVisible: Boolean,
    onToggleVisibility: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Emerald700, Emerald500, Emerald400)
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "TOTAL NET BALANCE | NET WORTH",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    IconButton(
                        onClick = onToggleVisibility,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            if (isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle visibility",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    if (isVisible) "₱%,.2f".format(netAvailable) else "₱ •••••••",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                )

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NetWorthSubCard(
                        label = "Total Credit",
                        value = if (isVisible) "₱%,.0f".format(totalCreditLimit) else "••••",
                        modifier = Modifier.weight(1f)
                    )
                    NetWorthSubCard(
                        label = "Outstanding",
                        value = if (isVisible) "₱%,.0f".format(totalOutstanding) else "••••",
                        modifier = Modifier.weight(1f)
                    )
                    NetWorthSubCard(
                        label = "Available",
                        value = if (isVisible) "₱%,.0f".format(netAvailable) else "••••",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun NetWorthSubCard(label: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(64.dp)
            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Column(verticalArrangement = Arrangement.Center) {
            Text(
                label,
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                softWrap = false
            )
            Spacer(Modifier.height(4.dp))
            Text(
                value,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                softWrap = false
            )
        }
    }
}

// ── Quick Stat Item ──

@Composable
private fun QuickStatItem(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = appCardColor()),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(6.dp))
            Text(
                value,
                color = appTextColor(),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                label,
                color = appTextSubColor(),
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ── Section Header ──

@Composable
private fun HomeSectionHeader(
    title: String,
    trailing: String = "",
    onTrailingClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = appTextColor(), fontWeight = FontWeight.Bold, fontSize = 17.sp)
        if (onTrailingClick != null) {
            Text(
                trailing,
                color = Emerald400,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { onTrailingClick() }
            )
        } else if (trailing.isNotEmpty()) {
            Text(trailing, color = appTextSubColor(), fontSize = 13.sp)
        }
    }
}

// ── Upcoming Due Row ──

@Composable
private fun UpcomingDueRow(expense: ExpenseEntity, isVisible: Boolean, swipedBy: String) {
    val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
    val daysLeft = ((expense.dueDate - System.currentTimeMillis()) / (24 * 60 * 60 * 1000L)).toInt()
    val urgencyColor = when {
        daysLeft <= 1 -> RedAlert
        daysLeft <= 3 -> YellowWarn
        else -> Emerald500
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 3.dp),
        colors = CardDefaults.cardColors(containerColor = appCardColor()),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(urgencyColor, CircleShape)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    expense.merchantName,
                    color = appTextColor(),
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        "Due: ${sdf.format(Date(expense.dueDate))}",
                        color = appTextSubColor(),
                        fontSize = 11.sp,
                        softWrap = false
                    )
                    Box(
                        modifier = Modifier
                            .background(Surface700.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Text(
                            "Swiped by: $swipedBy",
                            color = appTextColor().copy(alpha = 0.8f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            softWrap = false
                        )
                    }
                }
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    if (isVisible) "₱%,.2f".format(expense.monthlyAmortization) else "••••",
                    color = urgencyColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    softWrap = false
                )
                Text(
                    if (daysLeft <= 0) "Overdue!" else "$daysLeft day${if (daysLeft != 1) "s" else ""}",
                    color = urgencyColor,
                    fontSize = 10.sp,
                    softWrap = false
                )
            }
        }
    }
}

// ── Recent Transaction Row ──

@Composable
private fun RecentTransactionRow(expense: ExpenseEntity, isVisible: Boolean, swipedBy: String) {
    val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 3.dp),
        colors = CardDefaults.cardColors(containerColor = appCardColor()),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(expense.category.emoji, fontSize = 24.sp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    expense.merchantName,
                    color = appTextColor(),
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        "${expense.category.displayName} • ${sdf.format(Date(expense.purchaseDate))}",
                        color = appTextSubColor(),
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        softWrap = false
                    )
                    Box(
                        modifier = Modifier
                            .background(Surface700.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Text(
                            "Swiped by: $swipedBy",
                            color = appTextColor().copy(alpha = 0.8f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            softWrap = false
                        )
                    }
                }
            }
            Spacer(Modifier.width(8.dp))
            Text(
                if (isVisible) "₱%,.2f".format(expense.monthlyAmortization) else "••••",
                color = if (expense.isPaid) GreenSuccess else appTextColor(),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                softWrap = false
            )
        }
    }
}

// ── Utility ──

fun getTimeGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11 -> "Morning!"
        in 12..17 -> "Afternoon!"
        in 18..21 -> "Evening!"
        else -> "Night!"
    }
}
