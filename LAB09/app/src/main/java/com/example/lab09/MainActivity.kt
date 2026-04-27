package com.example.lab09

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.WorkInfo
import com.example.lab09.ui.CurrencyAdapter
import com.example.lab09.ui.MainViewModel
import com.example.lab09.worker.WorkManagerScheduler
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private val adapter = CurrencyAdapter()
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* разрешение запрошено, результат не критичен */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestNotificationPermissionIfNeeded()

        val rvRates = findViewById<RecyclerView>(R.id.rvRates)
        val tvLastUpdate = findViewById<TextView>(R.id.tvLastUpdate)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        val btnRefresh = findViewById<Button>(R.id.btnRefresh)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        rvRates.layoutManager = LinearLayoutManager(this)
        rvRates.adapter = adapter

        WorkManagerScheduler(this).schedulePeriodicUpdate()

        viewModel.rates.observe(this) { rates ->
            adapter.submitList(rates)
            viewModel.loadLastUpdateTime()
        }

        viewModel.lastUpdateTime.observe(this) { ts ->
            tvLastUpdate.text = if (ts != null)
                "Обновлено: ${dateFormat.format(Date(ts))}"
            else
                "Обновлено: —"
        }

        viewModel.workStatus.observe(this) { infos ->
            val active = infos.firstOrNull()
            tvStatus.text = when (active?.state) {
                WorkInfo.State.RUNNING -> "Статус: загрузка..."
                WorkInfo.State.SUCCEEDED -> "Статус: успешно"
                WorkInfo.State.FAILED -> "Статус: ошибка"
                WorkInfo.State.ENQUEUED -> "Статус: в очереди"
                else -> "Статус: ожидание"
            }
            progressBar.visibility =
                if (active?.state == WorkInfo.State.RUNNING) View.VISIBLE else View.GONE
            if (active?.state == WorkInfo.State.SUCCEEDED || active?.state == WorkInfo.State.FAILED) {
                viewModel.loadLastUpdateTime()
            }
        }

        viewModel.isLoading.observe(this) { loading ->
            progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        btnRefresh.setOnClickListener { viewModel.refreshNow() }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
