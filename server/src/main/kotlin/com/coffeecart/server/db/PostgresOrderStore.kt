package com.coffeecart.server.db

import com.coffeecart.shared.model.Order
import com.coffeecart.shared.model.OrderItem
import com.coffeecart.shared.model.OrderStatus
import com.coffeecart.shared.model.PaymentStatus
import java.util.UUID
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.and
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

    fun getUnprintedByCart(cartId: String): List<Order> = transaction {
        OrdersTable.selectAll()
            .where { (OrdersTable.cartId eq cartId) and (OrdersTable.printed eq false) }
            .map { it.toOrder() }
    }

    fun markPrinted(orderId: String): Boolean = transaction {
        OrdersTable.update({ OrdersTable.id eq orderId }) { it[printed] = true } > 0
    }

    fun getById(orderId: String): Order? = transaction {
        OrdersTable.selectAll().where { OrdersTable.id eq orderId }.singleOrNull()?.toOrder()
    }

    fun setCheckoutUrl(orderId: String, url: String): Boolean = transaction {
        OrdersTable.update({ OrdersTable.id eq orderId }) { it[checkoutUrl] = url } > 0
    }

    /** Called from the Rapyd webhook once payment is confirmed. Returns null if the order isn't found. */
    fun markPaid(orderId: String): Order? = transaction {
        val row = OrdersTable.selectAll().where { OrdersTable.id eq orderId }.singleOrNull() ?: return@transaction null
        OrdersTable.update({ OrdersTable.id eq orderId }) {
            it[paymentStatus] = PaymentStatus.PAID.name
            it[checkoutUrl] = null
        }
        row.toOrder().copy(paymentStatus = PaymentStatus.PAID, checkoutUrl = null)
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
        paymentStatus = PaymentStatus.valueOf(this[OrdersTable.paymentStatus]),
        checkoutUrl = this[OrdersTable.checkoutUrl],
        printed = this[OrdersTable.printed],
    )
}
