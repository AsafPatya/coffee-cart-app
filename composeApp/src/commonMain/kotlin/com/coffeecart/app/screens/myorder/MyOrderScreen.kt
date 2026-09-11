package com.coffeecart.app.screens.myorder

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import com.coffeecart.shared.feature.myorder.formatPrice
import com.coffeecart.shared.model.Order
import com.coffeecart.shared.model.OrderItem
import com.coffeecart.shared.model.OrderStatus
import com.coffeecart.shared.model.lineTotal
import com.coffeecart.shared.model.unitPrice
import com.coffeecart.shared.model.PaymentStatus
import com.coffeecart.shared.model.Product
import coffeecart.composeapp.generated.resources.Res
import coffeecart.composeapp.generated.resources.strComments
import coffeecart.composeapp.generated.resources.strNoOpenOrder
import coffeecart.composeapp.generated.resources.strPlaceOrder
import coffeecart.composeapp.generated.resources.strStartNewOrder
import coffeecart.composeapp.generated.resources.strUpdateItem
import kotlinx.coroutines.launch
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
    // A query marker, not a path: the server redirects back to root ("/") rather than a
    // /payments/complete path, since Compose Resources' asset loading (fonts, local images) breaks
    // when the wasmJs SPA loads from a non-root path. Matching by substring keeps this independent
    // of which domain actually serves it (web app vs API server — see webPublicBaseUrl in
    // Application.kt), across QA/prod/local and Android/iOS/web alike.
    val completeUrlPrefix = "payment=complete"
    val errorUrlPrefix = "payment=error"

    // On web, checkout redirects the current tab away and Grow redirects back to complete/error —
    // the app reloads fresh at that URL, so check for it once on startup (no-op on Android/iOS,
    // which embed checkout in a native WebView within the same session).
    LaunchedEffect(Unit) {
        val returnUrl = currentPageUrl()
        when {
            returnUrl.contains(completeUrlPrefix) -> {
                viewModel.onCheckoutSuccess(returnUrl)
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

    val successOrder by viewModel.successOrder.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        if (checkoutUrl == null && successOrder == null) {
            MyOrderContent(
                state = state,
                isPlacingOrder = isPlacingOrder,
                onQuantityChange = { item, quantity -> viewModel.updateQuantity(item.product, quantity, item.selectedOptionIds) },
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
                onComplete = { navigatedUrl -> viewModel.onCheckoutSuccess(navigatedUrl) },
                onError = { message -> viewModel.onCheckoutError(message) },
                onCancel = { viewModel.onCheckoutCancel() },
                modifier = Modifier.fillMaxSize(),
            )
        }

        successOrder?.let { order ->
            PaymentSuccessContent(
                order = order,
                onDismiss = { viewModel.dismissSuccessOrder() },
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
                viewModel.updateItem(item.product, quantity, comment, item.selectedOptionIds)
                selectedItem = null
            }
        )
    }
}

@Composable
private fun PaymentSuccessContent(order: Order, onDismiss: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(Spacing.Large.dp),
        ) {
            Text(
                text = "Payment successful!",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = Spacing.Medium.dp),
            )

            LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                items(order.items, key = { "${it.product.name}|${it.selectedOptionIds.joinToString(",")}" }) { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("${item.quantity}x ${item.product.name}", style = MaterialTheme.typography.bodyLarge)
                        Text(formatPrice(item.lineTotal()), style = MaterialTheme.typography.bodyLarge)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.Small.dp))
                }
            }

            Spacer(Modifier.height(Spacing.Medium.dp))

            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Done")
            }
        }
    }
}

