package com.example.credittrackph.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
            .background(appBackgroundColor())
    ) {
        // ── Header ──
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            Text(
                "CREDITTRACK PH",
                color = appPrimaryColor(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
            Text(
                "Transactions",
                color = appTextColor(),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "${transactionItems.size} transaction group${if (transactionItems.size != 1) "s" else ""}",
                color = appTextSubColor(),
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
                        selectedContainerColor = appAccentColor(),
                        selectedLabelColor = appOnAccentColor()
                    )
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // ── Filter Dropdown (Profile) ──
        if (allProfiles.isNotEmpty()) {
            var profileDropdownExpanded by remember { mutableStateOf(false) }
            val selectedProfileName = remember(selectedProfileFilterId, allProfiles) {
                if (selectedProfileFilterId == null) {
                    "Overall transaction"
                } else {
                    val p = allProfiles.find { it.id == selectedProfileFilterId }
                    if (p != null) "List for ${p.name}" else "Overall transaction"
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { profileDropdownExpanded = true },
                    color = appCardColor(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, appBorderColor())
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("👤", fontSize = 14.sp)
                            Text(
                                selectedProfileName,
                                color = appTextColor(),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(
                            if (profileDropdownExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                            contentDescription = "Select Profile",
                            tint = appPrimaryColor()
                        )
                    }
                }

                DropdownMenu(
                    expanded = profileDropdownExpanded,
                    onDismissRequest = { profileDropdownExpanded = false },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .background(appCardColor())
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Overall transaction",
                                color = if (selectedProfileFilterId == null) appPrimaryColor() else appTextColor(),
                                fontWeight = if (selectedProfileFilterId == null) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            selectedProfileFilterId = null
                            profileDropdownExpanded = false
                        }
                    )
                    allProfiles.forEach { profile ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "List for ${profile.name}",
                                    color = if (selectedProfileFilterId == profile.id) appPrimaryColor() else appTextColor(),
                                    fontWeight = if (selectedProfileFilterId == profile.id) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                selectedProfileFilterId = profile.id
                                profileDropdownExpanded = false
                            }
                        )
                    }
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
                        color = appTextColor(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        "Your transactions will appear here",
                        color = appTextSubColor(),
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                groupedByDate.forEach { (dateLabel, items) ->
                    item(key = "header_$dateLabel") {
                        Text(
                            dateLabel,
                            color = appTextSubColor(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        )
                    }
                    items(
                        items = items,
                        key = { item ->
                            when (item) {
                                is TransactionItem.Single -> "single_${item.expense.id}"
                                is TransactionItem.InstallmentGroup -> "group_${item.merchantName}_${item.purchaseDate}"
                            }
                        }
                    ) { item ->
                        when (item) {
                            is TransactionItem.Single -> {
                                val swipedBy = allProfiles.find { it.id == item.expense.profileId }?.name ?: "Unknown"
                                val dismissState = rememberSwipeToDismissBoxState(
                                    positionalThreshold = { totalDistance -> totalDistance * 0.25f },
                                    confirmValueChange = { dismissValue ->
                                        when (dismissValue) {
                                            SwipeToDismissBoxValue.StartToEnd -> {
                                                if (item.expense.isPaid) {
                                                    expenseViewModel.markAsUnpaid(item.expense.id)
                                                } else {
                                                    expenseViewModel.markAsPaid(item.expense.id)
                                                }
                                                false
                                            }
                                            SwipeToDismissBoxValue.EndToStart -> {
                                                expenseViewModel.deleteExpense(item.expense)
                                                true
                                            }
                                            else -> false
                                        }
                                    }
                                )
                                SwipeToDismissBox(
                                    state = dismissState,
                                    modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null),
                                    backgroundContent = {
                                        val direction = dismissState.dismissDirection
                                        val bgColor by animateColorAsState(
                                            when {
                                                direction == SwipeToDismissBoxValue.StartToEnd -> appSuccessColor().copy(alpha = 0.85f)
                                                direction == SwipeToDismissBoxValue.EndToStart -> RedAlert.copy(alpha = 0.85f)
                                                else -> Color.Transparent
                                            },
                                            label = "swipeBg"
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(horizontal = 16.dp, vertical = 3.dp)
                                                .background(bgColor, RoundedCornerShape(12.dp)),
                                            contentAlignment = if (direction == SwipeToDismissBoxValue.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd
                                        ) {
                                            if (direction == SwipeToDismissBoxValue.StartToEnd) {
                                                Row(
                                                    modifier = Modifier.padding(start = 20.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(22.dp))
                                                    Text(if (item.expense.isPaid) "Unmark Paid" else "Mark Paid", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                }
                                            } else {
                                                Row(
                                                    modifier = Modifier.padding(end = 20.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                    Icon(Icons.Default.Delete, null, tint = Color.White, modifier = Modifier.size(22.dp))
                                                }
                                            }
                                        }
                                    }
                                ) {
                                    TransactionRow(
                                        expense = item.expense,
                                        swipedBy = swipedBy,
                                        onTogglePaid = {
                                            if (item.expense.isPaid) {
                                                expenseViewModel.markAsUnpaid(item.expense.id)
                                            } else {
                                                expenseViewModel.markAsPaid(item.expense.id)
                                            }
                                        }
                                    )
                                }
                            }
                            is TransactionItem.InstallmentGroup -> {
                                val swipedBy = allProfiles.find { it.id == item.expenses.first().profileId }?.name ?: "Unknown"
                                val nextUnpaid = item.expenses.firstOrNull { !it.isPaid }
                                val groupDismissState = rememberSwipeToDismissBoxState(
                                    positionalThreshold = { totalDistance -> totalDistance * 0.25f },
                                    confirmValueChange = { dismissValue ->
                                        when (dismissValue) {
                                            SwipeToDismissBoxValue.StartToEnd -> {
                                                if (nextUnpaid != null) {
                                                    expenseViewModel.markAsPaid(nextUnpaid.id)
                                                } else {
                                                    item.expenses.lastOrNull()?.let { expenseViewModel.markAsUnpaid(it.id) }
                                                }
                                                false
                                            }
                                            SwipeToDismissBoxValue.EndToStart -> {
                                                expenseViewModel.deleteInstallmentGroup(item.merchantName, item.purchaseDate)
                                                true
                                            }
                                            else -> false
                                        }
                                    }
                                )
                                SwipeToDismissBox(
                                    state = groupDismissState,
                                    modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null),
                                    backgroundContent = {
                                        val direction = groupDismissState.dismissDirection
                                        val bgColor by animateColorAsState(
                                            when {
                                                direction == SwipeToDismissBoxValue.StartToEnd -> appSuccessColor().copy(alpha = 0.85f)
                                                direction == SwipeToDismissBoxValue.EndToStart -> RedAlert.copy(alpha = 0.85f)
                                                else -> Color.Transparent
                                            },
                                            label = "swipeGroupBg"
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(horizontal = 16.dp, vertical = 3.dp)
                                                .background(bgColor, RoundedCornerShape(12.dp)),
                                            contentAlignment = if (direction == SwipeToDismissBoxValue.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd
                                        ) {
                                            if (direction == SwipeToDismissBoxValue.StartToEnd) {
                                                Row(
                                                    modifier = Modifier.padding(start = 20.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(22.dp))
                                                    Text(
                                                        if (nextUnpaid != null) "Pay Month ${nextUnpaid.currentInstallmentMonth}/${nextUnpaid.totalInstallmentMonths}" else "Undo Last Month",
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp
                                                    )
                                                }
                                            } else {
                                                Row(
                                                    modifier = Modifier.padding(end = 20.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text("Delete All", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                    Icon(Icons.Default.Delete, null, tint = Color.White, modifier = Modifier.size(22.dp))
                                                }
                                            }
                                        }
                                    }
                                ) {
                                    GroupedInstallmentRow(
                                        group = item,
                                        swipedBy = swipedBy,
                                        onPayAll = {
                                            expenseViewModel.payOffInstallmentGroup(item.merchantName, item.purchaseDate)
                                        },
                                        onDeleteGroup = {
                                            expenseViewModel.deleteInstallmentGroup(item.merchantName, item.purchaseDate)
                                        },
                                        onToggleExpensePaid = { id, isPaid ->
                                            if (isPaid) {
                                                expenseViewModel.markAsUnpaid(id)
                                            } else {
                                                expenseViewModel.markAsPaid(id)
                                            }
                                        }
                                    )
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
private fun TransactionRow(
    expense: ExpenseEntity,
    swipedBy: String,
    onTogglePaid: () -> Unit = {}
) {
    val now = System.currentTimeMillis()
    val daysLeft = ((expense.dueDate - now) / (24 * 60 * 60 * 1000L)).toInt()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 3.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (expense.isPaid) appCardColor().copy(alpha = 0.5f) else appCardColor()
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
                    .background(appSurfaceColor(), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(expense.category.emoji, fontSize = 20.sp)
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    expense.merchantName,
                    color = if (expense.isPaid) appTextColor().copy(0.5f) else appTextColor(),
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
                        expense.category.displayName,
                        color = appTextSubColor(),
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Box(
                        modifier = Modifier
                            .background(appSurfaceColor(), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Text(
                            "Swiped by: $swipedBy",
                            color = appTextSubColor(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            softWrap = false
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "₱%,.2f".format(expense.monthlyAmortization),
                    color = if (expense.isPaid) appTextColor().copy(0.4f) else appTextColor(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(2.dp))
                Surface(
                    color = if (expense.isPaid) appSoftSuccessColor() else if (daysLeft < 0) RedAlert.copy(alpha = 0.15f) else appSurfaceColor(),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.clickable { onTogglePaid() }
                ) {
                    Text(
                        text = if (expense.isPaid) "✓ Paid (undo)" else if (daysLeft < 0) "Overdue • Pay" else "Due in $daysLeft d • Pay",
                        color = if (expense.isPaid) appSuccessColor() else if (daysLeft < 0) RedAlert else appTextSubColor(),
                        fontSize = 10.sp,
                        fontWeight = if (expense.isPaid) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

// ── Grouped Installment Row ──

@Composable
private fun GroupedInstallmentRow(
    group: TransactionItem.InstallmentGroup,
    swipedBy: String,
    onPayAll: () -> Unit = {},
    onDeleteGroup: () -> Unit = {},
    onToggleExpensePaid: (Int, Boolean) -> Unit = { _, _ -> }
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    val firstExpense = group.expenses.first()

    // Total original purchase amount
    val totalAmount = group.expenses.sumOf { it.monthlyAmortization }
    val paidCount = group.expenses.count { it.isPaid }
    val remainingCount = group.expenses.size - paidCount
    val isFullyPaid = paidCount == group.expenses.size

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            containerColor = appCardColor(),
            title = {
                Text("Delete Installment?", color = appTextColor(), fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Are you sure you want to delete all payments for ${group.merchantName}? This will completely remove all ${group.expenses.size} monthly installments.",
                    color = appTextSubColor(),
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteGroup()
                    }
                ) {
                    Text("Delete", color = RedAlert, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel", color = appTextSubColor())
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 3.dp)
            .animateContentSize(animationSpec = tween(300)),
        colors = CardDefaults.cardColors(
            containerColor = if (isFullyPaid) appCardColor().copy(alpha = 0.5f) else appCardColor()
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
                        .background(appSurfaceColor(), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(firstExpense.category.emoji, fontSize = 20.sp)
                }

                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        group.merchantName,
                        color = if (isFullyPaid) appTextColor().copy(0.5f) else appTextColor(),
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
                            firstExpense.category.displayName,
                            color = appTextSubColor(),
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Box(
                            modifier = Modifier
                                .background(appSoftSuccessColor(), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                "Installment ($paidCount/${group.expenses.size})",
                                color = appPrimaryColor(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                    Spacer(Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .background(appSurfaceColor(), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Text(
                            "Swiped by: $swipedBy",
                            color = appTextSubColor(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            softWrap = false
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "₱%,.2f".format(totalAmount),
                        color = if (isFullyPaid) appTextColor().copy(0.4f) else appTextColor(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand",
                        tint = appTextSubColor(),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Dropdown Items & Lifecycle Actions
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(animationSpec = tween(300)),
                exit = shrinkVertically(animationSpec = tween(300))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(appSurfaceColor().copy(alpha = 0.5f))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
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
                                    color = appTextColor().copy(0.7f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.width(36.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    sdf.format(Date(expense.dueDate)),
                                    color = appTextSubColor(),
                                    fontSize = 12.sp
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = if (expense.isPaid) appSoftSuccessColor() else if (daysLeft < 0) RedAlert.copy(alpha = 0.15f) else appCardColor(),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier
                                        .padding(end = 10.dp)
                                        .clickable { onToggleExpensePaid(expense.id, expense.isPaid) }
                                ) {
                                    Text(
                                        text = if (expense.isPaid) "✓ Paid" else if (daysLeft < 0) "Overdue" else "Due in $daysLeft d",
                                        color = if (expense.isPaid) appSuccessColor() else if (daysLeft < 0) RedAlert else appTextSubColor(),
                                        fontSize = 10.sp,
                                        fontWeight = if (expense.isPaid) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Text(
                                    "₱%,.2f".format(expense.monthlyAmortization),
                                    color = if (expense.isPaid) appTextColor().copy(0.4f) else appTextColor(),
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider(color = appSurfaceColor())
                    Spacer(Modifier.height(8.dp))

                    // ── Installment Lifecycle Actions (Pay Off Early & Delete) ──
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!isFullyPaid) {
                            Button(
                                onClick = onPayAll,
                                colors = ButtonDefaults.buttonColors(containerColor = appAccentColor()),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    "✓ Pay Off All ($remainingCount mos)",
                                    color = appOnAccentColor(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Text(
                                "🎉 Fully Paid Early!",
                                color = appSuccessColor(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        TextButton(
                            onClick = { showDeleteConfirmDialog = true },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Installment", tint = RedAlert, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Delete Group", color = RedAlert, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

