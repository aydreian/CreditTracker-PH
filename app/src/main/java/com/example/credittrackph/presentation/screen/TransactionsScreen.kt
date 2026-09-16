package com.example.credittrackph.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.credittrackph.data.db.entity.ExpenseEntity
import com.example.credittrackph.presentation.viewmodel.ExpenseViewModel
import com.example.credittrackph.theme.*
import java.text.SimpleDateFormat
import java.util.*

sealed class TransactionItem {
    abstract val purchaseDate: Long
    
    data class Single(val expense: ExpenseEntity) : TransactionItem() {
        override val purchaseDate = expense.purchaseDate
    }
    
    data class InstallmentGroup(
        val expenses: List<ExpenseEntity>,
        override val purchaseDate: Long,
        val merchantName: String
    ) : TransactionItem()
}

@Composable
fun TransactionsScreen(
    expenseViewModel: ExpenseViewModel = hiltViewModel(),
    profileViewModel: com.example.credittrackph.presentation.viewmodel.ProfileViewModel = hiltViewModel()
) {
    val allExpenses by expenseViewModel.allExpenses.collectAsState()
    val allProfiles by profileViewModel.allProfiles.collectAsState()

    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "This Month", "Unpaid", "Paid", "Installment")

    var selectedProfileFilterId by remember { mutableStateOf<Int?>(null) } // null means "Overall"

    val transactionItems = remember(allExpenses, selectedFilter, selectedProfileFilterId) {
        val cal = Calendar.getInstance()
        val currentYear = cal.get(Calendar.YEAR)
        val currentMonth = cal.get(Calendar.MONTH)

        // 1. Filter by Status/Time
        var filtered = when (selectedFilter) {
            "This Month" -> allExpenses.filter { expense ->
                val expCal = Calendar.getInstance().apply { timeInMillis = expense.purchaseDate }
                expCal.get(Calendar.YEAR) == currentYear && expCal.get(Calendar.MONTH) == currentMonth
            }
            "Unpaid" -> allExpenses.filter { !it.isPaid }
            "Paid" -> allExpenses.filter { it.isPaid }
            "Installment" -> allExpenses.filter { it.isInstallment }
            else -> allExpenses
        }
        
        // 1.5 Filter by Profile
        if (selectedProfileFilterId != null) {
            filtered = filtered.filter { it.profileId == selectedProfileFilterId }
        }
        
        filtered = filtered.sortedByDescending { it.purchaseDate }

        // 2. Group Installments
        val groupedList = mutableListOf<TransactionItem>()
        val processedIds = mutableSetOf<Int>()
        
        filtered.forEach { expense ->
            if (processedIds.contains(expense.id)) return@forEach
            
            if (expense.isInstallment) {
                val siblings = allExpenses.filter { 
                    it.isInstallment && it.purchaseDate == expense.purchaseDate && it.merchantName == expense.merchantName 
                }.sortedBy { it.currentInstallmentMonth }
                
                if (siblings.isNotEmpty()) {
                    groupedList.add(TransactionItem.InstallmentGroup(siblings, expense.purchaseDate, expense.merchantName))
                    processedIds.addAll(siblings.map { it.id })
                }
            } else {
                groupedList.add(TransactionItem.Single(expense))
                processedIds.add(expense.id)
            }
        }
        groupedList.sortedByDescending { it.purchaseDate }
    }

    // Group by date label
    val groupedByDate = remember(transactionItems) {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val todayCal = Calendar.getInstance()
        val today = sdf.format(todayCal.time)
        todayCal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = sdf.format(todayCal.time)

        transactionItems.groupBy { item ->
            val dateStr = sdf.format(Date(item.purchaseDate))
            when (dateStr) {
                today -> "Today"
                yesterday -> "Yesterday"
                else -> dateStr
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface950)
    ) {
        // ── Header ──
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            Text(
                "CREDITTRACK PH",
                color = Emerald400,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
            Text(
                "Transactions",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "${transactionItems.size} transaction group${if (transactionItems.size != 1) "s" else ""}",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 13.sp
            )
        }

        // ── Filter Chips (Time / Status) ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filters.forEach { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Emerald500,
                        selectedLabelColor = Surface950
                    )
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // ── Filter Dropdown (Profile) ──
        if (allProfiles.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedProfileFilterId == null,
                    onClick = { selectedProfileFilterId = null },
                    label = { Text("Overall transaction", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Emerald500,
                        selectedLabelColor = Surface950
                    )
                )
                allProfiles.forEach { profile ->
                    FilterChip(
                        selected = selectedProfileFilterId == profile.id,
                        onClick = { selectedProfileFilterId = profile.id },
                        label = { Text("List for ${profile.name}", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Emerald500,
                            selectedLabelColor = Surface950
                        )
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        if (transactionItems.isEmpty()) {
            // ── Empty State ──
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📋", fontSize = 48.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "No transactions found",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        "Your transactions will appear here",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            // ── Grouped Transaction List ──
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                groupedByDate.forEach { (dateLabel, items) ->
                    item {
                        Text(
                            dateLabel,
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        )
                    }
                    items(items, key = { it.hashCode() }) { item ->
                        Box(modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null)) {
                            when (item) {
                                is TransactionItem.Single -> {
                                    val swipedBy = allProfiles.find { it.id == item.expense.profileId }?.name ?: "Unknown"
                                    TransactionRow(expense = item.expense, swipedBy = swipedBy)
                                }
                                is TransactionItem.InstallmentGroup -> {
                                    val swipedBy = allProfiles.find { it.id == item.expenses.first().profileId }?.name ?: "Unknown"
                                    GroupedInstallmentRow(group = item, swipedBy = swipedBy)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Single Transaction Row ──

@Composable
private fun TransactionRow(expense: ExpenseEntity, swipedBy: String) {
    val now = System.currentTimeMillis()
    val daysLeft = ((expense.dueDate - now) / (24 * 60 * 60 * 1000L)).toInt()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 3.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (expense.isPaid) Surface800.copy(alpha = 0.5f) else Surface800
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(Surface700, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(expense.category.emoji, fontSize = 20.sp)
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    expense.merchantName,
                    color = if (expense.isPaid) Color.White.copy(0.5f) else Color.White,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        expense.category.displayName,
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 11.sp
                    )
                    Spacer(Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .background(Surface700.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            "Swiped by: $swipedBy",
                            color = Color.White.copy(0.6f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "₱%,.2f".format(expense.monthlyAmortization),
                    color = if (expense.isPaid) Color.White.copy(0.4f) else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                if (expense.isPaid) {
                    Text("✓ Paid", color = GreenSuccess, fontSize = 10.sp)
                } else if (daysLeft < 0) {
                    Text("Overdue", color = RedAlert, fontSize = 10.sp)
                } else {
                    Text("Due in $daysLeft d", color = Color.White.copy(0.4f), fontSize = 10.sp)
                }
            }
        }
    }
}

// ── Grouped Installment Row ──

@Composable
private fun GroupedInstallmentRow(group: TransactionItem.InstallmentGroup, swipedBy: String) {
    var expanded by remember { mutableStateOf(false) }
    val firstExpense = group.expenses.first()
    
    // Total original purchase amount
    val totalAmount = group.expenses.sumOf { it.monthlyAmortization }
    val paidCount = group.expenses.count { it.isPaid }
    val isFullyPaid = paidCount == group.expenses.size

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 3.dp)
            .animateContentSize(animationSpec = tween(300)),
        colors = CardDefaults.cardColors(
            containerColor = if (isFullyPaid) Surface800.copy(alpha = 0.5f) else Surface800
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            // Master Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(Surface700, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(firstExpense.category.emoji, fontSize = 20.sp)
                }

                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        group.merchantName,
                        color = if (isFullyPaid) Color.White.copy(0.5f) else Color.White,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            firstExpense.category.displayName,
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 11.sp
                        )
                        Spacer(Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(Emerald500.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                "Installment ($paidCount/${group.expenses.size})",
                                color = Emerald400,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .background(Surface700.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                "Swiped by: $swipedBy",
                                color = Color.White.copy(0.6f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "₱%,.2f".format(totalAmount),
                        color = if (isFullyPaid) Color.White.copy(0.4f) else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand",
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Dropdown Items
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(animationSpec = tween(300)),
                exit = shrinkVertically(animationSpec = tween(300))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Surface900.copy(alpha = 0.5f))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    val now = System.currentTimeMillis()
                    
                    group.expenses.forEach { expense ->
                        val daysLeft = ((expense.dueDate - now) / (24 * 60 * 60 * 1000L)).toInt()
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "${expense.currentInstallmentMonth}/${expense.totalInstallmentMonths}",
                                    color = Color.White.copy(0.7f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.width(36.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    sdf.format(Date(expense.dueDate)),
                                    color = Color.White.copy(0.5f),
                                    fontSize = 12.sp
                                )
                            }
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (expense.isPaid) {
                                    Text("Paid", color = GreenSuccess, fontSize = 11.sp, modifier = Modifier.padding(end = 12.dp))
                                } else if (daysLeft < 0) {
                                    Text("Overdue", color = RedAlert, fontSize = 11.sp, modifier = Modifier.padding(end = 12.dp))
                                }
                                
                                Text(
                                    "₱%,.2f".format(expense.monthlyAmortization),
                                    color = if (expense.isPaid) Color.White.copy(0.4f) else Color.White,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
