package com.example.credittrackph.presentation.screen

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.credittrackph.data.db.entity.CardEntity
import com.example.credittrackph.data.db.entity.ExpenseEntity
import com.example.credittrackph.data.model.InterestType
import com.example.credittrackph.presentation.components.CreditCardView
import com.example.credittrackph.presentation.viewmodel.ExpenseViewModel
import com.example.credittrackph.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDetailScreen(
    card: CardEntity,
    onBack: () -> Unit,
    onAddExpense: () -> Unit,
    onEditCard: (CardEntity) -> Unit = {},
    viewModel: ExpenseViewModel = hiltViewModel()
) {
    LaunchedEffect(card.id) { viewModel.selectCard(card.id) }

    val expenses by viewModel.expenses.collectAsState()
    val outstanding by viewModel.selectedCardOutstanding.collectAsState()
    val filterYear by viewModel.filterYear.collectAsState()
    val filterMonth by viewModel.filterMonth.collectAsState()

    val months = listOf("01","02","03","04","05","06","07","08","09","10","11","12")
    val monthNames = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val years = (currentYear - 1..currentYear + 1).map { it.toString() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        card.label,
                        color = appTextColor(),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = appTextColor()) }
                },
                actions = {
                    IconButton(onClick = { onEditCard(card) }) {
                        Icon(Icons.Default.Edit, "Edit Card", tint = appPrimaryColor())
                    }
                    IconButton(onClick = onAddExpense) {
                        Icon(Icons.Default.Add, "Add Expense", tint = appPrimaryColor())
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = appCardColor())
            )
        },
        containerColor = appBackgroundColor()
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 60.dp)
        ) {
            // Card visual
            item {
                Box(Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                    CreditCardView(card = card, totalOutstanding = outstanding)
                }
            }

            // Outstanding summary
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    colors = CardDefaults.cardColors(containerColor = appCardColor()),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Outstanding Balance", color = appTextSubColor(), fontSize = 12.sp)
                            Text("₱${String.format("%,.2f", outstanding)}", color = RedAlert, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Due Day", color = appTextSubColor(), fontSize = 12.sp)
                            Text("Day ${card.dueDay}", color = appTextColor(), fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // Month/Year Filter
            item {
                Spacer(Modifier.height(16.dp))
                Text("Filter by Period", color = appTextSubColor(), fontSize = 12.sp, modifier = Modifier.padding(horizontal = 20.dp))
                Spacer(Modifier.height(8.dp))
                // Year chips
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp).horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = filterYear == null,
                        onClick = { viewModel.clearFilter() },
                        label = { Text("All") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = appAccentColor(), selectedLabelColor = if (LocalIsDarkTheme.current) Surface950 else Color.White)
                    )
                    years.forEach { year ->
                        FilterChip(
                            selected = filterYear == year,
                            onClick = { viewModel.setFilter(year, filterMonth ?: months[Calendar.getInstance().get(Calendar.MONTH)]) },
                            label = { Text(year) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = appAccentColor(), selectedLabelColor = if (LocalIsDarkTheme.current) Surface950 else Color.White)
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                // Month chips (only if year selected)
                if (filterYear != null) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp).horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        monthNames.forEachIndexed { index, name ->
                            FilterChip(
                                selected = filterMonth == months[index],
                                onClick = { viewModel.setFilter(filterYear, months[index]) },
                                label = { Text(name, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = appAccentColor(), selectedLabelColor = if (LocalIsDarkTheme.current) Surface950 else Color.White)
                            )
                        }
                    }
                }
            }

            // Expenses header
            item {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Expenses (${expenses.size})", color = appTextColor(), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Sorted by due date ↑", color = appTextSubColor(), fontSize = 11.sp)
                }
                Spacer(Modifier.height(8.dp))
            }

            if (expenses.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📊", fontSize = 48.sp)
                            Spacer(Modifier.height(12.dp))
                            Text("No expenses yet", color = appTextColor(), fontWeight = FontWeight.Bold)
                            Text("Tap + above to add an expense", color = appTextSubColor(), fontSize = 13.sp)
                        }
                    }
                }
            } else {
                items(expenses) { expense ->
                    ExpenseRow(expense = expense, onMarkPaid = { viewModel.markAsPaid(expense.id) }, onDelete = { viewModel.deleteExpense(expense) })
                }
            }
        }
    }
}

