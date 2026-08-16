package com.example.mbpsaas_mobile_based_poultry_security_and_alert_system.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    // =====================================================================================
    // BASE_URL: USB Cable Tunneling via `adb reverse tcp:8080 tcp:80`
    // =====================================================================================
    private const val BASE_URL = "http://localhost:8080/mbpsaas_api/"

    val service: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
