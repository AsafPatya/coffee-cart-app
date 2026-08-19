package com.coffeecart.app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.coffeecart.app.theme.Spacing
import com.coffeecart.app.theme.dp
import com.coffeecart.shared.domain.ShoppingCartRepositoryInterface
import com.coffeecart.shared.domain.ShoppingCartState
import com.coffeecart.shared.model.OrderItem
import com.coffeecart.shared.model.Product
import coffeecart.composeapp.generated.resources.Res
import coffeecart.composeapp.generated.resources.strNoOpenOrder
import coffeecart.composeapp.generated.resources.strStartNewOrder
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

/** The basket for the in-progress order — a single coffee cart's products, held locally on-device. */
@Composable
fun MyOrderScreen(
    onExploreCartsClick: () -> Unit,
    shoppingCartRepositoryInterface: ShoppingCartRepositoryInterface = koinInject(),
) {
    val state by shoppingCartRepositoryInterface.state.collectAsState()
    MyOrderContent(
        state = state,
        onQuantityChange = { product, quantity -> shoppingCartRepositoryInterface.updateQuantity(product, quantity) },
        onExploreCartsClick = onExploreCartsClick,
    )
}

@Composable
private fun MyOrderContent(
    state: ShoppingCartState,
    onQuantityChange: (Product, Int) -> Unit,
    onExploreCartsClick: () -> Unit,
) {
    if (state.items.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(Spacing.Large.dp)
            ) {
                Text(
                    text = stringResource(Res.string.strNoOpenOrder),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = Spacing.Medium.dp)
                )
                Button(onClick = onExploreCartsClick) {
                    Text(text = stringResource(Res.string.strStartNewOrder))
                }
            }
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize().padding(Spacing.Large.dp)) {
        state.cartName?.let {
            Text(it, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(Spacing.Small.dp))
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(state.items, key = { it.product.name }) { item ->
                OrderItemRow(item = item, onQuantityChange = { quantity -> onQuantityChange(item.product, quantity) })
                HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.Small.dp))
            }
        }

        val total = state.items.sumOf { it.product.price * it.quantity }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = Spacing.Small.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Total", style = MaterialTheme.typography.titleMedium)
            Text(formatPrice(total), style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun OrderItemRow(item: OrderItem, onQuantityChange: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.product.name, style = MaterialTheme.typography.titleMedium)
            Text(formatPrice(item.product.price), style = MaterialTheme.typography.bodyMedium)
        }
        IconButton(onClick = { onQuantityChange(item.quantity - 1) }) {
            Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease quantity")
        }
        Text("${item.quantity}", style = MaterialTheme.typography.titleMedium)
        IconButton(onClick = { onQuantityChange(item.quantity + 1) }) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Increase quantity")
        }
    }
}

private fun formatPrice(price: Double): String {
    val cents = price.toString().substringAfter(".", "00").padEnd(2, '0').take(2)
    val dollars = price.toString().substringBefore(".")
    return "$$dollars.$cents"
}

@Preview
@Composable
private fun MyOrderScreenEmptyPreview() {
    MyOrderContent(state = ShoppingCartState(), onQuantityChange = { _, _ -> }, onExploreCartsClick = {})
}

@Preview
@Composable
private fun MyOrderScreenPreview() {
    val latte = Product(
        name = "Caffè Latte",
        price = 4.50,
        description = "Rich espresso with steamed milk and a thin layer of foam.",
        imageUrl = "https://picsum.photos/seed/latte/200"
    )
    val cappuccino = Product(
        name = "Cappuccino",
        price = 4.25,
        description = "Espresso balanced with steamed milk and a thick layer of foam.",
        imageUrl = "https://picsum.photos/seed/capp/200"
    )
    MyOrderContent(
        state = ShoppingCartState(
            cartId = "1",
            cartName = "Downtown Espresso Cart",
            items = listOf(OrderItem(latte, quantity = 2), OrderItem(cappuccino, quantity = 1)),
        ),
        onQuantityChange = { _, _ -> },
        onExploreCartsClick = {},
    )
}
