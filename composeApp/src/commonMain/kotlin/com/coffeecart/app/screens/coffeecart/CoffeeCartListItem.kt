package com.coffeecart.app.screens.coffeecart

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import coffeecart.composeapp.generated.resources.Res
import coffeecart.composeapp.generated.resources.strKm
import coil3.compose.AsyncImage
import com.coffeecart.app.theme.Spacing
import com.coffeecart.shared.model.CoffeeCart
import org.jetbrains.compose.resources.stringResource
import com.coffeecart.app.theme.dp as spacingDp

@Composable
fun CoffeeCartListItem(cart: CoffeeCart, formattedDistance: String? = null, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(Spacing.Large.spacingDp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = cart.imageUrl,
                contentDescription = cart.name,
                modifier = Modifier.size(Spacing.XXXXLarge.spacingDp).clip(RoundedCornerShape(Spacing.Small.spacingDp)),
                contentScale = ContentScale.Crop,
            )
            Spacer(Modifier.width(Spacing.Medium.spacingDp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    cart.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(Spacing.XXSmall.spacingDp))
                Text(cart.address, style = MaterialTheme.typography.bodyMedium)
            }
            if (formattedDistance != null) {
                Spacer(Modifier.width(Spacing.Small.spacingDp))
                Text(
                    "$formattedDistance ${stringResource(Res.string.strKm)}",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Preview
@Composable
private fun CoffeeCartListItemPreview() {
    val stubCart = CoffeeCart(
        id = "1",
        name = "Downtown Espresso Cart",
        address = "123 Main St",
        imageUrl = "https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb",
    )
    CoffeeCartListItem(
        cart = stubCart,
        formattedDistance = "1.2",
        onClick = {},
    )
}

@Preview
@Composable
private fun CoffeeCartListItemHebrewPreview() {
    val stubCart = CoffeeCart(
        id = "1",
        name = "עגלת אספרסו מרכז העיר",
        address = "רחוב הרצל 123",
        imageUrl = "https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb",
    )
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        CoffeeCartListItem(
            cart = stubCart,
            formattedDistance = "1.2",
            onClick = {},
        )
    }
}

