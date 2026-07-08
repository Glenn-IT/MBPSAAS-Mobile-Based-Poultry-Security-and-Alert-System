package com.example.mbpsaas_mobile_based_poultry_security_and_alert_system.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    // PC's Wi-Fi LAN IP — phone and PC must be on the same Wi-Fi network.
    // If your PC's IP changes (run `ipconfig` to check), update it here.
    // For the Android EMULATOR use "http://10.0.2.2/mbpsaas_api/" instead.
    private const val BASE_URL = "http://192.168.254.104/mbpsaas_api/"

    val service: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
