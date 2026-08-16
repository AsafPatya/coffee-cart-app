package com.coffeecart.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val name: String,
    val price: Double,
    val description: String,
    val imageUrl: String,
)

