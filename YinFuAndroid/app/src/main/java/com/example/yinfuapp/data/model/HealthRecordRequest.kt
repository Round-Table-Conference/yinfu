package com.example.yinfuapp.data.model

data class HealthRecordRequest(
    val recordType: String,
    val value: String,
    val unit: String,
    val remark: String? = null
)