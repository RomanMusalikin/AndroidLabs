package com.example.lab09.data.network

data class ExchangeRatesResponse(
    val base: String,
    val date: String,
    val rates: Map<String, Double>
)
