package com.example.credittrackph.data.network

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

data class GroqRequest(
    @SerializedName("model") val model: String,
    @SerializedName("messages") val messages: List<GroqMessage>,
    @SerializedName("temperature") val temperature: Float = 0.1f,
    @SerializedName("max_tokens") val maxTokens: Int = 256
)

data class GroqMessage(
    @SerializedName("role") val role: String,
    @SerializedName("content") val content: String
)

data class GroqResponse(
    @SerializedName("choices") val choices: List<GroqChoice>
)

data class GroqChoice(
    @SerializedName("message") val message: GroqMessage
)

data class GroqTranscriptionResponse(
    @SerializedName("text") val text: String
)

interface GroqApiService {
    @POST("openai/v1/chat/completions")
    suspend fun chatCompletion(
        @Header("Authorization") authorization: String,
        @Body request: GroqRequest
    ): GroqResponse

    @Multipart
    @POST("openai/v1/audio/transcriptions")
    suspend fun transcribeAudio(
        @Header("Authorization") authorization: String,
        @Part file: MultipartBody.Part,
        @Part("model") model: RequestBody,
        @Part("prompt") prompt: RequestBody? = null,
        @Part("temperature") temperature: RequestBody? = null,
        @Part("response_format") responseFormat: RequestBody? = null
    ): GroqTranscriptionResponse
}
