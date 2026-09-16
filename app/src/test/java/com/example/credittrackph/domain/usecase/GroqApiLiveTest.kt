package com.example.credittrackph.domain.usecase

import com.example.credittrackph.data.network.GroqApiService
import com.example.credittrackph.data.network.GroqMessage
import com.example.credittrackph.data.network.GroqRequest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class GroqApiLiveTest {

    private val apiKey = "YOUR_GROQ_API_KEY_HERE"

    @Test
    fun testGroqApi_liveSmsParsing() = runBlocking {
        println("\n==========================================")
        println("🤖 TESTING GROQ AI API WITH SAMPLE PH BANK SMS")
        println("==========================================")

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.groq.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(GroqApiService::class.java)

        val sampleSms = "BDO: Your card ending in 1234 was charged PHP 45000.00 at Power Mac Center on 2026-09-15 for 12 months 0% promo."

        val prompt = """
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
              "isZeroInterest": boolean or null
            }
            SMS: "$sampleSms"
        """.trimIndent()

        val request = GroqRequest(
            model = "openai/gpt-oss-20b",
            messages = listOf(
                GroqMessage(role = "system", content = "You are a Philippine bank SMS parser. Return only valid JSON, no explanation."),
                GroqMessage(role = "user", content = prompt)
            ),
            temperature = 0.1f,
            maxTokens = 256
        )

        try {
            val response = service.chatCompletion("Bearer $apiKey", request)
            val jsonResult = response.choices.firstOrNull()?.message?.content

            println("\n==========================================")
            println("🎉 SUCCESS! GROQ AI RESPONDED PERFECTLY!")
            println("------------------------------------------")
            println("Parsed JSON Output:")
            println(jsonResult)
            println("==========================================\n")

            assertNotNull(jsonResult)
            assertTrue(jsonResult!!.contains("Power Mac Center") || jsonResult.contains("45000"))
        } catch (e: HttpException) {
            val code = e.code()
            val errorBody = e.response()?.errorBody()?.string()
            println("\n❌ GROQ HTTP ERROR $code: $errorBody\n")
            throw e
        } catch (e: Exception) {
            println("\n❌ GROQ API ERROR: ${e.message}\n")
            e.printStackTrace()
            throw e
        }
    }
}
