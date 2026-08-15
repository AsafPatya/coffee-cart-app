package com.coffeecart.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import coil3.compose.AsyncImage
import com.coffeecart.app.theme.Colors
import com.coffeecart.app.theme.Spacing
import com.coffeecart.app.theme.dp as spacingDp
import com.coffeecart.shared.model.CoffeeCart

private val CART_IMAGE_SIZE = 64.dp

@Composable
fun CoffeeCartListItem(cart: CoffeeCart, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(Spacing.Large.spacingDp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(cart.name, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(Spacing.XXSmall.spacingDp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(Spacing.Small.spacingDp)
                            .background(
                                color = if (cart.isOpen) Colors.Green else Colors.Red,
                                shape = CircleShape,
                            ),
                    )
                    Spacer(Modifier.width(Spacing.XXSmall.spacingDp))
                    Text(cart.address, style = MaterialTheme.typography.bodyMedium)
                }
            }
            Spacer(Modifier.width(Spacing.Small.spacingDp))
            AsyncImage(
                model = cart.imageUrl,
                contentDescription = cart.name,
                modifier = Modifier.size(CART_IMAGE_SIZE).clip(RoundedCornerShape(Spacing.Small.spacingDp)),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

@Preview
@Composable
private fun CoffeeCartListItemPreview() {
    val stubCart = CoffeeCart(
        id = "1",
        name = "Downtown Espresso Cart",
        isOpen = true,
        address = "123 Main St",
        imageUrl = "https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb",
    )
    CoffeeCartListItem(
        cart = stubCart,
        onClick = {},
    )
}

