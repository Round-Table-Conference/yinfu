package com.example.yinfuapp.data.model

data class LoginResponse(
    val code: Int,
    val message: String,
    val data: LoginData?
)

data class LoginData(
    val token: String,
    val user: User
)

data class User(
    val id: Long,
    val username: String,
    val phone: String?,
    val email: String?,
    val realName: String?,
    val age: Int?,
    val gender: String?,
    val bloodType: String?,
    val allergy: String?,
    val avatarUrl: String?
)