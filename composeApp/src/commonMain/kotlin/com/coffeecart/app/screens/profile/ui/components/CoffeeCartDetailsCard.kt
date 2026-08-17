package com.coffeecart.app.screens.profile.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import coil3.compose.AsyncImage
import com.coffeecart.app.theme.Spacing
import com.coffeecart.app.theme.dp
import com.coffeecart.shared.model.CoffeeCart

/**
 * A reusable, rich card view displaying a coffee cart's details, image, location,
 * and categories with nested products.
 */
@Composable
fun CoffeeCartDetailsCard(
    cart: CoffeeCart,
    modifier: Modifier = Modifier,
    showDetailedInfo: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.Medium.dp),
            horizontalArrangement = Arrangement.spacedBy(Spacing.Medium.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (cart.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = cart.imageUrl,
                    contentDescription = "${cart.name} image",
                    modifier = Modifier
                        .size(Spacing.XXXLarge.dp * 2)
                        .clip(MaterialTheme.shapes.medium),
                    contentScale = ContentScale.Crop
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.XXSmall.dp)
            ) {
                Text(
                    text = cart.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "ID: ${cart.id}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "📍 ${cart.address}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (showDetailedInfo) {
                    if (cart.categories.isNotEmpty()) {
                        Text(
                            text = "📂 Categories & Products:",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        cart.categories.forEach { category ->
                            val productsText = if (category.products.isNotEmpty()) {
                                category.products.joinToString { it.name }
                            } else {
                                "No products"
                            }
                            Text(
                                text = "  • ${category.name}: $productsText",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = Spacing.XSmall.dp)
                              )
                        }
                    } else {
                        Text(
                            text = "📂 Categories: None",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun CoffeeCartDetailsCardPreview() {
    CoffeeCartDetailsCard(
        cart = CoffeeCart("1", "Downtown Espresso Cart", "123 Main St", ""),
        showDetailedInfo = true
    )
}

