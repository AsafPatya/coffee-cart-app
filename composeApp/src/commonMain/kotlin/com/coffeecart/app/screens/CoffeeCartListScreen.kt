package com.coffeecart.app.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.coffeecart.app.theme.Spacing
import com.coffeecart.app.theme.dp
import androidx.compose.ui.tooling.preview.Preview

/**
 * Directory of coffee carts we work with. Placeholder data — real content comes from the
 * `GET /carts` endpoint once the backend and repository layer exist.
 */
@Composable
fun CoffeeCartListScreen(onCartClick: (String) -> Unit) {
    val placeholderCarts = listOf("Downtown Espresso Cart", "Riverside Brew", "Central Park Coffee")

    LazyColumn(modifier = Modifier.fillMaxSize().padding(Spacing.Large.dp)) {
        items(placeholderCarts) { cartName ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.Small.dp)
                    .clickable { onCartClick(cartName) },
            ) {
                Text(
                    text = cartName,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(Spacing.Large.dp),
                )
            }
        }
    }
}

@Preview
@Composable
private fun CoffeeCartListScreenPreview() {
    CoffeeCartListScreen(onCartClick = {})
}