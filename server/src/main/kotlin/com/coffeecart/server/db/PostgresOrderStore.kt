package com.coffeecart.server.db

import com.coffeecart.shared.model.Order
import com.coffeecart.shared.model.OrderItem
import com.coffeecart.shared.model.OrderStatus
import java.util.UUID
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update

/** Postgres-backed order storage, one row per submitted order. */
class PostgresOrderStore {

    fun getByCart(cartId: String): List<Order> = transaction {
        OrdersTable.selectAll().where { OrdersTable.cartId eq cartId }.map { it.toOrder() }
    }

    fun create(cartId: String, items: List<OrderItem>): Order = transaction {
        val order = Order(
            id = UUID.randomUUID().toString(),
            cartId = cartId,
            items = items,
            status = OrderStatus.ARRIVED,
            createdAt = System.currentTimeMillis(),
        )
        OrdersTable.insert {
            it[id] = order.id
            it[OrdersTable.cartId] = order.cartId
            it[itemsJson] = Json.encodeToString(order.items)
            it[status] = order.status.name
            it[createdAt] = order.createdAt
        }
        order
    }

    /** Advances the order to its next status (a no-op once already DONE). Returns null if not found. */
    fun advance(orderId: String): Order? = transaction {
        val row = OrdersTable.selectAll().where { OrdersTable.id eq orderId }.singleOrNull() ?: return@transaction null
        val next = OrderStatus.valueOf(row[OrdersTable.status]).next()
        OrdersTable.update({ OrdersTable.id eq orderId }) { it[status] = next.name }
        row.toOrder().copy(status = next)
    }

    private fun ResultRow.toOrder() = Order(
        id = this[OrdersTable.id],
        cartId = this[OrdersTable.cartId],
        items = Json.decodeFromString(this[OrdersTable.itemsJson]),
        status = OrderStatus.valueOf(this[OrdersTable.status]),
        createdAt = this[OrdersTable.createdAt],
    )
}
