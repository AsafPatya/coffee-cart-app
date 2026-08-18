package com.coffeecart.app.screens.profile.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.coffeecart.app.ui.location.rememberLocationPicker
import com.coffeecart.shared.model.CoffeeCart


@Composable
internal fun ResultDialog(
    message: String,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Result") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(message)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Dismiss")
            }
        }
    )
}



@Composable
internal fun EditCartDialog(
    cart: CoffeeCart,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, Double?, Double?) -> Unit,
) {
    var nameInput by remember { mutableStateOf(cart.name) }
    var addressInput by remember { mutableStateOf(cart.address) }
    var imageUrlInput by remember { mutableStateOf(cart.imageUrl) }
    var latitude by remember { mutableStateOf(cart.latitude) }
    var longitude by remember { mutableStateOf(cart.longitude) }

    val launchLocationPicker = rememberLocationPicker(onPicked = { lat, lng ->
        latitude = lat
        longitude = lng
    })

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Coffee Cart") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                CartDetailsForm(
                    name = nameInput,
                    onNameChange = { nameInput = it },
                    address = addressInput,
                    onAddressChange = { addressInput = it },
                    imageUrl = imageUrlInput,
                    onImageUrlChange = { imageUrlInput = it },
                )
                if (launchLocationPicker != null) {
                    Text(
                        text = if (latitude != null && longitude != null) {
                            "Location: $latitude, $longitude"
                        } else {
                            "Location: not set"
                        },
                    )
                    TextButton(onClick = launchLocationPicker) {
                        Text("Set Location")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = nameInput.trim().isNotEmpty() && addressInput.trim().isNotEmpty() && imageUrlInput.isNotEmpty(),
                onClick = {
                    onConfirm(cart.id, nameInput, addressInput, imageUrlInput, latitude, longitude)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
internal fun RemoveCartDialog(
    cart: CoffeeCart,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Remove Coffee Cart") },
        text = {
            Text("Are you sure you want to remove ${cart.name}? This action cannot be undone.")
        },
        confirmButton = {
            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                onClick = onConfirm
            ) {
                Text("Remove")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// ==========================================
// PREVIEWS
// ==========================================


@Preview
@Composable
private fun ResultDialogPreview() {
    ResultDialog(
        message = "Operation completed successfully!",
        onDismiss = {}
    )
}



@Preview
@Composable
private fun EditCartDialogPreview() {
    EditCartDialog(
        cart = CoffeeCart("1", "Downtown Espresso Cart", "123 Main St", ""),
        onDismiss = {},
        onConfirm = { _, _, _, _, _, _ -> }
    )
}

@Preview
@Composable
private fun RemoveCartDialogPreview() {
    RemoveCartDialog(
        cart = CoffeeCart("1", "Downtown Espresso Cart", "123 Main St", ""),
        onDismiss = {},
        onConfirm = {}
    )
}
