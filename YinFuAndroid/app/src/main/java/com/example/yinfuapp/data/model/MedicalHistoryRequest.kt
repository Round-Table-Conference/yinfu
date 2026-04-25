package com.example.yinfuapp.data.model

data class MedicalHistoryRequest(
    val diseaseName: String,
    val diagnosisDate: String? = null,
    val currentStatus: String? = null,
    val description: String? = null
)