package com.coffeecart.shared.data.repository

import com.coffeecart.shared.domain.AddProductResult
import com.coffeecart.shared.domain.ShoppingCartRepositoryInterface
import com.coffeecart.shared.domain.ShoppingCartState
import com.coffeecart.shared.model.OrderItem
import com.coffeecart.shared.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ShoppingCartRepository : ShoppingCartRepositoryInterface {
    private val _state = MutableStateFlow(ShoppingCartState())
    override val state: StateFlow<ShoppingCartState> = _state.asStateFlow()

    override fun addProduct(cartId: String, cartName: String, product: Product): AddProductResult {
        val current = _state.value
        if (current.items.isNotEmpty() && current.cartId != cartId) {
            return AddProductResult.BlockedDifferentCart
        }

        val existingIndex = current.items.indexOfFirst { it.product == product }
        val (newItems, result) = if (existingIndex >= 0) {
            val existing = current.items[existingIndex]
            current.items.toMutableList().apply {
                this[existingIndex] = existing.copy(quantity = existing.quantity + 1)
            } to AddProductResult.IncrementedExisting
        } else {
            (current.items + OrderItem(product, quantity = 1)) to AddProductResult.Added
        }

        _state.value = ShoppingCartState(cartId = cartId, cartName = cartName, items = newItems)
        return result
    }

    override fun updateQuantity(product: Product, quantity: Int) {
        val current = _state.value
        val newItems = if (quantity <= 0) {
            current.items.filterNot { it.product == product }
        } else {
            current.items.map { if (it.product == product) it.copy(quantity = quantity) else it }
        }

        _state.value = if (newItems.isEmpty()) {
            ShoppingCartState()
        } else {
            current.copy(items = newItems)
        }
    }

    override fun clear() {
        _state.value = ShoppingCartState()
    }
}
