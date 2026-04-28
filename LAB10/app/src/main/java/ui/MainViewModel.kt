package com.example.lab10.ui
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lab10.data.RetrofitClient
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.net.UnknownHostException
import java.net.SocketTimeoutException

@OptIn(FlowPreview::class)
class MainViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val searchQuery = MutableStateFlow("")

    init {
        searchQuery
            .debounce(500)
            .distinctUntilChanged()
            .onEach { query -> fetchProducts(query) }
            .launchIn(viewModelScope)
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }

    fun refreshData() {
        fetchProducts(searchQuery.value)
    }

    private fun fetchProducts(query: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val response = if (query.isEmpty()) {
                    RetrofitClient.apiService.getProducts()
                } else {
                    RetrofitClient.apiService.searchProducts(query)
                }
                _uiState.value = UiState.Success(response.productsList)
            } catch (e: UnknownHostException) {
                _uiState.value = UiState.Error("Нет подключения к интернету")
            } catch (e: SocketTimeoutException) {
                _uiState.value = UiState.Error("Сервер не отвечает. Попробуйте позже")
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Что-то пошло не так. Попробуйте ещё раз")
            }
        }
    }
}