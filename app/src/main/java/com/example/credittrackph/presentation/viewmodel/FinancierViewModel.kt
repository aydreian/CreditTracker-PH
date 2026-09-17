package com.example.credittrackph.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.credittrackph.BuildConfig
import com.example.credittrackph.data.db.dao.CardDao
import com.example.credittrackph.data.db.dao.ExpenseDao
import com.example.credittrackph.data.db.dao.ProfileDao
import com.example.credittrackph.data.db.entity.ExpenseEntity
import com.example.credittrackph.data.model.ExpenseCategory
import com.example.credittrackph.data.model.inferExpenseCategory
import com.example.credittrackph.data.network.GroqApiService
import com.example.credittrackph.data.network.GroqMessage
import com.example.credittrackph.data.network.GroqRequest
import com.example.credittrackph.data.network.GroqResponse
import com.example.credittrackph.domain.calculator.InstallmentCalculator
import com.example.credittrackph.util.PreferencesManager
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.File
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

@HiltViewModel
class FinancierViewModel @Inject constructor(
    private val groqApiService: GroqApiService,
    private val expenseDao: ExpenseDao,
    private val cardDao: CardDao,
    private val profileDao: ProfileDao,
    private val installmentCalculator: InstallmentCalculator,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _chatState = MutableStateFlow<List<GroqMessage>>(emptyList())
    val chatState: StateFlow<List<GroqMessage>> = _chatState

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isTranscribing = MutableStateFlow(false)
    val isTranscribing: StateFlow<Boolean> = _isTranscribing

    // Tracks the most recent action executed by Financier for instant undo/reversal
    private var lastAction: JsonObject? = null

    private val systemPrompt = GroqMessage(
        role = "system",
        content = "You are 'Financier', a strict but helpful bulldog AI assistant for CreditTrack PH. " +
                "You are 'paldo' (slang for very rich), use bulldog puns ('Woof!', 'Ruff!'), and protect the user from credit card debt. " +
                "CREATOR WATERMARK: You were created with pride by aydreian (github.com/aydreian) for Filipino credit card holders. " +
                "If anyone asks who made or built you, proudly mention your creator aydreian!\n\n" +
                "CATEGORIES: FOOD, TRANSPORT, SHOPPING, UTILITIES, HEALTH, ENTERTAINMENT, TRAVEL, EDUCATION, GROCERIES, ONLINE, OTHER.\n" +
                "When creating installments or tracking spending, always infer the most accurate category from the merchant (e.g. Jollibee -> FOOD, Shell -> TRANSPORT, Uniqlo -> SHOPPING, Meralco -> UTILITIES, Puregold -> GROCERIES).\n\n" +
                "ACTIONS: If the user asks to perform an action, append an exact JSON block wrapped in <ACTION>...</ACTION>.\n" +
                "Supported actions:\n" +
                "1. Mark an expense paid: <ACTION>{\"type\": \"MARK_PAID\", \"expenseId\": 123}</ACTION>\n" +
                "2. Mark an expense unpaid / undo accidental pay: <ACTION>{\"type\": \"UNMARK_PAID\", \"expenseId\": 123}</ACTION>\n" +
                "3. Pay off whole installment group early: <ACTION>{\"type\": \"MARK_INSTALLMENT_GROUP_PAID\", \"merchantName\": \"Merchant\", \"purchaseDate\": 123456789}</ACTION>\n" +
                "4. Delete whole installment group: <ACTION>{\"type\": \"DELETE_INSTALLMENT_GROUP\", \"merchantName\": \"Merchant\", \"purchaseDate\": 123456789}</ACTION>\n" +
                "5. Undo last action: <ACTION>{\"type\": \"UNDO_LAST\"}</ACTION>\n" +
                "6. Create installment: <ACTION>{\"type\": \"CREATE_INSTALLMENT\", \"cardId\": 1, \"profileId\": 1, \"category\": \"SHOPPING\", \"merchantName\": \"Merchant\", \"amount\": 1000.0, \"months\": 12, \"interestRate\": 0.0}</ACTION>\n\n" +
                "RULES FOR profileId:\n" +
                "- If the user specifies who swiped (e.g., 'for John', 'Mom bought', etc.), match with the Profiles list and set 'profileId' to that person's ID.\n" +
                "- If no person is specified, set 'profileId' to the Main User's profile ID.\n" +
                "Do not use markdown inside <ACTION>. Exact JSON only."
    )

    init {
        _chatState.value = listOf(systemPrompt)
    }

    suspend fun getSavedApiKey(): String {
        return preferencesManager.getGroqApiKey() ?: ""
    }

    fun saveApiKey(newKey: String) {
        viewModelScope.launch {
            preferencesManager.setGroqApiKey(newKey.trim())
            _chatState.value = _chatState.value + GroqMessage(
                "assistant",
                "🐶 *Happy tail wag!* Groq API Key saved successfully. What can I calculate or record for you, paldo?"
            )
        }
    }

    private suspend fun getEffectiveApiKey(): String? {
        val customKey = preferencesManager.getGroqApiKey()
        if (!customKey.isNullOrBlank()) return customKey
        val buildKey = BuildConfig.GROQ_API_KEY
        if (buildKey.isNotBlank() && buildKey != "YOUR_GROQ_API_KEY_HERE") return buildKey
        return null
    }

    fun sendMessage(userMessageText: String) {
        val userMsg = GroqMessage(role = "user", content = userMessageText)
        val updatedHistory = _chatState.value + userMsg
        _chatState.value = updatedHistory

        viewModelScope.launch {
            try {
                val expenses = expenseDao.getAllExpensesSync()
                val cards = cardDao.getAllCardsSync()
                val profiles = profileDao.getAllProfilesSync()
                val mainUser = profileDao.getMainUserSync()

                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val profileStr = profiles.joinToString("\n") { p ->
                    "- [ID:${p.id}] ${p.name} ${if (p.isMainUser) "(Main User)" else ""}"
                }
                val cardStr = cards.joinToString("\n") { "- [ID:${it.id}] ${it.bank.name} ending in ${it.lastFourDigits}" }
                val expenseStr = expenses.joinToString(separator = "\n") {
                    "- [ID:${it.id}] ${it.merchantName}: ₱${it.amount} (Due: ${dateFormat.format(Date(it.dueDate))}, Paid: ${it.isPaid}, PurchaseDate: ${it.purchaseDate}, IsInstallment: ${it.isInstallment}, Profile ID: ${it.profileId ?: "None"})"
                }

                val lastActionInfo = if (lastAction != null) {
                    "Last Executed Action: ${lastAction.toString()}\n(If the user says 'undo', 'wrong item', or asks to revert, use <ACTION>{\"type\": \"UNDO_LAST\"}</ACTION>)"
                } else {
                    "Last Executed Action: None"
                }

                val sysMsg = GroqMessage(
                    role = "system",
                    content = "Current App Context:\nProfiles:\n$profileStr\nDefault Main User Profile ID: ${mainUser?.id ?: 1}\n\nCards:\n$cardStr\n\nPending Expenses:\n$expenseStr\n\n$lastActionInfo\nUse this data to answer questions or formulate <ACTION> JSON blocks."
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
                val apiKey = getEffectiveApiKey()
                if (apiKey.isNullOrBlank()) {
                    _chatState.value = _chatState.value + GroqMessage(
                        "assistant",
                        "🐶 *Bark!* No Groq API Key found. Tap the 🔑 key icon in the top right to configure your Groq API key and activate Financier!"
                    )
                    return@launch
                }

                val response = callGroqWithFallback(apiKey, messages)
                val aiReply = response.choices.firstOrNull()?.message
                if (aiReply != null) {
                    val rawContent = aiReply.content
                    val (cleanContent, actionJson) = extractAction(rawContent)

                    if (actionJson != null) {
                        executeAction(actionJson)
                    }

                    _chatState.value = _chatState.value + GroqMessage("assistant", cleanContent)
                }
            } catch (e: HttpException) {
                e.printStackTrace()
                val msg = when (e.code()) {
                    401 -> "🐶 *Sad whimper* Groq returned 401 Unauthorized (Invalid API Key). Tap the 🔑 key icon above to enter a valid key."
                    429 -> "🐶 *Panting* Groq rate limit reached! Please wait a moment before trying again."
                    else -> "🐶 *Sad whimper* Groq server returned error code ${e.code()}. Please check your connection or key."
                }
                _chatState.value = _chatState.value + GroqMessage("assistant", msg)
            } catch (e: UnknownHostException) {
                _chatState.value = _chatState.value + GroqMessage(
                    "assistant",
                    "🐶 *Sad whimper* No internet connection detected. Please verify your phone is connected to Wi-Fi or cellular data."
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _chatState.value = _chatState.value + GroqMessage(
                    "assistant",
                    "🐶 *Sad whimper* Could not reach Financier (${e.message ?: "Unknown error"}). Tap the 🔑 icon to verify your API key."
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun callGroqWithFallback(apiKey: String, messages: List<GroqMessage>): GroqResponse {
        val primaryModel = "openai/gpt-oss-20b"
        val fallbackModel = "qwen/qwen3.8-27b"

        return try {
            val primaryReq = GroqRequest(
                model = primaryModel,
                messages = messages,
                temperature = 0.7f,
                maxTokens = 2048
            )
            groqApiService.chatCompletion("Bearer $apiKey", primaryReq)
        } catch (e: Exception) {
            if (e is HttpException && e.code() == 401) {
                throw e
            }
            val fallbackReq = GroqRequest(
                model = fallbackModel,
                messages = messages,
                temperature = 0.7f,
                maxTokens = 2048
            )
            groqApiService.chatCompletion("Bearer $apiKey", fallbackReq)
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
                        lastAction = JsonObject().apply {
                            addProperty("type", "MARK_PAID")
                            addProperty("expenseId", expenseId)
                        }
                        expenseDao.markAsPaid(expenseId)
                    }
                }
                "UNMARK_PAID" -> {
                    val expenseId = action.get("expenseId")?.asInt
                    if (expenseId != null) {
                        lastAction = JsonObject().apply {
                            addProperty("type", "UNMARK_PAID")
                            addProperty("expenseId", expenseId)
                        }
                        expenseDao.markAsUnpaid(expenseId)
                    }
                }
                "MARK_INSTALLMENT_GROUP_PAID" -> {
                    val merchant = action.get("merchantName")?.asString ?: return
                    val purchaseDate = action.get("purchaseDate")?.asLong ?: return
                    lastAction = JsonObject().apply {
                        addProperty("type", "MARK_INSTALLMENT_GROUP_PAID")
                        addProperty("merchantName", merchant)
                        addProperty("purchaseDate", purchaseDate)
                    }
                    expenseDao.markInstallmentGroupAsPaid(purchaseDate, merchant)
                }
                "DELETE_INSTALLMENT_GROUP" -> {
                    val merchant = action.get("merchantName")?.asString ?: return
                    val purchaseDate = action.get("purchaseDate")?.asLong ?: return
                    lastAction = JsonObject().apply {
                        addProperty("type", "DELETE_INSTALLMENT_GROUP")
                        addProperty("merchantName", merchant)
                        addProperty("purchaseDate", purchaseDate)
                    }
                    expenseDao.deleteInstallmentGroup(purchaseDate, merchant)
                }
                "UNDO_LAST" -> {
                    val prev = lastAction
                    if (prev != null) {
                        when (prev.get("type")?.asString) {
                            "MARK_PAID" -> {
                                val id = prev.get("expenseId")?.asInt
                                if (id != null) expenseDao.markAsUnpaid(id)
                            }
                            "UNMARK_PAID" -> {
                                val id = prev.get("expenseId")?.asInt
                                if (id != null) expenseDao.markAsPaid(id)
                            }
                        }
                        lastAction = null
                    }
                }
                "CREATE_INSTALLMENT" -> {
                    val cardId = action.get("cardId")?.asInt ?: return
                    val merchant = action.get("merchantName")?.asString ?: "Installment"
                    val amount = action.get("amount")?.asDouble ?: 0.0
                    val months = action.get("months")?.asInt ?: 1
                    val rate = action.get("interestRate")?.asDouble ?: 0.0

                    val categoryStr = action.get("category")?.asString?.uppercase()
                    val category = try {
                        if (categoryStr != null) ExpenseCategory.valueOf(categoryStr) else inferExpenseCategory(merchant)
                    } catch (e: Exception) {
                        inferExpenseCategory(merchant)
                    }

                    val parsedProfileId = action.get("profileId")?.takeIf { !it.isJsonNull }?.asInt
                    val mainProfile = profileDao.getMainUserSync()
                    val targetProfileId = parsedProfileId ?: mainProfile?.id

                    val card = cardDao.getAllCardsSync().find { it.id == cardId } ?: return
                    val purchaseDate = System.currentTimeMillis()

                    val result = if (rate > 0.0) {
                        installmentCalculator.calculateWithInterest(amount, months, card.bank)
                    } else {
                        installmentCalculator.calculateZeroInterest(amount, months)
                    }

                    val expenses = result.schedule.map { payment ->
                        val dueCal = Calendar.getInstance()
                        dueCal.timeInMillis = purchaseDate
                        dueCal.add(Calendar.MONTH, payment.month)
                        dueCal.set(Calendar.DAY_OF_MONTH, card.dueDay)

                        ExpenseEntity(
                            cardId = cardId,
                            profileId = targetProfileId,
                            merchantName = merchant,
                            category = category,
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

    fun transcribeAudio(audioFile: File, onResult: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isTranscribing.value = true
            try {
                val apiKey = getEffectiveApiKey()
                if (apiKey.isNullOrBlank()) {
                    onError("No Groq API key found. Tap 🔑 to configure your Groq key.")
                    return@launch
                }

                val requestFile = audioFile.asRequestBody("audio/m4a".toMediaTypeOrNull())
                val filePart = MultipartBody.Part.createFormData("file", audioFile.name, requestFile)
                val modelPart = "whisper-large-v3-turbo".toRequestBody("text/plain".toMediaTypeOrNull())
                val promptPart = "CreditTrack PH, Philippine finance, credit card, installment, mark as done, mark as paid, mark as unpaid, BDO, BPI, Metrobank, UnionBank, RCBC, Security Bank, Jollibee, Shopee, Lazada, Grab, undo, delete installment, swiped, amortize.".toRequestBody("text/plain".toMediaTypeOrNull())
                val responseFormatPart = "json".toRequestBody("text/plain".toMediaTypeOrNull())
                val temperaturePart = "0.0".toRequestBody("text/plain".toMediaTypeOrNull())

                val response = groqApiService.transcribeAudio(
                    authorization = "Bearer $apiKey",
                    file = filePart,
                    model = modelPart,
                    prompt = promptPart,
                    temperature = temperaturePart,
                    responseFormat = responseFormatPart
                )

                var text = response.text.trim()
                if (text.contains("asdan", ignoreCase = true)) {
                    text = text.replace(Regex("(?i)\\basdan\\b"), "as done")
                }
                if (text.isNotBlank()) {
                    onResult(text)
                } else {
                    onError("No speech recognized. Please speak clearly while holding.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                val message = when (e) {
                    is UnknownHostException -> "No internet connection for Whisper AI."
                    is HttpException -> "Whisper error (${e.code()}): ${e.message()}"
                    else -> e.localizedMessage ?: "Voice transcription failed"
                }
                onError(message)
            } finally {
                _isTranscribing.value = false
            }
        }
    }
}
