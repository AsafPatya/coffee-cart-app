package com.coffeecart.shared.feature.cartdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coffeecart.shared.domain.CoffeeCartRepository
import com.coffeecart.shared.model.CoffeeCart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface CoffeeCartDetailsUiState {
    data object Loading : CoffeeCartDetailsUiState
    data class Success(val cart: CoffeeCart) : CoffeeCartDetailsUiState
    data class Error(val message: String) : CoffeeCartDetailsUiState
}

class CoffeeCartDetailsViewModel(
    private val repository: CoffeeCartRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<CoffeeCartDetailsUiState>(CoffeeCartDetailsUiState.Loading)
    val uiState: StateFlow<CoffeeCartDetailsUiState> = _uiState.asStateFlow()

    fun loadCart(id: String) {
        viewModelScope.launch {
            _uiState.value = CoffeeCartDetailsUiState.Loading
            try {
                val carts = repository.getCoffeeCarts()
                val cart = carts.find { it.id == id }
                if (cart != null) {
                    _uiState.value = CoffeeCartDetailsUiState.Success(cart)
                } else {
                    _uiState.value = CoffeeCartDetailsUiState.Error("Coffee cart not found.")
                }
            } catch (e: Exception) {
                _uiState.value = CoffeeCartDetailsUiState.Error(e.message ?: "Failed to load coffee cart detail.")
            }
        }
    }
}

