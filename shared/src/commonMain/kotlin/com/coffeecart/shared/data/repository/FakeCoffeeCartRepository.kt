package com.coffeecart.shared.data.repository

import com.coffeecart.shared.domain.CoffeeCartRepository
import com.coffeecart.shared.model.CoffeeCart
import kotlinx.coroutines.delay

class FakeCoffeeCartRepository : CoffeeCartRepository {
    override suspend fun getCoffeeCarts(): List<CoffeeCart> {
        delay(500)
        return listOf(
            CoffeeCart(
                id = "1",
                name = "Downtown Espresso Cart",
                isOpen = true,
                address = "123 Main St",
                imageUrl = "https://picsum.photos/seed/1/200",
            ),
            CoffeeCart(
                id = "2",
                name = "Riverside Brew",
                isOpen = false,
                address = "45 River Rd",
                imageUrl = "https://picsum.photos/seed/2/200",
            ),
            CoffeeCart(
                id = "3",
                name = "Central Park Coffee",
                isOpen = true,
                address = "9 Park Ave",
                imageUrl = "https://picsum.photos/seed/3/200",
            ),
        )
    }
}
