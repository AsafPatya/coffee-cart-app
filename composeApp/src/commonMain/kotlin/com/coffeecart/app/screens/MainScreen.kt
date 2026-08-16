package com.coffeecart.app.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.coffeecart.app.nav.Destination
import com.coffeecart.app.ui.BottomBar
import androidx.compose.ui.tooling.preview.Preview
import com.coffeecart.app.screens.coffeecart.CoffeeCartListScreen

/**
 * Main application container screen that sets up the internal navigation host and bottom navigation.
 */
@Composable
fun MainScreen() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            val currentEntry by navController.currentBackStackEntryAsState()
            val currentRoute = currentEntry?.destination?.route

            BottomBar(
                currentRoute = currentRoute,
                onNavigate = { destination ->
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destination.Home.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Destination.Home.route) {
                HomeScreen(onNewOrderClick = { navController.navigate(Destination.CoffeeCart.route) })
            }
            composable(Destination.CoffeeCart.route) {
                CoffeeCartListScreen(onCartClick = { cartId -> navController.navigate(Destination.Orders.route) })
            }
            composable(Destination.Orders.route) {
                OrdersScreen()
            }
            composable(Destination.Profile.route) {
                ProfileScreen()
            }
        }
    }
}

@Preview
@Composable
private fun MainScreenPreview() {
    MainScreen()
}

