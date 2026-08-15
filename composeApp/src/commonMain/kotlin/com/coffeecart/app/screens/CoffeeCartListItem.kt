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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.coffeecart.app.theme.Spacing
import com.coffeecart.app.theme.dp as spacingDp
import com.coffeecart.shared.model.CoffeeCart

private val openDotColor = Color(0xFF4CAF50)
private val closedDotColor = Color(0xFFF44336)

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
                            .size(8.dp)
                            .background(
                                color = if (cart.isOpen) openDotColor else closedDotColor,
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
                modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
            )
        }
    }
}
