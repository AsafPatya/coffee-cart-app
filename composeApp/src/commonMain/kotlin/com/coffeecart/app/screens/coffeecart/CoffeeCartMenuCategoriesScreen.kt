package com.coffeecart.app.screens.coffeecart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import coffeecart.composeapp.generated.resources.Res
import coffeecart.composeapp.generated.resources.strNoCategoriesAvailable
import coil3.compose.AsyncImage
import com.coffeecart.app.theme.Spacing
import com.coffeecart.app.theme.dp
import com.coffeecart.shared.feature.cartdetails.CoffeeCartDetailsUiState
import com.coffeecart.shared.feature.cartdetails.CoffeeCartDetailsViewModel
import com.coffeecart.shared.model.MenuCategory
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

/**
 * Screen displaying the menu categories of the selected coffee cart as a two-column square grid.
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
            Text(
                text = stringResource(Res.string.strNoCategoriesAvailable),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
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
    val shape = RoundedCornerShape(Spacing.Large.dp)
    Card(
        onClick = onClick,
        shape = shape,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .shadow(
                elevation = Spacing.Small.dp,
                shape = shape,
                clip = false
            ),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (category.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = category.imageUrl,
                    contentDescription = category.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.White.copy(alpha = 0.88f))
                    .padding(Spacing.Small.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                    )
                    if (category.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(Spacing.XXXSmall.dp))
                        Text(
                            text = category.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.DarkGray,
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun CoffeeCartMenuCategoriesScreenSuccessPreview() {
    val sampleProducts = listOf(
        com.coffeecart.shared.model.Product("קרואסון חמאה", 14.0, "קרואסון פריך וטרי", ""),
        com.coffeecart.shared.model.Product("קפוצ׳ינו גדול", 16.0, "אספרסו כפול עם חלב מוקצף", ""),
    )
    val placeholder = listOf(
        MenuCategory("המתוקים שלנו", "https://picsum.photos/seed/cat1/600/300", sampleProducts, description = "מאפים ומתוקים טריים"),
        MenuCategory("המלוחים שלנו", "https://picsum.photos/seed/cat2/600/300"),
        MenuCategory("מה שותים", "https://picsum.photos/seed/cat3/600/300", sampleProducts),
        MenuCategory("ארוחות בוקר", "https://picsum.photos/seed/cat4/600/300"),
    )
    CoffeeCartMenuCategoriesContent(
        categories = placeholder,
        onCategoryClick = {}
    )
}

@Preview
@Composable
private fun CoffeeCartMenuCategoriesScreenHebrewPreview() {
    val sampleProducts = listOf(
        com.coffeecart.shared.model.Product("קרואסון חמאה", 14.0, "קרואסון פריך וטרי", ""),
        com.coffeecart.shared.model.Product("קפוצ׳ינו גדול", 16.0, "אספרסו כפול עם חלב מוקצף", ""),
    )
    val placeholder = listOf(
        MenuCategory("המתוקים שלנו", "https://picsum.photos/seed/cat1/600/300", sampleProducts, description = "מאפים ומתוקים טריים"),
        MenuCategory("המלוחים שלנו", "https://picsum.photos/seed/cat2/600/300"),
        MenuCategory("מה שותים", "https://picsum.photos/seed/cat3/600/300", sampleProducts),
        MenuCategory("ארוחות בוקר", "https://picsum.photos/seed/cat4/600/300"),
    )
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        CoffeeCartMenuCategoriesContent(
            categories = placeholder,
            onCategoryClick = {}
        )
    }
}
