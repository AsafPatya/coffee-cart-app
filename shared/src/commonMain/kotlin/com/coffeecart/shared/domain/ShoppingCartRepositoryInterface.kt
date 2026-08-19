package com.coffeecart.shared.domain

import com.coffeecart.shared.model.OrderItem
import com.coffeecart.shared.model.Product
import kotlinx.coroutines.flow.StateFlow

data class ShoppingCartState(
    val cartId: String? = null,
    val cartName: String? = null,
    val items: List<OrderItem> = emptyList(),
)

enum class AddProductResult { Added, IncrementedExisting, BlockedDifferentCart }

/**
 * Local-only basket for the current in-progress order — a single coffee cart at a time.
 * Not persisted; resets on app restart. No product id is needed: line items are matched by the
 * [Product] value itself, since two genuinely different products won't share every field.
 */
interface ShoppingCartRepositoryInterface {
    val state: StateFlow<ShoppingCartState>

    fun addProduct(cartId: String, cartName: String, product: Product): AddProductResult

    /** Setting quantity to 0 or less removes the line item. */
    fun updateQuantity(product: Product, quantity: Int)

    fun clear()
}
