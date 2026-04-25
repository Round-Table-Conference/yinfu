package com.example.yinfuapp.data.model

data class MedicationReminder(
    val id: Long = 0,
    val userId: Long = 0,
    val name: String = "",
    val dosage: String = "",
    val time: String = "",
    val frequency: String = "",
    val remark: String? = null,
    val isActive: Int? = 1,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    var isTaken: Int? = 0
)
