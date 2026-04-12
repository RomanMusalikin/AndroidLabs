package com.example.lab4_weather

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class WeatherPagerAdapter(
    activity: AppCompatActivity,
    private val forecastList: List<ForecastItem>
) : FragmentStateAdapter(activity) {

    // Сколько всего экранов можно свайпнуть (по количеству дней)
    override fun getItemCount(): Int = forecastList.size

    // Создаем фрагмент для конкретной позиции при свайпе
    override fun createFragment(position: Int): Fragment {
        return DayDetailFragment.newInstance(forecastList[position])
    }
}