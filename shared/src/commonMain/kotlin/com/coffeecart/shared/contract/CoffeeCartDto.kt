package com.coffeecart.shared.contract

import com.coffeecart.shared.model.CoffeeCart
import kotlinx.serialization.Serializable

@Serializable
data class CoffeeCartDto(
    val id: String,
    val name: String,
    val address: String,
    val imageUrl: String,
)

fun CoffeeCartDto.toModel(): CoffeeCart = CoffeeCart(
    id = id,
    name = name,
    address = address,
    imageUrl = imageUrl,
)

fun CoffeeCart.toDto(): CoffeeCartDto = CoffeeCartDto(
    id = id,
    name = name,
    address = address,
    imageUrl = imageUrl,
)

@Serializable
data class CreateCoffeeCartRequest(
    val name: String,
    val address: String,
    val imageUrl: String,
)
