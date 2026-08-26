package com.coffeecart.app.appcontainer

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.savedstate.read
import com.coffeecart.app.screens.home.HomeScreen
import com.coffeecart.app.screens.MyOrderScreen
import com.coffeecart.app.screens.coffeecart.CoffeeCartCategoryProductsScreen
import com.coffeecart.app.screens.coffeecart.CoffeeCartDetailsScreen
import com.coffeecart.app.screens.coffeecart.CoffeeCartListScreen
import com.coffeecart.app.screens.coffeecart.CoffeeCartMenuCategoriesScreen
import com.coffeecart.app.screens.profile.CoffeeCartAddCategoryScreen
import com.coffeecart.app.screens.profile.EditCartScreen
import com.coffeecart.app.screens.profile.OrderDashboardScreen
import com.coffeecart.app.screens.profile.ProfileScreen
import com.coffeecart.shared.feature.profile.ProfileViewModel
import org.koin.compose.koinInject

@Composable
fun AppNavHost(
    navController: NavHostController,
    innerPadding: PaddingValues,
    onCartNameLoaded: (String) -> Unit,
) {
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
            HomeScreen(onCtaButtonClick = { navController.navigate(Destination.CoffeeCart.route) })
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
                onCartNameLoaded = onCartNameLoaded,
                onCtaClick = { id -> navController.navigate(Routes.coffeeCartMenuCategories(id)) }
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
            MyOrderScreen(
                onExploreCartsClick = {
                    navController.navigate(Destination.CoffeeCart.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = false
                        }
                        launchSingleTop = true
                        restoreState = false
                    }
                }
            )
        }
        composable(Destination.Profile.route) {
            ProfileScreen(
                onAddCategoryClick = { cartId ->
                    navController.navigate(Routes.coffeeCartAddCategoryWizard(cartId))
                },
                onEditCartClick = { cartId ->
                    navController.navigate(Routes.coffeeCartEdit(cartId))
                },
                onViewOrdersClick = { cartId ->
                    navController.navigate(Routes.orderDashboard(cartId))
                }
            )
        }
        composable(Routes.ORDER_DASHBOARD) { backStackEntry ->
            val cartId = backStackEntry.arguments?.read {
                getStringOrNull("cartId")
            } ?: ""
            OrderDashboardScreen(cartId = cartId)
        }
    }
}

