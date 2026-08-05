package com.example.mbpsaas_mobile_based_poultry_security_and_alert_system.data

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @FormUrlEncoded
    @POST("login.php")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String,
    ): LoginResponse

    @FormUrlEncoded
    @POST("check_user.php")
    suspend fun checkUser(
        @Field("username") username: String,
    ): BasicResponse

    @GET("get_questions.php")
    suspend fun getQuestions(): QuestionsResponse

    @FormUrlEncoded
    @POST("verify_security_answer.php")
    suspend fun verifySecurityAnswer(
        @Field("username") username: String,
        @Field("security_question") securityQuestion: String,
        @Field("security_answer") securityAnswer: String,
    ): VerifyAnswerResponse

    @FormUrlEncoded
    @POST("reset_password.php")
    suspend fun resetPassword(
        @Field("username") username: String,
        @Field("reset_token") resetToken: String,
        @Field("new_password") newPassword: String,
    ): BasicResponse

    @GET("get_motion_events.php")
    suspend fun getMotionEvents(): MotionEventsResponse

    @GET("get_sensors.php")
    suspend fun getSensors(): SensorsResponse

    @FormUrlEncoded
    @POST("toggle_sensor.php")
    suspend fun toggleSensor(
        @Field("sensor_code") sensorCode: String,
        @Field("is_enabled") isEnabled: Int,
    ): BasicResponse

    @FormUrlEncoded
    @POST("update_profile.php")
    suspend fun updateProfile(
        @Field("user_id") userId: Int,
        @Field("current_password") currentPassword: String,
        @Field("new_username") newUsername: String,
        @Field("new_email") newEmail: String,
        @Field("new_password") newPassword: String,
    ): UpdateProfileResponse
}
