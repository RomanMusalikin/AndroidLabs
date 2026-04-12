package com.example.lab4_weather

import android.os.Bundle
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
import android.content.Intent //
class MainActivity : AppCompatActivity() {

    private lateinit var adapter: WeatherAdapter
    // Сохраняем загруженный список, чтобы потом передать его на экран деталей
    private var currentForecastList: List<ForecastItem> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etCity = findViewById<EditText>(R.id.etCity)
        val btnSearch = findViewById<Button>(R.id.btnSearch)
        val tvCityName = findViewById<TextView>(R.id.tvCityName)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewWeather)

        // 1. Настройка списка (RecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Передаем пустой список при старте и описываем, что делать при клике на день
        adapter = WeatherAdapter(emptyList()) { clickedItem ->
            // Находим индекс дня, на который кликнули
            val position = currentForecastList.indexOf(clickedItem)

            // Создаем Intent для перехода на DetailActivity
            val intent = Intent(this, DetailActivity::class.java).apply {
                // Передаем весь список прогнозов и стартовую позицию
                putExtra("forecast_list", ArrayList(currentForecastList))
                putExtra("start_position", position)
            }
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        // 2. Обработка нажатия на кнопку "Найти"
        btnSearch.setOnClickListener {
            val city = etCity.text.toString().trim()
            if (city.isNotEmpty()) {
                tvCityName.text = "Погода: $city"
                fetchWeather(city)
            } else {
                Toast.makeText(this, "Введите название города", Toast.LENGTH_SHORT).show()
            }
        }

        // 3. Загрузим погоду по умолчанию при старте приложения
        fetchWeather("Moscow")
    }

    private fun fetchWeather(city: String) {
        val apiKey = "072544508f5c1825beac25eee9e22c5c"

        // Запускаем корутину (фоновый процесс) для сетевого запроса
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Делаем запрос к API
                val response = RetrofitClient.api.getForecast(city, apiKey)

                // ВАЖНО: API выдает прогноз каждые 3 часа (40 элементов на 5 дней).
                // Чтобы получить краткий прогноз на дни, отфильтруем только дневные часы (например, на 12:00)
                val dailyForecast = response.list.filter { it.dt_txt.contains("12:00:00") }

                // Возвращаемся в главный поток, чтобы обновить интерфейс
                withContext(Dispatchers.Main) {
                    currentForecastList = dailyForecast
                    adapter.updateData(dailyForecast) // Отправляем данные в адаптер
                }
            } catch (e: Exception) {
                // Обработка ошибок (нет интернета, неверный город, опечатка)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Ошибка: Город не найден или нет сети", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}