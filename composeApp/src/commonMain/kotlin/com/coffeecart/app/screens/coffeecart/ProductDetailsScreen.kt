package com.coffeecart.app.screens.coffeecart

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coffeecart.composeapp.generated.resources.Res
import coffeecart.composeapp.generated.resources.strAddToCart
import coffeecart.composeapp.generated.resources.strAddedToBasket
import coffeecart.composeapp.generated.resources.strComments
import com.coffeecart.app.theme.Spacing
import com.coffeecart.app.theme.dp
import com.coffeecart.app.ui.buttons.OverlayBackButton
import com.coffeecart.shared.feature.products.ProductsUiState
import com.coffeecart.shared.feature.products.ProductsViewModel
import com.coffeecart.shared.model.Customization
import com.coffeecart.shared.model.CustomizationOption
import com.coffeecart.shared.model.CustomizationType
import com.coffeecart.shared.model.Product
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

/**
 * Full-screen product page (hero image with a small overlay back button, matching
 * CoffeeCartDetailsScreen). Re-loads the category's products by [cartId]/[categoryName] and finds
 * [productName] among them, since Product has no stable id of its own.
 */
@Composable
fun ProductDetailsScreen(
    cartId: String,
    categoryName: String,
    productName: String,
    onBackClick: () -> Unit,
    viewModel: ProductsViewModel = koinInject(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val addedText = stringResource(Res.string.strAddedToBasket)

    LaunchedEffect(cartId, categoryName) {
        viewModel.loadProducts(cartId, categoryName)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is ProductsUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is ProductsUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is ProductsUiState.Success -> {
                val product = state.products.find { it.name == productName }
                if (product == null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "Product not found.", color = MaterialTheme.colorScheme.error)
                    }
                } else {
                    ProductDetailsContent(
                        product = product,
                        onBackClick = onBackClick,
                        onAddToCart = { quantity, comment, selectedOptionIds ->
                            viewModel.addProductToCart(
                                cartId = cartId,
                                product = product,
                                quantity = quantity,
                                comment = comment,
                                addedText = addedText,
                                selectedOptionIds = selectedOptionIds,
                            )
                            onBackClick()
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductDetailsContent(
    product: Product,
    onBackClick: () -> Unit,
    onAddToCart: (quantity: Int, comment: String, selectedOptionIds: List<String>) -> Unit,
) {
    var quantity by remember { mutableStateOf(1) }
    var comment by remember { mutableStateOf("") }
    var selectedOptionIds by remember(product) { mutableStateOf(emptySet<String>()) }
    val scrollState = rememberScrollState()

    val priceExtraByOptionId = remember(product) {
        product.customizations.flatMap { it.options }.associate { it.id to it.priceExtra }
    }
    val unitPrice =
        product.price + selectedOptionIds.sumOf { id -> priceExtraByOptionId[id] ?: 0.0 }
    val allRequiredSatisfied = product.customizations.all { customization ->
        !customization.required || customization.options.count { it.id in selectedOptionIds } >= customization.minSelect
    }
    val selectionSummary = remember(product, selectedOptionIds) {
        product.customizations
            .flatMap { it.options }
            .filter { it.id in selectedOptionIds }
            .joinToString(" | ") { it.name }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Spacing.HeroHeight.dp),
            ) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillWidth,
                )
                OverlayBackButton(
                    onClick = onBackClick,
                    modifier = Modifier.align(Alignment.TopStart),
                )
            }

            Column(modifier = Modifier.fillMaxWidth().padding(Spacing.Large.dp)) {
                Text(product.name, style = MaterialTheme.typography.headlineSmall)

                if (product.description.isNotEmpty()) {
                    Text(
                        text = product.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = Spacing.XXSmall.dp),
                    )
                }

                Text(
                    text = formatPrice(unitPrice),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = Spacing.Small.dp),
                )

                if (selectionSummary.isNotEmpty()) {
                    Text(
                        text = "נבחר: $selectionSummary",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                product.customizations.forEach { customization ->
                    CustomizationSection(
                        customization = customization,
                        selectedOptionIds = selectedOptionIds,
                        onToggle = { optionId ->
                            selectedOptionIds = toggleCustomizationOption(
                                customization,
                                optionId,
                                selectedOptionIds
                            )
                        },
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.Medium.dp))

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = {
                        Text(
                            text = stringResource(Res.string.strComments),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                        )
                    },
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth(),
                )

                // Extra room so the floating bottom bar never covers the last scrolled content.
                Spacer(modifier = Modifier.height(Spacing.XXXXLarge.dp * 2))
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(Spacing.Large.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(
                onClick = { onAddToCart(quantity, comment, selectedOptionIds.toList()) },
                enabled = allRequiredSatisfied,
                shape = MaterialTheme.shapes.extraLarge,
                modifier = Modifier
                    .weight(1f)
                    .height(Spacing.XXXXLarge.dp),
            ) {
                Text(stringResource(Res.string.strAddToCart))
            }

            Spacer(modifier = Modifier.width(Spacing.Small.dp))

            // Quantity Controls Container
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceVariant, // Or secondaryContainer / surface
                modifier = Modifier.height(Spacing.XXXXLarge.dp) // Matches the Button height
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = Spacing.XSmall.dp)
                ) {
                    IconButton(onClick = { if (quantity > 1) quantity-- }) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Decrease quantity"
                        )
                    }
                    Text(
                        text = "$quantity",
                        style = MaterialTheme.typography.titleLarge
                    )
                    IconButton(onClick = { quantity++ }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Increase quantity"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomizationSection(
    customization: Customization,
    selectedOptionIds: Set<String>,
    onToggle: (optionId: String) -> Unit,
) {
    var expanded by remember(customization) { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Spacing.Medium.dp)
            .clip(MaterialTheme.shapes.medium)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = MaterialTheme.shapes.medium,
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(Spacing.Medium.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row {
                    Text(
                        text = customization.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    if (customization.required) {
                        Text(
                            text = " *",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
                val selectedNames = remember(customization, selectedOptionIds) {
                    customization.options.filter { it.id in selectedOptionIds }.joinToString(", ") { it.name }
                }
                if (selectedNames.isNotEmpty()) {
                    Text(
                        text = "נבחר: $selectedNames",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            val rotation by animateFloatAsState(targetValue = if (expanded) 180f else 0f)
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.rotate(rotation),
            )
        }

        if (expanded) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.Medium.dp, vertical = Spacing.Small.dp)) {
                customization.options.forEach { option ->
                    val selected = option.id in selectedOptionIds
                    // Only auto-close when actually selecting — clicking an already-selected option
                    // clears it instead, and should leave the drawer open to pick something else.
                    val selectOption = {
                        onToggle(option.id)
                        if (!selected) expanded = false
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectOption() }
                            .padding(vertical = Spacing.XXSmall.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (customization.type == CustomizationType.SINGLE) {
                            RadioButton(selected = selected, onClick = { selectOption() })
                        } else {
                            Checkbox(checked = selected, onCheckedChange = { selectOption() })
                        }
                        Text(
                            text = option.name,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                        )
                        if (option.priceExtra > 0) {
                            Text(
                                text = "+${formatPrice(option.priceExtra)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

/** SINGLE clears the rest of the group before selecting, or clears the group entirely if the
 *  already-selected option is clicked again; MULTIPLE toggles freely up to maxSelect. */
private fun toggleCustomizationOption(
    customization: Customization,
    optionId: String,
    current: Set<String>,
): Set<String> {
    val groupIds = customization.options.map { it.id }.toSet()
    return when (customization.type) {
        CustomizationType.SINGLE -> when {
            optionId in current -> current - groupIds
            else -> (current - groupIds) + optionId
        }
        CustomizationType.MULTIPLE -> when {
            optionId in current -> current - optionId
            current.count { it in groupIds } >= customization.maxSelect -> current
            else -> current + optionId
        }
    }
}

@Preview
@Composable
private fun ProductDetailsContentPreview() {
    ProductDetailsContent(
        product = Product(
            name = "Caffè Latte",
            price = 4.50,
            description = "Rich espresso with steamed milk and a thin layer of foam.",
            imageUrl = "https://picsum.photos/seed/latte/200",
            customizations = listOf(
                Customization(
                    id = "cust_strength",
                    title = "Strong / weak",
                    type = CustomizationType.SINGLE,
                    required = true,
                    minSelect = 1,
                    maxSelect = 1,
                    options = listOf(
                        CustomizationOption(id = "opt_strong", name = "Strong"),
                        CustomizationOption(id = "opt_weak", name = "Weak"),
                    ),
                ),
                Customization(
                    id = "cust_extras",
                    title = "Extras",
                    type = CustomizationType.MULTIPLE,
                    required = false,
                    minSelect = 0,
                    maxSelect = 2,
                    options = listOf(
                        CustomizationOption(id = "opt_extra_shot", name = "Extra espresso shot", priceExtra = 3.0),
                        CustomizationOption(id = "opt_whipped_cream", name = "Whipped cream", priceExtra = 4.0),
                    ),
                ),
            ),
        ),
        onBackClick = {},
        onAddToCart = { _, _, _ -> }
    )
}
