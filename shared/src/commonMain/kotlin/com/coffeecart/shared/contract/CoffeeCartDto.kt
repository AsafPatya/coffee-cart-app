package com.coffeecart.shared.contract

import com.coffeecart.shared.model.CoffeeCart
import com.coffeecart.shared.model.MenuCategory
import com.coffeecart.shared.model.Product
import kotlinx.serialization.Serializable

@Serializable
data class ProductDto(
    val name: String,
    val price: Double,
    val description: String,
    val imageUrl: String,
)

@Serializable
data class MenuCategoryDto(
    val name: String,
    val imageUrl: String,
    val products: List<ProductDto> = emptyList(),
    val description: String = "",
)

@Serializable
data class CoffeeCartDto(
    val id: String,
    val name: String,
    val address: String,
    val imageUrl: String,
    val categories: List<MenuCategoryDto> = emptyList(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val openingHours: List<String> = emptyList(),
    val placeId: String? = null,
    val phone: String? = null,
)

fun ProductDto.toModel(): Product = Product(
    name = name,
    price = price,
    description = description,
    imageUrl = imageUrl,
)

fun Product.toDto(): ProductDto = ProductDto(
    name = name,
    price = price,
    description = description,
    imageUrl = imageUrl,
)

fun MenuCategoryDto.toModel(): MenuCategory = MenuCategory(
    name = name,
    imageUrl = imageUrl,
    products = products.map { it.toModel() },
    description = description,
)

fun MenuCategory.toDto(): MenuCategoryDto = MenuCategoryDto(
    name = name,
    imageUrl = imageUrl,
    products = products.map { it.toDto() },
    description = description,
)

fun CoffeeCartDto.toModel(): CoffeeCart = CoffeeCart(
    id = id,
    name = name,
    address = address,
    imageUrl = imageUrl,
    categories = categories.map { it.toModel() },
    latitude = latitude,
    longitude = longitude,
    openingHours = openingHours,
    placeId = placeId,
    phone = phone,
)

fun CoffeeCart.toDto(): CoffeeCartDto = CoffeeCartDto(
    id = id,
    name = name,
    address = address,
    imageUrl = imageUrl,
    categories = categories.map { it.toDto() },
    latitude = latitude,
    longitude = longitude,
    openingHours = openingHours,
    placeId = placeId,
    phone = phone,
)

@Serializable
data class UploadImageResponse(val url: String)

@Serializable
data class PaymentAccountResponse(val url: String)

@Serializable
data class CheckoutResponse(val url: String)

@Serializable
data class CreateCoffeeCartRequest(
    val name: String,
    val address: String,
    val imageUrl: String,
    val placeId: String? = null,
    val phone: String? = null,
)
