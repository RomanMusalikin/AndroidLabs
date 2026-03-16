package com.example.lab03

data class ItemData(
    val title: String,
    val description: String,
    val imageResId: Int,
    val category: String,
    val soundResId: Int? = null
)