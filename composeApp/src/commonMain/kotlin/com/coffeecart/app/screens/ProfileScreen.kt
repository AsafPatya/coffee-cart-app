package com.coffeecart.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.coffeecart.app.theme.Spacing
import com.coffeecart.app.theme.dp
import com.coffeecart.shared.feature.profile.ProfileViewModel
import com.coffeecart.shared.model.CoffeeCart
import org.koin.compose.koinInject

/** Screen enabling operational calls (GET/POST/DELETE) for coffee carts. */
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = koinInject(),
    onAddCategoryClick: (String) -> Unit,
) {
    val dialogMessage by viewModel.dialogMessage.collectAsState()
    val cartsList by viewModel.cartsList.collectAsState()

    ProfileContent(
        dialogMessage = dialogMessage,
        cartsList = cartsList,
        onGetClick = { viewModel.getCoffeeCarts() },
        onConfirmAdd = { name, address, imageUrl ->
            viewModel.addCoffeeCart(name, address, imageUrl)
        },
        onConfirmEdit = { id, name, address, imageUrl ->
            viewModel.editCoffeeCart(id, name, address, imageUrl)
        },
        onConfirmDelete = { id -> viewModel.removeCoffeeCart(id) },
        onDismissDialog = { viewModel.dismissDialog() },
        onAddCategoryClick = onAddCategoryClick
    )
}

@Composable
fun ProfileContent(
    dialogMessage: String?,
    cartsList: List<CoffeeCart>,
    onGetClick: () -> Unit,
    onConfirmAdd: (String, String, String) -> Unit,
    onConfirmEdit: (String, String, String, String) -> Unit,
    onConfirmDelete: (String) -> Unit,
    onDismissDialog: () -> Unit,
    onAddCategoryClick: (String) -> Unit,
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showPickCartDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(Spacing.XXLarge.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
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

        Button(
            onClick = { showPickCartDialog = true },
            modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.Small.dp)
        ) {
            Text("Add New Category")
        }

        if (dialogMessage != null) {
            ResultDialog(
                message = dialogMessage,
                onDismiss = onDismissDialog
            )
        }

        if (showPickCartDialog) {
            PickCartDialog(
                cartsList = cartsList,
                onDismiss = { showPickCartDialog = false },
                onConfirm = { cartId ->
                    onAddCategoryClick(cartId)
                    showPickCartDialog = false
                }
            )
        }

        if (showAddDialog) {
            AddCartDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { name, address, imageUrl ->
                    onConfirmAdd(name, address, imageUrl)
                    showAddDialog = false
                }
            )
        }

        if (showEditDialog) {
            EditCartDialog(
                cartsList = cartsList,
                onDismiss = { showEditDialog = false },
                onConfirm = { id, name, address, imageUrl ->
                    onConfirmEdit(id, name, address, imageUrl)
                    showEditDialog = false
                }
            )
        }

        if (showDeleteDialog) {
            RemoveCartDialog(
                cartsList = cartsList,
                onDismiss = { showDeleteDialog = false },
                onConfirm = { cartId ->
                    onConfirmDelete(cartId)
                    showDeleteDialog = false
                }
            )
        }
    }
}

