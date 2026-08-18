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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
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
private fun RemoveCartDialogPreview() {
    RemoveCartDialog(
        cart = CoffeeCart("1", "Downtown Espresso Cart", "123 Main St", ""),
        onDismiss = {},
        onConfirm = {}
    )
}
