package com.coffeecart.shared.feature.cartlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coffeecart.shared.domain.CoffeeCartRepository
import com.coffeecart.shared.model.CoffeeCart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

    private val _userLocation = MutableStateFlow<Pair<Double, Double>?>(null)
    val userLocation: StateFlow<Pair<Double, Double>?> = _userLocation.asStateFlow()

    init {
        loadCarts()
    }

    fun setUserLocation(latitude: Double, longitude: Double) {
        _userLocation.value = latitude to longitude
    }

    fun loadCarts() {
        viewModelScope.launch {
            _uiState.value = CoffeeCartListUiState.Loading
            _uiState.value = try {
                CoffeeCartListUiState.Success(repository.getCoffeeCarts())
            } catch (e: Exception) {
                CoffeeCartListUiState.Error(e.message ?: "Failed to load coffee carts")
            }
        }
    }
}
