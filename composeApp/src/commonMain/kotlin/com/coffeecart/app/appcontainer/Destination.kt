package com.coffeecart.app.appcontainer

import coffeecart.composeapp.generated.resources.Res
import coffeecart.composeapp.generated.resources.strHome
import coffeecart.composeapp.generated.resources.strCoffeeCarts
import coffeecart.composeapp.generated.resources.strMyOrder
import coffeecart.composeapp.generated.resources.strProfile
import org.jetbrains.compose.resources.StringResource

/** The four bottom-nav tabs. Each is its own route; real screens replace the placeholders later. */
enum class Destination(
    val route: String,
    val label: StringResource,
    val activeSubRoutes: List<String> = emptyList()
) {
    Home("home", Res.string.strHome),
    CoffeeCart(
        "coffee_carts",
        Res.string.strCoffeeCarts,
        listOf(
            Routes.COFFEE_CART_DETAILS,
            Routes.COFFEE_CART_MENU_CATEGORIES,
            Routes.COFFEE_CART_CATEGORY_PRODUCTS
        )
    ),
    Orders("orders", Res.string.strMyOrder),
    Profile(
        "profile",
        Res.string.strProfile,
        listOf(
            Routes.COFFEE_CART_ADD_FROM_GOOGLE,
            Routes.COFFEE_CART_ADD_CATEGORY_WIZARD,
            Routes.COFFEE_CART_EDIT,
            Routes.COFFEE_CART_SELECT_CATEGORY_TO_EDIT,
            Routes.COFFEE_CART_EDIT_CATEGORY
        )
    );

    fun isRouteActive(currentRoute: String?): Boolean {
        if (currentRoute == null) return false
        return currentRoute == route || activeSubRoutes.contains(currentRoute)
    }
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

    const val COFFEE_CART_SELECT_CATEGORY_TO_EDIT = "coffee_cart_select_category_to_edit/{cartId}"
    fun coffeeCartSelectCategoryToEdit(cartId: String): String = "coffee_cart_select_category_to_edit/$cartId"

    const val COFFEE_CART_EDIT_CATEGORY = "coffee_cart_edit_category/{cartId}/{categoryName}"
    fun coffeeCartEditCategory(cartId: String, categoryName: String): String = "coffee_cart_edit_category/$cartId/$categoryName"

    const val ORDER_DASHBOARD = "order_dashboard/{cartId}"
    fun orderDashboard(cartId: String): String = "order_dashboard/$cartId"

    const val COFFEE_CART_ADD_FROM_GOOGLE = "coffee_cart_add_from_google"
}

