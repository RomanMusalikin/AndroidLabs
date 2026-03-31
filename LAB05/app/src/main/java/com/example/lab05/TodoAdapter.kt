package com.example.lab05

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class TodoAdapter(
    private val onToggle: (Todo) -> Unit,
    private val onDelete: (Todo) -> Unit,
    private val onEdit: (Todo) -> Unit // Добавили передачу функции редактирования
) : ListAdapter<Todo, TodoAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cbDone: CheckBox = view.findViewById(R.id.cbDone)
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_todo, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val todo = getItem(position)

        holder.tvTitle.text = todo.title
        holder.cbDone.isChecked = todo.isDone

        if (todo.isDone) {
            holder.tvTitle.paintFlags = holder.tvTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            holder.tvTitle.paintFlags = holder.tvTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }

        holder.cbDone.setOnClickListener { onToggle(todo) }
        holder.btnDelete.setOnClickListener { onDelete(todo) }

        // Долгий клик по карточке задачи вызывает функцию редактирования
        holder.itemView.setOnLongClickListener {
            onEdit(todo)
            true // Возвращаем true, показывая, что мы обработали долгое нажатие
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Todo>() {
        override fun areItemsTheSame(a: Todo, b: Todo) = a.id == b.id
        override fun areContentsTheSame(a: Todo, b: Todo) = a == b
    }
}