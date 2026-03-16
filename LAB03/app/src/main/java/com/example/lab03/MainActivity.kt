package com.example.lab03

import android.content.Intent
import android.os.Bundle
import android.widget.SearchView
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : ComponentActivity() {

    private lateinit var adapter: ItemsAdapter
    private lateinit var fullList: List<ItemData>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout)

        // 1. Подготовка данных.
        // Добавляем R.raw.название_файла для тех, у кого есть звук.
        // Для остальных (машины, деревья) ставим null.
        fullList = listOf(
            ItemData("Лев", "Царь зверей", R.drawable.lion, "animals", R.raw.lion_roar),
            ItemData("Слон", "Огромный и добрый", R.drawable.elephant, "animals", R.raw.elephant_trumpet),
            ItemData("Tesla", "Быстрый электрокар", R.drawable.tesla, "cars", null),
            ItemData("Дуб", "Старый и мудрый", R.drawable.dub, "trees", null)
        )

        val recyclerView = findViewById<RecyclerView>(R.id.list)

        // Сетка в 2 колонки
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        // 2. Настройка клика
        adapter = ItemsAdapter(fullList) { item ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("title", item.title)
            intent.putExtra("desc", item.description)
            intent.putExtra("image", item.imageResId)
            intent.putExtra("category", item.category)

            // Передаем ID звука. Если звука нет (null), передаем 0.
            intent.putExtra("sound_res", item.soundResId ?: 0)

            startActivity(intent)
        }
        recyclerView.adapter = adapter

        // 3. Поиск
        val searchView = findViewById<SearchView>(R.id.search)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean = false
            override fun onQueryTextChange(text: String?): Boolean {
                val filtered = if (text.isNullOrEmpty()) fullList
                else fullList.filter { it.title.contains(text, ignoreCase = true) }
                adapter.updateList(filtered)
                return true
            }
        })
    }
}