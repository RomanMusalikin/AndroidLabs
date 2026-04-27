package com.example.lab09.worker

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class WorkManagerScheduler(private val context: Context) {

    private val workManager = WorkManager.getInstance(context)

    val constraints: Constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    fun schedulePeriodicUpdate() {
        val request = OneTimeWorkRequestBuilder<CurrencyUpdateWorker>()
            .setConstraints(constraints)
            .addTag(WORK_TAG)
            .build()

        workManager.enqueueUniqueWork(
            UNIQUE_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    fun runOnce() {
        val request = OneTimeWorkRequestBuilder<CurrencyUpdateWorker>()
            .setConstraints(constraints)
            .addTag(WORK_TAG)
            .build()
        workManager.enqueueUniqueWork(
            UNIQUE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun cancelUpdates() {
        workManager.cancelAllWorkByTag(WORK_TAG)
    }

    fun getWorkInfoLiveData(): LiveData<List<WorkInfo>> =
        workManager.getWorkInfosByTagLiveData(WORK_TAG)

    companion object {
        const val WORK_TAG = "currency_update"
        const val UNIQUE_WORK_NAME = "currency_one_time_update"
        const val REPEAT_DELAY_SECONDS = 30L
    }
}
