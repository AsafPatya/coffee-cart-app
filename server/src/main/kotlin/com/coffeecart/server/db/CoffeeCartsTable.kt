package com.coffeecart.server.db

import org.jetbrains.exposed.v1.core.Table

object CoffeeCartsTable : Table("coffee_carts") {
    val id = varchar("id", 64)
    val name = varchar("name", 255)
    val address = varchar("address", 255)
    val imageUrl = varchar("image_url", 1024)
    val menuJson = text("menu_json").nullable()
    val latitude = double("latitude").nullable()
    val longitude = double("longitude").nullable()
    val paymentAccountId = varchar("payment_account_id", 128).nullable()
    val paymentAccountVerified = bool("payment_account_verified").default(false)
    val openingHours = text("opening_hours").nullable()
    val placeId = varchar("place_id", 128).nullable()
    val phone = varchar("phone", 64).nullable()
    val cartImages = text("cart_images").nullable()

    override val primaryKey = PrimaryKey(id)
}
