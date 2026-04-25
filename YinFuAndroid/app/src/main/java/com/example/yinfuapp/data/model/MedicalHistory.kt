package com.example.yinfuapp.data.model

data class MedicalHistory(
    val id: Long,
    val userId: Long,
    val diseaseName: String,
    val diagnosisDate: String?,
    val currentStatus: String?,
    val description: String?,
    val createdAt: String?
)