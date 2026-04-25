package com.example.yinfuapp.data.model

data class MedicationReminderRequest(
    val name: String,
    val dosage: String,
    val time: String,
    val frequency: String,
    val remark: String? = null,
    val isActive: Int? = 1
)