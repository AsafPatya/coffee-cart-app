package com.coffeecart.app.screens.coffeecart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import coil3.compose.AsyncImage
import com.coffeecart.app.theme.Spacing
import com.coffeecart.app.theme.dp
import com.coffeecart.app.ui.location.CoffeeCartMap
import com.coffeecart.shared.feature.cartdetails.CoffeeCartDetailsUiState
import com.coffeecart.shared.feature.cartdetails.CoffeeCartDetailsViewModel
import com.coffeecart.shared.model.CoffeeCart
import coffeecart.composeapp.generated.resources.Res
import coffeecart.composeapp.generated.resources.strStartYourOrderNow
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

/**
 * Details screen displaying the details of the selected coffee cart.* Plain content — the top bar (title + back button) is owned by AppContainer's single Scaffold.
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
        uiState = uiState,
        onViewCategoriesClick = { onViewCategoriesClick(cartId) },
    )
}

@Composable
fun CoffeeCartDetailsContent(
    uiState: CoffeeCartDetailsUiState,
    onViewCategoriesClick: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        when (uiState) {
            is CoffeeCartDetailsUiState.Loading -> {
                CircularProgressIndicator()
            }
            is CoffeeCartDetailsUiState.Error -> {
                Text(
                    text = uiState.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(Spacing.XXLarge.dp)
                )
            }
            is CoffeeCartDetailsUiState.Success -> {
                val cart = uiState.cart
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top,
                ) {
                    if (cart.imageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = cart.imageUrl,
                            contentDescription = "${cart.name} banner",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(Spacing.HeroHeight.dp),
                            contentScale = ContentScale.Crop,
                        )
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.XXLarge.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = cart.name,
                            style = MaterialTheme.typography.headlineLarge,
                            modifier = Modifier.padding(bottom = Spacing.Medium.dp)
                        )
                        Text(
                            text = cart.address,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(bottom = Spacing.Large.dp)
                        )
                        Button(
                            onClick = onViewCategoriesClick,
                            modifier = Modifier.padding(bottom = Spacing.Large.dp)
                        ) {
                            Text(stringResource(Res.string.strStartYourOrderNow))
                        }
                        val latitude = cart.latitude
                        val longitude = cart.longitude
                        if (latitude != null && longitude != null) {
                            CoffeeCartMap(
                                latitude = latitude,
                                longitude = longitude,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(Spacing.MapHeight.dp)
                                    .padding(bottom = Spacing.XXLarge.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun CoffeeCartDetailsScreenSuccessPreview() {
    CoffeeCartDetailsContent(
        uiState = CoffeeCartDetailsUiState.Success(
            CoffeeCart(
                id = "123",
                name = "Downtown Espresso Cart",
                address = "123 Main St",
                imageUrl = "https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb"
            )
        ),
        onViewCategoriesClick = {}
    )
}

@Preview
@Composable
private fun CoffeeCartDetailsScreenLoadingPreview() {
    CoffeeCartDetailsContent(
        uiState = CoffeeCartDetailsUiState.Loading,
        onViewCategoriesClick = {}
    )
}

@Preview
@Composable
private fun CoffeeCartDetailsScreenErrorPreview() {
    CoffeeCartDetailsContent(
        uiState = CoffeeCartDetailsUiState.Error("Failed to load coffee cart detail."),
        onViewCategoriesClick = {}
    )
}
