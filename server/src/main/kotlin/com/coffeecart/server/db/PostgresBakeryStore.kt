package com.coffeecart.server.db

import com.coffeecart.shared.model.Bakery
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

/** Postgres-backed bakery storage. Returns an empty list until real bakery rows are added. */
class PostgresBakeryStore {
    fun getAll(): List<Bakery> = transaction {
        BakeriesTable.selectAll().map { it.toBakery() }
    }

    private fun ResultRow.toBakery() = Bakery(
        id = this[BakeriesTable.id],
        name = this[BakeriesTable.name],
        address = this[BakeriesTable.address],
        imageUrl = this[BakeriesTable.imageUrl],
        latitude = this[BakeriesTable.latitude],
        longitude = this[BakeriesTable.longitude],
    )
}
