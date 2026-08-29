package com.coffeecart.shared.feature.products

import com.coffeecart.shared.data.repository.ShoppingCartRepository
import com.coffeecart.shared.domain.CoffeeCartRepositoryInterface
import com.coffeecart.shared.model.CoffeeCart
import com.coffeecart.shared.model.MenuCategory
import com.coffeecart.shared.model.Product
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
private class TestCoffeeCartRepository(private val carts: List<CoffeeCart>) : CoffeeCartRepositoryInterface {
    override suspend fun getCoffeeCarts(): List<CoffeeCart> = carts
    override suspend fun fetchPlaceDetails(placeId: String) = error("Not used")
    override suspend fun addCoffeeCart(name: String, address: String, imageUrl: String, placeId: String?): CoffeeCart = error("Not used")
    override suspend fun updateCoffeeCart(id: String, name: String, address: String, imageUrl: String, placeId: String?, latitude: Double?, longitude: Double?): Boolean = error("Not used")
    override suspend fun removeCoffeeCart(id: String): Boolean = error("Not used")
    override suspend fun updateCoffeeCartFull(cart: CoffeeCart): Boolean = error("Not used")
    override suspend fun deleteCategory(cartId: String, categoryName: String): Boolean = error("Not used")
    override suspend fun uploadImage(bytes: ByteArray, fileName: String): String = error("Not used")
}

@OptIn(ExperimentalCoroutinesApi::class)
class ProductsViewModelTest {
    private val sampleProduct = Product("Espresso", 12.0, "Dark roast", "")
    private val sampleCart = CoffeeCart(
        id = "cart1",
        name = "Main Coffee Cart",
        address = "123 Main St",
        imageUrl = "",
        categories = listOf(
            MenuCategory("Hot Drinks", "", listOf(sampleProduct))
        )
    )

    @Test
    fun `loadProducts succeeds when cart and category exist`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)
        try {
            val repository = TestCoffeeCartRepository(listOf(sampleCart))
            val shoppingCartRepo = ShoppingCartRepository()
            val viewModel = ProductsViewModel(repository, shoppingCartRepo)

            viewModel.loadProducts("cart1", "Hot Drinks")
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertIs<ProductsUiState.Success>(state)
            assertEquals("Main Coffee Cart", state.cartName)
            assertEquals("Hot Drinks", state.categoryName)
            assertEquals(listOf(sampleProduct), state.products)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `loadProducts returns error when cart is not found`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)
        try {
            val repository = TestCoffeeCartRepository(listOf(sampleCart))
            val shoppingCartRepo = ShoppingCartRepository()
            val viewModel = ProductsViewModel(repository, shoppingCartRepo)

            viewModel.loadProducts("non_existent", "Hot Drinks")
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertIs<ProductsUiState.Error>(state)
            assertEquals("Coffee cart not found.", state.message)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `loadProducts returns error when category is not found`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)
        try {
            val repository = TestCoffeeCartRepository(listOf(sampleCart))
            val shoppingCartRepo = ShoppingCartRepository()
            val viewModel = ProductsViewModel(repository, shoppingCartRepo)

            viewModel.loadProducts("cart1", "Cold Drinks")
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertIs<ProductsUiState.Error>(state)
            assertEquals("Category 'Cold Drinks' not found.", state.message)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `addProductToCart emits snackbar message on success`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)
        try {
            val repository = TestCoffeeCartRepository(listOf(sampleCart))
            val shoppingCartRepo = ShoppingCartRepository()
            val viewModel = ProductsViewModel(repository, shoppingCartRepo)

            viewModel.loadProducts("cart1", "Hot Drinks")
            advanceUntilIdle()

            viewModel.addProductToCart("cart1", sampleProduct, 1, "", "added to basket")
            advanceUntilIdle()

            val shoppingCartState = shoppingCartRepo.state.value
            assertEquals("cart1", shoppingCartState.cartId)
            assertEquals(1, shoppingCartState.items.size)
            assertEquals(sampleProduct, shoppingCartState.items.first().product)
        } finally {
            Dispatchers.resetMain()
        }
    }
}



