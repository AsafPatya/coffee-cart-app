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

enum class PaymentStatus { PENDING, PAID, FAILED }

@Serializable
data class Order(
    val id: String,
    val cartId: String,
    val items: List<OrderItem>,
    val status: OrderStatus,
    val createdAt: Long,
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    val checkoutUrl: String? = null,
)
