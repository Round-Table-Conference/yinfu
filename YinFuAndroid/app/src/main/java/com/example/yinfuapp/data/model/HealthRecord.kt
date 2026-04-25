package com.example.yinfuapp.data.model

data class HealthRecord(
    val id: Long,
    val userId: Long,
    val recordType: String,
    val value: String,
    val unit: String,
    val measuredAt: String?,
    val remark: String?,
    val createdAt: String?
)

data class HealthProfile(
    val bloodPressure: HealthRecord?,
    val heartRate: HealthRecord?,
    val bloodSugar: HealthRecord?,
    val temperature: HealthRecord?
)