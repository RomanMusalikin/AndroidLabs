package com.example.lab4_weather

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load // Это та самая библиотека Coil, которую мы добавили для картинок

class WeatherAdapter(
    private var forecastList: List<ForecastItem>,
    private val onItemClick: (ForecastItem) -> Unit // Передаем нажатие (позже свяжем со свайпами)
) : RecyclerView.Adapter<WeatherAdapter.WeatherViewHolder>() {

    class WeatherViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDateTime: TextView = view.findViewById(R.id.tvDateTime)
        val tvTemperature: TextView = view.findViewById(R.id.tvTemperature)
        val ivWeatherIcon: ImageView = view.findViewById(R.id.ivWeatherIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_weather, parent, false)
        return WeatherViewHolder(view)
    }

    override fun onBindViewHolder(holder: WeatherViewHolder, position: Int) {
        val item = forecastList[position]

        // Вставляем дату
        holder.tvDateTime.text = item.dt_txt

        // Вставляем температуру (округляем до целого числа)
        holder.tvTemperature.text = "${item.main.temp.toInt()}°C"

        // OpenWeatherAPI возвращает код иконки (например, "10d").
        // Мы подставляем его в URL и Coil сам скачивает картинку!
        val iconCode = item.weather.firstOrNull()?.icon
        if (iconCode != null) {
            val iconUrl = "https://openweathermap.org/img/wn/$iconCode@2x.png"
            holder.ivWeatherIcon.load(iconUrl) {
                crossfade(true) // Плавное появление картинки
            }
        }

        // Слушатель нажатия на всю карточку
        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = forecastList.size

    // Этот метод пригодится нам, когда мы будем искать новый город
    fun updateData(newList: List<ForecastItem>) {
        forecastList = newList
        notifyDataSetChanged()
    }
}