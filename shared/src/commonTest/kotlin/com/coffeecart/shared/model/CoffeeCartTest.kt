// shared/src/commonTest/kotlin/com/coffeecart/shared/model/CoffeeCartTest.kt
package com.coffeecart.shared.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class CoffeeCartTest {
    @Test
    fun `holds all fields as provided`() {
        val cart = CoffeeCart(
            id = "1",
            name = "Downtown Espresso Cart",
            address = "123 Main St",
            imageUrl = "https://example.com/cart.png",
        )

        assertEquals("1", cart.id)
        assertEquals("Downtown Espresso Cart", cart.name)
        assertEquals("123 Main St", cart.address)
        assertEquals("https://example.com/cart.png", cart.imageUrl)
    }
}
