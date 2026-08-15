package com.coffeecart.app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.coffeecart.app.theme.Spacing
import com.coffeecart.app.theme.dp
import com.coffeecart.shared.feature.cartlist.CoffeeCartListUiState
import com.coffeecart.shared.feature.cartlist.CoffeeCartListViewModel
import com.coffeecart.shared.model.CoffeeCart
import org.koin.compose.koinInject

@Composable
fun CoffeeCartListScreen(
    onCartClick: (String) -> Unit,
    viewModel: CoffeeCartListViewModel = koinInject(),
) {
    val uiState by viewModel.uiState.collectAsState()
    CoffeeCartListContent(
        uiState = uiState,
        onCartClick = onCartClick,
    )
}

@Composable
fun CoffeeCartListContent(
    uiState: CoffeeCartListUiState,
    onCartClick: (String) -> Unit,
) {
    when (uiState) {
        is CoffeeCartListUiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
            CircularProgressIndicator()
        }
        is CoffeeCartListUiState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) {
            Text(uiState.message)
        }
        is CoffeeCartListUiState.Success -> LazyColumn(
            modifier = Modifier.fillMaxSize().padding(Spacing.Large.dp),
            verticalArrangement = Arrangement.spacedBy(Spacing.Small.dp),
        ) {
            items(uiState.carts, key = { it.id }) { cart ->
                CoffeeCartListItem(cart = cart, onClick = { onCartClick(cart.id) })
            }
        }
    }
}

@Preview
@Composable
private fun CoffeeCartListScreenSuccessPreview() {
    val stubCarts = listOf(
        CoffeeCart("1", "Downtown Espresso Cart", "123 Main St", ""),
        CoffeeCart("2", "Riverside Brew", "456 River Rd", ""),
        CoffeeCart("3", "Central Park Coffee", "789 Park Ave", ""),
    )
    CoffeeCartListContent(
        uiState = CoffeeCartListUiState.Success(stubCarts),
        onCartClick = {},
    )
}

@Preview
@Composable
private fun CoffeeCartListScreenLoadingPreview() {
    CoffeeCartListContent(
        uiState = CoffeeCartListUiState.Loading,
        onCartClick = {},
    )
}

@Preview
@Composable
private fun CoffeeCartListScreenErrorPreview() {
    CoffeeCartListContent(
        uiState = CoffeeCartListUiState.Error("An error occurred while loading coffee carts."),
        onCartClick = {},
    )
}

