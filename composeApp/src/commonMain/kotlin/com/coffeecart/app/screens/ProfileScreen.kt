package com.coffeecart.app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
        onConfirmAdd = { name, address, imageUrl ->
            viewModel.addCoffeeCart(name, address, imageUrl)
        },
        onConfirmEdit = { id, name, address, imageUrl ->
            viewModel.editCoffeeCart(id, name, address, imageUrl)
        },
        onConfirmDelete = { id -> viewModel.removeCoffeeCart(id) },
        onDismissDialog = { viewModel.dismissDialog() },
    )
}

@Composable
fun ProfileContent(
    dialogMessage: String?,
    onGetClick: () -> Unit,
    onConfirmAdd: (String, String, String) -> Unit,
    onConfirmEdit: (String, String, String, String) -> Unit,
    onConfirmDelete: (String) -> Unit,
    onDismissDialog: () -> Unit,
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
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
            onClick = { showAddDialog = true },
            modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.Small.dp)
        ) {
            Text("Add Coffee Cart")
        }

        Button(
            onClick = { showEditDialog = true },
            modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.Small.dp)
        ) {
            Text("Edit Coffee Cart")
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
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(dialogMessage)
                    }
                },
                confirmButton = {
                    TextButton(onClick = onDismissDialog) {
                        Text("Dismiss")
                    }
                }
            )
        }

        if (showAddDialog) {
            var nameInput by remember { mutableStateOf("") }
            var addressInput by remember { mutableStateOf("") }
            var imageUrlInput by remember { mutableStateOf("https://picsum.photos/seed/100/200") }

            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Add Coffee Cart") },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(Spacing.Small.dp)
                    ) {
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text("Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = addressInput,
                            onValueChange = { addressInput = it },
                            label = { Text("Address") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = imageUrlInput,
                            onValueChange = { imageUrlInput = it },
                            label = { Text("Image URL") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onConfirmAdd(nameInput, addressInput, imageUrlInput)
                            showAddDialog = false
                        }
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showEditDialog) {
            var idInput by remember { mutableStateOf("") }
            var nameInput by remember { mutableStateOf("") }
            var addressInput by remember { mutableStateOf("") }
            var imageUrlInput by remember { mutableStateOf("https://picsum.photos/seed/100/200") }

            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                title = { Text("Edit Coffee Cart") },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(Spacing.Small.dp)
                    ) {
                        OutlinedTextField(
                            value = idInput,
                            onValueChange = { idInput = it },
                            label = { Text("Cart ID to Edit") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text("New Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = addressInput,
                            onValueChange = { addressInput = it },
                            label = { Text("New Address") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = imageUrlInput,
                            onValueChange = { imageUrlInput = it },
                            label = { Text("New Image URL") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onConfirmEdit(idInput, nameInput, addressInput, imageUrlInput)
                            showEditDialog = false
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditDialog = false }) {
                        Text("Cancel")
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
        onConfirmAdd = { _, _, _ -> },
        onConfirmEdit = { _, _, _, _ -> },
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
        onConfirmAdd = { _, _, _ -> },
        onConfirmEdit = { _, _, _, _ -> },
        onConfirmDelete = {},
        onDismissDialog = {},
    )
}