@Composable
private fun ResultDialog(
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
private fun PickCartDialog(
    cartsList: List<CoffeeCart>,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var selectedCartId by remember { mutableStateOf(cartsList.firstOrNull()?.id ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Coffee Cart") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Spacing.Small.dp)
            ) {
                Text("Choose which coffee cart you'd like to add a category to:")

                if (cartsList.isEmpty()) {
                    Text(
                        text = "No coffee carts found. Please click 'Get Coffee Carts' or add a new cart first.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Column {
                        cartsList.forEach { cart ->
                            val isSelected = cart.id == selectedCartId
                            TextButton(
                                onClick = { selectedCartId = cart.id },
                                modifier = Modifier.fillMaxWidth(),
                                colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                                )
                            ) {
                                Text(
                                    text = "${cart.name} (ID: ${cart.id})",
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = selectedCartId.isNotEmpty(),
                onClick = { onConfirm(selectedCartId) }
            ) {
                Text("Proceed")
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
private fun AddCartDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit,
) {
    var nameInput by remember { mutableStateOf("") }
    var addressInput by remember { mutableStateOf("") }
    var imageUrlInput by remember { mutableStateOf("https://picsum.photos/seed/100/200") }

    var showGallery by remember { mutableStateOf(false) }
    var showCamera by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Coffee Cart") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
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

                Text(
                    text = "Select Cart Image",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = Spacing.Small.dp)
                )

                if (imageUrlInput.isNotEmpty()) {
                    Card(modifier = Modifier.fillMaxWidth().height(Spacing.XXXLarge.dp * 2)) {
                        AsyncImage(
                            model = imageUrlInput,
                            contentDescription = "Cart Preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.Medium.dp)
                ) {
                    Button(
                        onClick = { showGallery = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = "Gallery")
                        Spacer(Modifier.width(Spacing.Small.dp))
                        Text("Storage")
                    }
                    Button(
                        onClick = { showCamera = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Camera, contentDescription = "Camera")
                        Spacer(Modifier.width(Spacing.Small.dp))
                        Text("Camera")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = nameInput.trim().isNotEmpty() && addressInput.trim().isNotEmpty() && imageUrlInput.isNotEmpty(),
                onClick = { onConfirm(nameInput, addressInput, imageUrlInput) }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    // LOCAL PHOTO PICKERS
    if (showGallery) {
        val galleryOptions = listOf(
            "Downtown Cart" to "https://picsum.photos/seed/1/200",
            "Riverside Brew" to "https://picsum.photos/seed/2/200",
            "Central Park Coffee" to "https://picsum.photos/seed/3/200",
            "Cozy Corner Brew" to "https://picsum.photos/seed/4/200",
            "Metro Espresso" to "https://picsum.photos/seed/5/200"
        )

        AlertDialog(
            onDismissRequest = { showGallery = false },
            title = { Text("Simulated Media Storage") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(Spacing.Small.dp)
                ) {
                    Text("Pick an image from simulated device storage:")
                    Column(
                        modifier = Modifier.height(Spacing.XXXLarge.dp * 5).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(Spacing.XSmall.dp)
                    ) {
                        galleryOptions.forEach { (label, url) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        imageUrlInput = url
                                        showGallery = false
                                    }
                                    .padding(Spacing.Small.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = url,
                                    contentDescription = label,
                                    modifier = Modifier.size(Spacing.XXXLarge.dp).clip(MaterialTheme.shapes.small),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(Spacing.Medium.dp))
                                Text(text = label, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showGallery = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showCamera) {
        var cameraStage by remember { mutableStateOf(0) }

        AlertDialog(
            onDismissRequest = {
                showCamera = false
                cameraStage = 0
            },
            title = { Text("Simulated Device Camera") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(Spacing.Medium.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (cameraStage == 0) {
                        Text("Point your camera and snapshot the coffee cart")
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(4f / 3f)
                                .background(Color.Black)
                                .border(2.dp, MaterialTheme.colorScheme.outline),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "📷 Simulated Viewfinder Active...",
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }

                        IconButton(
                            onClick = { cameraStage = 1 },
                            modifier = Modifier
                                .size(Spacing.XXXLarge.dp * 2)
                                .background(Color.White, CircleShape)
                                .border(2.dp, Color.Gray, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Camera,
                                contentDescription = "Shutter",
                                modifier = Modifier.size(Spacing.XXXLarge.dp)
                            )
                        }
                    } else {
                        val generatedUrl = "https://picsum.photos/seed/camera_${(100..999).random()}/200"
                        Text("Snapshot capturing succeeded!")
                        Card(modifier = Modifier.fillMaxWidth().aspectRatio(4f / 3f)) {
                            AsyncImage(
                                model = generatedUrl,
                                contentDescription = "Captured Photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Spacing.Medium.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextButton(
                                onClick = { cameraStage = 0 },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Retake")
                            }
                            Button(
                                onClick = {
                                    imageUrlInput = generatedUrl
                                    showCamera = false
                                    cameraStage = 0
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Use Photo")
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                if (cameraStage == 0) {
                    TextButton(
                        onClick = {
                            showCamera = false
                            cameraStage = 0
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            }
        )
    }
}

@Composable
private fun EditCartDialog(
    cartsList: List<CoffeeCart>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit,
) {
    var selectedCart by remember { mutableStateOf<CoffeeCart?>(null) }
    var nameInput by remember { mutableStateOf("") }
    var addressInput by remember { mutableStateOf("") }
    var imageUrlInput by remember { mutableStateOf("") }

    var showGallery by remember { mutableStateOf(false) }
    var showCamera by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Coffee Cart") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Spacing.Small.dp)
            ) {
                if (cartsList.isEmpty()) {
                    Text(
                        text = "No coffee carts found. Please click 'Get Coffee Carts' or add a new cart first.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Text("1. Select a Cart to Edit:", style = MaterialTheme.typography.titleMedium)
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        cartsList.forEach { cart ->
                            val isSelected = selectedCart?.id == cart.id
                            TextButton(
                                onClick = {
                                    selectedCart = cart
                                    nameInput = cart.name
                                    addressInput = cart.address
                                    imageUrlInput = cart.imageUrl
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                                )
                            ) {
                                Text(
                                    text = "${cart.name} (ID: ${cart.id})",
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    if (selectedCart != null) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.Small.dp))
                        Text(
                            "2. Edit Coffee Cart Specifics:",
                            style = MaterialTheme.typography.titleMedium
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

                        Text(
                            text = "Select New Cart Image",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = Spacing.Small.dp)
                        )

                        if (imageUrlInput.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth().height(Spacing.XXXLarge.dp * 2)
                            ) {
                                AsyncImage(
                                    model = imageUrlInput,
                                    contentDescription = "Cart Preview",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.Medium.dp)
                        ) {
                            Button(
                                onClick = { showGallery = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = "Gallery"
                                )
                                Spacer(Modifier.width(Spacing.Small.dp))
                                Text("Storage")
                            }
                            Button(
                                onClick = { showCamera = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Camera,
                                    contentDescription = "Camera"
                                )
                                Spacer(Modifier.width(Spacing.Small.dp))
                                Text("Camera")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = selectedCart != null && nameInput.trim()
                    .isNotEmpty() && addressInput.trim().isNotEmpty() && imageUrlInput.isNotEmpty(),
                onClick = {
                    selectedCart?.let { cart ->
                        onConfirm(cart.id, nameInput, addressInput, imageUrlInput)
                    }
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

    // LOCAL PHOTO PICKERS
    if (showGallery) {
        val galleryOptions = listOf(
            "Downtown Cart" to "https://picsum.photos/seed/1/200",
            "Riverside Brew" to "https://picsum.photos/seed/2/200",
            "Central Park Coffee" to "https://picsum.photos/seed/3/200",
            "Cozy Corner Brew" to "https://picsum.photos/seed/4/200",
            "Metro Espresso" to "https://picsum.photos/seed/5/200"
        )

        AlertDialog(
            onDismissRequest = { showGallery = false },
            title = { Text("Simulated Media Storage") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(Spacing.Small.dp)
                ) {
                    Text("Pick an image from simulated device storage:")
                    Column(
                        modifier = Modifier.height(Spacing.XXXLarge.dp * 5).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(Spacing.XSmall.dp)
                    ) {
                        galleryOptions.forEach { (label, url) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        imageUrlInput = url
                                        showGallery = false
                                    }
                                    .padding(Spacing.Small.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = url,
                                    contentDescription = label,
                                    modifier = Modifier.size(Spacing.XXXLarge.dp).clip(MaterialTheme.shapes.small),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(Spacing.Medium.dp))
                                Text(text = label, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showGallery = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showCamera) {
        var cameraStage by remember { mutableStateOf(0) }

        AlertDialog(
            onDismissRequest = {
                showCamera = false
                cameraStage = 0
            },
            title = { Text("Simulated Device Camera") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(Spacing.Medium.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (cameraStage == 0) {
                        Text("Point your camera and snapshot the coffee cart")
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(4f / 3f)
                                .background(Color.Black)
                                .border(2.dp, MaterialTheme.colorScheme.outline),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "📷 Simulated Viewfinder Active...",
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }

                        IconButton(
                            onClick = { cameraStage = 1 },
                            modifier = Modifier
                                .size(Spacing.XXXLarge.dp * 2)
                                .background(Color.White, CircleShape)
                                .border(2.dp, Color.Gray, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Camera,
                                contentDescription = "Shutter",
                                modifier = Modifier.size(Spacing.XXXLarge.dp)
                            )
                        }
                    } else {
                        val generatedUrl = "https://picsum.photos/seed/camera_${(100..999).random()}/200"
                        Text("Snapshot capturing succeeded!")
                        Card(modifier = Modifier.fillMaxWidth().aspectRatio(4f / 3f)) {
                            AsyncImage(
                                model = generatedUrl,
                                contentDescription = "Captured Photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Spacing.Medium.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextButton(
                                onClick = { cameraStage = 0 },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Retake")
                            }
                            Button(
                                onClick = {
                                    imageUrlInput = generatedUrl
                                    showCamera = false
                                    cameraStage = 0
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Use Photo")
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                if (cameraStage == 0) {
                    TextButton(
                        onClick = {
                            showCamera = false
                            cameraStage = 0
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            }
        )
    }
}

@Composable
private fun RemoveCartDialog(
    cartsList: List<CoffeeCart>,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var selectedCartId by remember { mutableStateOf(cartsList.firstOrNull()?.id ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Remove Coffee Cart") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Spacing.Small.dp)
            ) {
                Text("Select the coffee cart you want to remove:")

                if (cartsList.isEmpty()) {
                    Text(
                        text = "No coffee carts found. Please click 'Get Coffee Carts' or add a new cart first.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        cartsList.forEach { cart ->
                            val isSelected = selectedCartId == cart.id
                            TextButton(
                                onClick = { selectedCartId = cart.id },
                                modifier = Modifier.fillMaxWidth(),
                                colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                                )
                            ) {
                                Text(
                                    text = "${cart.name} (ID: ${cart.id})",
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = selectedCartId.isNotEmpty(),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                onClick = { onConfirm(selectedCartId) }
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

@Preview
@Composable
private fun ProfileScreenPreview() {
    ProfileContent(
        dialogMessage = null,
        cartsList = emptyList(),
        onGetClick = {},
        onConfirmAdd = { _, _, _ -> },
        onConfirmEdit = { _, _, _, _ -> },
        onConfirmDelete = {},
        onDismissDialog = {},
        onAddCategoryClick = {}
    )
}

@Preview
@Composable
private fun ProfileScreenWithDialogPreview() {
    ProfileContent(
        dialogMessage = "Existing Coffee Carts:\n\nDowntown Espresso Cart\n📍 123 Main St\n\nRiverside Brew\n📍 456 River Rd",
        cartsList = listOf(
            CoffeeCart("1", "Downtown Espresso Cart", "123 Main St", ""),
            CoffeeCart("2", "Riverside Brew", "456 River Rd", "")
        ),
        onGetClick = {},
        onConfirmAdd = { _, _, _ -> },
        onConfirmEdit = { _, _, _, _ -> },
        onConfirmDelete = {},
        onDismissDialog = {},
        onAddCategoryClick = {}
    )
}
