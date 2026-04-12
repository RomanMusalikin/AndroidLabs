package com.example.lab4_weather // Твой пакет

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://api.openweathermap.org/"

    // Инициализируем Retrofit лениво (только когда он реально понадобится)
    val api: WeatherApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // Конвертер JSON -> Data classes
            .build()
            .create(WeatherApi::class.java)
    }
}