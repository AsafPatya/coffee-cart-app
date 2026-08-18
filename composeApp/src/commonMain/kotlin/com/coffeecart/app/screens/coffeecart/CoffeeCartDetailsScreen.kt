package com.coffeecart.app.screens.coffeecart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.coffeecart.app.theme.Spacing
import com.coffeecart.app.theme.dp
import com.coffeecart.app.ui.location.CoffeeCartMap
import com.coffeecart.shared.feature.cartdetails.CoffeeCartDetailsUiState
import com.coffeecart.shared.feature.cartdetails.CoffeeCartDetailsViewModel
import com.coffeecart.shared.model.CoffeeCart
import org.koin.compose.koinInject

/**
 * Details screen displaying the details of the selected coffee cart.
 * Plain content — the top bar (title + back button) is owned by MainScreen's single Scaffold.
 */
@Composable
fun CoffeeCartDetailsScreen(
    cartId: String,
    onCartNameLoaded: (String) -> Unit,
    onViewCategoriesClick: (String) -> Unit,
    viewModel: CoffeeCartDetailsViewModel = koinInject(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(cartId) {
        viewModel.loadCart(cartId)
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is CoffeeCartDetailsUiState.Success -> onCartNameLoaded(state.cart.name)
            is CoffeeCartDetailsUiState.Error -> onCartNameLoaded("Coffee Cart Details")
            is CoffeeCartDetailsUiState.Loading -> onCartNameLoaded("Coffee Cart Details")
        }
    }

    CoffeeCartDetailsContent(
        cartId = cartId,
        uiState = uiState,
        onViewCategoriesClick = { onViewCategoriesClick(cartId) },
    )
}

@Composable
fun CoffeeCartDetailsContent(
    cartId: String,
    uiState: CoffeeCartDetailsUiState,
    onViewCategoriesClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.XXLarge.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        when (uiState) {
            is CoffeeCartDetailsUiState.Loading -> {
                CircularProgressIndicator()
            }
            is CoffeeCartDetailsUiState.Error -> {
                Text(
                    text = uiState.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
            is CoffeeCartDetailsUiState.Success -> {
                val cart = uiState.cart
                Text(
                    text = cart.name,
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(bottom = Spacing.Medium.dp)
                )
                Text(
                    text = cart.address,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = Spacing.Small.dp)
                )
                Text(
                    text = "Cart ID: $cartId",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = Spacing.Medium.dp)
                )
                val latitude = cart.latitude
                val longitude = cart.longitude
                if (latitude != null && longitude != null) {
                    CoffeeCartMap(
                        latitude = latitude,
                        longitude = longitude,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(bottom = Spacing.XXLarge.dp),
                    )
                }
                Button(
                    onClick = onViewCategoriesClick,
                ) {
                    Text("View Categories")
                }
            }
        }
    }
}

@Preview
@Composable
private fun CoffeeCartDetailsScreenSuccessPreview() {
    CoffeeCartDetailsContent(
        cartId = "123",
        uiState = CoffeeCartDetailsUiState.Success(
            CoffeeCart(
                id = "123",
                name = "Downtown Espresso Cart",
                address = "123 Main St",
                imageUrl = ""
            )
        ),
        onViewCategoriesClick = {}
    )
}

@Preview
@Composable
private fun CoffeeCartDetailsScreenLoadingPreview() {
    CoffeeCartDetailsContent(
        cartId = "123",
        uiState = CoffeeCartDetailsUiState.Loading,
        onViewCategoriesClick = {}
    )
}

@Preview
@Composable
private fun CoffeeCartDetailsScreenErrorPreview() {
    CoffeeCartDetailsContent(
        cartId = "123",
        uiState = CoffeeCartDetailsUiState.Error("Failed to load coffee cart detail."),
        onViewCategoriesClick = {}
    )
}
