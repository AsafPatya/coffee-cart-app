package com.coffeecart.shared.feature.cartlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coffeecart.shared.domain.CoffeeCartRepository
import com.coffeecart.shared.model.CoffeeCart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

sealed interface CoffeeCartListUiState {
    data object Loading : CoffeeCartListUiState
    data class Success(val carts: List<CoffeeCart>) : CoffeeCartListUiState
    data class Error(val message: String) : CoffeeCartListUiState
}

class CoffeeCartListViewModel(
    private val repository: CoffeeCartRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<CoffeeCartListUiState>(CoffeeCartListUiState.Loading)
    val uiState: StateFlow<CoffeeCartListUiState> = _uiState.asStateFlow()

    init {
        loadCarts()
    }

    fun loadCarts() {
        runBlocking {
            _uiState.value = CoffeeCartListUiState.Loading
            _uiState.value = try {
                CoffeeCartListUiState.Success(repository.getCoffeeCarts())
            } catch (e: Exception) {
                CoffeeCartListUiState.Error(e.message ?: "Failed to load coffee carts")
            }
        }
    }
}
