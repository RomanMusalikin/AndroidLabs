package com.example.lab8_notifications

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class ReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    @SuppressLint("MissingPermission")
    override fun doWork(): Result {
        // Забираем текст по новому ключу
        val message = inputData.getString("NOTIFICATION_MESSAGE") ?: "Время вышло!"
        val alertChannelId = "my_custom_task_channel"

        // 1. Инициализация канала уведомлений
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Система оповещений"
            val channelDesc = "Канал для пользовательских таймеров"

            val channel = NotificationChannel(
                alertChannelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = channelDesc
            }

            val manager: NotificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        // 2. Сборка внешнего вида уведомления (поменяли иконку на колокольчик/событие)
        val notificationBuilder = NotificationCompat.Builder(applicationContext, alertChannelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Новая задача!")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_MAX) // Сделали приоритет максимальным
            .setAutoCancel(true)

        // 3. Показ уведомления
        with(NotificationManagerCompat.from(applicationContext)) {
            notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
        }

        return Result.success()
    }
}