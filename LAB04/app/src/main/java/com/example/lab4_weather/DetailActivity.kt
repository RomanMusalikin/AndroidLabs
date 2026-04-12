package com.example.lab4_weather

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2

class DetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        val viewPager = findViewById<ViewPager2>(R.id.viewPager)

        // Получаем список дней и позицию, на которую кликнул пользователь
        val forecastList = intent.getSerializableExtra("forecast_list") as? ArrayList<ForecastItem>
        val startPosition = intent.getIntExtra("start_position", 0)

        if (forecastList != null) {
            // Настраиваем карусель
            val pagerAdapter = WeatherPagerAdapter(this, forecastList)
            viewPager.adapter = pagerAdapter

            // Устанавливаем карусель ровно на тот день, по которому кликнули
            viewPager.setCurrentItem(startPosition, false)
        }
    }
}