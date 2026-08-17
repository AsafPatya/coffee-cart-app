package com.coffeecart.app.screens.profile.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.coffeecart.app.theme.Spacing
import com.coffeecart.app.theme.dp
import com.coffeecart.app.ui.AppBottomSheet
import com.coffeecart.shared.model.CoffeeCart

@Composable
internal fun ShowCartsBottomSheet(
    cartsList: List<CoffeeCart>,
    onDismiss: () -> Unit,
) {
    AppBottomSheet(onDismiss = onDismiss) {
        Text(
            text = "Existing Coffee Carts",
            style = MaterialTheme.typography.titleLarge
        )

        if (cartsList.isEmpty()) {
            Text(
                text = "No coffee carts found. Please add a new cart first.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            cartsList.forEach { cart ->
                CoffeeCartDetailsCard(cart = cart)
            }
        }
    }
}

@Composable
internal fun CartSelectionBottomSheet(
    cartsList: List<CoffeeCart>,
    onDismiss: () -> Unit,
    onCartSelected: (CoffeeCart) -> Unit,
) {
    AppBottomSheet(onDismiss = onDismiss) {
        Text(
            text = "Select a Coffee Cart",
            style = MaterialTheme.typography.titleLarge
        )
        if (cartsList.isEmpty()) {
            Text(
                text = "No coffee carts found. Please get or add a new cart first.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            cartsList.forEach { cart ->
                CoffeeCartDetailsCard(
                    cart = cart,
                    showDetailedInfo = false,
                    onClick = { onCartSelected(cart) }
                )
            }
        }
    }
}

@Composable
internal fun AddCartBottomSheet(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit,
) {
    var nameInput by remember { mutableStateOf("") }
    var addressInput by remember { mutableStateOf("") }
    var imageUrlInput by remember { mutableStateOf("https://picsum.photos/seed/100/200") }

    AppBottomSheet(onDismiss = onDismiss) {
        To_Header_Title(text = "Add Coffee Cart")

        CartDetailsForm(
            name = nameInput,
            onNameChange = { nameInput = it },
            address = addressInput,
            onAddressChange = { addressInput = it },
            imageUrl = imageUrlInput,
            onImageUrlChange = { imageUrlInput = it }
        )

        Spacer(modifier = Modifier.height(Spacing.Medium.dp))

        Button(
            enabled = nameInput.trim().isNotEmpty() && addressInput.trim().isNotEmpty() && imageUrlInput.isNotEmpty(),
            onClick = {
                onConfirm(nameInput, addressInput, imageUrlInput)
                onDismiss()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Cart")
        }
    }
}

@Composable
private fun To_Header_Title(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge
    )
}

// ==========================================
// PREVIEWS
// ==========================================

@Preview
@Composable
private fun ShowCartsBottomSheetPreview() {
    ShowCartsBottomSheet(
        cartsList = listOf(
            CoffeeCart("1", "Downtown Espresso Cart", "123 Main St", ""),
            CoffeeCart("2", "Riverside Brew", "456 River Rd", "")
        ),
        onDismiss = {}
    )
}

@Preview
@Composable
private fun CartSelectionBottomSheetPreview() {
    CartSelectionBottomSheet(
        cartsList = listOf(
            CoffeeCart("1", "Downtown Espresso Cart", "123 Main St", ""),
            CoffeeCart("2", "Riverside Brew", "456 River Rd", "")
        ),
        onDismiss = {},
        onCartSelected = {}
    )
}

@Preview
@Composable
private fun AddCartBottomSheetPreview() {
    AddCartBottomSheet(
        onDismiss = {},
        onConfirm = { _, _, _ -> }
    )
}
