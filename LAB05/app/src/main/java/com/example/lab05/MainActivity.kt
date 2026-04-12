package com.example.lab05

import android.os.Bundle
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var dao: TodoDao
    private lateinit var adapter: TodoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dao = AppDatabase.getDatabase(this).todoDao()

        val etTodo = findViewById<TextInputEditText>(R.id.etTodo)
        val btnAdd = findViewById<MaterialButton>(R.id.btnAdd)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)

        adapter = TodoAdapter(
            onToggle = { todo ->
                lifecycleScope.launch {
                    dao.update(todo.copy(isDone = !todo.isDone))
                }
            },
            onDelete = { todo ->
                lifecycleScope.launch {
                    dao.delete(todo)
                }
            },
            onEdit = { todo ->
                showEditDialog(todo)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        btnAdd.setOnClickListener {
            val text = etTodo.text.toString().trim()
            if (text.isNotEmpty()) {
                lifecycleScope.launch {
                    dao.insert(Todo(title = text))
                }
                etTodo.text?.clear()
            }
        }

        lifecycleScope.launch {
            dao.getAll().collectLatest { list ->
                adapter.submitList(list)
            }
        }
    }

    // Функция для создания и показа всплывающего окна редактирования
    private fun showEditDialog(todo: Todo) {
        val editText = EditText(this)
        editText.setText(todo.title)
        editText.setSelection(editText.text.length)

        val container = FrameLayout(this)
        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(50, 20, 50, 0)
        editText.layoutParams = params
        container.addView(editText)

        MaterialAlertDialogBuilder(this)
            .setTitle("Редактировать задачу")
            .setView(container)
            .setPositiveButton("Сохранить") { _, _ ->
                val newText = editText.text.toString().trim()
                if (newText.isNotEmpty() && newText != todo.title) {
                    // Если текст не пустой и изменился, обновляем запись в БД
                    lifecycleScope.launch {
                        dao.update(todo.copy(title = newText))
                    }
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
}