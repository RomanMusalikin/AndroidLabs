package com.example.lab02

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.lab02.ui.theme.LAB02Theme

class MainActivity : ComponentActivity() {

    private fun setupNumberButton(id: Int, value: String) {
        findViewById<Button>(id).setOnClickListener {
            // Если начинаем новую операцию или на экране висит одинокий "0"
            if (isNewOp || tvScreen.text.toString() == "0") {
                if (value == ".") {
                    tvScreen.text = "0." // Если первая кнопка - точка, делаем "0."
                } else {
                    tvScreen.text = value
                }
                isNewOp = false
            } else {
                // Защита от нескольких точек в одном числе
                if (value == "." && tvScreen.text.contains(".")) return@setOnClickListener
                tvScreen.append(value)
            }
        }
    }

    private fun setupOperationButton(id: Int, operator: String) {
        findViewById<Button>(id).setOnClickListener {
            // Запоминаем текущее число с экрана
            firstValue = tvScreen.text.toString().toDoubleOrNull() ?: 0.0
            // Запоминаем, какую операцию выбрали
            currentOperator = operator
            // Ставим флаг, что следующее нажатие цифры должно начать новое число
            isNewOp = true
        }
    }

    // Поздняя инициализация экрана
    private lateinit var tvScreen: TextView

    // Переменные для логики
    private var firstValue: Double = 0.0
    private var currentOperator: String = ""
    private var isNewOp: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout)

        tvScreen = findViewById(R.id.tvScreen)

        // Цифры
        setupNumberButton(R.id.button1, "1")
        setupNumberButton(R.id.button2, "2")
        setupNumberButton(R.id.button3, "3")
        setupNumberButton(R.id.button4, "4")
        setupNumberButton(R.id.button5, "5")
        setupNumberButton(R.id.button6, "6")
        setupNumberButton(R.id.button7, "7")
        setupNumberButton(R.id.button8, "8")
        setupNumberButton(R.id.button9, "9")
        setupNumberButton(R.id.button0, "0")
        setupNumberButton(R.id.button00, "00")
        setupNumberButton(R.id.buttonPoint, ".")
        setupOperationButton(R.id.buttonAdd, "+")
        setupOperationButton(R.id.buttonSubtraction, "-")
        setupOperationButton(R.id.buttonMultiplication, "*")
        setupOperationButton(R.id.buttonDivision, "/")

        findViewById<Button>(R.id.buttonEqual).setOnClickListener {
            val secondValue = tvScreen.text.toString().toDoubleOrNull() ?: 0.0
            var result = 0.0

            when (currentOperator) {
                "+" -> result = firstValue + secondValue
                "-" -> result = firstValue - secondValue
                "*" -> result = firstValue * secondValue
                "/" -> if (secondValue != 0.0) result = firstValue / secondValue else tvScreen.text = "Ошибка"
            }

            // Выводим результат, если нет ошибки деления на ноль
            if (tvScreen.text != "Ошибка") {
                // Если число целое (например 5.0), отбрасываем ноль
                val finalResult = if (result % 1 == 0.0) result.toInt().toString() else result.toString()
                tvScreen.text = finalResult
            }

            // Сбрасываем флаг, чтобы следующее нажатие цифры начало новый ввод
            isNewOp = true
        }

        // Кнопка С (Полный сброс)
        findViewById<Button>(R.id.buttonClear).setOnClickListener {
            tvScreen.text = "0"
            firstValue = 0.0
            currentOperator = ""
            isNewOp = true
        }

        // Кнопка DEL (Стереть последний символ)
        findViewById<Button>(R.id.buttonDelete).setOnClickListener {
            val text = tvScreen.text.toString()
            if (text.length > 1) {
                tvScreen.text = text.dropLast(1)
            } else {
                tvScreen.text = "0"
                isNewOp = true
            }
        }
    }
}