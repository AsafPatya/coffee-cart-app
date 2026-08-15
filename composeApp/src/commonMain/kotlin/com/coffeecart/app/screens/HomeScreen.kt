package com.coffeecart.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.coffeecart.app.theme.Spacing
import com.coffeecart.app.theme.dp
import androidx.compose.ui.tooling.preview.Preview

/** Landing tab: title, hero image placeholder, and a CTA into the coffee cart directory. */
@Composable
fun HomeScreen(onNewOrderClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(Spacing.XXLarge.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Coffee Cart", style = MaterialTheme.typography.headlineLarge)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(HERO_IMAGE_HEIGHT)
                .padding(vertical = Spacing.XXLarge.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(Spacing.Large.dp)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("🖼️ hero image placeholder", style = MaterialTheme.typography.bodyMedium)
        }

        Button(onClick = onNewOrderClick) {
            Text("New Order")
        }
    }
}

// A fixed component dimension, not a spacing value — outside the Spacing scale on purpose.
private val HERO_IMAGE_HEIGHT = 200.dp

@Preview
@Composable
private fun HomeScreenPreview() {
    HomeScreen(onNewOrderClick = {})
}
