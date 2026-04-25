package com.example.yinfuapp.data.model

data class UserProfileRequest(
    val realName: String? = null,
    val age: Int? = null,
    val gender: String? = null,
    val bloodType: String? = null,
    val allergy: String? = null,
    val phone: String? = null,
    val email: String? = null
)