package com.example.lab09.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.lab09.R
import com.example.lab09.data.db.CurrencyRate

class CurrencyAdapter : ListAdapter<CurrencyRate, CurrencyAdapter.ViewHolder>(DIFF) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCurrency: TextView = view.findViewById(R.id.tvCurrency)
        val tvRate: TextView = view.findViewById(R.id.tvRate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_currency, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.tvCurrency.text = item.currency
        holder.tvRate.text = String.format("%.4f", item.rate)
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<CurrencyRate>() {
            override fun areItemsTheSame(a: CurrencyRate, b: CurrencyRate) = a.currency == b.currency
            override fun areContentsTheSame(a: CurrencyRate, b: CurrencyRate) = a == b
        }
    }
}
