package com.example.lab09.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.lab09.data.db.AppDatabase
import com.example.lab09.data.db.CurrencyRate
import com.example.lab09.data.network.NetworkModule

class CurrencyRepository(context: Context) {

    private val dao = AppDatabase.getInstance(context).currencyRateDao()
    private val api = NetworkModule.apiService

    suspend fun fetchAndSave() {
        val response = api.getRates()
        val timestamp = System.currentTimeMillis()
        val rates = response.rates.map { (currency, rate) ->
            CurrencyRate(currency = currency, rate = rate, timestamp = timestamp)
        }
        dao.insertAll(rates)
        dao.deleteOldRates()
    }

    fun getLatestRates(): LiveData<List<CurrencyRate>> = dao.getLatestRates()

    suspend fun getLastUpdateTime(): Long? = dao.getLatestTimestamp()
}
