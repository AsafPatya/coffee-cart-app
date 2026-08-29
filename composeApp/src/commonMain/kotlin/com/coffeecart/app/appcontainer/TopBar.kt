package com.coffeecart.app.appcontainer

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import coffeecart.composeapp.generated.resources.Res
import coffeecart.composeapp.generated.resources.strAddMenuCategory
import coffeecart.composeapp.generated.resources.strBack
import coffeecart.composeapp.generated.resources.strEditCoffeeCart
import coffeecart.composeapp.generated.resources.strMenu
import coffeecart.composeapp.generated.resources.strOrders
import coffeecart.composeapp.generated.resources.strProducts
import org.jetbrains.compose.resources.stringResource

/**
 * TopBar component for application navigation and actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    currentRoute: String?,
    categoryName: String?,
    topBarTitle: String?,
    onBackClick: () -> Unit,
) {
    if (currentRoute == Routes.COFFEE_CART_MENU_CATEGORIES ||
        currentRoute == Routes.COFFEE_CART_CATEGORY_PRODUCTS ||
        currentRoute == Routes.COFFEE_CART_ADD_CATEGORY_WIZARD ||
        currentRoute == Routes.COFFEE_CART_EDIT ||
        currentRoute == Routes.ORDER_DASHBOARD
    ) {
        val titleString = when (currentRoute) {
            Routes.COFFEE_CART_MENU_CATEGORIES -> stringResource(Res.string.strMenu)
            Routes.COFFEE_CART_CATEGORY_PRODUCTS -> categoryName ?: stringResource(Res.string.strProducts)
            Routes.COFFEE_CART_ADD_CATEGORY_WIZARD -> stringResource(Res.string.strAddMenuCategory)
            Routes.COFFEE_CART_EDIT -> stringResource(Res.string.strEditCoffeeCart)
            Routes.ORDER_DASHBOARD -> stringResource(Res.string.strOrders)
            else -> topBarTitle
        }
        TopAppBar(
            title = {
                titleString?.let { Text(it) }
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(Res.string.strBack)
                    )
                }
            }
        )
    }
}

@Preview
@Composable
private fun TopBarPreview() {
    TopBar(
        currentRoute = Routes.COFFEE_CART_MENU_CATEGORIES,
        categoryName = null,
        topBarTitle = null,
        onBackClick = {}
    )
}
