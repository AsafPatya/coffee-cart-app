package com.coffeecart.server.db

import org.jetbrains.exposed.v1.core.Table

object CoffeeCartsTable : Table("coffee_carts") {
    val id = varchar("id", 64)
    val name = varchar("name", 255)
    val isOpen = bool("is_open")
    val address = varchar("address", 255)
    val imageUrl = varchar("image_url", 1024)

    override val primaryKey = PrimaryKey(id)
}
