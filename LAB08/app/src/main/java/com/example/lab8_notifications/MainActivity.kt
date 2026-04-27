package com.example.lab8_notifications

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(this, "Без разрешения уведомления не будут работать", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkAndRequestPermissions()

        val etTaskDesc = findViewById<EditText>(R.id.etTaskDescription)
        val etTimeValue = findViewById<EditText>(R.id.etDelayValue)

        // Находим нужные кнопки для выбора времени
        val rbMinutes = findViewById<RadioButton>(R.id.rbMinutes)
        val rbHours = findViewById<RadioButton>(R.id.rbHours)
        val btnSetAlarm = findViewById<Button>(R.id.btnSetAlarm)

        btnSetAlarm.setOnClickListener {
            val taskMessage = etTaskDesc.text.toString().trim()
            val timeString = etTimeValue.text.toString().trim()

            if (taskMessage.isEmpty()) {
                Toast.makeText(this, "Опишите задачу!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (timeString.isEmpty()) {
                Toast.makeText(this, "Укажите время задержки!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val delayValue = timeString.toLongOrNull() ?: 0L
            if (delayValue <= 0) {
                Toast.makeText(this, "Время должно быть больше нуля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Определяем выбранную единицу измерения через when
            val timeUnit = when {
                rbHours.isChecked -> TimeUnit.HOURS
                rbMinutes.isChecked -> TimeUnit.MINUTES
                else -> TimeUnit.SECONDS
            }

            // Текст для всплывающего сообщения
            val unitText = when {
                rbHours.isChecked -> "ч."
                rbMinutes.isChecked -> "мин."
                else -> "сек."
            }

            scheduleCustomNotification(taskMessage, delayValue, timeUnit)

            Toast.makeText(this, "Готово! Ждите через $delayValue $unitText", Toast.LENGTH_SHORT).show()

            etTaskDesc.text.clear()
            etTimeValue.text.clear()
        }
    }

    private fun scheduleCustomNotification(message: String, delay: Long, unit: TimeUnit) {
        val data = Data.Builder()
            .putString("NOTIFICATION_MESSAGE", message)
            .build()

        val notificationWork = OneTimeWorkRequest.Builder(ReminderWorker::class.java)
            .setInitialDelay(delay, unit)
            .setInputData(data)
            .build()

        WorkManager.getInstance(this).enqueue(notificationWork)
    }

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}