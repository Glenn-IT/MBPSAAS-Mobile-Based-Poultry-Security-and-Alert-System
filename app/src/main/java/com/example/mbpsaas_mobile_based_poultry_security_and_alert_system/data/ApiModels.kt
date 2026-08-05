package com.example.mbpsaas_mobile_based_poultry_security_and_alert_system.data

import com.google.gson.annotations.SerializedName

data class User(
    val id: Int,
    val username: String,
    val email: String,
)

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val user: User? = null,
)

data class QuestionsResponse(
    val success: Boolean,
    val message: String,
    val questions: List<String>? = null,
)

data class VerifyAnswerResponse(
    val success: Boolean,
    val message: String,
    @SerializedName("reset_token") val resetToken: String? = null,
)

data class BasicResponse(
    val success: Boolean,
    val message: String,
)

data class SensorZone(
    val id: Int,
    @SerializedName("sensor_code") val sensorCode: String,
    val name: String,
    @SerializedName("is_enabled") val isEnabled: Boolean,
)

data class SensorsResponse(
    val success: Boolean,
    val message: String,
    val sensors: List<SensorZone>? = null,
)

data class MotionEvent(
    val id: Int,
    val zone: String? = null,
    @SerializedName("detected_at") val detectedAt: String,
    @SerializedName("buzzer_triggered") val buzzerTriggered: Boolean,
    val note: String?,
)

data class MotionEventsResponse(
    val success: Boolean,
    val message: String,
    val events: List<MotionEvent>? = null,
)

data class UpdateProfileResponse(
    val success: Boolean,
    val message: String,
    val user: User? = null,
)
