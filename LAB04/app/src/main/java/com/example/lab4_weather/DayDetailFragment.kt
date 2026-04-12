package com.example.lab4_weather

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import coil.load

class DayDetailFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Подключаем наш XML макет
        return inflater.inflate(R.layout.fragment_day_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Получаем переданные данные погоды
        val weatherItem = arguments?.getSerializable(ARG_WEATHER) as? ForecastItem

        if (weatherItem != null) {
            val tvDate = view.findViewById<TextView>(R.id.tvDetailDate)
            val tvTemp = view.findViewById<TextView>(R.id.tvDetailTemp)
            val tvDesc = view.findViewById<TextView>(R.id.tvDetailDesc)
            val tvHumidity = view.findViewById<TextView>(R.id.tvDetailHumidity)
            val ivIcon = view.findViewById<ImageView>(R.id.ivDetailIcon)

            // Заполняем интерфейс
            tvDate.text = weatherItem.dt_txt
            tvTemp.text = "${weatherItem.main.temp.toInt()}°C"
            tvDesc.text = weatherItem.weather.firstOrNull()?.description ?: "Нет данных"
            tvHumidity.text = "Влажность: ${weatherItem.main.humidity}%"

            // Загружаем иконку через Coil
            val iconCode = weatherItem.weather.firstOrNull()?.icon
            if (iconCode != null) {
                ivIcon.load("https://openweathermap.org/img/wn/$iconCode@4x.png") {
                    crossfade(true)
                }
            }
        }
    }

    // Специальный блок для правильной передачи данных во фрагмент
    companion object {
        private const val ARG_WEATHER = "arg_weather"

        fun newInstance(item: ForecastItem): DayDetailFragment {
            val fragment = DayDetailFragment()
            val args = Bundle()
            args.putSerializable(ARG_WEATHER, item)
            fragment.arguments = args
            return fragment
        }
    }
}