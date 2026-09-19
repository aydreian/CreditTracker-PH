package com.example.credittrackph.presentation.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.credittrackph.data.model.ExpenseCategory
import com.example.credittrackph.presentation.components.PaymentCalendarView
import com.example.credittrackph.presentation.viewmodel.ExpenseViewModel
import com.example.credittrackph.theme.*
import java.util.Calendar

private val CategoryColors = listOf(
    Color(0xFFFF5252), // Red
    Color(0xFF38BDF8), // Sky Blue
    Color(0xFFFFB74D), // Amber / Orange
    Color(0xFF10B981), // Emerald
    Color(0xFFAB47BC), // Purple
    Color(0xFF26C6DA), // Cyan
    Color(0xFFFF7043), // Deep Orange
    Color(0xFF8D6E63), // Brown
    Color(0xFFEC407A), // Pink
    Color(0xFF818CF8), // Indigo
    Color(0xFF94A3B8)  // Slate
)

data class CategoryStat(
    val category: ExpenseCategory,
    val totalAmount: Double
)

@Composable
fun AnalyticsScreen(
    expenseViewModel: ExpenseViewModel = hiltViewModel()
) {
    val expenses by expenseViewModel.allExpenses.collectAsState()

    var timeFilter by remember { mutableStateOf("All Time") }

    val filteredExpenses = remember(expenses, timeFilter) {
        if (timeFilter == "This Month") {
            val cal = Calendar.getInstance()
            val currentYear = cal.get(Calendar.YEAR)
            val currentMonth = cal.get(Calendar.MONTH)
            expenses.filter { expense ->
                val expCal = Calendar.getInstance().apply { timeInMillis = expense.purchaseDate }
                expCal.get(Calendar.YEAR) == currentYear && expCal.get(Calendar.MONTH) == currentMonth
            }
        } else {
            expenses
        }
    }

    val categoryTotals: List<CategoryStat> = remember(filteredExpenses) {
        ExpenseCategory.entries.mapNotNull { cat ->
            val sum = filteredExpenses.filter { it.category == cat }.sumOf { it.monthlyAmortization }
            if (sum > 0) CategoryStat(cat, sum) else null
        }.sortedByDescending { it.totalAmount }
    }

    val totalSpend: Double = remember(categoryTotals) {
        categoryTotals.sumOf { it.totalAmount }
    }

    val topStat: CategoryStat? = categoryTotals.firstOrNull()

    // Smooth one-time entrance animation on open
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
        )
    }

    var selectedViewMode by remember { mutableStateOf("Breakdown") }
    var showSummaryCardDialog by remember { mutableStateOf(false) }

    if (showSummaryCardDialog) {
        SummaryCardDialog(
            expenses = filteredExpenses,
            onDismiss = { showSummaryCardDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(appBackgroundColor())
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
                    "Statistics",
                    color = appTextColor(),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Share Card Button
            Surface(
                modifier = Modifier.clickable { showSummaryCardDialog = true },
                color = Emerald500.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = Emerald400, modifier = Modifier.size(16.dp))
                    Text("Share Card", color = Emerald400, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // ── View Mode Switcher (Breakdown vs Calendar) ──
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedViewMode == "Breakdown",
                onClick = { selectedViewMode = "Breakdown" },
                label = { Text("📊 Category Breakdown", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Emerald500,
                    selectedLabelColor = Surface950
                )
            )
            FilterChip(
                selected = selectedViewMode == "Calendar",
                onClick = { selectedViewMode = "Calendar" },
                label = { Text("📅 Payment Calendar", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Emerald500,
                    selectedLabelColor = Surface950
                )
            )
        }

        if (selectedViewMode == "Calendar") {
            // ── Calendar View ──
            PaymentCalendarView(
                expenses = expenses,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        } else {
            // ── Time Filter ──
            Row(
                modifier = Modifier.padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All Time", "This Month").forEach { filter ->
                    FilterChip(
                        selected = timeFilter == filter,
                        onClick = { timeFilter = filter },
                        label = { Text(filter, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Emerald500,
                            selectedLabelColor = Surface950
                        )
                    )
                }
            }

            // ── Summary Card ──
            Card(
                colors = CardDefaults.cardColors(containerColor = appCardColor()),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Total Tracked Spend",
                    style = MaterialTheme.typography.labelMedium,
                    color = appTextSubColor()
                )
                Text(
                    text = "₱%,.2f".format(totalSpend * animProgress.value),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = appTextColor()
                )
                if (topStat != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Highest: ${topStat.category.emoji} ${topStat.category.displayName} (₱%,.2f)".format(topStat.totalAmount),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Emerald400
                    )
                }
            }
        }

        if (categoryTotals.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No expenses recorded yet.\nAdd expenses to view category statistics.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = appTextSubColor()
                )
            }
        } else {
            // ── Donut Chart ──
            Card(
                colors = CardDefaults.cardColors(containerColor = appCardColor()),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Category Breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = appTextColor(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Box(
                        modifier = Modifier.size(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            var startAngle = -90f
                            categoryTotals.forEachIndexed { index, stat ->
                                val fullSweep = ((stat.totalAmount / totalSpend) * 360f).toFloat()
                                val sweepAngle = fullSweep * animProgress.value
                                val color = CategoryColors[index % CategoryColors.size]

                                drawArc(
                                    color = color,
                                    startAngle = startAngle,
                                    sweepAngle = sweepAngle,
                                    useCenter = false,
                                    style = Stroke(width = 34.dp.toPx())
                                )
                                startAngle += sweepAngle
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${categoryTotals.size}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = appTextColor()
                            )
                            Text(
                                text = "Categories",
                                style = MaterialTheme.typography.labelSmall,
                                color = appTextSubColor()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // ── Legend & Progress Bars ──
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        categoryTotals.forEachIndexed { index, stat ->
                            val percentage = (stat.totalAmount / totalSpend) * 100
                            val color = CategoryColors[index % CategoryColors.size]

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(appSurfaceColor(), RoundedCornerShape(10.dp))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "${stat.category.emoji} ${stat.category.displayName}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = appTextColor(),
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "₱%,.2f (%.1f%%)".format(stat.totalAmount, percentage),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Emerald400,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { ((percentage.toFloat() / 100f) * animProgress.value).coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = color,
                                    trackColor = appBackgroundColor()
                                )
                            }
                        }
                    }
                }
            }
        }
    }

        Spacer(Modifier.height(80.dp))
    }
}
