package com.coffeecart.shared.domain

import com.coffeecart.shared.model.CoffeeCart

interface CoffeeCartRepository {
    suspend fun getCoffeeCarts(): List<CoffeeCart>
}
