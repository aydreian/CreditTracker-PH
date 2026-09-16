package com.example.credittrackph.domain.usecase

import com.example.credittrackph.data.model.InterestType
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SmsParserUseCaseTest {

    @Test
    fun parseGroqJson_zeroPercentInstallment() {
        val sampleJson = """
            {
              "merchant": "Power Mac Center",
              "amount": 45000.0,
              "cardLast4": "1234",
              "date": "2026-09-15",
              "currency": "PHP",
              "isInstallment": true,
              "installmentMonths": 12,
              "isZeroInterest": true,
              "interestRateMonthly": null
            }
        """.trimIndent()

        val gson = Gson()
        val obj = gson.fromJson(sampleJson, JsonObject::class.java)

        val parsed = ParsedSmsExpense(
            merchant = obj.get("merchant").asString,
            amount = obj.get("amount").asDouble,
            cardLast4 = obj.get("cardLast4").asString,
            date = obj.get("date").asString,
            currency = obj.get("currency").asString,
            isInstallment = obj.get("isInstallment").asBoolean,
            installmentMonths = obj.get("installmentMonths").asInt,
            isZeroInterest = obj.get("isZeroInterest").asBoolean,
            interestRateMonthly = null
        )

        assertEquals("Power Mac Center", parsed.merchant)
        assertEquals(45000.0, parsed.amount, 0.01)
        assertEquals(true, parsed.isInstallment)
        assertEquals(12, parsed.installmentMonths)
        assertEquals(true, parsed.isZeroInterest)
    }

    @Test
    fun parseGroqJson_uncertainInterest() {
        val sampleJson = """
            {
              "merchant": "SM Appliance",
              "amount": 15000.0,
              "cardLast4": "5678",
              "date": "2026-09-16",
              "currency": "PHP",
              "isInstallment": true,
              "installmentMonths": 6,
              "isZeroInterest": null,
              "interestRateMonthly": null
            }
        """.trimIndent()

        val gson = Gson()
        val obj = gson.fromJson(sampleJson, JsonObject::class.java)

        val parsed = ParsedSmsExpense(
            merchant = obj.get("merchant").asString,
            amount = obj.get("amount").asDouble,
            cardLast4 = obj.get("cardLast4").asString,
            date = obj.get("date").asString,
            currency = obj.get("currency").asString,
            isInstallment = obj.get("isInstallment").asBoolean,
            installmentMonths = obj.get("installmentMonths").asInt,
            isZeroInterest = if (obj.get("isZeroInterest").isJsonNull) null else obj.get("isZeroInterest").asBoolean,
            interestRateMonthly = null
        )

        assertEquals("SM Appliance", parsed.merchant)
        assertNull(parsed.isZeroInterest)
    }
}
