package com.example.lab09.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.frankfurter.app/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: CurrencyApiService by lazy {
        retrofit.create(CurrencyApiService::class.java)
    }
}
