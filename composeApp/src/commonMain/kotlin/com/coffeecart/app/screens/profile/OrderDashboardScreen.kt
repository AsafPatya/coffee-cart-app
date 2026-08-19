package com.coffeecart.app.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.coffeecart.app.theme.Spacing
import com.coffeecart.app.theme.dp
import com.coffeecart.shared.feature.orderdashboard.OrderDashboardViewModel
import com.coffeecart.shared.model.Order
import com.coffeecart.shared.model.OrderItem
import com.coffeecart.shared.model.OrderStatus
import com.coffeecart.shared.model.Product
import org.koin.compose.koinInject

/** A live (polled) grid of a coffee cart's orders. Tapping a card advances its status. */
@Composable
fun OrderDashboardScreen(
    cartId: String,
    viewModel: OrderDashboardViewModel = koinInject(),
) {
    val orders by viewModel.orders.collectAsState()

    LaunchedEffect(cartId) {
        viewModel.start(cartId)
    }

    OrderDashboardContent(orders = orders, onOrderClick = { order -> viewModel.advance(order.id) })
}

@Composable
private fun OrderDashboardContent(orders: List<Order>, onOrderClick: (Order) -> Unit) {
    if (orders.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No orders yet.", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(Spacing.Large.dp),
        horizontalArrangement = Arrangement.spacedBy(Spacing.Medium.dp),
        verticalArrangement = Arrangement.spacedBy(Spacing.Medium.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(orders, key = { it.id }) { order ->
            OrderCard(order = order, onClick = { onOrderClick(order) })
        }
    }
}

@Composable
private fun OrderCard(order: Order, onClick: () -> Unit) {
    val borderColor = when (order.status) {
        OrderStatus.ARRIVED -> Color.Red
        OrderStatus.IN_PROGRESS -> Color(0xFFFFC107)
        OrderStatus.DONE -> Color(0xFF4CAF50)
    }

    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .background(Color.Transparent),
        colors = CardDefaults.cardColors(),
        border = BorderStroke(Spacing.XSmall.dp, borderColor),
        shape = RoundedCornerShape(Spacing.Small.dp),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(Spacing.Medium.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Order #${order.id.take(6)}", style = MaterialTheme.typography.titleSmall)
            Column {
                order.items.forEach { item ->
                    Text(
                        "${item.quantity}x ${item.product.name}",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                    )
                }
            }
            Text(order.status.name, style = MaterialTheme.typography.labelMedium, color = borderColor)
        }
    }
}

@Preview
@Composable
private fun OrderDashboardScreenPreview() {
    val latte = Product(name = "Latte", price = 4.5, description = "", imageUrl = "")
    val cappuccino = Product(name = "Cappuccino", price = 4.25, description = "", imageUrl = "")
    OrderDashboardContent(
        orders = listOf(
            Order("1", "cart-1", listOf(OrderItem(latte, 2)), OrderStatus.ARRIVED, 0L),
            Order("2", "cart-1", listOf(OrderItem(cappuccino, 1)), OrderStatus.IN_PROGRESS, 0L),
            Order("3", "cart-1", listOf(OrderItem(latte, 1)), OrderStatus.DONE, 0L),
        ),
        onOrderClick = {},
    )
}
