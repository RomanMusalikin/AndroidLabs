package com.example.lab4_weather // Твой пакет

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    // Указываем путь к бесплатному 5-дневному прогнозу
    @GET("data/2.5/forecast")
    suspend fun getForecast(
        @Query("q") city: String,                // Название города
        @Query("appid") apiKey: String,          // Твой ключ от OpenWeather
        @Query("units") units: String = "metric",// Температура в градусах Цельсия
        @Query("lang") lang: String = "ru"       // Описание на русском языке
    ): WeatherResponse
}