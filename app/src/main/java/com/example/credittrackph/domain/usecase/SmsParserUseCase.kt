package com.example.credittrackph.domain.usecase

import android.util.Log
import com.example.credittrackph.data.model.InterestType
import com.example.credittrackph.data.network.GroqApiService
import com.example.credittrackph.data.network.GroqMessage
import com.example.credittrackph.data.network.GroqRequest
import com.example.credittrackph.data.repository.CardRepository
import com.example.credittrackph.data.repository.ExpenseRepository
import com.example.credittrackph.domain.calculator.InstallmentCalculator
import com.example.credittrackph.util.PreferencesManager
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.firstOrNull
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
    val interestRateMonthly: Double? = null
)

@Singleton
class SmsParserUseCase @Inject constructor(
    private val groqApiService: GroqApiService,
    private val cardRepository: CardRepository,
    private val expenseRepository: ExpenseRepository,
    private val installmentCalculator: InstallmentCalculator,
    private val preferencesManager: PreferencesManager,
    private val notificationHelper: com.example.credittrackph.notification.NotificationHelper
) {
    companion object {
        private const val TAG = "SmsParserUseCase"
        private const val MODEL = "openai/gpt-oss-20b"
    }

    // Pending SMS expenses awaiting user confirmation
    private val _pendingExpenses = mutableListOf<ParsedSmsExpense>()
    val pendingExpenses: List<ParsedSmsExpense> get() = _pendingExpenses.toList()

    suspend fun parseSms(smsBody: String) {
        try {
            val apiKey = com.example.credittrackph.BuildConfig.GROQ_API_KEY
            if (apiKey.isBlank()) {
                Log.w(TAG, "Groq API key not set in BuildConfig, cannot parse SMS")
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
            val parsed = parseGroqResponse(content)
            if (parsed != null) {
                _pendingExpenses.add(parsed)
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

    private fun parseGroqResponse(content: String): ParsedSmsExpense? {
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
                interestRateMonthly = obj.get("interestRateMonthly")?.takeIf { !it.isJsonNull }?.asDouble
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse Groq response: ${e.message}")
            null
        }
    }

    fun clearPendingExpense(expense: ParsedSmsExpense) {
        _pendingExpenses.remove(expense)
    }

    fun getInterestTypeFromParsed(parsed: ParsedSmsExpense): InterestType = when (parsed.isZeroInterest) {
        true -> InterestType.ZERO_PERCENT
        false -> InterestType.WITH_INTEREST
        null -> InterestType.UNCERTAIN
    }
}
