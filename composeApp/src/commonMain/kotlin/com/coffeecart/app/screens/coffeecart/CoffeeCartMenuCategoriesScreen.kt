package com.coffeecart.app.screens.coffeecart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import coil3.compose.AsyncImage
import com.coffeecart.app.theme.Spacing
import com.coffeecart.app.theme.dp
import com.coffeecart.shared.feature.cartdetails.CoffeeCartDetailsUiState
import com.coffeecart.shared.feature.cartdetails.CoffeeCartDetailsViewModel
import com.coffeecart.shared.model.MenuCategory
import org.koin.compose.koinInject

/**
 * Screen displaying the menu categories of the selected coffee cart as a two-column image grid.
 */
@Composable
fun CoffeeCartMenuCategoriesScreen(
    cartId: String,
    onCategoryClick: (String) -> Unit,
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
            CoffeeCartMenuCategoriesContent(
                categories = state.cart.categories,
                onCategoryClick = onCategoryClick
            )
        }
    }
}

@Composable
private fun CoffeeCartMenuCategoriesContent(
    categories: List<MenuCategory>,
    onCategoryClick: (String) -> Unit,
) {
    if (categories.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "No categories available.", style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(Spacing.Large.dp),
            horizontalArrangement = Arrangement.spacedBy(Spacing.Medium.dp),
            verticalArrangement = Arrangement.spacedBy(Spacing.Medium.dp),
        ) {
            items(categories) { category ->
                MenuCategoryCard(
                    category = category,
                    onClick = { onCategoryClick(category.name) }
                )
            }
        }
    }
}

@Composable
private fun MenuCategoryCard(
    category: MenuCategory,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
            AsyncImage(
                model = category.imageUrl,
                contentDescription = category.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.White.copy(alpha = 0.85f))
                    .padding(Spacing.Small.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black,
                )
            }
        }
    }
}

@Preview
@Composable
private fun CoffeeCartMenuCategoriesScreenPreview() {
    val placeholder = listOf(
        MenuCategory("Coffee & Pastries", "https://picsum.photos/seed/cat1/400"),
        MenuCategory("Drinks", "https://picsum.photos/seed/cat2/400"),
        MenuCategory("Bowls", "https://picsum.photos/seed/cat3/400"),
        MenuCategory("Baked Goods", "https://picsum.photos/seed/cat4/400"),
    )
    CoffeeCartMenuCategoriesContent(
        categories = placeholder,
        onCategoryClick = {}
    )
}
