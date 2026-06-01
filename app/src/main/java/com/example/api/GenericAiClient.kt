package com.example.api

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GenericMessage(
    val role: String,
    val content: String
)

@JsonClass(generateAdapter = true)
data class GenericChatRequest(
    val model: String,
    val messages: List<GenericMessage>,
    val temperature: Float = 0.5f
)

@JsonClass(generateAdapter = true)
data class GenericChoice(
    val message: GenericMessage
)

@JsonClass(generateAdapter = true)
data class GenericChatResponse(
    val choices: List<GenericChoice>? = null
)

object GenericAiClient {
    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val requestAdapter = moshi.adapter(GenericChatRequest::class.java)
    private val responseAdapter = moshi.adapter(GenericChatResponse::class.java)

    private val client = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .build()

    suspend fun generateChat(
        baseUrl: String,
        apiKey: String,
        model: String,
        systemInstruction: String,
        chatHistory: List<com.example.viewmodel.ChatMessage>,
        latestUserMessage: String
    ): String {
        // Build message list compatible with OpenAI's Chat API
        val messages = mutableListOf<GenericMessage>()
        
        // Add system message if present
        if (systemInstruction.isNotEmpty()) {
            messages.add(GenericMessage("system", systemInstruction))
        }

        // Add history in format
        chatHistory.forEach { msg ->
            val role = if (msg.sender == "assistant") "assistant" else "user"
            messages.add(GenericMessage(role, msg.text))
        }

        // Add final prompt
        messages.add(GenericMessage("user", latestUserMessage))

        val reqObj = GenericChatRequest(
            model = model,
            messages = messages,
            temperature = 0.5f
        )

        val jsonBody = requestAdapter.toJson(reqObj)
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonBody.toRequestBody(mediaType)

        // Make HTTP Request
        val requestBuilder = Request.Builder()
            .url(baseUrl)
            .post(requestBody)

        // Add standard Header Authorization unless Ollama local which doesn't need auth
        if (apiKey.trim().isNotEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer ${apiKey.trim()}")
        }
        
        // OpenRouter requires target origin for rankings (optional but good practice)
        if (baseUrl.contains("openrouter")) {
            requestBuilder.addHeader("HTTP-Referer", "https://google.com")
            requestBuilder.addHeader("X-Title", "Mobile Optimiser App")
        }

        val request = requestBuilder.build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errBody = response.body?.string() ?: ""
                throw IOException("AI API Request Failed: Code ${response.code}. Details: $errBody")
            }

            val respBody = response.body?.string() ?: throw IOException("Empty response body from AI server")
            val chatResponse = responseAdapter.fromJson(respBody)
            val aiReply = chatResponse?.choices?.firstOrNull()?.message?.content
            return aiReply ?: throw IOException("Could not parse choice list from payload response")
        }
    }
}
