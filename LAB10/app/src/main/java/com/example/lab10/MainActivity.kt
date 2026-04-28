package com.example.lab10

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.coroutines.launch

import com.example.lab10.data.RetrofitClient
import com.example.lab10.ui.MainViewModel
import com.example.lab10.ui.ProductAdapter
import com.example.lab10.ui.UiState
import com.example.lab10.R

class MainActivity : ComponentActivity() {

    // Подключаем нашу логику (ViewModel) и адаптер для списка
    private val viewModel: MainViewModel by viewModels()
    private val adapter = ProductAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ВАЖНО: Подключаем твой файл дизайна, который называется layout.xml
        setContentView(R.layout.layout)

        // Находим все элементы дизайна по их ID
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val swipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)
        val searchEditText = findViewById<EditText>(R.id.searchEditText)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val errorTextView = findViewById<TextView>(R.id.errorTextView)

        // Настраиваем список (RecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Настраиваем Pull-to-refresh (свайп вниз для обновления)
        swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshData()
        }

        // Слушаем, что пользователь печатает в строке поиска
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Передаем текст в ViewModel, а она уже сама сделает задержку (debounce)
                viewModel.onSearchQueryChanged(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Подписываемся на состояния (Загрузка, Успех, Ошибка) из ViewModel
        lifecycleScope.launch {
            // Код внутри будет работать только когда экран активен
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    // Выбираем, что показать на экране
                    when (state) {
                        is UiState.Loading -> {
                            // Если мы не обновляем свайпом, показываем кружок загрузки по центру
                            if (!swipeRefreshLayout.isRefreshing) {
                                progressBar.visibility = View.VISIBLE
                            }
                            errorTextView.visibility = View.GONE
                            recyclerView.visibility = View.GONE
                        }
                        is UiState.Success -> {
                            progressBar.visibility = View.GONE
                            swipeRefreshLayout.isRefreshing = false // Убираем анимацию свайпа
                            errorTextView.visibility = View.GONE
                            recyclerView.visibility = View.VISIBLE

                            // Отдаем скачанные данные в адаптер, чтобы он их нарисовал
                            adapter.submitList(state.data)
                        }
                        is UiState.Error -> {
                            progressBar.visibility = View.GONE
                            swipeRefreshLayout.isRefreshing = false
                            recyclerView.visibility = View.GONE

                            errorTextView.visibility = View.VISIBLE
                            errorTextView.text = state.message // Показываем текст ошибки
                        }
                    }
                }
            }
        }
    }
}