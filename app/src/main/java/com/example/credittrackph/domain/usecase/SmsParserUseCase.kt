package com.example.credittrackph.domain.usecase

import android.util.Log
import com.example.credittrackph.data.db.entity.ExpenseEntity
import com.example.credittrackph.data.model.ExpenseCategory
import com.example.credittrackph.data.model.ExpenseSource
import com.example.credittrackph.data.model.InterestType
import com.example.credittrackph.data.model.inferExpenseCategory
import com.example.credittrackph.data.network.GroqApiService
import com.example.credittrackph.data.network.GroqMessage
import com.example.credittrackph.data.network.GroqRequest
import com.example.credittrackph.data.repository.CardRepository
import com.example.credittrackph.data.repository.ExpenseRepository
import com.example.credittrackph.domain.calculator.DueDateCalculator
import com.example.credittrackph.domain.calculator.InstallmentCalculator
import com.example.credittrackph.util.PreferencesManager
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

data class ParsedSmsExpense(
    val merchant: String,
    val amount: Double,
    val cardLast4: String?,
    val date: String?,
    val currency: String = "PHP",
    val isInstallment: Boolean = false,
    val installmentMonths: Int? = null,
    val isZeroInterest: Boolean? = null,
    val interestRateMonthly: Double? = null,
    val rawSms: String = ""
)

