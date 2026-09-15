package com.coffeecart.shared.contract

import com.coffeecart.shared.model.Bakery
import kotlinx.serialization.Serializable

@Serializable
data class BakeryDto(
    val id: String,
    val name: String,
    val address: String,
    val imageUrl: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
)

fun BakeryDto.toModel(): Bakery = Bakery(
    id = id,
    name = name,
    address = address,
    imageUrl = imageUrl,
    latitude = latitude,
    longitude = longitude,
)

fun Bakery.toDto(): BakeryDto = BakeryDto(
    id = id,
    name = name,
    address = address,
    imageUrl = imageUrl,
    latitude = latitude,
    longitude = longitude,
)
