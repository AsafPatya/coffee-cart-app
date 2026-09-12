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

/** Selected option names grouped by their customization's title, e.g. ("סוג החלב", "חלב שיבולת שועל") —
 *  a MULTIPLE customization's several picks are comma-joined into one entry. Skips customizations
 *  with nothing selected. In product-customization order. */
fun OrderItem.selectedOptionsByCustomization(): List<Pair<String, String>> =
    product.customizations.mapNotNull { customization ->
        val names = customization.options.filter { it.id in selectedOptionIds }.map { it.name }
        if (names.isEmpty()) null else customization.title to names.joinToString(", ")
    }
