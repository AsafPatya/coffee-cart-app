package com.coffeecart.server

import com.coffeecart.shared.model.CoffeeCart

/** In-memory owner-managed cart list. No persistence yet — resets on server restart. */
class CartStore {
    private val carts = mutableListOf(
        CoffeeCart(id = "1", name = "Downtown Espresso Cart", isOpen = true, address = "123 Main St", imageUrl = "https://picsum.photos/seed/1/200"),
        CoffeeCart(id = "2", name = "Riverside Brew", isOpen = false, address = "45 River Rd", imageUrl = "https://picsum.photos/seed/2/200"),
        CoffeeCart(id = "3", name = "Central Park Coffee", isOpen = true, address = "9 Park Ave", imageUrl = "https://picsum.photos/seed/3/200"),
    )
    private var nextId = carts.size + 1

    @Synchronized
    fun getAll(): List<CoffeeCart> = carts.toList()

    @Synchronized
    fun add(name: String, address: String, imageUrl: String): CoffeeCart {
        val cart = CoffeeCart(id = (nextId++).toString(), name = name, isOpen = true, address = address, imageUrl = imageUrl)
        carts.add(cart)
        return cart
    }

    @Synchronized
    fun remove(id: String): Boolean = carts.removeAll { it.id == id }
}
