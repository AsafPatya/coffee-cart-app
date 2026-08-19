package com.coffeecart.shared.data.repository

import com.coffeecart.shared.contract.Endpoints
import com.coffeecart.shared.data.remote.ServerEnvironment
import com.coffeecart.shared.domain.OrderRepository
import com.coffeecart.shared.model.Order
import com.coffeecart.shared.model.OrderItem
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class KtorOrderRepository(private val client: HttpClient) : OrderRepository {

    override suspend fun submitOrder(cartId: String, items: List<OrderItem>): Order =
        client.post("${ServerEnvironment.baseUrl}${Endpoints.cartOrders(cartId)}") {
            contentType(ContentType.Application.Json)
            setBody(items)
        }.body()

    override suspend fun getOrders(cartId: String): List<Order> =
        client.get("${ServerEnvironment.baseUrl}${Endpoints.cartOrders(cartId)}").body()

    override suspend fun advanceOrder(cartId: String, orderId: String): Order =
        client.post("${ServerEnvironment.baseUrl}${Endpoints.advanceOrder(cartId, orderId)}").body()
}
