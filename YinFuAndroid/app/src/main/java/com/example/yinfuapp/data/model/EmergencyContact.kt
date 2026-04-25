package com.example.yinfuapp.data.model

data class EmergencyContact(
    val id: Long,
    val userId: Long,
    val name: String,
    val phone: String,
    val relationship: String?,
    val isPrimary: Int?,
    val createdAt: String?
)