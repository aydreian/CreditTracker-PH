package com.example.credittrackph.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.credittrackph.data.db.entity.CardEntity
import com.example.credittrackph.data.db.entity.ExpenseEntity
import com.example.credittrackph.data.model.*
import com.example.credittrackph.data.repository.CardRepository
import com.example.credittrackph.data.repository.ExpenseRepository
import com.example.credittrackph.domain.calculator.DueDateCalculator
import com.example.credittrackph.domain.calculator.InstallmentCalculator
import com.example.credittrackph.domain.usecase.ParsedSmsExpense
import com.example.credittrackph.domain.usecase.SmsParserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val cardRepository: CardRepository,
    private val installmentCalculator: InstallmentCalculator,
    private val dueDateCalculator: DueDateCalculator,
    private val smsParserUseCase: SmsParserUseCase
) : ViewModel() {

    val pendingSmsExpenses: StateFlow<List<ParsedSmsExpense>> = smsParserUseCase.pendingExpenses

    private val _selectedCardId = MutableStateFlow<Int?>(null)
    val selectedCardId: StateFlow<Int?> = _selectedCardId

    private val _filterYear = MutableStateFlow<String?>(null)
    private val _filterMonth = MutableStateFlow<String?>(null)
    val filterYear: StateFlow<String?> = _filterYear
    val filterMonth: StateFlow<String?> = _filterMonth

    @OptIn(ExperimentalCoroutinesApi::class)
    val expenses: StateFlow<List<ExpenseEntity>> = combine(
        _selectedCardId, _filterYear, _filterMonth
    ) { cardId, year, month -> Triple(cardId, year, month) }
        .flatMapLatest { (cardId, year, month) ->
            if (cardId == null) flowOf(emptyList())
            else if (year != null && month != null)
                expenseRepository.getExpensesByCardAndMonth(cardId, year, month.padStart(2, '0'))
            else
                expenseRepository.getExpensesByCard(cardId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedCardOutstanding: StateFlow<Double> = _selectedCardId
        .flatMapLatest { cardId ->
            if (cardId == null) flowOf(0.0)
            else expenseRepository.getTotalOutstandingByCard(cardId).map { it ?: 0.0 }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val upcomingDues: StateFlow<List<ExpenseEntity>> = run {
        val now = System.currentTimeMillis()
        val sevenDays = now + (7 * 24 * 60 * 60 * 1000L)
        expenseRepository.getUpcomingDueExpenses(now, sevenDays)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    val allExpenses: StateFlow<List<ExpenseEntity>> = expenseRepository.getAllExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ── New StateFlows for Home Screen ──

    val recentTransactions: StateFlow<List<ExpenseEntity>> = allExpenses
        .map { list -> list.sortedByDescending { it.purchaseDate }.take(5) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val thisMonthTotal: StateFlow<Double> = allExpenses
        .map { list ->
            val cal = Calendar.getInstance()
            val currentYear = cal.get(Calendar.YEAR)
            val currentMonth = cal.get(Calendar.MONTH)
            list.filter { expense ->
                val expCal = Calendar.getInstance().apply { timeInMillis = expense.purchaseDate }
                expCal.get(Calendar.YEAR) == currentYear && expCal.get(Calendar.MONTH) == currentMonth
            }.sumOf { it.monthlyAmortization }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val overdueCount: StateFlow<Int> = allExpenses
        .map { list ->
            val now = System.currentTimeMillis()
            list.count { !it.isPaid && it.dueDate < now }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // ── Actions ──

    fun selectCard(cardId: Int) { _selectedCardId.value = cardId }
    fun setFilter(year: String?, month: String?) {
        _filterYear.value = year
        _filterMonth.value = month
    }
    fun clearFilter() { _filterYear.value = null; _filterMonth.value = null }

    fun addManualExpense(
        card: CardEntity,
        merchantName: String,
        category: ExpenseCategory,
        amount: Double,
        purchaseDateMs: Long,
        isInstallment: Boolean,
        installmentMonths: Int,
        interestType: InterestType,
        note: String,
        profileId: Int?
    ) {
        viewModelScope.launch {
            if (!isInstallment) {
                val dueDate = dueDateCalculator.calculateDueDate(
                    purchaseDateMs, card.billingCutoffDay, card.dueDay
                )
                expenseRepository.insertExpense(
                    ExpenseEntity(
                        cardId = card.id,
                        merchantName = merchantName,
                        category = category,
                        amount = amount,
                        purchaseDate = purchaseDateMs,
                        monthlyAmortization = amount,
                        dueDate = dueDate,
                        note = note,
                        source = ExpenseSource.MANUAL,
                        profileId = profileId
                    )
                )
            } else {
                // Split into monthly installment entries
                val calcResult = when (interestType) {
                    InterestType.ZERO_PERCENT -> installmentCalculator.calculateZeroInterest(amount, installmentMonths)
                    InterestType.WITH_INTEREST -> installmentCalculator.calculateWithInterest(amount, installmentMonths, card.bank)
                    InterestType.UNCERTAIN -> installmentCalculator.calculateZeroInterest(amount, installmentMonths) // default to 0%
                }
                val installmentExpenses = (1..installmentMonths).map { month ->
                    val dueDate = dueDateCalculator.calculateInstallmentDueDate(
                        purchaseDateMs, card.billingCutoffDay, card.dueDay, month
                    )
                    ExpenseEntity(
                        cardId = card.id,
                        merchantName = merchantName,
                        category = category,
                        amount = amount,
                        purchaseDate = purchaseDateMs,
                        isInstallment = true,
                        totalInstallmentMonths = installmentMonths,
                        currentInstallmentMonth = month,
                        interestType = interestType,
                        monthlyAmortization = calcResult.monthlyAmortization,
                        totalInterest = calcResult.totalInterest,
                        interestRateMonthly = calcResult.interestRateMonthly,
                        dueDate = dueDate,
                        note = note,
                        source = ExpenseSource.MANUAL,
                        profileId = profileId
                    )
                }
                expenseRepository.insertExpenses(installmentExpenses)
            }
        }
    }

    fun markAsPaid(expenseId: Int) {
        viewModelScope.launch { expenseRepository.markAsPaid(expenseId) }
    }

    fun markAsUnpaid(expenseId: Int) {
        viewModelScope.launch { expenseRepository.markAsUnpaid(expenseId) }
    }

    fun payOffInstallmentGroup(merchantName: String, purchaseDate: Long) {
        viewModelScope.launch { expenseRepository.markInstallmentGroupAsPaid(merchantName, purchaseDate) }
    }

    fun deleteInstallmentGroup(merchantName: String, purchaseDate: Long) {
        viewModelScope.launch { expenseRepository.deleteInstallmentGroup(merchantName, purchaseDate) }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch { expenseRepository.deleteExpense(expense) }
    }

    fun confirmSmsExpense(parsed: ParsedSmsExpense, cardId: Int, profileId: Int? = null) {
        viewModelScope.launch {
            smsParserUseCase.confirmPendingExpense(parsed, cardId, profileId)
        }
    }

    fun dismissSmsExpense(parsed: ParsedSmsExpense) {
        smsParserUseCase.dismissPendingExpense(parsed)
    }
}

