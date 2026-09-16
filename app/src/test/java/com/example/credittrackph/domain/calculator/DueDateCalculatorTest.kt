package com.example.credittrackph.domain.calculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar

class DueDateCalculatorTest {

    private lateinit var calculator: DueDateCalculator

    @Before
    fun setUp() {
        calculator = DueDateCalculator()
    }

    @Test
    fun calculateDueDate_beforeCutoff_dueNextMonth() {
        // Jan 15 purchase, cutoff 20, due 10
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.JANUARY, 15, 10, 0, 0)
        }
        val dueDateMs = calculator.calculateDueDate(cal.timeInMillis, billingCutoffDay = 20, dueDay = 10)

        val dueCal = Calendar.getInstance().apply { timeInMillis = dueDateMs }
        assertEquals(Calendar.FEBRUARY, dueCal.get(Calendar.MONTH))
        assertEquals(10, dueCal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun calculateDueDate_afterCutoff_dueMonthAfterNext() {
        // Jan 25 purchase, cutoff 20, due 10
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.JANUARY, 25, 10, 0, 0)
        }
        val dueDateMs = calculator.calculateDueDate(cal.timeInMillis, billingCutoffDay = 20, dueDay = 10)

        val dueCal = Calendar.getInstance().apply { timeInMillis = dueDateMs }
        assertEquals(Calendar.MARCH, dueCal.get(Calendar.MONTH))
        assertEquals(10, dueCal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun calculateInstallmentDueDate_incrementsMonthsCorrectly() {
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.JANUARY, 10, 10, 0, 0)
        }
        // First due date: Feb 10, Second: Mar 10, Third: Apr 10
        val month3DueDateMs = calculator.calculateInstallmentDueDate(
            purchaseDateMs = cal.timeInMillis,
            billingCutoffDay = 20,
            dueDay = 10,
            installmentMonth = 3
        )

        val dueCal = Calendar.getInstance().apply { timeInMillis = month3DueDateMs }
        assertEquals(Calendar.APRIL, dueCal.get(Calendar.MONTH))
        assertEquals(10, dueCal.get(Calendar.DAY_OF_MONTH))
    }
}
