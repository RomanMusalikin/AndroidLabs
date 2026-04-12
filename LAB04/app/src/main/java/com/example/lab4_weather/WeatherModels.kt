package com.example.lab4_weather

import java.io.Serializable

data class WeatherResponse(val list: List<ForecastItem>)

// Добавляем : Serializable к классам, которые будем передавать
data class ForecastItem(
    val dt_txt: String,
    val main: MainTemp,
    val weather: List<Weather>
) : Serializable

data class MainTemp(
    val temp: Double,
    val temp_min: Double,
    val temp_max: Double,
    val humidity: Int
) : Serializable

data class Weather(
    val description: String,
    val icon: String
) : Serializable