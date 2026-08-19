package com.coffeecart.shared.model

import kotlinx.serialization.Serializable

enum class OrderStatus {
    ARRIVED, IN_PROGRESS, DONE;

    fun next(): OrderStatus = when (this) {
        ARRIVED -> IN_PROGRESS
        IN_PROGRESS -> DONE
        DONE -> DONE
    }
}

@Serializable
data class Order(
    val id: String,
    val cartId: String,
    val items: List<OrderItem>,
    val status: OrderStatus,
    val createdAt: Long,
)
