package com.coffeecart.server.db

import org.jetbrains.exposed.v1.core.Table

object BakeriesTable : Table("bakeries") {
    val id = varchar("id", 64)
    val name = varchar("name", 255)
    val address = varchar("address", 255)
    val imageUrl = varchar("image_url", 1024)
    val latitude = double("latitude").nullable()
    val longitude = double("longitude").nullable()

    override val primaryKey = PrimaryKey(id)
}
