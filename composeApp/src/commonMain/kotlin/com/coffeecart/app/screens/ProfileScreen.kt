package com.coffeecart.app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.coffeecart.app.theme.Spacing
import com.coffeecart.app.theme.dp
import com.coffeecart.shared.feature.profile.ProfileViewModel
import org.koin.compose.koinInject

/** Screen enabling operational calls (GET/POST/DELETE) for coffee carts. */
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = koinInject(),
) {
    val dialogMessage by viewModel.dialogMessage.collectAsState()

    ProfileContent(
        dialogMessage = dialogMessage,
        onGetClick = { viewModel.getCoffeeCarts() },
        onPostClick = { viewModel.addCoffeeCart() },
        onConfirmDelete = { id -> viewModel.removeCoffeeCart(id) },
        onDismissDialog = { viewModel.dismissDialog() },
    )
}

@Composable
fun ProfileContent(
    dialogMessage: String?,
    onGetClick: () -> Unit,
    onPostClick: () -> Unit,
    onConfirmDelete: (String) -> Unit,
    onDismissDialog: () -> Unit,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(Spacing.XXLarge.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Profile Operations", style = MaterialTheme.typography.headlineSmall)
        Text(
            text = "Testing suite for the backend and repository endpoints.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = Spacing.Small.dp, bottom = Spacing.XXLarge.dp),
        )

        Button(
            onClick = onGetClick,
            modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.Small.dp)
        ) {
            Text("Get Coffee Carts")
        }

        Button(
            onClick = onPostClick,
            modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.Small.dp)
        ) {
            Text("Add Coffee Cart")
        }

        Button(
            onClick = { showDeleteDialog = true },
            modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.Small.dp)
        ) {
            Text("Remove Coffee Cart")
        }

        if (dialogMessage != null) {
            AlertDialog(
                onDismissRequest = onDismissDialog,
                title = { Text("Result") },
                text = { Text(dialogMessage) },
                confirmButton = {
                    TextButton(onClick = onDismissDialog) {
                        Text("Dismiss")
                    }
                }
            )
        }

        if (showDeleteDialog) {
            var deleteIdInput by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Remove Coffee Cart") },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(Spacing.Small.dp)
                    ) {
                        Text("Enter the ID of the coffee cart you want to remove.")
                        OutlinedTextField(
                            value = deleteIdInput,
                            onValueChange = { deleteIdInput = it },
                            label = { Text("Cart ID") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onConfirmDelete(deleteIdInput)
                            showDeleteDialog = false
                        }
                    ) {
                        Text("Remove")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Preview
@Composable
private fun ProfileScreenPreview() {
    ProfileContent(
        dialogMessage = null,
        onGetClick = {},
        onPostClick = {},
        onConfirmDelete = {},
        onDismissDialog = {},
    )
}

@Preview
@Composable
private fun ProfileScreenWithDialogPreview() {
    ProfileContent(
        dialogMessage = "Existing Coffee Carts:\n\nDowntown Espresso Cart\n📍 123 Main St\n\nRiverside Brew\n📍 456 River Rd",
        onGetClick = {},
        onPostClick = {},
        onConfirmDelete = {},
        onDismissDialog = {},
    )
}
