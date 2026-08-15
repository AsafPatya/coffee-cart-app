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
import com.coffeecart.app.theme.Spacing
import com.coffeecart.app.theme.dp
import com.coffeecart.shared.feature.cartlist.CoffeeCartListUiState
import com.coffeecart.shared.feature.cartlist.CoffeeCartListViewModel
import org.koin.compose.koinInject

@Composable
fun CoffeeCartListScreen(
    onCartClick: (String) -> Unit,
    viewModel: CoffeeCartListViewModel = koinInject(),
) {
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is CoffeeCartListUiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
            CircularProgressIndicator()
        }
        is CoffeeCartListUiState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) {
            Text(state.message)
        }
        is CoffeeCartListUiState.Success -> LazyColumn(
            modifier = Modifier.fillMaxSize().padding(Spacing.Large.dp),
            verticalArrangement = Arrangement.spacedBy(Spacing.Small.dp),
        ) {
            items(state.carts, key = { it.id }) { cart ->
                CoffeeCartListItem(cart = cart, onClick = { onCartClick(cart.id) })
            }
        }
    }
}
