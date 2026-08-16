package com.coffeecart.app.screens.coffeecart

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.coffeecart.app.theme.Spacing
import com.coffeecart.app.theme.dp
import com.coffeecart.shared.feature.cartdetails.CoffeeCartDetailsUiState
import com.coffeecart.shared.feature.cartdetails.CoffeeCartDetailsViewModel
import com.coffeecart.shared.model.MenuCategory
import com.coffeecart.shared.model.Product
import org.koin.compose.koinInject

/**
 * Screen displaying a wizard that helps operations teams:
 * 1. Build a new Menu Category.
 * 2. Simulates choosing from storage or photographing using device camera.
 * 3. Builds a list of Category Products.
 * 4. Submits and commits the entire category list state.
 */
@Composable
fun CoffeeCartAddCategoryScreen(
    cartId: String,
    onSuccess: () -> Unit,
    viewModel: CoffeeCartDetailsViewModel = koinInject(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var isSaving by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(cartId) {
        viewModel.loadCart(cartId)
    }

    when (val state = uiState) {
        is CoffeeCartDetailsUiState.Loading -> {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is CoffeeCartDetailsUiState.Error -> {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text(text = "Error: " + state.message, color = MaterialTheme.colorScheme.error)
            }
        }
        is CoffeeCartDetailsUiState.Success -> {
            CoffeeCartAddCategoryWizard(
                cartName = state.cart.name,
                isSaving = isSaving,
                onSaveCategory = { category ->
                    isSaving = true
                    viewModel.addCategory(cartId = cartId, category = category) { success ->
                        isSaving = false
                        if (success) {
                            showSuccessDialog = true
                        } else {
                            showErrorDialog = "Failed to add new category. Please try again."
                        }
                    }
                }
            )
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                onSuccess()
            },
            title = { Text("Success") },
            text = { Text("Successfully added new category and products to your coffee cart!") },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onSuccess()
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }

    if (showErrorDialog != null) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = null },
            title = { Text("Error") },
            text = { Text(showErrorDialog ?: "") },
            confirmButton = {
                Button(onClick = { showErrorDialog = null }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun CoffeeCartAddCategoryWizard(
    cartName: String,
    isSaving: Boolean,
    onSaveCategory: (MenuCategory) -> Unit,
) {
    var step by remember { mutableStateOf(1) } // 1: Category Data, 2: Add Products

    // Category States
    var categoryName by remember { mutableStateOf("") }
    var categoryImageUrl by remember { mutableStateOf("") }

    // Product inputs (LIFTED to outer wizard scope to fix scoping bug!)
    var productNameInput by remember { mutableStateOf("") }
    var productPriceInput by remember { mutableStateOf("") }
    var productDescriptionInput by remember { mutableStateOf("") }
    var productImageUrlInput by remember { mutableStateOf("") }

    // Products list state
    val products = remember { mutableStateListOf<Product>() }

    // Dialog state for picks
    var activePickerTarget by remember { mutableStateOf<String?>(null) } // "category" or "product"
    var showGalleryDialog by remember { mutableStateOf(false) }
    var showCameraDialog by remember { mutableStateOf(false) }
    var localErrorDialog by remember { mutableStateOf<String?>(null) }

    // Callback on media picks
    val onMediaSelected = { url: String ->
        if (activePickerTarget == "category") {
            categoryImageUrl = url
        } else if (activePickerTarget == "product") {
            productImageUrlInput = url
        }
        activePickerTarget = null
    }

    if (step == 1) {
        // Step 1: Category Details Form
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.XXLarge.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.Medium.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Step 1: Category Details",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Add a new menu category for '$cartName'",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            OutlinedTextField(
                value = categoryName,
                onValueChange = { categoryName = it },
                label = { Text("Category Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(top = Spacing.Medium.dp)
            )

            Text(
                text = "Select Category Banner Image",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Start).padding(top = Spacing.Medium.dp)
            )

            if (categoryImageUrl.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Spacing.XXXLarge.dp * 4) // 128.dp
                ) {
                    AsyncImage(
                        model = categoryImageUrl,
                        contentDescription = "Selected Banner",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Spacing.XXXLarge.dp * 4)
                        .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
                        .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No Image Selected",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.Medium.dp)
            ) {
                Button(
                    onClick = {
                        activePickerTarget = "category"
                        showGalleryDialog = true
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                ) {
                    Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = "Gallery")
                    Spacer(Modifier.width(Spacing.Small.dp))
                    Text("Storage")
                }
                Button(
                    onClick = {
                        activePickerTarget = "category"
                        showCameraDialog = true
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                ) {
                    Icon(imageVector = Icons.Default.Camera, contentDescription = "Camera")
                    Spacer(Modifier.width(Spacing.Small.dp))
                    Text("Camera")
                }
            }

            Spacer(modifier = Modifier.height(Spacing.Large.dp))

            Button(
                enabled = categoryName.trim().isNotEmpty() && categoryImageUrl.isNotEmpty(),
                onClick = { step = 2 },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Next: Add Products")
            }
        }
    } else {
        // Step 2: Add Products list Form
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.XXLarge.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.Medium.dp)
        ) {
            Text(
                text = "Step 2: Add Products",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Define products for '$categoryName'",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Added Products (${products.size}):",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = Spacing.Medium.dp)
            )

            if (products.isEmpty()) {
                Text(
                    text = "No products yet. Fill in the form below to append items.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(Spacing.Medium.dp)) {
                        products.forEachIndexed { idx, product ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.XXSmall.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    AsyncImage(
                                        model = product.imageUrl,
                                        contentDescription = product.name,
                                        modifier = Modifier.size(Spacing.XXXLarge.dp).clip(MaterialTheme.shapes.small),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(Spacing.Small.dp))
                                    Column {
                                        Text(text = product.name, style = MaterialTheme.typography.bodyLarge)
                                        Text(text = "$${product.price}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                                TextButton(onClick = { products.removeAt(idx) }) {
                                    Text("Remove")
                                }
                            }
                            if (idx < products.size - 1) {
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.Medium.dp))

            Text(
                text = "Add New Product Form",
                style = MaterialTheme.typography.titleLarge
            )

            OutlinedTextField(
                value = productNameInput,
                onValueChange = { productNameInput = it },
                label = { Text("Product Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = productPriceInput,
                onValueChange = { productPriceInput = it },
                label = { Text("Price (e.g. 4.50)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = productDescriptionInput,
                onValueChange = { productDescriptionInput = it },
                label = { Text("Product Description") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Select Product Image",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = Spacing.Small.dp)
            )

            if (productImageUrlInput.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth().height(Spacing.XXXLarge.dp * 2)) {
                    AsyncImage(
                        model = productImageUrlInput,
                        contentDescription = "Product Preview",
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
                    onClick = {
                        activePickerTarget = "product"
                        showGalleryDialog = true
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                ) {
                    Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = "Gallery")
                    Spacer(Modifier.width(Spacing.Small.dp))
                    Text("Storage")
                }
                Button(
                    onClick = {
                        activePickerTarget = "product"
                        showCameraDialog = true
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                ) {
                    Icon(imageVector = Icons.Default.Camera, contentDescription = "Camera")
                    Spacer(Modifier.width(Spacing.Small.dp))
                    Text("Camera")
                }
            }

            Button(
                enabled = productNameInput.trim().isNotEmpty() &&
                        productPriceInput.replace("$", "").replace(",", ".").trim().toDoubleOrNull() != null &&
                        productDescriptionInput.trim().isNotEmpty() &&
                        productImageUrlInput.isNotEmpty(),
                onClick = {
                    val priceStr = productPriceInput.replace("$", "").replace(",", ".").trim()
                    val price = priceStr.toDoubleOrNull() ?: 0.0
                    products.add(
                        Product(
                            name = productNameInput.trim(),
                            price = price,
                            description = productDescriptionInput.trim(),
                            imageUrl = productImageUrlInput
                        )
                    )
                    // Reset single product states
                    productNameInput = ""
                    productPriceInput = ""
                    productDescriptionInput = ""
                    productImageUrlInput = ""
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
                Spacer(modifier = Modifier.width(Spacing.Small.dp))
                Text("Add Product to Category")
            }

            Spacer(modifier = Modifier.height(Spacing.Medium.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { step = 1 }) {
                    Text("Back to Step 1")
                }

                Button(
                    enabled = !isSaving,
                    onClick = {
                        val finalProducts = products.toMutableList()
                        val isProductFormFilled = productNameInput.trim().isNotEmpty() ||
                                productPriceInput.trim().isNotEmpty() ||
                                productDescriptionInput.trim().isNotEmpty() ||
                                productImageUrlInput.isNotEmpty()

                        if (isProductFormFilled) {
                            val priceStr = productPriceInput.replace("$", "").replace(",", ".").trim()
                            val priceParsed = priceStr.toDoubleOrNull()

                            if (productNameInput.trim().isEmpty()) {
                                localErrorDialog = "Please enter a product name."
                                return@Button
                            }
                            if (priceParsed == null) {
                                localErrorDialog = "Please enter a valid price (e.g. 4.50)."
                                return@Button
                            }
                            if (productDescriptionInput.trim().isEmpty()) {
                                localErrorDialog = "Please enter a product description."
                                return@Button
                            }
                            if (productImageUrlInput.isEmpty()) {
                                localErrorDialog = "Please select an image for your product."
                                return@Button
                            }

                            finalProducts.add(
                                Product(
                                    name = productNameInput.trim(),
                                    price = priceParsed,
                                    description = productDescriptionInput.trim(),
                                    imageUrl = productImageUrlInput
                                )
                            )
                        }

                        if (finalProducts.isEmpty()) {
                            localErrorDialog = "Please add at least one product to this category."
                            return@Button
                        }

                        val finalCategory = MenuCategory(
                            name = categoryName.trim(),
                            imageUrl = categoryImageUrl,
                            products = finalProducts.toList()
                        )
                        onSaveCategory(finalCategory)
                    }
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(Spacing.Large.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Save & Complete")
                    }
                }
            }
        }
    }

    // LOCAL ERROR DIALOG
    if (localErrorDialog != null) {
        AlertDialog(
            onDismissRequest = { localErrorDialog = null },
            title = { Text("Validation Requirement") },
            text = { Text(localErrorDialog ?: "") },
            confirmButton = {
                Button(onClick = { localErrorDialog = null }) {
                    Text("OK")
                }
            }
        )
    }

    // SIMULATED PHOTO PICKERS (Storage & Camera)
    if (showGalleryDialog) {
        val galleryOptions = listOf(
            "Coffee Banner" to "https://picsum.photos/seed/cat1/400",
            "Iced Beverages" to "https://picsum.photos/seed/cat2/400",
            "Mouthwatering Bakery" to "https://picsum.photos/seed/cat4/400",
            "Fresh Healthy Bowls" to "https://picsum.photos/seed/cat3/400",
            "Classic Latte" to "https://picsum.photos/seed/latte/200",
            "Tasty Croissant" to "https://picsum.photos/seed/crois/200"
        )

        AlertDialog(
            onDismissRequest = { showGalleryDialog = false },
            title = { Text("Simulated Media Storage") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(Spacing.Small.dp)
                ) {
                    Text("Pick an image from simulated device storage:")
                    Column(
                        modifier = Modifier.height(Spacing.XXXLarge.dp * 6).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(Spacing.XSmall.dp)
                    ) {
                        galleryOptions.forEach { (label, url) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onMediaSelected(url)
                                        showGalleryDialog = false
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
                TextButton(onClick = { showGalleryDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showCameraDialog) {
        var cameraStage by remember { mutableStateOf(0) } // 0: Viewfinder, 1: Flash/Preview

        AlertDialog(
            onDismissRequest = {
                showCameraDialog = false
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
                        Text("Point your camera and snapshot menu items")
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
                        val generatedUrl = "https://picsum.photos/seed/camera_${(100..999).random()}/400"
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
                                    onMediaSelected(generatedUrl)
                                    showCameraDialog = false
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
                            showCameraDialog = false
                            cameraStage = 0
                        }
                    ) {
                        Text("Option Cancel")
                    }
                }
            }
        )
    }
}

@Preview
@Composable
private fun CoffeeCartAddCategoryScreenPreview() {
    CoffeeCartAddCategoryWizard(
        cartName = "Downtown Espresso Cart",
        isSaving = false,
        onSaveCategory = {}
    )
}
