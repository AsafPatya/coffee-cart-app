package com.coffeecart.shared.domain

import com.coffeecart.shared.model.CoffeeCart

interface CoffeeCartRepository {
    suspend fun getCoffeeCarts(): List<CoffeeCart>
    suspend fun addCoffeeCart(name: String, address: String, imageUrl: String): CoffeeCart
    suspend fun updateCoffeeCart(
        id: String,
        name: String,
        address: String,
        imageUrl: String,
        latitude: Double? = null,
        longitude: Double? = null,
    ): Boolean
    suspend fun removeCoffeeCart(id: String): Boolean
    suspend fun updateCoffeeCartFull(cart: CoffeeCart): Boolean

    /** Uploads image bytes and returns the URL it's now servable from. Not tied to any particular cart. */
    suspend fun uploadImage(bytes: ByteArray, fileName: String): String
}
