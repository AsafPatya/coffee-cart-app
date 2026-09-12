package com.coffeecart.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class OrderItem(
    val product: Product,
    val quantity: Int,
    val comment: String = "",
    /** Ids of chosen [CustomizationOption]s, flattened across all of the product's [Customization]s —
     *  option ids are unique per product, so this doesn't need to be grouped by customization. */
    val selectedOptionIds: List<String> = emptyList(),
)

/** Unit price including the extra cost of any selected customization options. */
fun OrderItem.unitPrice(): Double {
    val allOptions = product.customizations.flatMap { it.options }
    val extra = selectedOptionIds.sumOf { id -> allOptions.find { it.id == id }?.priceExtra ?: 0.0 }
    return product.price + extra
}

fun OrderItem.lineTotal(): Double = unitPrice() * quantity

/** Names of the selected customization options, in product-customization order. */
fun OrderItem.selectedOptionNames(): List<String> {
    val allOptions = product.customizations.flatMap { it.options }
    return selectedOptionIds.mapNotNull { id -> allOptions.find { it.id == id }?.name }
}
