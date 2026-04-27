package com.example.lab09.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import com.example.lab09.data.db.CurrencyRate
import com.example.lab09.data.repository.CurrencyRepository
import com.example.lab09.worker.WorkManagerScheduler
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CurrencyRepository(application)
    private val scheduler = WorkManagerScheduler(application)

    val rates: LiveData<List<CurrencyRate>> = repository.getLatestRates()
    val workStatus: LiveData<List<WorkInfo>> = scheduler.getWorkInfoLiveData()

    private val _lastUpdateTime = MutableLiveData<Long?>()
    val lastUpdateTime: LiveData<Long?> = _lastUpdateTime

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        loadLastUpdateTime()
    }

    fun refreshNow() {
        _isLoading.value = true
        scheduler.runOnce()
    }

    fun loadLastUpdateTime() {
        viewModelScope.launch {
            _lastUpdateTime.value = repository.getLastUpdateTime()
        }
    }
}
