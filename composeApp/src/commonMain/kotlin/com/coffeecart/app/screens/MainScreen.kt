package com.coffeecart.app.screens

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.savedstate.read
import com.coffeecart.app.nav.Destination
import com.coffeecart.app.nav.Routes
import com.coffeecart.app.screens.coffeecart.CoffeeCartCategoryProductsScreen
import com.coffeecart.app.screens.coffeecart.CoffeeCartMenuCategoriesScreen
import com.coffeecart.app.screens.coffeecart.CoffeeCartDetailsScreen
import com.coffeecart.app.screens.coffeecart.CoffeeCartListScreen
import com.coffeecart.app.screens.profile.ProfileScreen
import com.coffeecart.app.screens.profile.CoffeeCartAddCategoryScreen
import com.coffeecart.app.screens.profile.EditCartScreen
import com.coffeecart.app.ui.BottomBar
import com.coffeecart.shared.feature.profile.ProfileViewModel
import org.koin.compose.koinInject

/**
 * Main application container screen that sets up the internal navigation host and bottom navigation.
 * Owns the single app-wide Scaffold — individual screens are plain content, so system insets (status
 * bar, nav bar) are only ever reserved once, here.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val currentEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentEntry?.destination?.route
    var topBarTitle by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            if (currentRoute == Routes.COFFEE_CART_DETAILS ||
                currentRoute == Routes.COFFEE_CART_MENU_CATEGORIES ||
                currentRoute == Routes.COFFEE_CART_CATEGORY_PRODUCTS ||
                currentRoute == Routes.COFFEE_CART_ADD_CATEGORY_WIZARD ||
                currentRoute == Routes.COFFEE_CART_EDIT
            ) {
                val titleString = when (currentRoute) {
                    Routes.COFFEE_CART_MENU_CATEGORIES -> "Menu Categories"
                    Routes.COFFEE_CART_CATEGORY_PRODUCTS -> {
                        currentEntry?.arguments?.read { getStringOrNull("categoryName") } ?: "Products"
                    }
                    Routes.COFFEE_CART_ADD_CATEGORY_WIZARD -> "Add Menu Category"
                    Routes.COFFEE_CART_EDIT -> "Edit Coffee Cart"
                    else -> topBarTitle
                }
                TopAppBar(
                    title = {
                        titleString?.let { Text(it) }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        },
        bottomBar = {
            BottomBar(
                currentRoute = currentRoute,
                onNavigate = { destination ->
                    val isCurrentTabActive = when (destination) {
                        Destination.Home -> currentRoute == Destination.Home.route
                        Destination.CoffeeCart -> currentRoute == Destination.CoffeeCart.route ||
                                currentRoute?.startsWith("coffee_cart_details") == true ||
                                currentRoute?.startsWith("coffee_cart_menu_categories") == true ||
                                currentRoute?.startsWith("coffee_cart_category_products") == true
                        Destination.Orders -> currentRoute == Destination.Orders.route
                        Destination.Profile -> currentRoute == Destination.Profile.route ||
                                currentRoute?.startsWith("coffee_cart_add_category_wizard") == true
                    }

                    if (isCurrentTabActive) {
                        if (currentRoute != destination.route) {
                            navController.navigate(destination.route) {
                                popUpTo(destination.route) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }
                    } else {
                        navController.navigate(destination.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = false
                            }
                            launchSingleTop = true
                            restoreState = false
                        }
                    }
                }
            )
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destination.Home.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None },
        ) {
            composable(Destination.Home.route) {
                HomeScreen(onNewOrderClick = { navController.navigate(Destination.CoffeeCart.route) })
            }
            composable(Destination.CoffeeCart.route) {
                CoffeeCartListScreen(onCartClick = { cartId -> navController.navigate(Routes.coffeeCartDetails(cartId)) })
            }
            composable(Routes.COFFEE_CART_DETAILS) { backStackEntry ->
                val cartId = backStackEntry.arguments?.read {
                    getStringOrNull("cartId")
                } ?: ""


                CoffeeCartDetailsScreen(
                    cartId = cartId,
                    onCartNameLoaded = { name -> topBarTitle = name },
                    onViewCategoriesClick = { id -> navController.navigate(Routes.coffeeCartMenuCategories(id)) }
                )
            }
            composable(Routes.COFFEE_CART_MENU_CATEGORIES) { backStackEntry ->
                val cartId = backStackEntry.arguments?.read {
                    getStringOrNull("cartId")
                } ?: ""
                CoffeeCartMenuCategoriesScreen(
                    cartId = cartId,
                    onCategoryClick = { categoryName ->
                        navController.navigate(Routes.coffeeCartCategoryProducts(cartId, categoryName))
                    }
                )
            }
            composable(Routes.COFFEE_CART_CATEGORY_PRODUCTS) { backStackEntry ->
                val cartId = backStackEntry.arguments?.read {
                    getStringOrNull("cartId")
                } ?: ""
                val categoryName = backStackEntry.arguments?.read {
                    getStringOrNull("categoryName")
                } ?: ""
                CoffeeCartCategoryProductsScreen(cartId = cartId, categoryName = categoryName)
            }
            composable(Routes.COFFEE_CART_ADD_CATEGORY_WIZARD) { backStackEntry ->
                val cartId = backStackEntry.arguments?.read {
                    getStringOrNull("cartId")
                } ?: ""
                CoffeeCartAddCategoryScreen(
                    cartId = cartId,
                    onSuccess = {
                        navController.popBackStack()
                    }
                )
            }
            composable(Routes.COFFEE_CART_EDIT) { backStackEntry ->
                val cartId = backStackEntry.arguments?.read {
                    getStringOrNull("cartId")
                } ?: ""
                val profileViewModel: ProfileViewModel = koinInject()
                EditCartScreen(
                    cartId = cartId,
                    onDismiss = { navController.popBackStack() },
                    onConfirm = { id, name, address, imageUrl, latitude, longitude ->
                        profileViewModel.editCoffeeCart(id, name, address, imageUrl, latitude, longitude)
                        navController.popBackStack()
                    }
                )
            }
            composable(Destination.Orders.route) {
                OrdersScreen()
            }
            composable(Destination.Profile.route) {
                ProfileScreen(
                    onAddCategoryClick = { cartId ->
                        navController.navigate(Routes.coffeeCartAddCategoryWizard(cartId))
                    },
                    onEditCartClick = { cartId ->
                        navController.navigate(Routes.coffeeCartEdit(cartId))
                    }
                )
            }
        }
    }
}

@Preview
@Composable
private fun MainScreenPreview() {
    MainScreen()
}

