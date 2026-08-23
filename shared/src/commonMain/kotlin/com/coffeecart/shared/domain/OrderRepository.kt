package com.coffeecart.shared.domain

import com.coffeecart.shared.model.Order
import com.coffeecart.shared.model.OrderItem

interface OrderRepository {
    suspend fun submitOrder(cartId: String, items: List<OrderItem>): Order

    suspend fun getOrders(cartId: String): List<Order>

    suspend fun advanceOrder(cartId: String, orderId: String): Order

    suspend fun getUnprintedOrders(cartId: String): List<Order>

    suspend fun markOrderPrinted(cartId: String, orderId: String): Boolean
}
