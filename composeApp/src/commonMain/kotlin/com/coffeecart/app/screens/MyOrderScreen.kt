package com.coffeecart.app.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import coil3.compose.AsyncImage
import com.coffeecart.app.theme.Spacing
import com.coffeecart.app.theme.dp
import com.coffeecart.app.ui.payment.CheckoutWebView
import com.coffeecart.app.ui.payment.clearCheckoutReturnUrl
import com.coffeecart.app.ui.payment.currentPageUrl
import com.coffeecart.shared.domain.ShoppingCartState
import com.coffeecart.shared.feature.myorder.MyOrderViewModel
import com.coffeecart.shared.model.OrderItem
import com.coffeecart.shared.model.Product
import coffeecart.composeapp.generated.resources.Res
import coffeecart.composeapp.generated.resources.strComments
import coffeecart.composeapp.generated.resources.strNoOpenOrder
import coffeecart.composeapp.generated.resources.strOrderPlaced
import coffeecart.composeapp.generated.resources.strPlaceOrder
import coffeecart.composeapp.generated.resources.strStartNewOrder
import coffeecart.composeapp.generated.resources.strUpdateItem
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

/** The basket for the in-progress order — a single coffee cart's products, held locally on-device. */
@Composable
fun MyOrderScreen(
    onExploreCartsClick: () -> Unit,
    viewModel: MyOrderViewModel = koinInject(),
) {
    val state by viewModel.cartState.collectAsState()
    var selectedItem by remember { mutableStateOf<OrderItem?>(null) }
    val isPlacingOrder by viewModel.isPlacingOrder.collectAsState()
    val checkoutUrl by viewModel.checkoutUrl.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    // Path only, not a full domain+path prefix: the server builds these against the web app's own
    // public domain (see webPublicBaseUrl in Application.kt), which differs from the API server's
    // domain (ServerEnvironment.baseUrl) — matching by path keeps this independent of which domain
    // actually serves it, across QA/prod/local and Android/iOS/web alike.
    val completeUrlPrefix = "/payments/complete"
    val errorUrlPrefix = "/payments/error"

    // On web, checkout redirects the current tab away and Grow redirects back to complete/error —
    // the app reloads fresh at that URL, so check for it once on startup (no-op on Android/iOS,
    // which embed checkout in a native WebView within the same session).
    LaunchedEffect(Unit) {
        val returnUrl = currentPageUrl()
        when {
            returnUrl.contains(completeUrlPrefix) -> {
                val message = getString(Res.string.strOrderPlaced)
                viewModel.onCheckoutComplete(message)
                clearCheckoutReturnUrl()
            }
            returnUrl.contains(errorUrlPrefix) -> {
                viewModel.onCheckoutError("Payment failed or was cancelled.")
                clearCheckoutReturnUrl()
            }
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.snackBarMessages.collect { message ->
            launch {
                snackBarHostState.showSnackbar(message)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (checkoutUrl == null) {
            MyOrderContent(
                state = state,
                isPlacingOrder = isPlacingOrder,
                onQuantityChange = { product, quantity -> viewModel.updateQuantity(product, quantity) },
                onExploreCartsClick = onExploreCartsClick,
                onItemClick = { item -> selectedItem = item },
                onPlaceOrderClick = { viewModel.placeOrder() },
            )
        }
        SnackbarHost(hostState = snackBarHostState, modifier = Modifier.align(Alignment.BottomCenter))

        checkoutUrl?.let { url ->
            CheckoutWebView(
                url = url,
                completeUrlPrefix = completeUrlPrefix,
                errorUrlPrefix = errorUrlPrefix,
                onComplete = {
                    coroutineScope.launch {
                        val message = getString(Res.string.strOrderPlaced)
                        viewModel.onCheckoutComplete(message)
                    }
                },
                onError = { message -> viewModel.onCheckoutError(message) },
                onCancel = { viewModel.onCheckoutCancel() },
                modifier = Modifier.fillMaxSize(),
            )
        }

        if (isPlacingOrder) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
    }

    selectedItem?.let { item ->
        EditOrderItemBottomSheet(
            item = item,
            onDismiss = { selectedItem = null },
            onUpdate = { quantity, comment ->
                viewModel.updateItem(item.product, quantity, comment)
                selectedItem = null
            }
        )
    }
}

@Composable
private fun MyOrderContent(
    state: ShoppingCartState,
    isPlacingOrder: Boolean,
    onQuantityChange: (Product, Int) -> Unit,
    onExploreCartsClick: () -> Unit,
    onItemClick: (OrderItem) -> Unit,
    onPlaceOrderClick: () -> Unit,
) {
    if (state.items.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(Spacing.Large.dp)
            ) {
                Text(
                    text = stringResource(Res.string.strNoOpenOrder),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = Spacing.Medium.dp)
                )
                Button(onClick = onExploreCartsClick) {
                    Text(text = stringResource(Res.string.strStartNewOrder))
                }
            }
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize().padding(Spacing.Large.dp)) {
        state.cartName?.let {
            Text(it, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(Spacing.Small.dp))
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(state.items, key = { it.product.name }) { item ->
                OrderItemRow(
                    item = item,
                    onQuantityChange = { quantity -> onQuantityChange(item.product, quantity) },
                    onClick = { onItemClick(item) },
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.Small.dp))
            }
        }

        val total = state.items.sumOf { it.product.price * it.quantity }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = Spacing.Small.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Total", style = MaterialTheme.typography.titleMedium)
            Text(formatPrice(total), style = MaterialTheme.typography.titleMedium)
        }

        Spacer(Modifier.height(Spacing.Small.dp))

        Button(
            onClick = onPlaceOrderClick,
            enabled = !isPlacingOrder,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(Res.string.strPlaceOrder))
        }
    }
}

