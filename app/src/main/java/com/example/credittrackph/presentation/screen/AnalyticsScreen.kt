package com.example.credittrackph.presentation.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.credittrackph.presentation.viewmodel.ExpenseViewModel
import com.example.credittrackph.theme.*
import java.util.Calendar

private val CategoryColors = listOf(
    Color(0xFFFF5252), // Red
    Color(0xFF448AFF), // Blue
    Color(0xFFFFB74D), // Orange
    Color(0xFF66BB6A), // Green
    Color(0xFFAB47BC), // Purple
    Color(0xFF26C6DA), // Cyan
    Color(0xFFFF7043), // Deep Orange
    Color(0xFF8D6E63), // Brown
    Color(0xFFEC407A), // Pink
    Color(0xFF7E57C2), // Indigo
    Color(0xFF78909C)  // Blue Grey
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface950)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Header (tab-style, no back button) ──
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            Text(
                "CREDITTRACK PH",
                color = Emerald400,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
            Text(
                "Statistics",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }

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
            colors = CardDefaults.cardColors(containerColor = Surface800),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Total Tracked Spend",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.6f)
                )
                Text(
                    text = "₱%,.2f".format(totalSpend),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
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
                    color = Color.White.copy(0.5f)
                )
            }
        } else {
            // ── Donut Chart ──
            Card(
                colors = CardDefaults.cardColors(containerColor = Surface800),
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
                        color = Color.White,
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
                                val sweepAngle = ((stat.totalAmount / totalSpend) * 360f).toFloat()
                                val color = CategoryColors[index % CategoryColors.size]

                                drawArc(
                                    color = color,
                                    startAngle = startAngle,
                                    sweepAngle = sweepAngle,
                                    useCenter = false,
                                    style = Stroke(width = 36.dp.toPx())
                                )
                                startAngle += sweepAngle
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${categoryTotals.size}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Categories",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(0.6f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // ── Legend ──
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        categoryTotals.forEachIndexed { index, stat ->
                            val percentage = (stat.totalAmount / totalSpend) * 100
                            val color = CategoryColors[index % CategoryColors.size]

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Surface900, RoundedCornerShape(10.dp))
                                    .padding(12.dp),
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
                                    color = Color.White,
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
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}
