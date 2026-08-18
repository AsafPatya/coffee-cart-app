package com.coffeecart.app.nav

import coffeecart.composeapp.generated.resources.Res
import coffeecart.composeapp.generated.resources.strHome
import coffeecart.composeapp.generated.resources.strCoffeeCarts
import coffeecart.composeapp.generated.resources.strMyOrder
import coffeecart.composeapp.generated.resources.strProfile
import org.jetbrains.compose.resources.StringResource

/** The four bottom-nav tabs. Each is its own route; real screens replace the placeholders later. */
enum class Destination(val route: String, val label: StringResource) {
    Home("home", Res.string.strHome),
    CoffeeCart("coffee_carts", Res.string.strCoffeeCarts),
    Orders("orders", Res.string.strMyOrder),
    Profile("profile", Res.string.strProfile),
}

/** Sibling non-tab route patterns and navigation path builders. */
object Routes {
    const val COFFEE_CART_DETAILS = "coffee_cart_details/{cartId}"
    fun coffeeCartDetails(cartId: String): String = "coffee_cart_details/$cartId"

    const val COFFEE_CART_MENU_CATEGORIES = "coffee_cart_menu_categories/{cartId}"
    fun coffeeCartMenuCategories(cartId: String): String = "coffee_cart_menu_categories/$cartId"

    const val COFFEE_CART_CATEGORY_PRODUCTS = "coffee_cart_category_products/{cartId}/{categoryName}"
    fun coffeeCartCategoryProducts(cartId: String, categoryName: String): String = "coffee_cart_category_products/$cartId/$categoryName"

    const val COFFEE_CART_ADD_CATEGORY_WIZARD = "coffee_cart_add_category_wizard/{cartId}"
    fun coffeeCartAddCategoryWizard(cartId: String): String = "coffee_cart_add_category_wizard/$cartId"

    const val COFFEE_CART_EDIT = "coffee_cart_edit/{cartId}"
    fun coffeeCartEdit(cartId: String): String = "coffee_cart_edit/$cartId"
}

