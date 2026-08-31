package com.coffeecart.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class CoffeeCart(
    val id: String,
    val name: String,
    val address: String,
    val imageUrl: String,
    val categories: List<MenuCategory> = emptyList(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val paymentAccountId: String? = null,
    val paymentAccountVerified: Boolean = false,
    val openingHours: List<String> = emptyList(),
    val placeId: String? = null,
    val phone: String? = null,
    val cartImages: List<String> = emptyList(),
)
