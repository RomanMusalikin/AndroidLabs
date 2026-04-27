package com.example.lab09.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "currency_rates")
data class CurrencyRate(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val currency: String,
    val rate: Double,
    val timestamp: Long
)
