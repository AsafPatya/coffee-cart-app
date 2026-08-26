package com.coffeecart.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class MenuCategory(
    val name: String,
    val imageUrl: String,
    val products: List<Product> = emptyList(),
    val description: String = "",
)

