package com.example.credittrackph.domain.calculator

import com.example.credittrackph.data.model.Bank
import com.example.credittrackph.data.model.InterestType
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow

data class InstallmentResult(
    val monthlyAmortization: Double,
    val totalInterest: Double,
    val totalAmount: Double,
    val interestRateMonthly: Double,
    val interestType: InterestType,
    val schedule: List<MonthlyPayment>
)

data class MonthlyPayment(
    val month: Int,
    val amount: Double,
    val principal: Double,
    val interest: Double,
    val remainingBalance: Double
)

@Singleton
class InstallmentCalculator @Inject constructor() {

    /**
     * Mode A: 0% interest (promo installment)
     * Simply divides principal by number of months.
     */
    fun calculateZeroInterest(principal: Double, months: Int): InstallmentResult {
        val monthly = principal / months
        val schedule = (1..months).map { month ->
            MonthlyPayment(
                month = month,
                amount = monthly,
                principal = monthly,
                interest = 0.0,
                remainingBalance = principal - (monthly * month)
            )
        }
        return InstallmentResult(
            monthlyAmortization = monthly,
            totalInterest = 0.0,
            totalAmount = principal,
            interestRateMonthly = 0.0,
            interestType = InterestType.ZERO_PERCENT,
            schedule = schedule
        )
    }

    /**
     * Mode B: With bank interest (PMT formula)
     * PMT = P * [r(1+r)^n] / [(1+r)^n - 1]
     */
    fun calculateWithInterest(principal: Double, months: Int, bank: Bank): InstallmentResult {
        val r = bank.monthlyInterestRate
        val n = months.toDouble()
        val pmt = if (r == 0.0) {
            principal / months
        } else {
            principal * (r * (1 + r).pow(n)) / ((1 + r).pow(n) - 1)
        }

        var balance = principal
        val schedule = (1..months).map { month ->
            val interestPayment = balance * r
            val principalPayment = pmt - interestPayment
            balance -= principalPayment
            MonthlyPayment(
                month = month,
                amount = pmt,
                principal = principalPayment,
                interest = interestPayment,
                remainingBalance = maxOf(0.0, balance)
            )
        }

        return InstallmentResult(
            monthlyAmortization = pmt,
            totalInterest = (pmt * months) - principal,
            totalAmount = pmt * months,
            interestRateMonthly = r,
            interestType = InterestType.WITH_INTEREST,
            schedule = schedule
        )
    }

    /**
     * Mode C: AI uncertain — return both calculations for user to confirm
     */
    fun calculateBoth(principal: Double, months: Int, bank: Bank): Pair<InstallmentResult, InstallmentResult> {
        return Pair(
            calculateZeroInterest(principal, months),
            calculateWithInterest(principal, months, bank)
        )
    }
}
