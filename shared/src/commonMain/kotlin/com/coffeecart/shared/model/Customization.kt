package com.coffeecart.shared.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class CustomizationType {
    @SerialName("single") SINGLE,
    @SerialName("multiple") MULTIPLE,
}

@Serializable
data class CustomizationOption(
    val id: String,
    val name: String,
    val priceExtra: Double = 0.0,
    val imageUrl: String? = null,
)

@Serializable
data class Customization(
    val id: String,
    val title: String,
    val type: CustomizationType,
    val required: Boolean = false,
    val minSelect: Int = 0,
    val maxSelect: Int = 1,
    val options: List<CustomizationOption> = emptyList(),
)
