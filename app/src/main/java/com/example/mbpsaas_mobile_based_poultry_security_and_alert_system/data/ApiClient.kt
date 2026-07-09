package com.example.mbpsaas_mobile_based_poultry_security_and_alert_system.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    // USB mode: phone plugged into the XAMPP PC, after running
    //   adb reverse tcp:8080 tcp:80
    // "localhost" then tunnels through the cable to the PC. Works on any PC, no Wi-Fi needed.
    // For Wi-Fi instead, use the PC's LAN IP, e.g. "http://192.168.254.104/mbpsaas_api/".
    // For the Android EMULATOR use "http://10.0.2.2/mbpsaas_api/".
    private const val BASE_URL = "http://localhost:8080/mbpsaas_api/"

    val service: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
