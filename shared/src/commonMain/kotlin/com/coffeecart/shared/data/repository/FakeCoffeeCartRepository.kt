package com.coffeecart.shared.data.repository

import com.coffeecart.shared.domain.CoffeeCartRepository
import com.coffeecart.shared.model.CoffeeCart
import com.coffeecart.shared.model.MenuCategory
import com.coffeecart.shared.model.Product
import kotlinx.coroutines.delay

class FakeCoffeeCartRepository : CoffeeCartRepository {
    private val carts = mutableListOf(
        CoffeeCart(
            id = "1",
            name = "Downtown Espresso Cart",
            address = "123 Main St",
            imageUrl = "https://picsum.photos/seed/1/200",
            categories = listOf(
                MenuCategory(
                    name = "Espresso & Macchiato",
                    imageUrl = "https://picsum.photos/seed/cat1/400",
                    products = listOf(
                        Product(
                            name = "Caffè Latte",
                            price = 4.50,
                            description = "Rich espresso with steamed milk and a thin layer of foam.",
                            imageUrl = "https://picsum.photos/seed/latte/200"
                        ),
                        Product(
                            name = "Cappuccino",
                            price = 4.25,
                            description = "Espresso balanced with steamed milk and a thick layer of foam.",
                            imageUrl = "https://picsum.photos/seed/capp/200"
                        ),
                        Product(
                            name = "Americano",
                            price = 3.50,
                            description = "Espresso shots topped with hot water for a smooth finish.",
                            imageUrl = "https://picsum.photos/seed/amer/200"
                        )
                    )
                ),
                MenuCategory(
                    name = "Cold Brews",
                    imageUrl = "https://picsum.photos/seed/cat2/400",
                    products = listOf(
                        Product(
                            name = "Classic Cold Brew",
                            price = 4.00,
                            description = "Smooth, slow-steeped cold brew served over ice.",
                            imageUrl = "https://picsum.photos/seed/cold/200"
                        ),
                        Product(
                            name = "Nitro Cold Brew",
                            price = 4.75,
                            description = "Velvety-smooth cold brew infused with nitrogen for a rich head.",
                            imageUrl = "https://picsum.photos/seed/nitro/200"
                        )
                    )
                ),
                MenuCategory(
                    name = "Fresh Pastries",
                    imageUrl = "https://picsum.photos/seed/cat4/400",
                    products = listOf(
                        Product(
                            name = "Butter Croissant",
                            price = 3.50,
                            description = "Flaky, buttery traditional French pastry.",
                            imageUrl = "https://picsum.photos/seed/crois/200"
                        ),
                        Product(
                            name = "Chocolate Muffin",
                            price = 3.75,
                            description = "Decadent muffin packed with rich chocolate chips.",
                            imageUrl = "https://picsum.photos/seed/muff/200"
                        )
                    )
                )
            )
        ),
        CoffeeCart(
            id = "2",
            name = "Riverside Brew",
            address = "45 River Rd",
            imageUrl = "https://picsum.photos/seed/2/200",
            categories = listOf(
                MenuCategory(
                    name = "Classic Coffees",
                    imageUrl = "https://picsum.photos/seed/cat2/400",
                    products = listOf(
                        Product(
                            name = "Drip Coffee",
                            price = 3.00,
                            description = "Signature drip coffee brewed with premium house blend beans.",
                            imageUrl = "https://picsum.photos/seed/drip/200"
                        )
                    )
                ),
                MenuCategory(
                    name = "Baked Treats",
                    imageUrl = "https://picsum.photos/seed/cat4/400",
                    products = listOf(
                        Product(
                            name = "Cinnamon Roll",
                            price = 4.00,
                            description = "Warm pastry swirl with cinnamon sugar and cream cheese icing.",
                            imageUrl = "https://picsum.photos/seed/roll/200"
                        )
                    )
                )
            )
        ),
        CoffeeCart(
            id = "3",
            name = "Central Park Coffee",
            address = "9 Park Ave",
            imageUrl = "https://picsum.photos/seed/3/200",
            categories = listOf(
                MenuCategory(
                    name = "Park Specials",
                    imageUrl = "https://picsum.photos/seed/cat5/400",
                    products = listOf(
                        Product(
                            name = "Central Match Latte",
                            price = 5.00,
                            description = "Stone-ground matcha whisked with steamed milk.",
                            imageUrl = "https://picsum.photos/seed/matcha/200"
                        )
                    )
                )
            )
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

    override suspend fun updateCoffeeCart(
        id: String,
        name: String,
        address: String,
        imageUrl: String,
        latitude: Double?,
        longitude: Double?,
    ): Boolean {
        delay(200)
        val idx = carts.indexOfFirst { it.id == id }
        if (idx == -1) return false
        carts[idx] = CoffeeCart(id = id, name = name, address = address, imageUrl = imageUrl, latitude = latitude, longitude = longitude)
        return true
    }

    override suspend fun removeCoffeeCart(id: String): Boolean {
        delay(200)
        return carts.removeAll { it.id == id }
    }

    override suspend fun updateCoffeeCartFull(cart: CoffeeCart): Boolean {
        delay(200)
        val idx = carts.indexOfFirst { it.id == cart.id }
        if (idx == -1) return false
        carts[idx] = cart
        return true
    }
}
