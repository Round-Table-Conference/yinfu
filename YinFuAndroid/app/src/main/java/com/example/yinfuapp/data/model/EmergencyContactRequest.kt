package com.example.yinfuapp.data.model

data class EmergencyContactRequest(
    val name: String,
    val phone: String,
    val relationship: String? = null,
    val isPrimary: Int? = 0
)