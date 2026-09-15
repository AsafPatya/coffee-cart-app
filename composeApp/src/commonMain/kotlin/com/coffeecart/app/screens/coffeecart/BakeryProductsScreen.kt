package com.coffeecart.app.screens.coffeecart

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.coffeecart.shared.feature.bakerydetails.BakeryDetailsUiState
import com.coffeecart.shared.feature.bakerydetails.BakeryDetailsViewModel
import com.coffeecart.shared.model.Product
import org.koin.compose.koinInject

/**
 * Screen displaying the products of a selected bakery category in a list row.
 * Reuses the coffee cart's product list layout — products aren't cart-specific.
 */
@Composable
fun BakeryProductsScreen(
    bakeryId: String,
    categoryName: String,
    onProductClick: (Product) -> Unit,
    viewModel: BakeryDetailsViewModel = koinInject(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(bakeryId) {
        viewModel.loadBakery(bakeryId)
    }

    when (val state = uiState) {
        is BakeryDetailsUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is BakeryDetailsUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = state.message, color = MaterialTheme.colorScheme.error)
            }
        }
        is BakeryDetailsUiState.Success -> {
            val products = state.bakery.categories.find { it.name == categoryName }?.products ?: emptyList()
            CoffeeCartCategoryProductsContent(
                products = products,
                onProductClick = onProductClick,
            )
        }
    }
}
