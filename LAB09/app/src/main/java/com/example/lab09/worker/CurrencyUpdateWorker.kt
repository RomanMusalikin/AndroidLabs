package com.example.lab09.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.lab09.data.repository.CurrencyRepository
import com.example.lab09.notification.NotificationHelper
import java.util.concurrent.TimeUnit

class CurrencyUpdateWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "Worker started")
        val repository = CurrencyRepository(applicationContext)
        val notificationHelper = NotificationHelper(applicationContext)
        return try {
            repository.fetchAndSave()
            notificationHelper.showUpdateNotification("Курсы валют обновлены")
            Log.d(TAG, "Worker finished successfully")
            scheduleNext()
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Worker failed: ${e.message}")
            scheduleNext()
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    private fun scheduleNext() {
        val scheduler = WorkManagerScheduler(applicationContext)
        val next = OneTimeWorkRequestBuilder<CurrencyUpdateWorker>()
            .setConstraints(scheduler.constraints)
            .setInitialDelay(WorkManagerScheduler.REPEAT_DELAY_SECONDS, TimeUnit.SECONDS)
            .addTag(WorkManagerScheduler.WORK_TAG)
            .build()
        WorkManager.getInstance(applicationContext).enqueueUniqueWork(
            WorkManagerScheduler.UNIQUE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            next
        )
    }

    companion object {
        const val TAG = "CurrencyWorker"
    }
}
