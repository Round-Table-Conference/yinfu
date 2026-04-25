package com.example.yinfuapp.data.api

import android.util.Base64
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

object SiliconFlowClient {
    
    private const val BASE_URL = "https://api.siliconflow.cn/v1"
    private const val API_KEY = "sk-your-api-key-here"
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
    
    private val gson = Gson()
    
    data class ChatMessage(
        val role: String,
        val content: List<Map<String, Any>>
    )
    
    suspend fun chatWithAudio(
        audioFile: File,
        systemPrompt: String,
        historyMessages: List<Pair<String, String>>
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val base64Audio = Base64.encodeToString(audioFile.readBytes(), Base64.NO_WRAP)
            val audioUrl = "data:audio/wav;base64,$base64Audio"
            
            val messages = mutableListOf<Map<String, Any>>()
            messages.add(mapOf(
                "role" to "system",
                "content" to listOf(mapOf("type" to "text", "text" to systemPrompt))
            ))
            
            historyMessages.forEach { (role, content) ->
                messages.add(mapOf(
                    "role" to role,
                    "content" to listOf(mapOf("type" to "text", "text" to content))
                ))
            }
            
            messages.add(mapOf(
                "role" to "user",
                "content" to listOf(
                    mapOf("type" to "audio_url", "audio_url" to mapOf("url" to audioUrl)),
                    mapOf("type" to "text", "text" to "请描述这段音频的内容并以文字形式回复")
                )
            ))
            
            val requestBody = JsonObject().apply {
                addProperty("model", "Qwen/Qwen3-Omni-30B-A3B-Instruct")
                add("messages", gson.toJsonTree(messages))
                addProperty("stream", false)
            }.toString()
            
            val request = Request.Builder()
                .url("$BASE_URL/chat/completions")
                .addHeader("Authorization", "Bearer $API_KEY")
                .addHeader("Content-Type", "application/json")
                .post(requestBody.toRequestBody("application/json".toMediaType()))
                .build()
            
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: return@withContext Result.failure(Exception("Empty response"))
            
            val json = gson.fromJson(responseBody, JsonObject::class.java)
            val content = json
                .getAsJsonArray("choices")
                ?.get(0)
                ?.asJsonObject
                ?.get("message")
                ?.asJsonObject
                ?.get("content")
                ?.asString
            
            if (content != null) {
                Result.success(content)
            } else {
                val error = json.get("error")?.asJsonObject?.get("message")?.asString
                Result.failure(Exception(error ?: "No content in response"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun textToSpeech(
        text: String,
        voice: String = "FunAudioLLM/CosyVoice2-0.5B:claire"
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val requestBody = JsonObject().apply {
                addProperty("model", "FunAudioLLM/CosyVoice2-0.5B")
                addProperty("input", text)
                addProperty("voice", voice)
                addProperty("response_format", "mp3")
            }.toString()
            
            val request = Request.Builder()
                .url("$BASE_URL/audio/speech")
                .addHeader("Authorization", "Bearer $API_KEY")
                .addHeader("Content-Type", "application/json")
                .post(requestBody.toRequestBody("application/json".toMediaType()))
                .build()
            
            val response = client.newCall(request).execute()
            val audioBytes = response.body?.bytes() ?: return@withContext Result.failure(Exception("Empty audio"))
            
            val audioFile = File.createTempFile("tts_", ".mp3")
            FileOutputStream(audioFile).use { it.write(audioBytes) }
            
            Result.success(audioFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun chatWithText(
        messages: List<Map<String, Any>>
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val requestBody = JsonObject().apply {
                addProperty("model", "Qwen/Qwen3-Omni-30B-A3B-Instruct")
                add("messages", gson.toJsonTree(messages))
                addProperty("stream", false)
            }.toString()
            
            val request = Request.Builder()
                .url("$BASE_URL/chat/completions")
                .addHeader("Authorization", "Bearer $API_KEY")
                .addHeader("Content-Type", "application/json")
                .post(requestBody.toRequestBody("application/json".toMediaType()))
                .build()
            
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: return@withContext Result.failure(Exception("Empty response"))
            
            val json = gson.fromJson(responseBody, JsonObject::class.java)
            val content = json
                .getAsJsonArray("choices")
                ?.get(0)
                ?.asJsonObject
                ?.get("message")
                ?.asJsonObject
                ?.get("content")
                ?.asString
            
            if (content != null) {
                Result.success(content)
            } else {
                val error = json.get("error")?.asJsonObject?.get("message")?.asString
                Result.failure(Exception(error ?: "No content in response"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}