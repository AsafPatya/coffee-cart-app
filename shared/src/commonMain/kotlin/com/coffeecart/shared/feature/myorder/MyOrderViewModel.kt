package com.coffeecart.shared.feature.myorder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coffeecart.shared.domain.OrderRepository
import com.coffeecart.shared.domain.PaymentRepository
import com.coffeecart.shared.domain.ShoppingCartRepositoryInterface
import com.coffeecart.shared.domain.ShoppingCartState
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

    fun onCheckoutComplete(onPlacedMessage: String) {
        viewModelScope.launch {
            _checkoutUrl.value = null
            shoppingCartRepository.clear()
            _snackBarMessages.emit(onPlacedMessage)
        }
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

    fun updateQuantity(product: Product, quantity: Int) {
        shoppingCartRepository.updateQuantity(product, quantity)
    }

    fun updateItem(product: Product, quantity: Int, comment: String) {
        shoppingCartRepository.updateItem(product, quantity, comment)
    }
}

