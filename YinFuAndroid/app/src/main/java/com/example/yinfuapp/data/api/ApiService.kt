package com.example.yinfuapp.data.api

import com.example.yinfuapp.data.model.*
import retrofit2.http.*

interface ApiService {

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): ResultResponse

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @GET("api/auth/info")
    suspend fun getUserInfo(): ResultResponse

    @GET("api/user/profile")
    suspend fun getProfile(): ResultResponse

    @PUT("api/user/profile")
    suspend fun updateProfile(@Body request: UserProfileRequest): ResultResponse

    @GET("api/medication")
    suspend fun getMedications(): ResultResponse

    @GET("api/medication/today")
    suspend fun getTodayMedications(): ResultResponse

    @POST("api/medication")
    suspend fun addMedication(@Body request: MedicationReminderRequest): ResultResponse

    @PUT("api/medication/{id}")
    suspend fun updateMedication(@Path("id") id: Long, @Body request: MedicationReminderRequest): ResultResponse

    @DELETE("api/medication/{id}")
    suspend fun deleteMedication(@Path("id") id: Long): ResultResponse

    @POST("api/medication/{id}/take")
    suspend fun takeMedication(@Path("id") id: Long): ResultResponse

    @DELETE("api/medication/{id}/take")
    suspend fun untakeMedication(@Path("id") id: Long): ResultResponse

    @GET("api/health/record")
    suspend fun getHealthRecord(): ResultResponse

    @POST("api/health/record")
    suspend fun addHealthRecord(@Body request: HealthRecordRequest): ResultResponse

    @GET("api/health/history")
    suspend fun getMedicalHistory(): ResultResponse

    @POST("api/health/history")
    suspend fun addMedicalHistory(@Body request: MedicalHistoryRequest): ResultResponse

    @GET("api/emergency-contact")
    suspend fun getEmergencyContacts(): ResultResponse

    @POST("api/emergency-contact")
    suspend fun addEmergencyContact(@Body request: EmergencyContactRequest): ResultResponse

    @PUT("api/emergency-contact/{id}")
    suspend fun updateEmergencyContact(@Path("id") id: Long, @Body request: EmergencyContactRequest): ResultResponse

    @DELETE("api/emergency-contact/{id}")
    suspend fun deleteEmergencyContact(@Path("id") id: Long): ResultResponse

    @POST("api/sos/trigger")
    suspend fun triggerSos(@Body request: SosRequest): ResultResponse

    @GET("api/sos/records")
    suspend fun getSosRecords(): ResultResponse

    @POST("api/ai/chat")
    suspend fun aiChat(@Body request: AiChatRequest): ResultResponse

    @POST("api/ai/diagnosis")
    suspend fun aiDiagnosis(@Body request: AiDiagnosisRequest): ResultResponse
}