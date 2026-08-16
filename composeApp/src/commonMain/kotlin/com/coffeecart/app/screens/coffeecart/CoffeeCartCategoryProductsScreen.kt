package com.coffeecart.app.screens.coffeecart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import coil3.compose.AsyncImage
import com.coffeecart.app.theme.Spacing
import com.coffeecart.app.theme.dp
import com.coffeecart.shared.feature.cartdetails.CoffeeCartDetailsUiState
import com.coffeecart.shared.feature.cartdetails.CoffeeCartDetailsViewModel
import com.coffeecart.shared.model.Product
import org.koin.compose.koinInject

/**
 * Screen displaying the products of a selected category in a list row.
 */
@Composable
fun CoffeeCartCategoryProductsScreen(
    cartId: String,
    categoryName: String,
    viewModel: CoffeeCartDetailsViewModel = koinInject(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(cartId) {
        viewModel.loadCart(cartId)
    }

    when (val state = uiState) {
        is CoffeeCartDetailsUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is CoffeeCartDetailsUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = state.message, color = MaterialTheme.colorScheme.error)
            }
        }
        is CoffeeCartDetailsUiState.Success -> {
            val category = state.cart.categories.find { it.name == categoryName }
            if (category == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Category '$categoryName' not found.")
                }
            } else {
                CoffeeCartCategoryProductsContent(products = category.products)
            }
        }
    }
}

@Composable
private fun CoffeeCartCategoryProductsContent(products: List<Product>) {
    if (products.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "No products available in this category.", style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(Spacing.Large.dp),
            verticalArrangement = Arrangement.spacedBy(Spacing.Medium.dp)
        ) {
            items(products) { product ->
                ProductListItem(product = product)
                HorizontalDivider(
                    modifier = Modifier.padding(top = Spacing.Medium.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
    }
}

@Composable
private fun ProductListItem(product: Product) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.Small.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Image
        AsyncImage(
            model = product.imageUrl,
            contentDescription = product.name,
            modifier = Modifier
                .size(Spacing.XXXLarge.dp + Spacing.XXXLarge.dp) // 64.dp
                .clip(MaterialTheme.shapes.small),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(Spacing.Medium.dp))

        // 2. Column of Name and Description
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(Spacing.XXSmall.dp))
            Text(
                text = product.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(Spacing.Medium.dp))

        // 3. Price at the end
        val cents = product.price.toString().substringAfter(".", "00").padEnd(2, '0').take(2)
        val dollars = product.price.toString().substringBefore(".")
        val formattedPrice = "$$dollars.$cents"

        Text(
            text = formattedPrice,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Preview
@Composable
private fun CoffeeCartCategoryProductsScreenPreview() {
    val placeholder = listOf(
        Product(
            name = "Caffè Latte",
            price = 4.50,
            description = "Rich espresso with steamed milk and a thin layer of foam.",
            imageUrl = "https://picsum.photos/seed/latte/200"
        ),
        Product(
            name = "Cappuccino",
            price = 4.25,
            description = "Espresso balanced with steamed milk and a thick layer of foam.",
            imageUrl = "https://picsum.photos/seed/capp/200"
        )
    )
    CoffeeCartCategoryProductsContent(products = placeholder)
}

