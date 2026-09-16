package com.example.credittrackph.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.credittrackph.BuildConfig
import com.example.credittrackph.data.db.dao.ExpenseDao
import com.example.credittrackph.data.network.GroqApiService
import com.example.credittrackph.data.network.GroqMessage
import com.example.credittrackph.data.network.GroqRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.credittrackph.data.db.dao.CardDao
import com.example.credittrackph.domain.calculator.InstallmentCalculator
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.example.credittrackph.data.model.ExpenseCategory
import com.example.credittrackph.data.db.entity.ExpenseEntity

@HiltViewModel
class FinancierViewModel @Inject constructor(
    private val groqApiService: GroqApiService,
    private val expenseDao: ExpenseDao,
    private val cardDao: CardDao,
    private val installmentCalculator: InstallmentCalculator
) : ViewModel() {

    private val _chatState = MutableStateFlow<List<GroqMessage>>(emptyList())
    val chatState: StateFlow<List<GroqMessage>> = _chatState

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val systemPrompt = GroqMessage(
        role = "system",
        content = "You are 'Financier', a strict but helpful bulldog AI assistant for a finance app called CreditTrack PH. " +
                "You are 'paldo' (slang for very rich) and use bulldog puns ('Woof!'). " +
                "Your goal is to summarize spending and give financial advice. " +
                "IMPORTANT: If the user asks you to perform an action (like marking a bill as paid, or creating a new installment), you MUST execute it by appending a JSON block wrapped exactly in <ACTION>...</ACTION>. " +
                "Supported JSON formats:\n" +
                "<ACTION>{\"type\": \"MARK_PAID\", \"expenseId\": 123}</ACTION>\n" +
                "<ACTION>{\"type\": \"CREATE_INSTALLMENT\", \"cardId\": 1, \"merchantName\": \"Merchant\", \"amount\": 1000.0, \"months\": 12, \"interestRate\": 0.0}</ACTION>\n" +
                "Do not use markdown inside the <ACTION> block. You must use the exact JSON format, not plain text."
    )

    init {
        // Start the conversation with the system prompt
        _chatState.value = listOf(systemPrompt)
    }

    fun sendMessage(userMessageText: String) {
        val userMsg = GroqMessage(role = "user", content = userMessageText)
        val updatedHistory = _chatState.value + userMsg
        _chatState.value = updatedHistory
        
        viewModelScope.launch {
            try {
                val expenses = expenseDao.getAllExpensesSync()
                val cards = cardDao.getAllCardsSync()
                
                val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                val expenseStr = expenses.joinToString(separator = "\n") { 
                    "- [ID:${it.id}] ${it.merchantName}: ₱${it.amount} (Due: ${dateFormat.format(java.util.Date(it.dueDate))}, Paid: ${it.isPaid})" 
                }
                val cardStr = cards.joinToString(separator = "\n") { "- [ID:${it.id}] ${it.bank.name} ending in ${it.lastFourDigits}" }
                
                val sysMsg = GroqMessage(
                    role = "system",
                    content = "System Context:\nCards:\n$cardStr\n\nPending Expenses:\n$expenseStr\nUse this data to answer questions or formulate <ACTION> JSON blocks."
                )
                
                val messagesToSend = updatedHistory.toMutableList()
                if (messagesToSend.isNotEmpty()) {
                    messagesToSend.add(1, sysMsg)
                }
                
                fetchAiResponse(messagesToSend)
            } catch (e: Exception) {
                e.printStackTrace()
                fetchAiResponse(updatedHistory)
            }
        }
    }
    
    fun generateSummary() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val expenses = expenseDao.getAllExpensesSync()
                val totalSpend = expenses.sumOf { it.monthlyAmortization }
                
                val contextMsg = "Here is the user's current data: Total Tracked Spend is ₱${totalSpend}. " +
                        "There are ${expenses.size} total transactions. " +
                        "Give them a quick summary and some strict bulldog advice."
                        
                val promptMsg = GroqMessage(role = "user", content = contextMsg)
                val updatedHistory = _chatState.value + promptMsg
                
                // We don't add contextMsg to the visible chat state right away, 
                // just send it to the AI.
                fetchAiResponse(updatedHistory)
                
            } catch (e: Exception) {
                e.printStackTrace()
                _isLoading.value = false
            }
        }
    }

    private fun fetchAiResponse(messages: List<GroqMessage>) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val request = GroqRequest(
                    model = "mixtral-8x7b-32768",
                    messages = messages,
                    temperature = 0.7f,
                    maxTokens = 2048
                )
                
                val response = groqApiService.chatCompletion(
                    authorization = "Bearer ${BuildConfig.GROQ_API_KEY}",
                    request = request
                )
                
                val aiReply = response.choices.firstOrNull()?.message
                if (aiReply != null) {
                    val rawContent = aiReply.content
                    val (cleanContent, actionJson) = extractAction(rawContent)
                    
                    if (actionJson != null) {
                        executeAction(actionJson)
                    }
                    
                    _chatState.value = _chatState.value + GroqMessage("assistant", cleanContent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _chatState.value = _chatState.value + GroqMessage("assistant", "*Sad whimper* I couldn't connect to the server right now.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun extractAction(content: String): Pair<String, String?> {
        val startIndex = content.indexOf("<ACTION>")
        val endIndex = content.indexOf("</ACTION>")
        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            val json = content.substring(startIndex + 8, endIndex).trim()
            val cleanMsgBefore = content.substring(0, startIndex).trim()
            val cleanMsgAfter = content.substring(endIndex + 9).trim()
            val cleanMsg = listOf(cleanMsgBefore, cleanMsgAfter).filter { it.isNotEmpty() }.joinToString("\n\n")
            return Pair(cleanMsg.ifEmpty { "Action processed successfully! Ruff!" }, json)
        }
        return Pair(content, null)
    }

    private suspend fun executeAction(jsonString: String) {
        try {
            val gson = Gson()
            val action = gson.fromJson(jsonString, JsonObject::class.java)
            val type = action.get("type")?.asString
            
            when (type) {
                "MARK_PAID" -> {
                    val expenseId = action.get("expenseId")?.asInt
                    if (expenseId != null) {
                        expenseDao.markAsPaid(expenseId)
                    }
                }
                "CREATE_INSTALLMENT" -> {
                    val cardId = action.get("cardId")?.asInt ?: return
                    val merchant = action.get("merchantName")?.asString ?: "Installment"
                    val amount = action.get("amount")?.asDouble ?: 0.0
                    val months = action.get("months")?.asInt ?: 1
                    val rate = action.get("interestRate")?.asDouble ?: 0.0
                    
                    val card = cardDao.getAllCardsSync().find { it.id == cardId } ?: return
                    val purchaseDate = System.currentTimeMillis()
                    
                    val result = if (rate > 0.0) {
                        installmentCalculator.calculateWithInterest(amount, months, card.bank)
                    } else {
                        installmentCalculator.calculateZeroInterest(amount, months)
                    }
                    
                    val expenses = result.schedule.map { payment ->
                        val dueCal = java.util.Calendar.getInstance()
                        dueCal.timeInMillis = purchaseDate
                        dueCal.add(java.util.Calendar.MONTH, payment.month)
                        dueCal.set(java.util.Calendar.DAY_OF_MONTH, card.dueDay)
                        
                        ExpenseEntity(
                            cardId = cardId,
                            merchantName = merchant,
                            category = ExpenseCategory.OTHER,
                            amount = amount,
                            purchaseDate = purchaseDate,
                            isInstallment = true,
                            totalInstallmentMonths = months,
                            currentInstallmentMonth = payment.month,
                            interestType = result.interestType,
                            monthlyAmortization = payment.amount,
                            totalInterest = payment.interest,
                            interestRateMonthly = result.interestRateMonthly,
                            dueDate = dueCal.timeInMillis
                        )
                    }
                    expenseDao.insertExpenses(expenses)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
