package com.example.credittrackph.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.credittrackph.data.db.entity.ExpenseEntity
import com.example.credittrackph.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PaymentCalendarView(
    expenses: List<ExpenseEntity>,
    modifier: Modifier = Modifier
) {
    var calendarMonth by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDayOfMonth by remember { mutableStateOf<Int?>(null) }

    val monthFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
    val dayHeaders = listOf("S", "M", "T", "W", "T", "F", "S")

    // Clone calendar to compute days
    val year = calendarMonth.get(Calendar.YEAR)
    val month = calendarMonth.get(Calendar.MONTH)

    val currentCal = Calendar.getInstance()
    val isCurrentMonth = currentCal.get(Calendar.YEAR) == year && currentCal.get(Calendar.MONTH) == month
    val todayDate = if (isCurrentMonth) currentCal.get(Calendar.DAY_OF_MONTH) else -1

    // Compute start day of week & days in month
    val firstDayCal = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month)
        set(Calendar.DAY_OF_MONTH, 1)
    }
    val startDayOfWeek = firstDayCal.get(Calendar.DAY_OF_WEEK) - 1 // 0-indexed (Sunday = 0)
    val daysInMonth = firstDayCal.getActualMaximum(Calendar.DAY_OF_MONTH)

    // Map dayOfMonth -> list of expenses due on that day
    val duesByDay = remember(expenses, year, month) {
        val map = mutableMapOf<Int, MutableList<ExpenseEntity>>()
        expenses.forEach { exp ->
            val dueCal = Calendar.getInstance().apply { timeInMillis = exp.dueDate }
            if (dueCal.get(Calendar.YEAR) == year && dueCal.get(Calendar.MONTH) == month) {
                val day = dueCal.get(Calendar.DAY_OF_MONTH)
                map.getOrPut(day) { mutableListOf() }.add(exp)
            }
        }
        map
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = appCardColor()),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Month Navigation Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        val prev = Calendar.getInstance().apply {
                            timeInMillis = calendarMonth.timeInMillis
                            add(Calendar.MONTH, -1)
                        }
                        calendarMonth = prev
                        selectedDayOfMonth = null
                    }
                ) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous Month", tint = appTextColor())
                }

                Text(
                    text = monthFormat.format(calendarMonth.time),
                    color = appTextColor(),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = {
                        val next = Calendar.getInstance().apply {
                            timeInMillis = calendarMonth.timeInMillis
                            add(Calendar.MONTH, 1)
                        }
                        calendarMonth = next
                        selectedDayOfMonth = null
                    }
                ) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Month", tint = appTextColor())
                }
            }

            Spacer(Modifier.height(8.dp))

            // Day of Week Headers
            Row(modifier = Modifier.fillMaxWidth()) {
                dayHeaders.forEach { header ->
                    Text(
                        text = header,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        color = appTextSubColor(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Calendar Days Grid (6 rows max x 7 columns)
            val totalSlots = ((startDayOfWeek + daysInMonth + 6) / 7) * 7
            for (row in 0 until (totalSlots / 7)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (col in 0..6) {
                        val slotIndex = row * 7 + col
                        val dayNumber = slotIndex - startDayOfWeek + 1
                        val isValidDay = dayNumber in 1..daysInMonth

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .padding(2.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    when {
                                        isValidDay && selectedDayOfMonth == dayNumber -> Emerald500.copy(alpha = 0.25f)
                                        isValidDay && dayNumber == todayDate -> appSurfaceColor()
                                        else -> Color.Transparent
                                    }
                                )
                                .then(
                                    if (isValidDay && dayNumber == todayDate) {
                                        Modifier.border(1.dp, Emerald500, RoundedCornerShape(10.dp))
                                    } else Modifier
                                )
                                .clickable(enabled = isValidDay) {
                                    selectedDayOfMonth = if (selectedDayOfMonth == dayNumber) null else dayNumber
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isValidDay) {
                                val duesOnDay = duesByDay[dayNumber] ?: emptyList()
                                val hasDues = duesOnDay.isNotEmpty()
                                val hasUnpaid = duesOnDay.any { !it.isPaid }

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "$dayNumber",
                                        color = if (selectedDayOfMonth == dayNumber) Emerald400 else appTextColor(),
                                        fontSize = 13.sp,
                                        fontWeight = if (dayNumber == todayDate || selectedDayOfMonth == dayNumber) FontWeight.Bold else FontWeight.Normal
                                    )
                                    if (hasDues) {
                                        Box(
                                            modifier = Modifier
                                                .size(5.dp)
                                                .background(
                                                    if (hasUnpaid) RedAlert else GreenSuccess,
                                                    CircleShape
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = appSurfaceColor().copy(alpha = 0.5f))
            Spacer(Modifier.height(10.dp))

            // Details section for selected day or monthly summary
            val displayedDues = if (selectedDayOfMonth != null) {
                duesByDay[selectedDayOfMonth] ?: emptyList()
            } else {
                duesByDay.values.flatten()
            }

            Text(
                text = if (selectedDayOfMonth != null) "Dues on ${monthFormat.format(calendarMonth.time).split(" ")[0]} $selectedDayOfMonth (${displayedDues.size})"
                       else "All Upcoming Dues for this Month (${displayedDues.size})",
                color = appTextColor(),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(8.dp))

            if (displayedDues.isEmpty()) {
                Text(
                    text = if (selectedDayOfMonth != null) "🎉 No payments due on this date!" else "🎉 No payments due in ${monthFormat.format(calendarMonth.time)}",
                    color = appTextSubColor(),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    displayedDues.forEach { due ->
                        val daysLeft = ((due.dueDate - System.currentTimeMillis()) / (24 * 60 * 60 * 1000L)).toInt()
                        Surface(
                            color = appSurfaceColor(),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(due.category.emoji, fontSize = 18.sp)
                                    Column {
                                        Text(
                                            due.merchantName,
                                            color = appTextColor(),
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 13.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            if (due.isPaid) "✓ Paid" else if (daysLeft < 0) "Overdue by ${-daysLeft}d" else "Due in ${daysLeft}d",
                                            color = if (due.isPaid) GreenSuccess else if (daysLeft < 0) RedAlert else YellowWarn,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                                Text(
                                    "₱%,.2f".format(due.monthlyAmortization),
                                    color = if (due.isPaid) appTextColor().copy(alpha = 0.5f) else appTextColor(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
