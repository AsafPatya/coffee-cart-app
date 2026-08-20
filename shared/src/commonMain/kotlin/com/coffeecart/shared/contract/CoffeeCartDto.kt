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
)

fun MenuCategory.toDto(): MenuCategoryDto = MenuCategoryDto(
    name = name,
    imageUrl = imageUrl,
    products = products.map { it.toDto() },
)

fun CoffeeCartDto.toModel(): CoffeeCart = CoffeeCart(
    id = id,
    name = name,
    address = address,
    imageUrl = imageUrl,
    categories = categories.map { it.toModel() },
    latitude = latitude,
    longitude = longitude,
)

fun CoffeeCart.toDto(): CoffeeCartDto = CoffeeCartDto(
    id = id,
    name = name,
    address = address,
    imageUrl = imageUrl,
    categories = categories.map { it.toDto() },
    latitude = latitude,
    longitude = longitude,
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
)