@Composable
private fun OrderItemRow(
    item: OrderItem,
    onQuantityChange: (Int) -> Unit,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.product.name, style = MaterialTheme.typography.titleMedium)
            Text(formatPrice(item.product.price), style = MaterialTheme.typography.bodyMedium)
            if (item.comment.isNotEmpty()) {
                Text(
                    text = item.comment,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Spacing.XXSmall.dp)
                )
            }
        }
        IconButton(onClick = { onQuantityChange(item.quantity - 1) }) {
            Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease quantity")
        }
        Text("${item.quantity}", style = MaterialTheme.typography.titleMedium)
        IconButton(onClick = { onQuantityChange(item.quantity + 1) }) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Increase quantity")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditOrderItemBottomSheet(
    item: OrderItem,
    onDismiss: () -> Unit,
    onUpdate: (quantity: Int, comment: String) -> Unit,
) {
    var quantity by remember { mutableStateOf(item.quantity) }
    var comment by remember { mutableStateOf(item.comment) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.Large.dp),
        ) {
            if (item.product.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = item.product.imageUrl,
                    contentDescription = item.product.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Spacing.XXXXLarge.dp * 3)
                        .clip(MaterialTheme.shapes.medium),
                    contentScale = ContentScale.Crop,
                )
                Spacer(modifier = Modifier.height(Spacing.Medium.dp))
            }

            Text(item.product.name, style = MaterialTheme.typography.headlineSmall)
            Text(
                text = formatPrice(item.product.price),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = Spacing.Medium.dp)
            )

            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                label = { Text(stringResource(Res.string.strComments)) },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(Spacing.Medium.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { if (quantity > 1) quantity-- }) {
                    Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease quantity")
                }
                Text("$quantity", style = MaterialTheme.typography.titleLarge)
                IconButton(onClick = { quantity++ }) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Increase quantity")
                }
            }

            Spacer(modifier = Modifier.height(Spacing.Medium.dp))

            Button(
                onClick = { onUpdate(quantity, comment) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(Res.string.strUpdateItem))
            }
        }
    }
}

private fun formatPrice(price: Double): String {
    val cents = price.toString().substringAfter(".", "00").padEnd(2, '0').take(2)
    val dollars = price.toString().substringBefore(".")
    return "$$dollars.$cents"
}

@Preview
@Composable
private fun MyOrderScreenEmptyPreview() {
    MyOrderContent(
        state = ShoppingCartState(),
        isPlacingOrder = false,
        onQuantityChange = { _, _ -> },
        onExploreCartsClick = {},
        onItemClick = {},
        onPlaceOrderClick = {},
    )
}

@Preview
@Composable
private fun MyOrderScreenPreview() {
    val latte = Product(
        name = "Caffè Latte",
        price = 4.50,
        description = "Rich espresso with steamed milk and a thin layer of foam.",
        imageUrl = "https://picsum.photos/seed/latte/200"
    )
    val cappuccino = Product(
        name = "Cappuccino",
        price = 4.25,
        description = "Espresso balanced with steamed milk and a thick layer of foam.",
        imageUrl = "https://picsum.photos/seed/capp/200"
    )
    MyOrderContent(
        state = ShoppingCartState(
            cartId = "1",
            cartName = "Downtown Espresso Cart",
            items = listOf(OrderItem(latte, quantity = 2, comment = "Extra hot, oat milk"), OrderItem(cappuccino, quantity = 1)),
        ),
        isPlacingOrder = false,
        onQuantityChange = { _, _ -> },
        onExploreCartsClick = {},
        onItemClick = {},
        onPlaceOrderClick = {},
    )
}
