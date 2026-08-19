package com.coffeecart.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class OrderItem(val product: Product, val quantity: Int, val comment: String = "")
