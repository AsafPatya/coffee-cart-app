package com.coffeecart.shared.data.repository

import com.coffeecart.shared.domain.CoffeeCartRepository
import com.coffeecart.shared.model.CoffeeCart
import kotlinx.coroutines.delay

class FakeCoffeeCartRepository : CoffeeCartRepository {
    private val carts = mutableListOf(
        CoffeeCart(
            id = "1",
            name = "Downtown Espresso Cart",
            address = "123 Main St",
            imageUrl = "https://picsum.photos/seed/1/200",
        ),
        CoffeeCart(
            id = "2",
            name = "Riverside Brew",
            address = "45 River Rd",
            imageUrl = "https://picsum.photos/seed/2/200",
        ),
        CoffeeCart(
            id = "3",
            name = "Central Park Coffee",
            address = "9 Park Ave",
            imageUrl = "https://picsum.photos/seed/3/200",
        ),
    )

    override suspend fun getCoffeeCarts(): List<CoffeeCart> {
        delay(500)
        return carts.toList()
    }

    override suspend fun addCoffeeCart(name: String, address: String, imageUrl: String): CoffeeCart {
        delay(200)
        val nextId = ((carts.mapNotNull { it.id.toIntOrNull() }.maxOrNull() ?: 0) + 1).toString()
        val newCart = CoffeeCart(
            id = nextId,
            name = name,
            address = address,
            imageUrl = imageUrl,
        )
        carts.add(newCart)
        return newCart
    }

    override suspend fun updateCoffeeCart(id: String, name: String, address: String, imageUrl: String): Boolean {
        delay(200)
        val idx = carts.indexOfFirst { it.id == id }
        if (idx == -1) return false
        carts[idx] = CoffeeCart(id = id, name = name, address = address, imageUrl = imageUrl)
        return true
    }

    override suspend fun removeCoffeeCart(id: String): Boolean {
        delay(200)
        return carts.removeAll { it.id == id }
    }
}
