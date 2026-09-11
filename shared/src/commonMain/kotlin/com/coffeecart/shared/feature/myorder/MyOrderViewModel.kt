package com.coffeecart.shared.feature.myorder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coffeecart.shared.domain.OrderRepository
import com.coffeecart.shared.domain.PaymentRepository
import com.coffeecart.shared.domain.ShoppingCartRepositoryInterface
import com.coffeecart.shared.domain.ShoppingCartState
import com.coffeecart.shared.model.Order
import com.coffeecart.shared.model.Product
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MyOrderViewModel(
    private val shoppingCartRepository: ShoppingCartRepositoryInterface,
    private val orderRepository: OrderRepository,
    private val paymentRepository: PaymentRepository,
) : ViewModel() {

    val cartState: StateFlow<ShoppingCartState> = shoppingCartRepository.state

    private val _isPlacingOrder = MutableStateFlow(false)
    val isPlacingOrder: StateFlow<Boolean> = _isPlacingOrder.asStateFlow()

    private val _checkoutUrl = MutableStateFlow<String?>(null)
    val checkoutUrl: StateFlow<String?> = _checkoutUrl.asStateFlow()

    private val _successOrder = MutableStateFlow<Order?>(null)
    val successOrder: StateFlow<Order?> = _successOrder.asStateFlow()

    private val _snackBarMessages = MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val snackBarMessages: SharedFlow<String> = _snackBarMessages.asSharedFlow()

    fun placeOrder() {
        val currentState = cartState.value
        val cartId = currentState.cartId
        if (cartId == null) {
            viewModelScope.launch {
                _snackBarMessages.emit("No active coffee cart found.")
            }
            return
        }
        viewModelScope.launch {
            _isPlacingOrder.value = true
            try {
                val order = orderRepository.submitOrder(cartId, currentState.items)
                val url = paymentRepository.createCheckout(cartId, order.id)
                _checkoutUrl.value = url
            } catch (e: Exception) {
                _snackBarMessages.emit(e.message ?: "Failed to start payment.")
            } finally {
                _isPlacingOrder.value = false
            }
        }
    }

    /** [url] is the completeUrl the payment provider redirected to, carrying the paid order's id
     *  as a query param — see Application.kt's completeUrl construction. */
    fun onCheckoutSuccess(url: String) {
        val orderId = extractQueryParam(url, "orderId") ?: return
        viewModelScope.launch {
            _successOrder.value = orderRepository.markOrderPaid(orderId)
            _checkoutUrl.value = null
            shoppingCartRepository.clear()
        }
    }

    fun dismissSuccessOrder() {
        _successOrder.value = null
    }

    fun onCheckoutError(message: String) {
        viewModelScope.launch {
            _checkoutUrl.value = null
            _snackBarMessages.emit(message)
        }
    }

    fun onCheckoutCancel() {
        _checkoutUrl.value = null
    }

    fun updateQuantity(product: Product, quantity: Int, selectedOptionIds: List<String> = emptyList()) {
        shoppingCartRepository.updateQuantity(product, quantity, selectedOptionIds)
    }

    fun updateItem(product: Product, quantity: Int, comment: String, selectedOptionIds: List<String> = emptyList()) {
        shoppingCartRepository.updateItem(product, quantity, comment, selectedOptionIds)
    }
}

private fun extractQueryParam(url: String, key: String): String? {
    val query = url.substringAfter('?', missingDelimiterValue = "")
    if (query.isEmpty()) return null
    return query.split("&")
        .map { it.split("=", limit = 2) }
        .firstOrNull { it.getOrNull(0) == key }
        ?.getOrNull(1)
}

/** Formats a price in Israeli shekels, e.g. 4.5 -> "₪4.50". */
fun formatPrice(price: Double): String {
    val agora = price.toString().substringAfter(".", "00").padEnd(2, '0').take(2)
    val shekels = price.toString().substringBefore(".")
    return "₪$shekels.$agora"
}

