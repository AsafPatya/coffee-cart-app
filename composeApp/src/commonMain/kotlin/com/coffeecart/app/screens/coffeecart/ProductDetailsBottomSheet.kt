package com.coffeecart.app.screens.coffeecart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import coil3.compose.AsyncImage
import coffeecart.composeapp.generated.resources.Res
import coffeecart.composeapp.generated.resources.strAddToCart
import coffeecart.composeapp.generated.resources.strComments
import com.coffeecart.app.theme.Spacing
import com.coffeecart.app.theme.dp
import com.coffeecart.app.ui.AppBottomSheet
import com.coffeecart.shared.model.Product
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProductDetailsBottomSheet(
    product: Product,
    onDismiss: () -> Unit,
    onAddToCart: (quantity: Int, comment: String) -> Unit,
) {
    var quantity by remember { mutableStateOf(1) }
    var comment by remember { mutableStateOf("") }

    AppBottomSheet(onDismiss = onDismiss) {
        AsyncImage(
            model = product.imageUrl,
            contentDescription = product.name,
            modifier = Modifier
                .fillMaxWidth()
                .height(Spacing.HeroHeight.dp)
                .clip(MaterialTheme.shapes.medium),
            contentScale = ContentScale.Crop,
        )

        Text(product.name, style = MaterialTheme.typography.headlineSmall)
        Text(
            formatPrice(product.price),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        OutlinedTextField(
            value = comment,
            onValueChange = { comment = it },
            label = { Text(stringResource(Res.string.strComments)) },
            modifier = Modifier.fillMaxWidth(),
        )

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

        Button(
            onClick = { onAddToCart(quantity, comment) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(Res.string.strAddToCart))
        }
    }
}

@Preview
@Composable
private fun ProductDetailsBottomSheetPreview() {
    ProductDetailsBottomSheet(
        product = Product(
            name = "Caffè Latte",
            price = 4.50,
            description = "Rich espresso with steamed milk and a thin layer of foam.",
            imageUrl = "https://picsum.photos/seed/latte/200"
        ),
        onDismiss = {},
        onAddToCart = { _, _ -> }
    )
}

