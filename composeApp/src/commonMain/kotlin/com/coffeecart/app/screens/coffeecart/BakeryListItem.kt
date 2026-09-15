package com.coffeecart.app.screens.coffeecart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import coffeecart.composeapp.generated.resources.Res
import coffeecart.composeapp.generated.resources.strKm
import coil3.compose.AsyncImage
import com.coffeecart.app.theme.Spacing
import com.coffeecart.app.theme.dp
import com.coffeecart.app.theme.dp as spacingDp
import com.coffeecart.shared.model.Bakery
import org.jetbrains.compose.resources.stringResource

@Composable
fun BakeryListItem(bakery: Bakery, formattedDistance: String? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = Spacing.Small.dp,
                shape = MaterialTheme.shapes.large,
                clip = false
            )
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surface)
            .padding(Spacing.XXSmall.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(Spacing.Large.spacingDp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = bakery.imageUrl,
                contentDescription = bakery.name,
                modifier = Modifier.size(Spacing.XXXXLarge.spacingDp).clip(RoundedCornerShape(Spacing.Small.spacingDp)),
                contentScale = ContentScale.Crop,
            )
            Spacer(Modifier.width(Spacing.Medium.spacingDp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    bakery.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(Spacing.XXSmall.spacingDp))
                Text(bakery.address, style = MaterialTheme.typography.bodyMedium)
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
private fun BakeryListItemPreview() {
    val stubBakery = Bakery(
        id = "bakery-1",
        name = "Sunrise Bakery",
        address = "12 Baker St",
        imageUrl = "https://images.unsplash.com/photo-1509440159596-0249088772ff",
    )
    BakeryListItem(
        bakery = stubBakery,
        formattedDistance = "0.8",
    )
}
