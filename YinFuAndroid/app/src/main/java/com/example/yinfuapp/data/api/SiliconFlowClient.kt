package com.example.yinfuapp.data.api

import android.util.Base64
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

object SiliconFlowClient {
    
    private const val BASE_URL = "https://api.siliconflow.cn/v1"
    private const val API_KEY = "sk-yckdyvnnixuckmwqkqjpbjltfipybwuviznbkorrzbegzbyf"
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()
    
    private val gson = Gson()
    
    suspend fun speechToText(audioFile: File): Result<String> = withContext(Dispatchers.IO) {
        try {
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    audioFile.name,
                    audioFile.asRequestBody("audio/mpeg".toMediaType())
                )
                .addFormDataPart("model", "TeleAI/TeleSpeechASR")
                .build()
            
            val request = Request.Builder()
                .url("$BASE_URL/audio/transcriptions")
                .addHeader("Authorization", "Bearer $API_KEY")
                .post(requestBody)
                .build()
            
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: return@withContext Result.failure(Exception("Empty response"))
            
            android.util.Log.d("SiliconFlow", "speechToText Response: $responseBody")
            
            val json = gson.fromJson(responseBody, JsonObject::class.java)
            val text = json.get("text")?.asString
            
            if (text != null) {
                Result.success(text)
            } else {
                val error = json.get("error")?.asJsonObject?.get("message")?.asString
                    ?: json.get("message")?.asString
                Result.failure(Exception(error ?: "Transcription failed"))
            }
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
            
            android.util.Log.d("SiliconFlow", "chatWithText Response: $responseBody")
            
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
}