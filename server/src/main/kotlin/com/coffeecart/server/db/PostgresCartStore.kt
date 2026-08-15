package com.coffeecart.server.db

import com.coffeecart.shared.model.CoffeeCart
import java.util.UUID
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

/** Postgres-backed cart storage. Replaces the old in-memory CartStore — data now survives restarts. */
class PostgresCartStore {
    init {
        seedIfEmpty()
    }

    fun getAll(): List<CoffeeCart> = transaction {
        CoffeeCartsTable.selectAll().map { it.toCoffeeCart() }
    }

    fun add(name: String, address: String, imageUrl: String): CoffeeCart {
        val cart = CoffeeCart(id = UUID.randomUUID().toString(), name = name, isOpen = true, address = address, imageUrl = imageUrl)
        transaction {
            CoffeeCartsTable.insert {
                it[id] = cart.id
                it[CoffeeCartsTable.name] = cart.name
                it[isOpen] = cart.isOpen
                it[CoffeeCartsTable.address] = cart.address
                it[CoffeeCartsTable.imageUrl] = cart.imageUrl
            }
        }
        return cart
    }

    fun remove(id: String): Boolean = transaction {
        CoffeeCartsTable.deleteWhere { CoffeeCartsTable.id eq id } > 0
    }

    private fun seedIfEmpty() = transaction {
        if (CoffeeCartsTable.selectAll().empty()) {
            listOf(
                CoffeeCart(id = "1", name = "Downtown Espresso Cart", isOpen = true, address = "123 Main St", imageUrl = "https://picsum.photos/seed/1/200"),
                CoffeeCart(id = "2", name = "Riverside Brew", isOpen = false, address = "45 River Rd", imageUrl = "https://picsum.photos/seed/2/200"),
                CoffeeCart(id = "3", name = "Central Park Coffee", isOpen = true, address = "9 Park Ave", imageUrl = "https://picsum.photos/seed/3/200"),
            ).forEach { cart ->
                CoffeeCartsTable.insert {
                    it[id] = cart.id
                    it[name] = cart.name
                    it[isOpen] = cart.isOpen
                    it[address] = cart.address
                    it[imageUrl] = cart.imageUrl
                }
            }
        }
    }

    private fun ResultRow.toCoffeeCart() = CoffeeCart(
        id = this[CoffeeCartsTable.id],
        name = this[CoffeeCartsTable.name],
        isOpen = this[CoffeeCartsTable.isOpen],
        address = this[CoffeeCartsTable.address],
        imageUrl = this[CoffeeCartsTable.imageUrl],
    )
}
