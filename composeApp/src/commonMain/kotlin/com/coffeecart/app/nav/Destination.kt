package com.coffeecart.app.nav

/** The four bottom-nav tabs. Each is its own route; real screens replace the placeholders later. */
enum class Destination(val route: String, val label: String) {
    Home("home", "Home"),
    CoffeeCart("coffee_carts", "CoffeeCart"),
    Orders("orders", "Orders"),
    Profile("profile", "Profile"),
}

/** Sibling non-tab route patterns and navigation path builders. */
object Routes {
    const val COFFEE_CART_DETAILS = "coffee_cart_details/{cartId}"
    fun coffeeCartDetails(cartId: String): String = "coffee_cart_details/$cartId"

    const val COFFEE_CART_MENU_CATEGORIES = "coffee_cart_menu_categories/{cartId}"
    fun coffeeCartMenuCategories(cartId: String): String = "coffee_cart_menu_categories/$cartId"

    const val COFFEE_CART_CATEGORY_PRODUCTS = "coffee_cart_category_products/{cartId}/{categoryName}"
    fun coffeeCartCategoryProducts(cartId: String, categoryName: String): String = "coffee_cart_category_products/$cartId/$categoryName"
}

