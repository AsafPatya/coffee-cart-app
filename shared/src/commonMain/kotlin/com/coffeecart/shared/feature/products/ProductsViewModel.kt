package com.coffeecart.shared.feature.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coffeecart.shared.domain.AddProductResult
import com.coffeecart.shared.domain.CoffeeCartRepositoryInterface
import com.coffeecart.shared.domain.ShoppingCartRepositoryInterface
import com.coffeecart.shared.model.Product
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ProductsUiState {
    data object Loading : ProductsUiState
    data class Success(
        val cartName: String,
        val categoryName: String,
        val products: List<Product>,
    ) : ProductsUiState
    data class Error(val message: String) : ProductsUiState
}

class ProductsViewModel(
    private val repository: CoffeeCartRepositoryInterface,
    private val shoppingCartRepositoryInterface: ShoppingCartRepositoryInterface,
) : ViewModel() {
    private val _uiState = MutableStateFlow<ProductsUiState>(ProductsUiState.Loading)
    val uiState: StateFlow<ProductsUiState> = _uiState.asStateFlow()

    private val _snackBarMessages = MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val snackBarMessages: SharedFlow<String> = _snackBarMessages.asSharedFlow()

    fun loadProducts(cartId: String, categoryName: String) {
        viewModelScope.launch {
            _uiState.value = ProductsUiState.Loading
            try {
                val carts = repository.getCoffeeCarts()
                val cart = carts.find { it.id == cartId }
                if (cart == null) {
                    _uiState.value = ProductsUiState.Error("Coffee cart not found.")
                    return@launch
                }
                val category = cart.categories.find { it.name == categoryName }
                if (category == null) {
                    _uiState.value = ProductsUiState.Error("Category '$categoryName' not found.")
                    return@launch
                }
                _uiState.value = ProductsUiState.Success(
                    cartName = cart.name,
                    categoryName = category.name,
                    products = category.products,
                )
            } catch (e: Exception) {
                _uiState.value = ProductsUiState.Error(e.message ?: "Failed to load products.")
            }
        }
    }

    fun addProductToCart(
        cartId: String,
        product: Product,
        quantity: Int = 1,
        comment: String = "",
        addedText: String = "added to basket",
    ) {
        val cartName = (uiState.value as? ProductsUiState.Success)?.cartName ?: ""
        val result = shoppingCartRepositoryInterface.addProduct(cartId, cartName, product, quantity, comment)
        viewModelScope.launch {
            when (result) {
                AddProductResult.BlockedDifferentCart -> {
                    _snackBarMessages.emit(
                        "Finish or clear your current order before adding from a different coffee cart."
                    )
                }
                AddProductResult.Added, AddProductResult.IncrementedExisting -> {
                    _snackBarMessages.emit("${product.name} $addedText")
                }
            }
        }
    }
}


