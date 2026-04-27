package com.example.lab09.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CurrencyRateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rates: List<CurrencyRate>)

    @Query("SELECT * FROM currency_rates WHERE timestamp = (SELECT MAX(timestamp) FROM currency_rates)")
    fun getLatestRates(): LiveData<List<CurrencyRate>>

    @Query("SELECT MAX(timestamp) FROM currency_rates")
    suspend fun getLatestTimestamp(): Long?

    @Query("DELETE FROM currency_rates WHERE timestamp NOT IN (SELECT DISTINCT timestamp FROM currency_rates ORDER BY timestamp DESC LIMIT 1)")
    suspend fun deleteOldRates()
}