@Composable
private fun MyOrderContent(
    state: ShoppingCartState,
    isPlacingOrder: Boolean,
    onQuantityChange: (OrderItem, Int) -> Unit,
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
            items(state.items, key = { "${it.product.name}|${it.selectedOptionIds.joinToString(",")}" }) { item ->
                OrderItemRow(
                    item = item,
                    onQuantityChange = { quantity -> onQuantityChange(item, quantity) },
                    onClick = { onItemClick(item) },
                )
                Spacer(modifier = Modifier.padding(vertical = Spacing.Small.dp))
            }
        }

        val total = state.items.sumOf { it.lineTotal() }
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
            .shadow(
                elevation = Spacing.Small.dp,
                shape = MaterialTheme.shapes.large,
                clip = false
            )
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(Spacing.Medium.dp)
,
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.product.name, style = MaterialTheme.typography.titleMedium)
            if (item.product.description.isNotEmpty()) {
                Text(
                    text = item.product.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (item.comment.isNotEmpty()) {
                Text(
                    text = item.comment,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Spacing.XXSmall.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = Spacing.Small.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(formatPrice(item.unitPrice()), style = MaterialTheme.typography.bodyMedium)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Composed in reverse of the intended left-to-right visual order (+, count, −, 🗑) —
                    // the app is RTL, and Row mirrors child placement under RTL layout direction.
                    IconButton(onClick = { onQuantityChange(0) }) {
                        Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Remove item")
                    }
                    Spacer(modifier = Modifier.width(Spacing.Small.dp))
                    FilledTonalIconButton(
                        shape = MaterialTheme.shapes.small,
                        onClick = { onQuantityChange(item.quantity - 1) }
                    ) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease quantity")
                    }
                    Text(
                        "${item.quantity}",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = Spacing.Small.dp),
                    )
                    FilledTonalIconButton(
                        shape = MaterialTheme.shapes.small,
                        onClick = { onQuantityChange(item.quantity + 1) }
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Increase quantity")
                    }
                }
            }
        }

        if (item.product.imageUrl.isNotEmpty()) {
            Spacer(modifier = Modifier.width(Spacing.Medium.dp))
            AsyncImage(
                model = item.product.imageUrl,
                contentDescription = item.product.name,
                modifier = Modifier
                    .size(Spacing.XXXXXLarge.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .align(Alignment.CenterVertically),
                contentScale = ContentScale.Crop,
            )
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
                text = formatPrice(item.unitPrice()),
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

@Preview
@Composable
private fun MyOrderScreenEmptyPreview() {
    Box(modifier = Modifier.background(Color.White).fillMaxSize()) {
        MyOrderContent(
            state = ShoppingCartState(),
            isPlacingOrder = false,
            onQuantityChange = { _, _ -> },
            onExploreCartsClick = {},
            onItemClick = {},
            onPlaceOrderClick = {},
        )
    }
}

@Preview(locale = "iw")
@Composable
private fun MyOrderScreenPreview() {
    val latte = Product(
        name = "לאטה",
        price = 4.50,
        description = "אספרסו עשיר עם חלב מוקצף ושכבה דקה של קצף",
        imageUrl = "https://picsum.photos/seed/latte/200"
    )
    val cappuccino = Product(
        name = "קפוצ'ינו",
        price = 4.25,
        description = "אספרסו מאוזן עם חלב מוקצף ושכבה עבה של קצף",
        imageUrl = "https://picsum.photos/seed/capp/200"
    )
    Box(modifier = Modifier.background(Color.White).fillMaxSize()) {
        MyOrderContent(
            state = ShoppingCartState(
                cartId = "1",
                cartName = "עגלת אספרסו במרכז העיר",
                items = listOf(
                    OrderItem(latte, quantity = 2, comment = "חם מאוד, חלב שיבולת שועל"),
                    OrderItem(cappuccino, quantity = 1)
                ),
            ),
            isPlacingOrder = false,
            onQuantityChange = { _, _ -> },
            onExploreCartsClick = {},
            onItemClick = {},
            onPlaceOrderClick = {},
        )
    }
}

@Preview(locale = "iw")
@Composable
private fun PaymentSuccessContentPreview() {
    val latte = Product(
        name = "לאטה",
        price = 4.50,
        description = "אספרסו עשיר עם חלב מוקצף ושכבה דקה של קצף",
        imageUrl = "https://picsum.photos/seed/latte/200"
    )
    val cappuccino = Product(
        name = "קפוצ'ינו",
        price = 4.25,
        description = "אספרסו מאוזן עם חלב מוקצף ושכבה עבה של קצף",
        imageUrl = "https://picsum.photos/seed/capp/200"
    )
    Box(modifier = Modifier.background(Color.White).fillMaxSize()) {
        PaymentSuccessContent(
            order = Order(
                id = "preview-order-id",
                cartId = "1",
                items = listOf(
                    OrderItem(latte, quantity = 2, comment = "חם מאוד, חלב שיבולת שועל"),
                    OrderItem(cappuccino, quantity = 1),
                ),
                status = OrderStatus.ARRIVED,
                createdAt = 0L,
                paymentStatus = PaymentStatus.PAID,
            ),
            onDismiss = {},
        )
    }
}
