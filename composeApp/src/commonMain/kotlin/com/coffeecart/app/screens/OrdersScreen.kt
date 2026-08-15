package com.coffeecart.app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.coffeecart.app.theme.Spacing
import com.coffeecart.app.theme.dp
import androidx.compose.ui.tooling.preview.Preview

private enum class OrderStage(val label: String) {
    Received("Received"),
    InProgress("In Progress"),
    Done("Done"),
}

/**
 * Placeholder for the order flow: the basket while building an order, then the order's
 * progress once submitted. Wiring to real basket/order state comes with the backend integration.
 */
@Composable
fun OrdersScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(Spacing.XXLarge.dp)) {
        Text("Your Basket", style = MaterialTheme.typography.headlineSmall)
        Text(
            text = "No items yet — add something from a coffee cart's menu.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = Spacing.Small.dp, bottom = Spacing.XXLarge.dp),
        )

        HorizontalDivider()

        Text(
            text = "Order Status",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = Spacing.XXLarge.dp, bottom = Spacing.Large.dp),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            OrderStage.entries.forEach { stage ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(stage.label, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Preview
@Composable
private fun OrdersScreenPreview() {
    OrdersScreen()
}
