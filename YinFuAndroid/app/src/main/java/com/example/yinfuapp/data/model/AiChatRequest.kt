package com.example.yinfuapp.data.model

data class AiChatRequest(
    val messages: List<Message>
)

data class Message(
    val role: String,
    val content: String
)