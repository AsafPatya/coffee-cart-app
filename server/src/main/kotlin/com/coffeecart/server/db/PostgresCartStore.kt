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

    fun getById(cartId: String): CoffeeCart? = transaction {
        CoffeeCartsTable.selectAll().where { CoffeeCartsTable.id eq cartId }.singleOrNull()?.toCoffeeCart()
    }

    fun setPaymentAccount(cartId: String, accountId: String): Boolean = transaction {
        CoffeeCartsTable.update({ CoffeeCartsTable.id eq cartId }) {
            it[paymentAccountId] = accountId
            it[paymentAccountVerified] = false
        } > 0
    }

    fun setPaymentAccountVerified(accountId: String, verified: Boolean): Boolean = transaction {
        CoffeeCartsTable.update({ CoffeeCartsTable.paymentAccountId eq accountId }) {
            it[paymentAccountVerified] = verified
        } > 0
    }

    fun add(name: String, address: String, imageUrl: String, placeId: String? = null, phone: String? = null, cartImages: List<String> = emptyList()): CoffeeCart {
        val cart = CoffeeCart(id = UUID.randomUUID().toString(), name = name, address = address, imageUrl = imageUrl, placeId = placeId, phone = phone, cartImages = cartImages)
        transaction {
            CoffeeCartsTable.insert {
                it[id] = cart.id
                it[CoffeeCartsTable.name] = cart.name
                it[CoffeeCartsTable.address] = cart.address
                it[CoffeeCartsTable.imageUrl] = cart.imageUrl
                it[CoffeeCartsTable.placeId] = cart.placeId
                it[CoffeeCartsTable.phone] = cart.phone
                it[CoffeeCartsTable.cartImages] = cart.cartImages.joinToString("\n").ifBlank { null }
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
            it[latitude] = cart.latitude
            it[longitude] = cart.longitude
            it[openingHours] = cart.openingHours.joinToString("\n").ifBlank { null }
            it[placeId] = cart.placeId
            it[phone] = cart.phone
            it[cartImages] = cart.cartImages.joinToString("\n").ifBlank { null }
        } > 0
    }

    fun updateOpeningHours(cartId: String, hours: List<String>, googlePlaceId: String? = null): Boolean = transaction {
        CoffeeCartsTable.update({ CoffeeCartsTable.id eq cartId }) {
            it[openingHours] = hours.joinToString("\n").ifBlank { null }
            if (googlePlaceId != null) {
                it[placeId] = googlePlaceId
            }
        } > 0
    }

    fun deleteCategory(cartId: String, categoryName: String): Boolean = transaction {
        val cart = getById(cartId) ?: return@transaction false
        val updatedCategories = cart.categories.filter { it.name != categoryName }
        val updatedCart = cart.copy(categories = updatedCategories)
        updateFull(updatedCart)
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
        } ?: emptyList(),
        latitude = this[CoffeeCartsTable.latitude],
        longitude = this[CoffeeCartsTable.longitude],
        paymentAccountId = this[CoffeeCartsTable.paymentAccountId],
        paymentAccountVerified = this[CoffeeCartsTable.paymentAccountVerified],
        openingHours = this[CoffeeCartsTable.openingHours]?.split("\n")?.filter { it.isNotBlank() } ?: emptyList(),
        placeId = this[CoffeeCartsTable.placeId],
        phone = this[CoffeeCartsTable.phone],
        cartImages = this[CoffeeCartsTable.cartImages]?.split("\n")?.filter { it.isNotBlank() } ?: emptyList(),
    )
}
