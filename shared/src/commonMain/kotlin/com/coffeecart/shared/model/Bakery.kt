package com.coffeecart.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class Bakery(
    val id: String,
    val name: String,
    val address: String,
    val imageUrl: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
)
