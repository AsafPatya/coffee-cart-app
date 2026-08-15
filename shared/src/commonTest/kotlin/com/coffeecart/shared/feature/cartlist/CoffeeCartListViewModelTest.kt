package com.coffeecart.shared.feature.cartlist

import com.coffeecart.shared.domain.CoffeeCartRepository
import com.coffeecart.shared.model.CoffeeCart
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

private class SucceedingRepository(private val carts: List<CoffeeCart>) : CoffeeCartRepository {
    override suspend fun getCoffeeCarts(): List<CoffeeCart> = carts
}

private class FailingRepository(private val exception: Exception) : CoffeeCartRepository {
    override suspend fun getCoffeeCarts(): List<CoffeeCart> = throw exception
}

class CoffeeCartListViewModelTest {
    private val sampleCart = CoffeeCart(
        id = "1",
        name = "Downtown Espresso Cart",
        isOpen = true,
        address = "123 Main St",
        imageUrl = "https://example.com/cart.png",
    )

    @Test
    fun `starts loading then succeeds with carts from repository`() = runTest {
        val viewModel = CoffeeCartListViewModel(SucceedingRepository(listOf(sampleCart)))

        val state = viewModel.uiState.value
        assertIs<CoffeeCartListUiState.Success>(state)
        assertEquals(listOf(sampleCart), state.carts)
    }

    @Test
    fun `becomes an error state when the repository throws`() = runTest {
        val viewModel = CoffeeCartListViewModel(FailingRepository(RuntimeException("boom")))

        val state = viewModel.uiState.value
        assertIs<CoffeeCartListUiState.Error>(state)
        assertEquals("boom", state.message)
    }
}
