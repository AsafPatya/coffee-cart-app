package com.coffeecart.app.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import coil3.compose.AsyncImage
import com.coffeecart.app.screens.profile.ui.components.CartMediaPicker
import com.coffeecart.app.theme.Spacing
import com.coffeecart.app.theme.dp
import com.coffeecart.shared.feature.cartdetails.CoffeeCartDetailsUiState
import com.coffeecart.shared.feature.cartdetails.CoffeeCartDetailsViewModel
import com.coffeecart.shared.model.MenuCategory
import com.coffeecart.shared.model.Product
import org.koin.compose.koinInject

/**
 * Screen displaying a unified, single-screen form that helps operations teams:
 * 1. Build a new Menu Category with a name and a custom banner.
 * 2. Directly add/remove Category Products on the same screen.
 * 3. Verify validation constraints and commit the entire category list state.
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
            CoffeeCartAddCategoryForm(
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
fun CoffeeCartAddCategoryForm(
    cartName: String,
    isSaving: Boolean,
    onSaveCategory: (MenuCategory) -> Unit,
) {
    // Category States
    var categoryName by remember { mutableStateOf("") }
    var categoryDescription by remember { mutableStateOf("") }
    var categoryImageUrl by remember { mutableStateOf("") }

    // Product inputs
    var productNameInput by remember { mutableStateOf("") }
    var productPriceInput by remember { mutableStateOf("") }
    var productDescriptionInput by remember { mutableStateOf("") }
    var productImageUrlInput by remember { mutableStateOf("") }

    // Products list state
    val products = remember { mutableStateListOf<Product>() }

    var localErrorDialog by remember { mutableStateOf<String?>(null) }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.XXLarge.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(Spacing.Medium.dp)
    ) {
        Text(
            text = "Add Menu Category",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Create a new category and add its items for '$cartName'",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        // ==========================================
        // CATEGORY DETAILS SECTION
        // ==========================================
        OutlinedTextField(
            value = categoryName,
            onValueChange = { categoryName = it },
            label = { Text("Category Name") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            modifier = Modifier.fillMaxWidth().padding(top = Spacing.Medium.dp)
        )

        OutlinedTextField(
            value = categoryDescription,
            onValueChange = { categoryDescription = it },
            label = { Text("Category Description (optional)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            modifier = Modifier.fillMaxWidth()
        )

        CartMediaPicker(
            imageUrl = categoryImageUrl,
            onImageUrlChange = { categoryImageUrl = it },
            title = "Select Category Banner Image"
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.Medium.dp))

        // ====================f======================
        // NEW PRODUCT FORM SECTION
        // ==========================================
        Text(
            text = "Add Products to Category",
            style = MaterialTheme.typography.titleLarge
        )

        OutlinedTextField(
            value = productNameInput,
            onValueChange = { productNameInput = it },
            label = { Text("Product Name") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = productPriceInput,
            onValueChange = { productPriceInput = it },
            label = { Text("Price (e.g. 4.50)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = productDescriptionInput,
            onValueChange = { productDescriptionInput = it },
            label = { Text("Product Description") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                keyboardController?.hide()
                focusManager.clearFocus()
            }),
            )

        CartMediaPicker(
            imageUrl = productImageUrlInput,
            onImageUrlChange = { productImageUrlInput = it },
            title = "Select Product Image"
        )

        Button(
            enabled = true,
            onClick = {
                val productName = productNameInput.trim()
                if (productName.isEmpty()) {
                    localErrorDialog = "Please enter a product name."
                    return@Button
                }

                val priceStr = productPriceInput.replace("$", "").replace(",", ".").trim()
                val price = priceStr.toDoubleOrNull()
                if (price == null) {
                    localErrorDialog = "Please enter a valid price (e.g. 4.50)."
                    return@Button
                }

                products.add(
                    Product(
                        name = productName,
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
            Text("Add Product to Category List")
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.Medium.dp))

        // ==========================================
        // ADDED PRODUCTS LIST SECTION
        // ==========================================
        Text(
            text = "Added Products (${products.size}):",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = Spacing.XSmall.dp)
        )

        if (products.isEmpty()) {
            Text(
                text = "No products added yet. Use the form above to append products.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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

        Spacer(modifier = Modifier.height(Spacing.Large.dp))

        // ==========================================
        // SAVE & COMPLETE SUBMISSION
        // ==========================================
        Button(
            enabled = !isSaving,
            onClick = {
                if (categoryName.trim().isEmpty()) {
                    localErrorDialog = "Please enter a category name."
                    return@Button
                }

                val finalProducts = products.toMutableList()
                val isProductFormFilled = productNameInput.trim().isNotEmpty() ||
                        productPriceInput.trim().isNotEmpty() ||
                        productDescriptionInput.trim().isNotEmpty() ||
                        productImageUrlInput.isNotEmpty()

                if (isProductFormFilled) {
                    val productName = productNameInput.trim()
                    if (productName.isEmpty()) {
                        localErrorDialog = "You started entering a product. Please enter a product name."
                        return@Button
                    }

                    val priceStr = productPriceInput.replace("$", "").replace(",", ".").trim()
                    val priceParsed = priceStr.toDoubleOrNull()
                    if (priceParsed == null) {
                        localErrorDialog = "Please enter a valid product price (e.g. 4.50)."
                        return@Button
                    }

                    finalProducts.add(
                        Product(
                            name = productName,
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
                    products = finalProducts.toList(),
                    description = categoryDescription.trim(),
                )
                onSaveCategory(finalCategory)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(Spacing.Large.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Save & Complete")
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
}

@Preview
@Composable
private fun CoffeeCartAddCategoryScreenPreview() {
    CoffeeCartAddCategoryForm(
        cartName = "Downtown Espresso Cart",
        isSaving = false,
        onSaveCategory = {}
    )
}
