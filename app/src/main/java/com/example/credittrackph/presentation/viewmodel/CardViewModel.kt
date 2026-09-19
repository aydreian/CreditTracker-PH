package com.example.credittrackph.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.credittrackph.data.db.entity.CardEntity
import com.example.credittrackph.data.model.Bank
import com.example.credittrackph.data.model.CardType
import com.example.credittrackph.data.repository.CardRepository
import com.example.credittrackph.data.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CardViewModel @Inject constructor(
    private val cardRepository: CardRepository,
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    val allCards: StateFlow<List<CardEntity>> = cardRepository.getAllCards()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalOutstanding: StateFlow<Double> = expenseRepository.getTotalOutstanding()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
        .let { flow ->
            MutableStateFlow(0.0).also { mutable ->
                viewModelScope.launch {
                    flow.collect { mutable.value = it ?: 0.0 }
                }
            }
        }

    val totalCreditLimit: StateFlow<Double> = allCards
        .map { cards -> cards.sumOf { it.creditLimit } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val netAvailableCredit: StateFlow<Double> = combine(totalCreditLimit, totalOutstanding) { limit, outstanding ->
        limit - outstanding
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val cardCount: StateFlow<Int> = allCards
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _selectedCard = MutableStateFlow<CardEntity?>(null)
    val selectedCard: StateFlow<CardEntity?> = _selectedCard

    fun selectCard(card: CardEntity) { _selectedCard.value = card }

    // ── Budget Alert: list of cards where this-month spend ≥ 80% of their cap ──
    val cardsOverBudget: StateFlow<List<Pair<CardEntity, Double>>> = combine(
        allCards,
        expenseRepository.getAllExpenses()
    ) { cards, allExpenses ->
        val cal = java.util.Calendar.getInstance()
        val year = cal.get(java.util.Calendar.YEAR)
        val month = cal.get(java.util.Calendar.MONTH)
        cards.filter { it.monthlyBudgetCap > 0.0 }.mapNotNull { card ->
            val spent = allExpenses.filter { exp ->
                val c = java.util.Calendar.getInstance().apply { timeInMillis = exp.purchaseDate }
                exp.cardId == card.id &&
                    c.get(java.util.Calendar.YEAR) == year &&
                    c.get(java.util.Calendar.MONTH) == month
            }.sumOf { it.monthlyAmortization }
            val pct = spent / card.monthlyBudgetCap
            if (pct >= 0.8) Pair(card, pct) else null
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addCard(
        label: String, lastFourDigits: String, cardType: CardType,
        bank: Bank, colorArgb: Long, creditLimit: Double,
        billingCutoffDay: Int, dueDay: Int, monthlyBudgetCap: Double = 0.0
    ) {
        viewModelScope.launch {
            cardRepository.insertCard(
                CardEntity(
                    label = label,
                    lastFourDigits = lastFourDigits,
                    cardType = cardType,
                    bank = bank,
                    cardColorArgb = colorArgb,
                    creditLimit = creditLimit,
                    billingCutoffDay = billingCutoffDay,
                    dueDay = dueDay,
                    monthlyBudgetCap = monthlyBudgetCap
                )
            )
        }
    }

    fun updateCard(card: CardEntity) {
        viewModelScope.launch { cardRepository.updateCard(card) }
    }

    fun deleteCard(card: CardEntity) {
        viewModelScope.launch { cardRepository.deleteCard(card) }
    }
}
