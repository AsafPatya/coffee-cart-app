package com.coffeecart.app.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.coffeecart.app.nav.Destination

/**
 * Bottom navigation bar for standard app layout, rendering tab buttons based on [Destination].
 */
@Composable
fun BottomBar(
    currentRoute: String?,
    onNavigate: (Destination) -> Unit,
) {
    NavigationBar {
        Destination.entries.forEach { destination ->
            val isSelected = when (destination) {
                Destination.Home -> currentRoute == Destination.Home.route
                Destination.CoffeeCart -> currentRoute == Destination.CoffeeCart.route ||
                        currentRoute?.startsWith("coffee_cart_details") == true ||
                        currentRoute?.startsWith("coffee_cart_menu_categories") == true ||
                        currentRoute?.startsWith("coffee_cart_category_products") == true
                Destination.Orders -> currentRoute == Destination.Orders.route
                Destination.Profile -> currentRoute == Destination.Profile.route ||
                        currentRoute?.startsWith("coffee_cart_add_category_wizard") == true
            }
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(destination) },
                icon = { Icon(destination.icon, contentDescription = destination.label) },
                label = { Text(destination.label) },
            )
        }
    }
}

val Destination.icon: ImageVector
    get() = when (this) {
        Destination.Home -> Icons.Filled.Home
        Destination.CoffeeCart -> Icons.AutoMirrored.Filled.List
        Destination.Orders -> Icons.Filled.ShoppingCart
        Destination.Profile -> Icons.Filled.Person
    }

@Preview
@Composable
private fun BottomBarPreview() {
    BottomBar(
        currentRoute = Destination.Home.route,
        onNavigate = {},
    )
}

