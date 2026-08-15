package com.coffeecart.app.nav

/** The four bottom-nav tabs. Each is its own route; real screens replace the placeholders later. */
enum class Destination(val route: String, val label: String) {
    Home("home", "Home"),
    CoffeeCart("coffee_carts", "CoffeeCart"),
    Orders("orders", "Orders"),
    Profile("profile", "Profile"),
}
