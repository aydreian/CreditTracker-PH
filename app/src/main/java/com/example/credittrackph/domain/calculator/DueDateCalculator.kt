package com.example.credittrackph.domain.calculator

import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DueDateCalculator @Inject constructor() {

    /**
     * Calculate the due date for a given purchase.
     * Logic:
     * - If purchaseDate is on or before billingCutoffDay -> due date is dueDay of NEXT month
     * - If purchaseDate is after billingCutoffDay -> due date is dueDay of month after next
     */
    fun calculateDueDate(purchaseDateMs: Long, billingCutoffDay: Int, dueDay: Int): Long {
        val purchase = Calendar.getInstance().apply { timeInMillis = purchaseDateMs }
        val purchaseDayOfMonth = purchase.get(Calendar.DAY_OF_MONTH)

        val dueCalendar = Calendar.getInstance().apply { timeInMillis = purchaseDateMs }
        dueCalendar.set(Calendar.DAY_OF_MONTH, 1) // Reset to 1st to avoid overflow

        // Determine which statement period the purchase falls into
        if (purchaseDayOfMonth <= billingCutoffDay) {
            // Falls in current statement -> due next month
            dueCalendar.add(Calendar.MONTH, 1)
        } else {
            // Falls in next statement -> due month after next
            dueCalendar.add(Calendar.MONTH, 2)
        }

        // Set due day, clamped to last day of that month
        val maxDay = dueCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        dueCalendar.set(Calendar.DAY_OF_MONTH, minOf(dueDay, maxDay))
        dueCalendar.set(Calendar.HOUR_OF_DAY, 23)
        dueCalendar.set(Calendar.MINUTE, 59)
        dueCalendar.set(Calendar.SECOND, 59)

        return dueCalendar.timeInMillis
    }

    /**
     * Calculate due date for a specific installment month
     */
    fun calculateInstallmentDueDate(
        purchaseDateMs: Long,
        billingCutoffDay: Int,
        dueDay: Int,
        installmentMonth: Int // 1-based
    ): Long {
        val firstDueDate = calculateDueDate(purchaseDateMs, billingCutoffDay, dueDay)
        val cal = Calendar.getInstance().apply { timeInMillis = firstDueDate }
        cal.add(Calendar.MONTH, installmentMonth - 1)
        // Clamp to last day of that month
        val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        if (cal.get(Calendar.DAY_OF_MONTH) > maxDay) {
            cal.set(Calendar.DAY_OF_MONTH, maxDay)
        }
        return cal.timeInMillis
    }

    fun isDueSoon(dueDateMs: Long, daysThreshold: Int = 7): Boolean {
        val now = System.currentTimeMillis()
        val threshold = daysThreshold * 24 * 60 * 60 * 1000L
        return dueDateMs in now..(now + threshold)
    }

    fun isOverdue(dueDateMs: Long): Boolean {
        return dueDateMs < System.currentTimeMillis()
    }
}
