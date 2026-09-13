package com.coffeecart.app.appcontainer

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.coffeecart.app.theme.Colors.ActiveIconColor
import com.coffeecart.app.theme.Colors.ActivePillColor
import com.coffeecart.app.theme.Colors.BadgeColor
import com.coffeecart.app.theme.Colors.InactiveColor
import org.jetbrains.compose.resources.stringResource

/**
 * Bottom navigation bar for standard app layout, rendering tab buttons based on [Destination].
 */


@Composable
fun BottomBar(
    currentRoute: String?,
    cartProductCount: Int,
    onNavigate: (Destination) -> Unit,
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp, // Removes default Material 3 tonal elevation overlay
    ) {
        Destination.entries.forEach { destination ->
            val isSelected = destination.isRouteActive(currentRoute)
            NavigationBarItem(
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = ActivePillColor,
                    selectedIconColor = ActiveIconColor,
                    selectedTextColor = ActiveIconColor,
                    unselectedIconColor = InactiveColor,
                    unselectedTextColor = InactiveColor,
                ),
                selected = isSelected,
                onClick = { onNavigate(destination) },
                icon = {
                    if (destination == Destination.Orders && cartProductCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge (
                                    containerColor = BadgeColor,
                                    contentColor = MaterialTheme.colorScheme.background
                                ){
                                    Text(text = cartProductCount.toString())
                                }
                            }
                        ) {
                            Icon(destination.icon, contentDescription = stringResource(destination.label))
                        }
                    } else {
                        Icon(destination.icon, contentDescription = stringResource(destination.label))
                    }
                },
                label = { Text(stringResource(destination.label)) },
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
        cartProductCount = 2,
        onNavigate = {},
    )
}
