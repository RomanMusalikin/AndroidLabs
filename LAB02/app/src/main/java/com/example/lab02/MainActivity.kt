package com.example.lab02

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {

    private lateinit var tvScreen: TextView
    private var isNewOp: Boolean = true
    private val historyList = mutableListOf<String>() // Список для хранения истории

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

        // Кнопки операций
        setupOperationButton(R.id.buttonAdd, "+")
        setupOperationButton(R.id.buttonSubtraction, "-")
        setupOperationButton(R.id.buttonMultiplication, "*")
        setupOperationButton(R.id.buttonDivision, "/")

        // Кнопки управления
        findViewById<Button>(R.id.buttonEqual).setOnClickListener {
            calculateResult()
        }

        findViewById<Button>(R.id.buttonClear).setOnClickListener {
            tvScreen.text = "0"
            isNewOp = true
        }

        findViewById<Button>(R.id.buttonDelete).setOnClickListener {
            val text = tvScreen.text.toString()
            if (text.length > 1) {
                if (text.endsWith(" ")) {
                    tvScreen.text = text.dropLast(3)
                } else {
                    tvScreen.text = text.dropLast(1)
                }
            } else {
                tvScreen.text = "0"
                isNewOp = true
            }
        }

        findViewById<ImageButton>(R.id.buttonHistory).setOnClickListener {
            showHistoryDialog()
        }
    }

    // Сохранение важной инфы, например, при смене ориентации экрана
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(KEY_SCREEN, tvScreen.text.toString())
        outState.putBoolean(KEY_NEW_OP, isNewOp)
        outState.putStringArrayList(KEY_HISTORY, ArrayList(historyList))
    }

    // Инициализируется сразу после перезапуска MainActivity, подтягивает сохранённые ранее данные
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        tvScreen.text = savedInstanceState.getString(KEY_SCREEN, "0")
        isNewOp = savedInstanceState.getBoolean(KEY_NEW_OP, true)
        val savedHistory = savedInstanceState.getStringArrayList(KEY_HISTORY)
        if (savedHistory != null) {
            historyList.clear()
            historyList.addAll(savedHistory)
        }
    }

    private fun setupNumberButton(id: Int, value: String) {
        findViewById<Button>(id).setOnClickListener {
            val currentText = tvScreen.text.toString()

            if (currentText == "0" || isNewOp) {
                if (value == ".") {
                    tvScreen.text = "0."
                } else {
                    tvScreen.text = value
                }
                isNewOp = false
            } else {
                val parts = currentText.split(" ", "+", "-", "*", "/")
                val lastPart = parts.last()
                if (value == "." && lastPart.contains(".")) return@setOnClickListener
                tvScreen.append(value)
            }
        }
    }

    private fun setupOperationButton(id: Int, operator: String) {
        findViewById<Button>(id).setOnClickListener {
            val currentText = tvScreen.text.toString()

            if (currentText.isEmpty() || currentText.endsWith(" ")) return@setOnClickListener

            if (currentText.contains(" ")) {
                calculateResult()
            }

            tvScreen.append(" $operator ")
            isNewOp = false
        }
    }

    private fun calculateResult() {
        val currentText = tvScreen.text.toString()
        val parts = currentText.split(" ")

        if (parts.size < 3) return

        val val1 = parts[0].toDoubleOrNull() ?: 0.0
        val op = parts[1]
        val val2 = parts[2].toDoubleOrNull() ?: 0.0
        var result = 0.0

        when (op) {
            "+" -> result = val1 + val2
            "-" -> result = val1 - val2
            "*" -> result = val1 * val2
            "/" -> if (val2 != 0.0) {
                result = val1 / val2
            } else {
                tvScreen.text = "Ошибка"
                isNewOp = true
                return
            }
        }

        val formattedResult = if (result % 1 == 0.0) result.toLong().toString() else result.toString()
        val historyEntry = "$currentText = $formattedResult"
        historyList.add(historyEntry)
        tvScreen.text = formattedResult
        isNewOp = true
    }

    // Функция показа истории вычислений
    private fun showHistoryDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("История вычислений")

        if (historyList.isEmpty()) {
            builder.setMessage("История пока пуста")
        } else {
            builder.setMessage(historyList.joinToString("\n"))
        }

        // Кнопка для закрытия окна
        builder.setPositiveButton("Закрыть", null)

        // Кнопка для очистки истории
        builder.setNegativeButton("Очистить историю") { _, _ ->
            historyList.clear() // Удаляем все элементы из списка
        }

        // Показываем окно
        builder.show()
    }

    // Строковые ключи для сохранения состояния
    companion object {
        private const val KEY_SCREEN = "screen_text"
        private const val KEY_NEW_OP = "is_new_op"
        private const val KEY_HISTORY = "history_list"
    }

}