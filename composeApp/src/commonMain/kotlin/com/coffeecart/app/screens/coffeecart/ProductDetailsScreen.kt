package com.coffeecart.app.screens.coffeecart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import coil3.compose.AsyncImage
import coffeecart.composeapp.generated.resources.Res
import coffeecart.composeapp.generated.resources.strAddToCart
import coffeecart.composeapp.generated.resources.strAddedToBasket
import coffeecart.composeapp.generated.resources.strComments
import com.coffeecart.app.theme.Spacing
import com.coffeecart.app.theme.dp
import com.coffeecart.app.ui.buttons.OverlayBackButton
import com.coffeecart.shared.feature.products.ProductsUiState
import com.coffeecart.shared.feature.products.ProductsViewModel
import com.coffeecart.shared.model.Product
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

/**
 * Full-screen product page (hero image with a small overlay back button, matching
 * CoffeeCartDetailsScreen). Re-loads the category's products by [cartId]/[categoryName] and finds
 * [productName] among them, since Product has no stable id of its own.
 */
@Composable
fun ProductDetailsScreen(
    cartId: String,
    categoryName: String,
    productName: String,
    onBackClick: () -> Unit,
    viewModel: ProductsViewModel = koinInject(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val addedText = stringResource(Res.string.strAddedToBasket)

    LaunchedEffect(cartId, categoryName) {
        viewModel.loadProducts(cartId, categoryName)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is ProductsUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is ProductsUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is ProductsUiState.Success -> {
                val product = state.products.find { it.name == productName }
                if (product == null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "Product not found.", color = MaterialTheme.colorScheme.error)
                    }
                } else {
                    ProductDetailsContent(
                        product = product,
                        onBackClick = onBackClick,
                        onAddToCart = { quantity, comment ->
                            viewModel.addProductToCart(
                                cartId = cartId,
                                product = product,
                                quantity = quantity,
                                comment = comment,
                                addedText = addedText,
                            )
                            onBackClick()
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductDetailsContent(
    product: Product,
    onBackClick: () -> Unit,
    onAddToCart: (quantity: Int, comment: String) -> Unit,
) {
    var quantity by remember { mutableStateOf(1) }
    var comment by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(Spacing.HeroHeight.dp),
        ) {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillHeight,
            )
            OverlayBackButton(
                onClick = onBackClick,
                modifier = Modifier.align(Alignment.TopStart),
            )
        }

        Column(modifier = Modifier.fillMaxWidth().padding(Spacing.Large.dp)) {
            Text(product.name, style = MaterialTheme.typography.headlineSmall)

            if (product.description.isNotEmpty()) {
                Text(
                    text = product.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Spacing.XXSmall.dp),
                )
            }

            Text(
                text = formatPrice(product.price),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = Spacing.Small.dp),
            )

            Spacer(modifier = Modifier.height(Spacing.Medium.dp))

            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                label = { Text(stringResource(Res.string.strComments)) },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(Spacing.Medium.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { if (quantity > 1) quantity-- }) {
                    Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease quantity")
                }
                Text("$quantity", style = MaterialTheme.typography.titleLarge)
                IconButton(onClick = { quantity++ }) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Increase quantity")
                }
            }

            Spacer(modifier = Modifier.height(Spacing.Medium.dp))

            Button(
                onClick = { onAddToCart(quantity, comment) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(Res.string.strAddToCart))
            }
        }
    }
}

@Preview
@Composable
private fun ProductDetailsContentPreview() {
    ProductDetailsContent(
        product = Product(
            name = "Caffè Latte",
            price = 4.50,
            description = "Rich espresso with steamed milk and a thin layer of foam.",
            imageUrl = "https://picsum.photos/seed/latte/200"
        ),
        onBackClick = {},
        onAddToCart = { _, _ -> }
    )
}