@Composable
fun ExpenseRow(expense: ExpenseEntity, onMarkPaid: () -> Unit, onDelete: () -> Unit) {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val now = System.currentTimeMillis()
    val daysLeft = ((expense.dueDate - now) / (24 * 60 * 60 * 1000L)).toInt()
    val statusColor = when {
        expense.isPaid -> appSuccessColor()
        daysLeft < 0 -> RedAlert
        daysLeft <= 3 -> YellowWarn
        else -> appTextColor().copy(0.8f)
    }
    var showMenu by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = if (expense.isPaid) appCardColor().copy(alpha = 0.5f) else appCardColor()),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { if (expense.isInstallment) isExpanded = !isExpanded }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category emoji
                Text(expense.category.emoji, fontSize = 24.sp, modifier = Modifier.padding(end = 12.dp))

                // Main info
                Column(Modifier.weight(1f)) {
                    Text(
                        expense.merchantName,
                        color = if (expense.isPaid) appTextColor().copy(0.5f) else appTextColor(),
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text(
                        expense.category.displayName,
                        color = appTextSubColor(),
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    if (expense.isInstallment) {
                        val interestLabel = when (expense.interestType) {
                            InterestType.ZERO_PERCENT -> "0% Promo"
                            InterestType.WITH_INTEREST -> "${String.format("%.1f", expense.interestRateMonthly * 100)}%/mo"
                            InterestType.UNCERTAIN -> "?%"
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Installment ${expense.currentInstallmentMonth}/${expense.totalInstallmentMonths} • $interestLabel",
                                color = appPrimaryColor(), fontSize = 10.sp, fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Expand details",
                                tint = appPrimaryColor(),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Text("Due: ${sdf.format(Date(expense.dueDate))}", color = statusColor, fontSize = 11.sp)
                }

                // Amount + action
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "₱${String.format("%,.2f", expense.monthlyAmortization)}",
                        color = if (expense.isPaid) appTextColor().copy(0.4f) else statusColor,
                        fontWeight = FontWeight.Bold, fontSize = 15.sp
                    )
                    if (!expense.isPaid) {
                        Text(
                            if (daysLeft < 0) "Overdue" else if (daysLeft == 0) "Today" else "$daysLeft days",
                            color = statusColor, fontSize = 10.sp
                        )
                    } else {
                        Text("✓ Paid", color = appSuccessColor(), fontSize = 10.sp)
                    }

                    Box {
                        IconButton(onClick = { showMenu = true }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.MoreVert, null, tint = appTextSubColor(), modifier = Modifier.size(16.dp))
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, modifier = Modifier.background(appCardColor())) {
                            if (!expense.isPaid) {
                                DropdownMenuItem(
                                    text = { Text("✓ Mark as Paid", color = appSuccessColor()) },
                                    onClick = { onMarkPaid(); showMenu = false }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("🗑️ Delete", color = RedAlert) },
                                onClick = { onDelete(); showMenu = false }
                            )
                        }
                    }
                }
            }

            // Dropdown Sub-element for Installment Details
            AnimatedVisibility(visible = isExpanded && expense.isInstallment) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(appSurfaceColor())
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Installment Details & Schedule",
                        color = appTextColor(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Spacer(Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Original Total Purchase:", color = appTextSubColor(), fontSize = 11.sp)
                        Text("₱%,.2f".format(expense.amount), color = appTextColor(), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Monthly Amortization:", color = appTextSubColor(), fontSize = 11.sp)
                        Text("₱%,.2f / mo".format(expense.monthlyAmortization), color = appPrimaryColor(), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Interest Cost:", color = appTextSubColor(), fontSize = 11.sp)
                        Text("₱%,.2f".format(expense.totalInterest), color = if (expense.totalInterest > 0) RedAlert else appSuccessColor(), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(8.dp))
                    Text("Payment Schedule Progress:", color = appTextSubColor(), fontSize = 11.sp)
                    Spacer(Modifier.height(4.dp))

                    (1..expense.totalInstallmentMonths).forEach { m ->
                        val isCurrent = m == expense.currentInstallmentMonth
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                                .background(if (isCurrent) appSoftSuccessColor() else Color.Transparent, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Month $m of ${expense.totalInstallmentMonths}${if (isCurrent) " (Current)" else ""}",
                                color = if (isCurrent) appPrimaryColor() else appTextSubColor(),
                                fontSize = 11.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                            )
                            Text(
                                text = "₱%,.2f".format(expense.monthlyAmortization),
                                color = appTextColor(),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
