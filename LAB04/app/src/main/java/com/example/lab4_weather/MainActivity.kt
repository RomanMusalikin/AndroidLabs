package com.example.lab4_weather

import android.content.Intent
import android.os.Bundle
import android.util.Log // Не забудь импорт!
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: WeatherAdapter
    private var currentForecastList: List<ForecastItem> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etCity = findViewById<EditText>(R.id.etCity)
        val btnSearch = findViewById<Button>(R.id.btnSearch)
        val tvCityName = findViewById<TextView>(R.id.tvCityName)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewWeather)

        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = WeatherAdapter(emptyList()) { clickedItem ->
            val position = currentForecastList.indexOf(clickedItem)
            val intent = Intent(this, DetailActivity::class.java).apply {
                putExtra("forecast_list", ArrayList(currentForecastList))
                putExtra("start_position", position)
            }
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        btnSearch.setOnClickListener {
            val city = etCity.text.toString().trim()
            if (city.isNotEmpty()) {
                tvCityName.text = "Погода: $city"
                fetchWeather(city)
            } else {
                Toast.makeText(this, "Введите название города", Toast.LENGTH_SHORT).show()
            }
        }

        // Авто-загрузка при старте
        fetchWeather("Moscow")
    }

    private fun fetchWeather(city: String) {
        val apiKey = "072544508f5c1825beac25eee9e22c5c"

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Логируем начало запроса
                Log.d("WeatherDebug", "Запрашиваем погоду для: $city")

                val response = RetrofitClient.api.getForecast(city, apiKey)

                // Если дошли сюда, значит запрос успешный
                val dailyForecast = response.list.filter { it.dt_txt.contains("12:00:00") }

                Log.d("WeatherDebug", "Данные успешно получены. Элементов: ${dailyForecast.size}")

                withContext(Dispatchers.Main) {
                    currentForecastList = dailyForecast
                    adapter.updateData(dailyForecast)
                }
            } catch (e: Exception) {
                // ВАЖНО: Выводим полную ошибку в Logcat
                Log.e("WeatherDebug", "ПРОИЗОШЛА ОШИБКА ПРИ ЗАПРОСЕ!", e)

                withContext(Dispatchers.Main) {
                    // Выводим текст ошибки в Toast (для отладки можно вывести саму ошибку e.message)
                    Toast.makeText(this@MainActivity, "Ошибка: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}