package com.coffeecart.server.db

import org.jetbrains.exposed.v1.core.Table

object OrdersTable : Table("orders") {
    val id = varchar("id", 64)
    val cartId = varchar("cart_id", 64)
    val itemsJson = text("items_json")
    val status = varchar("status", 32)
    val createdAt = long("created_at")

    override val primaryKey = PrimaryKey(id)
}
