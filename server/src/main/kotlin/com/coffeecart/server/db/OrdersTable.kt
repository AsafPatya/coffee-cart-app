package com.coffeecart.server.db

import org.jetbrains.exposed.v1.core.Table

object OrdersTable : Table("orders") {
    val id = varchar("id", 64)
    val cartId = varchar("cart_id", 64)
    val itemsJson = text("items_json")
    val status = varchar("status", 32)
    val createdAt = long("created_at")
    val paymentStatus = varchar("payment_status", 32).default("PENDING")
    val checkoutUrl = text("checkout_url").nullable()
    val printed = bool("printed").default(false)

    override val primaryKey = PrimaryKey(id)
}
