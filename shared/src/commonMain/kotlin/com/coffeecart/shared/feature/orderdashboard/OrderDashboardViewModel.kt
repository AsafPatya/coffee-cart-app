package com.coffeecart.shared.feature.orderdashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coffeecart.shared.domain.OrderRepository
import com.coffeecart.shared.model.Order
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private const val POLL_INTERVAL_MS = 3000L

/** Polls a single coffee cart's orders while the dashboard screen is open. */
class OrderDashboardViewModel(
    private val repository: OrderRepository,
) : ViewModel() {
    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private var pollingCartId: String? = null

    fun start(cartId: String) {
        if (pollingCartId == cartId) return
        pollingCartId = cartId
        viewModelScope.launch {
            while (isActive) {
                try {
                    _orders.value = repository.getOrders(cartId)
                } catch (_: Exception) {
                    // Keep showing the last known orders; the next poll tick will retry.
                }
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    fun advance(orderId: String) {
        val cartId = pollingCartId ?: return
        viewModelScope.launch {
            try {
                val updated = repository.advanceOrder(cartId, orderId)
                _orders.value = _orders.value.map { if (it.id == updated.id) updated else it }
            } catch (_: Exception) {
                // Next poll tick will reconcile the real state.
            }
        }
    }
}
