package com.example.mbpsaas_mobile_based_poultry_security_and_alert_system.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    // =====================================================================================
    // BASE_URL Configuration:
    // - Wi-Fi Mode (Phone & Laptop on same Wi-Fi): "http://<LAPTOP_IP>/mbpsaas_api/" (e.g. 10.192.10.14)
    // - USB Cable Mode (via `adb reverse tcp:8080 tcp:80`): "http://localhost:8080/mbpsaas_api/"
    // =====================================================================================
    private const val BASE_URL = "http://10.45.170.14/mbpsaas_api/"

    val service: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
