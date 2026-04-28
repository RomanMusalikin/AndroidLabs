package com.example.lab10.ui
import com.example.lab10.data.Product

sealed class UiState {
    object Loading : UiState()
    data class Success(val data: List<Product>) : UiState()
    data class Error(val message: String) : UiState()
}