// shared/src/commonMain/kotlin/com/coffeecart/shared/model/CoffeeCart.kt
package com.coffeecart.shared.model

data class CoffeeCart(
    val id: String,
    val name: String,
    val isOpen: Boolean,
    val address: String,
    val imageUrl: String,
)
