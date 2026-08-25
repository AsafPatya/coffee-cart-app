package com.coffeecart.shared.data.repository

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FakeCoffeeCartRepositoryInterfaceTest {
    @Test
    fun `returns three non-empty carts`() = runTest {
        val repository = FakeCoffeeCartRepositoryInterface()

        val carts = repository.getCoffeeCarts()

        assertEquals(3, carts.size)
        assertTrue(carts.all { it.name.isNotBlank() && it.address.isNotBlank() && it.imageUrl.isNotBlank() })
    }
}
