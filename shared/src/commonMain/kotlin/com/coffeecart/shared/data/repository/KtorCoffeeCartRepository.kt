package com.coffeecart.shared.data.repository

import com.coffeecart.shared.contract.CoffeeCartDto
import com.coffeecart.shared.contract.CreateCoffeeCartRequest
import com.coffeecart.shared.contract.toModel
import com.coffeecart.shared.data.remote.ServerEnvironment
import com.coffeecart.shared.domain.CoffeeCartRepository
import com.coffeecart.shared.model.CoffeeCart
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class KtorCoffeeCartRepository(
    private val client: HttpClient,
) : CoffeeCartRepository {
    override suspend fun getCoffeeCarts(): List<CoffeeCart> =
        client.get("${ServerEnvironment.baseUrl}/carts").body<List<CoffeeCartDto>>().map { it.toModel() }

    suspend fun addCoffeeCart(request: CreateCoffeeCartRequest): CoffeeCart =
        client.post("${ServerEnvironment.baseUrl}/carts") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body<CoffeeCartDto>().toModel()

    suspend fun removeCoffeeCart(id: String) {
        client.delete("${ServerEnvironment.baseUrl}/carts/$id")
    }
}
