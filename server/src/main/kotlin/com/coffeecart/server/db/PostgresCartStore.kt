package com.coffeecart.server.db

import com.coffeecart.shared.model.CoffeeCart
import com.coffeecart.shared.model.MenuCategory
import java.util.UUID
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
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
        val cart = CoffeeCart(id = UUID.randomUUID().toString(), name = name, address = address, imageUrl = imageUrl)
        transaction {
            CoffeeCartsTable.insert {
                it[id] = cart.id
                it[CoffeeCartsTable.name] = cart.name
                it[CoffeeCartsTable.address] = cart.address
                it[CoffeeCartsTable.imageUrl] = cart.imageUrl
            }
        }
        return cart
    }

    fun remove(cartId: String): Boolean = transaction {
        CoffeeCartsTable.deleteWhere { CoffeeCartsTable.id eq cartId } > 0
    }

    fun update(cartId: String, name: String, address: String, imageUrl: String): Boolean = transaction {
        CoffeeCartsTable.update({ CoffeeCartsTable.id eq cartId }) {
            it[CoffeeCartsTable.name] = name
            it[CoffeeCartsTable.address] = address
            it[CoffeeCartsTable.imageUrl] = imageUrl
        } > 0
    }

    fun updateFull(cart: CoffeeCart): Boolean = transaction {
        val jsonString = Json.encodeToString(cart.categories)
        CoffeeCartsTable.update({ CoffeeCartsTable.id eq cart.id }) {
            it[CoffeeCartsTable.name] = cart.name
            it[CoffeeCartsTable.address] = cart.address
            it[CoffeeCartsTable.imageUrl] = cart.imageUrl
            it[menuJson] = jsonString
        } > 0
    }

    private fun seedIfEmpty() = transaction {
        if (CoffeeCartsTable.selectAll().empty()) {
            listOf(
                CoffeeCart(id = "1", name = "Downtown Espresso Cart", address = "123 Main St", imageUrl = "https://picsum.photos/seed/1/200"),
                CoffeeCart(id = "2", name = "Riverside Brew", address = "45 River Rd", imageUrl = "https://picsum.photos/seed/2/200"),
                CoffeeCart(id = "3", name = "Central Park Coffee", address = "9 Park Ave", imageUrl = "https://picsum.photos/seed/3/200"),
            ).forEach { cart ->
                CoffeeCartsTable.insert {
                    it[id] = cart.id
                    it[name] = cart.name
                    it[address] = cart.address
                    it[imageUrl] = cart.imageUrl
                }
            }
        }
    }

    private fun ResultRow.toCoffeeCart() = CoffeeCart(
        id = this[CoffeeCartsTable.id],
        name = this[CoffeeCartsTable.name],
        address = this[CoffeeCartsTable.address],
        imageUrl = this[CoffeeCartsTable.imageUrl],
        categories = this[CoffeeCartsTable.menuJson]?.let {
            try {
                Json.decodeFromString<List<MenuCategory>>(it)
            } catch (e: Exception) {
                emptyList()
            }
        } ?: emptyList()
    )
}
