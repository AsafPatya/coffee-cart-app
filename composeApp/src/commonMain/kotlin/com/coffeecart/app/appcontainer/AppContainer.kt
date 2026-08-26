package com.coffeecart.app.appcontainer

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.savedstate.read
import com.coffeecart.shared.data.repository.ShoppingCartRepository
import com.coffeecart.shared.feature.appcontainer.AppContainerViewModel
import org.koin.compose.koinInject

/**
 * AppContainer sets up the internal navigation host, bottom navigation, and top bar.
 * Owns the single app-wide Scaffold — individual screens are plain content, so system insets (status
 * bar, nav bar) are only ever reserved once, here.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppContainer(
    viewModel: AppContainerViewModel = koinInject(),
) {
    val navController = rememberNavController()
    val currentEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentEntry?.destination?.route
    var topBarTitle by remember { mutableStateOf<String?>(null) }

    val cartProductCount by viewModel.cartProductCount.collectAsState()

    Scaffold(
        topBar = {
            val categoryName = currentEntry?.arguments?.read { getStringOrNull("categoryName") }
            TopBar(
                currentRoute = currentRoute,
                categoryName = categoryName,
                topBarTitle = topBarTitle,
                onBackClick = { navController.popBackStack() }
            )
        },
        bottomBar = {
            BottomBar(
                currentRoute = currentRoute,
                cartProductCount = cartProductCount,
                onNavigate = { destination ->
                    val isCurrentTabActive = destination.isRouteActive(currentRoute)

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
        AppNavHost(
            navController = navController,
            innerPadding = innerPadding,
            onCartNameLoaded = { name -> topBarTitle = name }
        )
    }
}

@Preview
@Composable
private fun AppContainerPreview() {
    AppContainer(viewModel = AppContainerViewModel(shoppingCartRepository = ShoppingCartRepository()))
}

