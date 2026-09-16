package com.example.credittrackph.domain.calculator

import com.example.credittrackph.data.model.Bank
import com.example.credittrackph.data.model.InterestType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class InstallmentCalculatorTest {

    private lateinit var calculator: InstallmentCalculator

    @Before
    fun setUp() {
        calculator = InstallmentCalculator()
    }

    @Test
    fun calculateZeroInterest_dividesPrincipalEqually() {
        val principal = 12000.0
        val months = 12
        val result = calculator.calculateZeroInterest(principal, months)

        assertEquals(1000.0, result.monthlyAmortization, 0.01)
        assertEquals(0.0, result.totalInterest, 0.01)
        assertEquals(12000.0, result.totalAmount, 0.01)
        assertEquals(InterestType.ZERO_PERCENT, result.interestType)
        assertEquals(12, result.schedule.size)
        assertEquals(1000.0, result.schedule[0].amount, 0.01)
    }

    @Test
    fun calculateWithInterest_bdoRatesCalculatesPMT() {
        val principal = 10000.0
        val months = 6
        val result = calculator.calculateWithInterest(principal, months, Bank.BDO)

        assertTrue(result.monthlyAmortization > (principal / months))
        assertTrue(result.totalInterest > 0)
        assertEquals(InterestType.WITH_INTEREST, result.interestType)
        assertEquals(Bank.BDO.monthlyInterestRate, result.interestRateMonthly, 0.0001)
        assertEquals(6, result.schedule.size)
    }

    @Test
    fun calculateBoth_returnsZeroAndWithInterest() {
        val (zero, withInterest) = calculator.calculateBoth(10000.0, 6, Bank.BPI)

        assertEquals(InterestType.ZERO_PERCENT, zero.interestType)
        assertEquals(InterestType.WITH_INTEREST, withInterest.interestType)
        assertTrue(withInterest.totalAmount > zero.totalAmount)
    }
}
