package com.example.yinfuapp.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.yinfuapp.data.model.ChatMessage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ChatHistoryManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "chat_history"
        private const val KEY_AI_CHAT = "ai_chat_history"
        private const val KEY_AI_DIAGNOSIS = "ai_diagnosis_history"

        @Volatile
        private var instance: ChatHistoryManager? = null

        fun getInstance(context: Context): ChatHistoryManager {
            return instance ?: synchronized(this) {
                instance ?: ChatHistoryManager(context.applicationContext).also { instance = it }
            }
        }
    }

    fun saveAiChatHistory(messages: List<ChatMessage>) {
        val json = gson.toJson(messages)
        prefs.edit().putString(KEY_AI_CHAT, json).apply()
    }

    fun getAiChatHistory(): List<ChatMessage> {
        val json = prefs.getString(KEY_AI_CHAT, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<ChatMessage>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveAiDiagnosisHistory(messages: List<ChatMessage>) {
        val json = gson.toJson(messages)
        prefs.edit().putString(KEY_AI_DIAGNOSIS, json).apply()
    }

    fun getAiDiagnosisHistory(): List<ChatMessage> {
        val json = prefs.getString(KEY_AI_DIAGNOSIS, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<ChatMessage>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun clearAiChatHistory() {
        prefs.edit().remove(KEY_AI_CHAT).apply()
    }

    fun clearAiDiagnosisHistory() {
        prefs.edit().remove(KEY_AI_DIAGNOSIS).apply()
    }

    fun clearAllHistory() {
        prefs.edit().clear().apply()
    }
}