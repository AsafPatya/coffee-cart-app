package com.coffeecart.shared.feature.cartlist

import com.coffeecart.shared.domain.CoffeeCartRepositoryInterface
import com.coffeecart.shared.model.CoffeeCart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)

private class SucceedingRepositoryInterface(private val carts: List<CoffeeCart>) : CoffeeCartRepositoryInterface {
    override suspend fun getCoffeeCarts(): List<CoffeeCart> = carts
    
    override suspend fun addCoffeeCart(name: String, address: String, imageUrl: String): CoffeeCart {
        error("Not used in this test suite")
    }

    override suspend fun updateCoffeeCart(
        id: String,
        name: String,
        address: String,
        imageUrl: String,
        latitude: Double?,
        longitude: Double?,
    ): Boolean {
        error("Not used in this test suite")
    }

    override suspend fun removeCoffeeCart(id: String): Boolean {
        error("Not used in this test suite")
    }

    override suspend fun updateCoffeeCartFull(cart: CoffeeCart): Boolean {
        error("Not used in this test suite")
    }

    override suspend fun uploadImage(bytes: ByteArray, fileName: String): String {
        error("Not used in this test suite")
    }
}

private class FailingRepositoryInterface(private val exception: Exception) : CoffeeCartRepositoryInterface {
    override suspend fun getCoffeeCarts(): List<CoffeeCart> = throw exception

    override suspend fun addCoffeeCart(name: String, address: String, imageUrl: String): CoffeeCart = throw exception

    override suspend fun updateCoffeeCart(
        id: String,
        name: String,
        address: String,
        imageUrl: String,
        latitude: Double?,
        longitude: Double?,
    ): Boolean = throw exception

    override suspend fun removeCoffeeCart(id: String): Boolean = throw exception

    override suspend fun updateCoffeeCartFull(cart: CoffeeCart): Boolean = throw exception

    override suspend fun uploadImage(bytes: ByteArray, fileName: String): String = throw exception
}

class CoffeeCartListViewModelTest {
    private val sampleCart = CoffeeCart(
        id = "1",
        name = "Downtown Espresso Cart",
        address = "123 Main St",
        imageUrl = "https://example.com/cart.png",
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `starts loading then succeeds with carts from repository`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)
        try {
            val viewModel = CoffeeCartListViewModel(SucceedingRepositoryInterface(listOf(sampleCart)))
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertIs<CoffeeCartListUiState.Success>(state)
            assertEquals(listOf(sampleCart to null), state.carts)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `becomes an error state when the repository throws`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)
        try {
            val viewModel = CoffeeCartListViewModel(FailingRepositoryInterface(RuntimeException("boom")))
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertIs<CoffeeCartListUiState.Error>(state)
            assertEquals("boom", state.message)
        } finally {
            Dispatchers.resetMain()
        }
    }
}