@Singleton
class SmsParserUseCase @Inject constructor(
    private val groqApiService: GroqApiService,
    private val cardRepository: CardRepository,
    private val expenseRepository: ExpenseRepository,
    private val installmentCalculator: InstallmentCalculator,
    private val dueDateCalculator: DueDateCalculator,
    private val preferencesManager: PreferencesManager,
    private val notificationHelper: com.example.credittrackph.notification.NotificationHelper
) {
    companion object {
        private const val TAG = "SmsParserUseCase"
        private const val MODEL = "openai/gpt-oss-20b"
    }

    // Pending SMS expenses awaiting user review
    private val _pendingExpenses = MutableStateFlow<List<ParsedSmsExpense>>(emptyList())
    val pendingExpenses: StateFlow<List<ParsedSmsExpense>> = _pendingExpenses.asStateFlow()

    suspend fun parseSms(smsBody: String) {
        // Step 1: Fast local regex parsing for Philippine banks (works 100% offline)
        val localParsed = parseSmsLocally(smsBody)
        if (localParsed != null && localParsed.amount > 0) {
            Log.d(TAG, "Successfully parsed via local PH bank regex: $localParsed")
            addPendingExpense(localParsed)
            notificationHelper.sendNewTransactionNotification(
                merchant = localParsed.merchant,
                amount = localParsed.amount,
                notifId = localParsed.hashCode()
            )
            return
        }

        // Step 2: Fallback to Groq AI LLM if local regex was incomplete
        try {
            val userKey = preferencesManager.getGroqApiKey()
            val apiKey = if (!userKey.isNullOrBlank()) userKey else com.example.credittrackph.BuildConfig.GROQ_API_KEY
            if (apiKey.isBlank()) {
                Log.w(TAG, "Groq API key not set, cannot parse SMS with AI")
                return
            }

            val prompt = buildPrompt(smsBody)
            val request = GroqRequest(
                model = MODEL,
                messages = listOf(
                    GroqMessage(role = "system", content = "You are a Philippine bank SMS parser. Return only valid JSON, no explanation."),
                    GroqMessage(role = "user", content = prompt)
                ),
                temperature = 0.1f,
                maxTokens = 256
            )

            val response = groqApiService.chatCompletion(
                authorization = "Bearer $apiKey",
                request = request
            )

            val content = response.choices.firstOrNull()?.message?.content ?: return
            val parsed = parseGroqResponse(content, smsBody)
            if (parsed != null && parsed.amount > 0) {
                addPendingExpense(parsed)
                notificationHelper.sendNewTransactionNotification(
                    merchant = parsed.merchant,
                    amount = parsed.amount,
                    notifId = parsed.hashCode()
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing SMS: ${e.message}", e)
        }
    }

    /**
     * Local regex extractor specifically tuned for Philippine bank SMS notifications:
     * BDO, BPI, UnionBank, Metrobank, RCBC, Security Bank, EastWest, Maya, GCash.
     */
    private fun parseSmsLocally(sms: String): ParsedSmsExpense? {
        try {
            // Find Amount: PHP 1,500.00 / Php 850 / ₱1,200.50
            val amountRegex = Pattern.compile("""(?:PHP|Php|₱|\bPHP\b)\s*([0-9]{1,3}(?:,[0-9]{3})*(?:\.[0-9]{2})?|[0-9]+(?:\.[0-9]{2})?)""", Pattern.CASE_INSENSITIVE)
            val amountMatcher = amountRegex.matcher(sms)
            val amount = if (amountMatcher.find()) {
                amountMatcher.group(1)?.replace(",", "")?.toDoubleOrNull() ?: 0.0
            } else 0.0

            if (amount <= 0.0) return null

            // Find Card Last 4 digits: ending in 1234, ...1234, card no. **1234
            val cardRegex = Pattern.compile("""(?:ending(?:\s+in)?|\.{2,}|\*{2,}|card\s*(?:no\.?|ending)?)\s*([0-9]{4})\b""", Pattern.CASE_INSENSITIVE)
            val cardMatcher = cardRegex.matcher(sms)
            val cardLast4 = if (cardMatcher.find()) cardMatcher.group(1) else null

            // Find Merchant: at <MERCHANT> on / at <MERCHANT> \d / to <MERCHANT>
            val merchantRegex = Pattern.compile("""(?:at|to|for)\s+([A-Z0-9\s&'\.\-_]{2,30}?)(?:\s+(?:on|dated|\.|\d{1,2}/|\d{1,2}-|\bref\b|$))""", Pattern.CASE_INSENSITIVE)
            val merchantMatcher = merchantRegex.matcher(sms)
            val rawMerchant = if (merchantMatcher.find()) merchantMatcher.group(1)?.trim() else null

            val merchant = rawMerchant?.takeIf { it.isNotBlank() && it.length > 2 } ?: "Card Purchase"

            // Check if installment mentioned
            val isInstallment = sms.contains("installment", ignoreCase = true) || sms.contains("months", ignoreCase = true)

            return ParsedSmsExpense(
                merchant = merchant,
                amount = amount,
                cardLast4 = cardLast4,
                date = null,
                currency = "PHP",
                isInstallment = isInstallment,
                rawSms = sms
            )
        } catch (_: Exception) {
            return null
        }
    }

    private fun buildPrompt(smsBody: String): String = """
        Extract transaction details from this Philippine bank SMS.
        Return JSON only with these exact fields:
        {
          "merchant": "string",
          "amount": number,
          "cardLast4": "string or null",
          "date": "YYYY-MM-DD or null",
          "currency": "PHP",
          "isInstallment": boolean,
          "installmentMonths": number or null,
          "isZeroInterest": boolean or null,
          "interestRateMonthly": number or null
        }
        SMS: "$smsBody"
    """.trimIndent()

    private fun parseGroqResponse(content: String, rawSms: String): ParsedSmsExpense? {
        return try {
            val jsonStart = content.indexOf('{')
            val jsonEnd = content.lastIndexOf('}')
            if (jsonStart == -1 || jsonEnd == -1) return null
            val jsonStr = content.substring(jsonStart, jsonEnd + 1)

            val gson = Gson()
            val obj = gson.fromJson(jsonStr, JsonObject::class.java)

            ParsedSmsExpense(
                merchant = obj.get("merchant")?.asString ?: "Unknown Merchant",
                amount = obj.get("amount")?.asDouble ?: 0.0,
                cardLast4 = obj.get("cardLast4")?.takeIf { !it.isJsonNull }?.asString,
                date = obj.get("date")?.takeIf { !it.isJsonNull }?.asString,
                currency = obj.get("currency")?.asString ?: "PHP",
                isInstallment = obj.get("isInstallment")?.asBoolean ?: false,
                installmentMonths = obj.get("installmentMonths")?.takeIf { !it.isJsonNull }?.asInt,
                isZeroInterest = obj.get("isZeroInterest")?.takeIf { !it.isJsonNull }?.asBoolean,
                interestRateMonthly = obj.get("interestRateMonthly")?.takeIf { !it.isJsonNull }?.asDouble,
                rawSms = rawSms
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse Groq response: ${e.message}")
            null
        }
    }

    private fun addPendingExpense(expense: ParsedSmsExpense) {
        _pendingExpenses.value = _pendingExpenses.value + expense
    }

    fun dismissPendingExpense(expense: ParsedSmsExpense) {
        _pendingExpenses.value = _pendingExpenses.value - expense
    }

    /**
     * Confirms and persists the parsed SMS into Room DB expenses.
     */
    suspend fun confirmPendingExpense(
        parsed: ParsedSmsExpense,
        cardId: Int,
        profileId: Int? = null
    ) {
        val card = cardRepository.getCardById(cardId).firstOrNull() ?: return
        val purchaseDateMs = System.currentTimeMillis()
        val category = inferExpenseCategory(parsed.merchant)
        val dueDate = dueDateCalculator.calculateDueDate(purchaseDateMs, card.billingCutoffDay, card.dueDay)

        val entity = ExpenseEntity(
            cardId = card.id,
            merchantName = parsed.merchant,
            category = category,
            amount = parsed.amount,
            monthlyAmortization = parsed.amount,
            dueDate = dueDate,
            purchaseDate = purchaseDateMs,
            source = ExpenseSource.SMS_AUTO,
            rawSmsText = parsed.rawSms,
            profileId = profileId
        )

        expenseRepository.insertExpense(entity)
        dismissPendingExpense(parsed)
    }

    fun getInterestTypeFromParsed(parsed: ParsedSmsExpense): InterestType = when (parsed.isZeroInterest) {
        true -> InterestType.ZERO_PERCENT
        false -> InterestType.WITH_INTEREST
        null -> InterestType.UNCERTAIN
    }
}
