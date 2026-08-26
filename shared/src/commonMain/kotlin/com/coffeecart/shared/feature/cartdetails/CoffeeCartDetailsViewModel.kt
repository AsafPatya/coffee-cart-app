package com.coffeecart.shared.feature.cartdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coffeecart.shared.domain.AddProductResult
import com.coffeecart.shared.domain.CoffeeCartRepositoryInterface
import com.coffeecart.shared.domain.ShoppingCartRepositoryInterface
import com.coffeecart.shared.model.CoffeeCart
import com.coffeecart.shared.model.MenuCategory
import com.coffeecart.shared.model.Product
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
    private val repository: CoffeeCartRepositoryInterface,
    private val shoppingCartRepositoryInterface: ShoppingCartRepositoryInterface,
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

    fun addProductToCart(
        cartId: String,
        cartName: String,
        product: Product,
        quantity: Int = 1,
        comment: String = "",
    ): AddProductResult {
        return shoppingCartRepositoryInterface.addProduct(cartId, cartName, product, quantity, comment)
    }

    fun addCategory(cartId: String, category: MenuCategory, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val carts = repository.getCoffeeCarts()
                val cart = carts.find { it.id == cartId }
                if (cart != null) {
                    val updatedCart = cart.copy(categories = cart.categories + category)
                    val success = repository.updateCoffeeCartFull(updatedCart)
                    if (success) {
                        _uiState.value = CoffeeCartDetailsUiState.Success(updatedCart)
                        onResult(true)
                    } else {
                        onResult(false)
                    }
                } else {
                    onResult(false)
                }
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }
}

