package com.coffeecart.shared.domain

import com.coffeecart.shared.model.CoffeeCart

interface CoffeeCartRepository {
    suspend fun getCoffeeCarts(): List<CoffeeCart>
    suspend fun addCoffeeCart(name: String, address: String, imageUrl: String): CoffeeCart
    suspend fun updateCoffeeCart(id: String, name: String, address: String, imageUrl: String): Boolean
    suspend fun removeCoffeeCart(id: String): Boolean
}
