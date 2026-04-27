package com.example.lab09.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.example.lab09.R

class NotificationHelper(private val context: Context) {

    private val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Currency Updates",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = "Periodic currency rate updates" }
        manager.createNotificationChannel(channel)
    }

    fun showUpdateNotification(message: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Currency Tracker")
            .setContentText(message)
            .setAutoCancel(true)
            .build()
        manager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val CHANNEL_ID = "currency_channel"
        private const val NOTIFICATION_ID = 1001
    }
}
