package com.example.credittrackph.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.example.credittrackph.domain.usecase.SmsParserUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SmsReceiver : BroadcastReceiver() {

    @Inject
    lateinit var smsParserUseCase: SmsParserUseCase

    // Keywords that indicate a bank transaction SMS
    private val bankKeywords = listOf(
        "charged", "debited", "transaction", "purchase", "payment",
        "bdo", "bpi", "rcbc", "metrobank", "unionbank", "security bank",
        "eastwest", "pnb", "chinabank", "citibank", "hsbc", "landbank",
        "php", "₱", "card ending", "card no.", "installment"
    )

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        messages.forEach { smsMessage ->
            val body = smsMessage.messageBody.lowercase()
            val isBankSms = bankKeywords.any { keyword -> keyword in body }

            if (isBankSms) {
                Log.d("SmsReceiver", "Bank SMS detected: ${smsMessage.messageBody}")
                CoroutineScope(Dispatchers.IO).launch {
                    smsParserUseCase.parseSms(smsMessage.messageBody)
                }
            }
        }
    }
}
