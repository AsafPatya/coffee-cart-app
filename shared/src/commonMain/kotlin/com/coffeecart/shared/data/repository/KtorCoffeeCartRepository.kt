package com.coffeecart.shared.data.repository

import com.coffeecart.shared.contract.CoffeeCartDto
import com.coffeecart.shared.contract.CreateCoffeeCartRequest
import com.coffeecart.shared.contract.Endpoints
import com.coffeecart.shared.contract.toDto
import com.coffeecart.shared.contract.toModel
import com.coffeecart.shared.data.remote.ServerEnvironment
import com.coffeecart.shared.domain.CoffeeCartRepository
import com.coffeecart.shared.model.CoffeeCart
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class KtorCoffeeCartRepository(
    private val client: HttpClient,
) : CoffeeCartRepository {
    private val mutex = Mutex()
    private var cachedCarts: List<CoffeeCart>? = null

    override suspend fun getCoffeeCarts(): List<CoffeeCart> = mutex.withLock {
        cachedCarts?.let { return@withLock it }
        val carts = client.get("${ServerEnvironment.baseUrl}${Endpoints.CARTS}").body<List<CoffeeCartDto>>().map { it.toModel() }
        cachedCarts = carts
        carts
    }

    override suspend fun addCoffeeCart(name: String, address: String, imageUrl: String): CoffeeCart {
        val cart = client.post("${ServerEnvironment.baseUrl}${Endpoints.CARTS}") {
            contentType(ContentType.Application.Json)
            setBody(CreateCoffeeCartRequest(name = name, address = address, imageUrl = imageUrl))
        }.body<CoffeeCartDto>().toModel()
        mutex.withLock {
            cachedCarts = null
        }
        return cart
    }

    override suspend fun updateCoffeeCart(id: String, name: String, address: String, imageUrl: String): Boolean {
        // The server always persists the full cart on PUT, so fetch current categories first —
        // otherwise this name/address/image-only edit would silently wipe them out.
        val existingCategories = getCoffeeCarts().find { it.id == id }?.categories.orEmpty()
        return updateCoffeeCartFull(
            CoffeeCart(id = id, name = name, address = address, imageUrl = imageUrl, categories = existingCategories)
        )
    }

    override suspend fun removeCoffeeCart(id: String): Boolean {
        val response = client.delete("${ServerEnvironment.baseUrl}${Endpoints.cartById(id)}")
        val success = response.status == HttpStatusCode.NoContent
        if (success) {
            mutex.withLock {
                cachedCarts = null
            }
        }
        return success
    }

    override suspend fun updateCoffeeCartFull(cart: CoffeeCart): Boolean {
        val response = client.put("${ServerEnvironment.baseUrl}${Endpoints.cartById(cart.id)}") {
            contentType(ContentType.Application.Json)
            setBody(cart.toDto())
        }
        val success = response.status == HttpStatusCode.OK
        if (success) {
            mutex.withLock {
                cachedCarts = null
            }
        }
        return success
    }
}
