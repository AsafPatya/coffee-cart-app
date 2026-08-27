package com.coffeecart.app.screens.profile

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.coffeecart.app.theme.BorderWidth
import com.coffeecart.app.theme.Spacing
import com.coffeecart.app.theme.dp
import com.coffeecart.shared.feature.cartdetails.CoffeeCartDetailsUiState
import com.coffeecart.shared.feature.cartdetails.CoffeeCartDetailsViewModel
import com.coffeecart.shared.model.MenuCategory
import com.coffeecart.shared.model.Product
import org.koin.compose.koinInject

/**
 * Screen letting a cart owner edit an existing menu category's name/banner and its products
 * (add, edit in place, or remove) in one form, mirroring [CoffeeCartAddCategoryScreen].
 */
@Composable
fun CoffeeCartEditCategoryScreen(
    cartId: String,
    categoryName: String,
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
            val category = state.cart.categories.find { it.name == categoryName }
            if (category == null) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text("Category '$categoryName' not found.", color = MaterialTheme.colorScheme.error)
                }
            } else {
                CoffeeCartEditCategoryForm(
                    cartName = state.cart.name,
                    initialCategory = category,
                    isSaving = isSaving,
                    onSaveCategory = { updatedCategory ->
                        isSaving = true
                        viewModel.editCategory(
                            cartId = cartId,
                            originalCategoryName = categoryName,
                            updatedCategory = updatedCategory,
                        ) { success ->
                            isSaving = false
                            if (success) {
                                showSuccessDialog = true
                            } else {
                                showErrorDialog = "Failed to update category. Please try again."
                            }
                        }
                    },
                    onDeleteCategory = {
                        isSaving = true
                        viewModel.deleteCategory(
                            cartId = cartId,
                            categoryName = categoryName,
                        ) { success ->
                            isSaving = false
                            if (success) {
                                showSuccessDialog = true
                            } else {
                                showErrorDialog = "Failed to delete category. Please try again."
                            }
                        }
                    }
                )
            }
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                onSuccess()
            },
            title = { Text("Success") },
            text = { Text("Successfully updated the category!") },
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
fun CoffeeCartEditCategoryForm(
    cartName: String,
    initialCategory: MenuCategory,
    isSaving: Boolean,
    onSaveCategory: (MenuCategory) -> Unit,
    onDeleteCategory: (() -> Unit)? = null,
) {
    var categoryName by remember { mutableStateOf(initialCategory.name) }
    var categoryDescription by remember { mutableStateOf(initialCategory.description) }
    var categoryImageUrl by remember { mutableStateOf(initialCategory.imageUrl) }

    // Product inputs — also used when editing an existing product in place (see editingProductIndex).
    var productNameInput by remember { mutableStateOf("") }
    var productPriceInput by remember { mutableStateOf("") }
    var productDescriptionInput by remember { mutableStateOf("") }
    var productImageUrlInput by remember { mutableStateOf("") }
    var editingProductIndex by remember { mutableStateOf<Int?>(null) }

    val products = remember { mutableStateListOf(*initialCategory.products.toTypedArray()) }

    var localErrorDialog by remember { mutableStateOf<String?>(null) }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    fun clearProductForm() {
        productNameInput = ""
        productPriceInput = ""
        productDescriptionInput = ""
        productImageUrlInput = ""
        editingProductIndex = null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.XXLarge.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(Spacing.Medium.dp)
    ) {
        Text(
            text = "Edit Menu Category",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Update this category and its products for '$cartName'",
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

        // ==========================================
        // PRODUCT FORM SECTION (add new, or edit an existing one in place)
        // ==========================================
        Text(
            text = if (editingProductIndex == null) "Add a Product" else "Edit Product",
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.Small.dp),
        ) {
            Button(
                modifier = Modifier.weight(1f),
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

                    val product = Product(
                        name = productName,
                        price = price,
                        description = productDescriptionInput.trim(),
                        imageUrl = productImageUrlInput
                    )

                    val indexBeingEdited = editingProductIndex
                    if (indexBeingEdited != null) {
                        products[indexBeingEdited] = product
                    } else {
                        products.add(product)
                    }
                    clearProductForm()
                },
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Save product")
                Spacer(modifier = Modifier.width(Spacing.Small.dp))
                Text(if (editingProductIndex == null) "Add Product" else "Update Product")
            }

            if (editingProductIndex != null) {
                TextButton(onClick = { clearProductForm() }) {
                    Text("Cancel")
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.Medium.dp))

        // ==========================================
        // PRODUCTS LIST SECTION
        // ==========================================
        Text(
            text = "Products in this category (${products.size}):",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = Spacing.XSmall.dp)
        )

        if (products.isEmpty()) {
            Text(
                text = "No products yet. Use the form above to add some.",
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
                            Row {
                                TextButton(onClick = {
                                    productNameInput = product.name
                                    productPriceInput = product.price.toString()
                                    productDescriptionInput = product.description
                                    productImageUrlInput = product.imageUrl
                                    editingProductIndex = idx
                                }) {
                                    Text("Edit")
                                }
                                TextButton(onClick = {
                                    products.removeAt(idx)
                                    if (editingProductIndex == idx) clearProductForm()
                                }) {
                                    Text("Remove")
                                }
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
        // SAVE
        // ==========================================
        Button(
            enabled = !isSaving,
            onClick = {
                if (categoryName.trim().isEmpty()) {
                    localErrorDialog = "Please enter a category name."
                    return@Button
                }
                if (products.isEmpty()) {
                    localErrorDialog = "Please keep at least one product in this category."
                    return@Button
                }

                val finalCategory = MenuCategory(
                    name = categoryName.trim(),
                    imageUrl = categoryImageUrl,
                    products = products.toList(),
                    description = categoryDescription.trim(),
                )
                onSaveCategory(finalCategory)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(Spacing.Large.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Save Changes")
            }
        }

        if (onDeleteCategory != null) {
            var showConfirmDeleteCategoryDialog by remember { mutableStateOf(false) }

            OutlinedButton(
                enabled = !isSaving,
                onClick = { showConfirmDeleteCategoryDialog = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                border = BorderStroke(BorderWidth.XXXSmall.dp, MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete category")
                Spacer(modifier = Modifier.width(Spacing.Small.dp))
                Text("Delete Category")
            }

            if (showConfirmDeleteCategoryDialog) {
                AlertDialog(
                    onDismissRequest = { showConfirmDeleteCategoryDialog = false },
                    title = { Text("Delete Category") },
                    text = { Text("Are you sure you want to delete the category '${initialCategory.name}'?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                showConfirmDeleteCategoryDialog = false
                                onDeleteCategory()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showConfirmDeleteCategoryDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }

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
private fun CoffeeCartEditCategoryScreenPreview() {
    CoffeeCartEditCategoryForm(
        cartName = "Downtown Espresso Cart",
        initialCategory = MenuCategory(
            name = "Coffee & Pastries",
            imageUrl = "https://picsum.photos/seed/cat1/400",
            products = listOf(
                Product(name = "Espresso", price = 8.0, description = "Short & strong", imageUrl = "https://picsum.photos/seed/espresso/400")
            ),
            description = "Freshly baked pastries and specialty coffee drinks",
        ),
        isSaving = false,
        onSaveCategory = {}
    )
}
