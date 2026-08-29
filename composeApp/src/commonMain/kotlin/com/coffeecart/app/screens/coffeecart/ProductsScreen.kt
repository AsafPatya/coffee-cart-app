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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import coil3.compose.AsyncImage
import com.coffeecart.app.theme.Spacing
import com.coffeecart.app.theme.dp
import com.coffeecart.shared.feature.products.ProductsUiState
import com.coffeecart.shared.feature.products.ProductsViewModel
import com.coffeecart.shared.model.Product
import coffeecart.composeapp.generated.resources.Res
import coffeecart.composeapp.generated.resources.strAddedToBasket
import coffeecart.composeapp.generated.resources.strClickProductToAddToCart
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

/**
 * Screen displaying the products of a selected category in a list row.
 */
@Composable
fun ProductsScreen(
    cartId: String,
    categoryName: String,
    viewModel: ProductsViewModel = koinInject(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    val addedText = stringResource(Res.string.strAddedToBasket)

    LaunchedEffect(cartId, categoryName) {
        viewModel.loadProducts(cartId, categoryName)
    }

    LaunchedEffect(viewModel) {
        viewModel.snackBarMessages.collect { message ->
            snackBarHostState.showSnackbar(message)
        }
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
                CoffeeCartCategoryProductsContent(
                    products = state.products,
                    onProductClick = { product -> selectedProduct = product },
                )

                selectedProduct?.let { product ->
                    ProductDetailsBottomSheet(
                        product = product,
                        onDismiss = { selectedProduct = null },
                        onAddToCart = { quantity, comment ->
                            viewModel.addProductToCart(
                                cartId = cartId,
                                product = product,
                                quantity = quantity,
                                comment = comment,
                                addedText = addedText,
                            )
                            selectedProduct = null
                        },
                    )
                }
            }
        }
        SnackbarHost(hostState = snackBarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

internal fun formatPrice(price: Double): String {
    val cents = price.toString().substringAfter(".", "00").padEnd(2, '0').take(2)
    val dollars = price.toString().substringBefore(".")
    return "₪$dollars.$cents"
}

@Composable
private fun CoffeeCartCategoryProductsContent(products: List<Product>, onProductClick: (Product) -> Unit) {
    if (products.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "No products available in this category.", style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        val backgroundColor = MaterialTheme.colorScheme.background
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .drawWithContent {
                        drawContent()
                        val fadeHeight = Spacing.Small.dp.toPx()
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(backgroundColor, Color.Transparent),
                                startY = 0f,
                                endY = fadeHeight
                            )
                        )
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.Transparent, backgroundColor),
                                startY = size.height - fadeHeight,
                                endY = size.height
                            )
                        )
                    },
                contentPadding = PaddingValues(
                    top = Spacing.Medium.dp,
                    bottom = Spacing.Medium.dp,
                    start = Spacing.Large.dp,
                    end = Spacing.Large.dp
                ),
                verticalArrangement = Arrangement.spacedBy(Spacing.Medium.dp)
            ) {
                items(products) { product ->
                    ProductListItem(product = product, onClick = { onProductClick(product) })
                }
            }
            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = stringResource(Res.string.strClickProductToAddToCart),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
private fun ProductListItem(product: Product, onClick: () -> Unit) {
    val cardShape = RoundedCornerShape(Spacing.Large.dp)
    Card(
        onClick = onClick,
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = Spacing.Small.dp,
                shape = cardShape,
                clip = false
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.Large.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = Spacing.Medium.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (product.description.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(Spacing.XXSmall.dp))
                        Text(
                            text = product.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.Large.dp))

                Text(
                    text = formatPrice(product.price),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (product.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier
                        .size(Spacing.XXXXXLarge.dp)
                        .clip(RoundedCornerShape(Spacing.Large.dp))
                        .align(Alignment.CenterVertically),
                    contentScale = ContentScale.Crop
                )
            }
        }
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
    CoffeeCartCategoryProductsContent(products = placeholder, onProductClick = {})
}

@Preview
@Composable
private fun CoffeeCartCategoryProductsScreenHebrewPreview() {
    val placeholder = listOf(
        Product(
            name = "קרואסון וקפה",
            price = 25.00,
            description = "קרואסון חמאה וקפה פילטר",
            imageUrl = "https://picsum.photos/seed/croissant/200"
        ),
        Product(
            name = "סמוזי פיר��ת יער",
            price = 25.00,
            description = "סמוזי פירות יער",
            imageUrl = "https://picsum.photos/seed/smoothie/200"
        ),
    )
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        CoffeeCartCategoryProductsContent(products = placeholder, onProductClick = {})
    }
}

