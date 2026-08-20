package com.coffeecart.app.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.rememberCoroutineScope
import com.coffeecart.app.screens.profile.ui.components.AddCartBottomSheet
import com.coffeecart.app.screens.profile.ui.components.CartSelectionBottomSheet
import com.coffeecart.app.screens.profile.ui.components.RemoveCartDialog
import com.coffeecart.app.screens.profile.ui.components.ResultDialog
import com.coffeecart.app.screens.profile.ui.components.ShowCartsBottomSheet
import com.coffeecart.app.theme.Spacing
import com.coffeecart.app.theme.dp
import com.coffeecart.app.ui.payment.CheckoutWebView
import com.coffeecart.shared.domain.PaymentRepository
import com.coffeecart.shared.feature.profile.ProfileViewModel
import com.coffeecart.shared.model.CoffeeCart
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

enum class ProfileAction {
    NONE, EDIT, REMOVE, ADD_CATEGORY, VIEW_ORDERS, CONNECT_PAYMENT_ACCOUNT
}

/** Screen enabling operational calls (GET/POST/DELETE) for coffee carts. */
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = koinInject(),
    paymentRepository: PaymentRepository = koinInject(),
    onAddCategoryClick: (String) -> Unit,
    onEditCartClick: (String) -> Unit,
    onViewOrdersClick: (String) -> Unit,
) {
    val dialogMessage by viewModel.dialogMessage.collectAsState()
    val cartsList by viewModel.cartsList.collectAsState()
    var onboardingUrl by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }

    Box(modifier = Modifier.fillMaxSize()) {
        ProfileContent(
            dialogMessage = dialogMessage,
            cartsList = cartsList,
            onGetClick = { viewModel.loadCarts() },
            onConfirmAdd = { name, address, imageUrl ->
                viewModel.addCoffeeCart(name, address, imageUrl)
            },
            onConfirmEdit = { id, name, address, imageUrl, latitude, longitude ->
                viewModel.editCoffeeCart(id, name, address, imageUrl, latitude, longitude)
            },
            onConfirmDelete = { id -> viewModel.removeCoffeeCart(id) },
            onDismissDialog = { viewModel.dismissDialog() },
            onAddCategoryClick = onAddCategoryClick,
            onEditCartClick = onEditCartClick,
            onViewOrdersClick = onViewOrdersClick,
            onConnectPaymentAccountClick = { cartId ->
                coroutineScope.launch {
                    try {
                        onboardingUrl = paymentRepository.connectPaymentAccount(cartId)
                    } catch (e: Exception) {
                        snackBarHostState.showSnackbar(e.message ?: "Failed to connect payment account.")
                    }
                }
            },
        )
        SnackbarHost(hostState = snackBarHostState, modifier = Modifier.align(Alignment.BottomCenter))

        onboardingUrl?.let { url ->
            CheckoutWebView(
                url = url,
                completeUrlPrefix = "unused",
                errorUrlPrefix = "unused",
                onComplete = { onboardingUrl = null },
                onError = { onboardingUrl = null },
                onCancel = { onboardingUrl = null },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileContent(
    dialogMessage: String?,
    cartsList: List<CoffeeCart>,
    onGetClick: () -> Unit,
    onConfirmAdd: (String, String, String) -> Unit,
    onConfirmEdit: (String, String, String, String, Double?, Double?) -> Unit,
    onConfirmDelete: (String) -> Unit,
    onDismissDialog: () -> Unit,
    onAddCategoryClick: (String) -> Unit,
    onEditCartClick: (String) -> Unit,
    onViewOrdersClick: (String) -> Unit,
    onConnectPaymentAccountClick: (String) -> Unit,
) {
    var activeAction by remember { mutableStateOf(ProfileAction.NONE) }
    var selectedCartForDelete by remember { mutableStateOf<CoffeeCart?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showCartsDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(Spacing.XXLarge.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Button(
                onClick = {
                    onGetClick()
                    showCartsDialog = true
                },
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
                onClick = {
                    onGetClick()
                    activeAction = ProfileAction.EDIT
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.Small.dp)
            ) {
                Text("Edit Coffee Cart")
            }

            Button(
                onClick = {
                    onGetClick()
                    activeAction = ProfileAction.REMOVE
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.Small.dp)
            ) {
                Text("Remove Coffee Cart")
            }

            Text(
                modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.Small.dp),
                text = "Relevant for Coffee Carts Owners"
            )

            Button(
                onClick = {
                    onGetClick()
                    activeAction = ProfileAction.ADD_CATEGORY
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.Small.dp)
            ) {
                Text("Add New Category")
            }

            Button(
                onClick = {
                    onGetClick()
                    activeAction = ProfileAction.VIEW_ORDERS
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.Small.dp)
            ) {
                Text("View Orders")
            }

            Button(
                onClick = {
                    onGetClick()
                    activeAction = ProfileAction.CONNECT_PAYMENT_ACCOUNT
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.Small.dp)
            ) {
                Text("Connect Payment Account")
            }
        }

        if (dialogMessage != null) {
            ResultDialog(
                message = dialogMessage,
                onDismiss = onDismissDialog
            )
        }

        if (showCartsDialog) {
            ShowCartsBottomSheet(
                cartsList = cartsList,
                onDismiss = { showCartsDialog = false }
            )
        }

        if (activeAction != ProfileAction.NONE) {
            CartSelectionBottomSheet(
                cartsList = cartsList,
                onDismiss = { activeAction = ProfileAction.NONE },
                onCartSelected = { cart ->
                    val lastAction = activeAction
                    activeAction = ProfileAction.NONE
                    when (lastAction) {
                        ProfileAction.EDIT -> onEditCartClick(cart.id)
                        ProfileAction.REMOVE -> selectedCartForDelete = cart
                        ProfileAction.ADD_CATEGORY -> onAddCategoryClick(cart.id)
                        ProfileAction.VIEW_ORDERS -> onViewOrdersClick(cart.id)
                        ProfileAction.CONNECT_PAYMENT_ACCOUNT -> onConnectPaymentAccountClick(cart.id)
                        else -> {}
                    }
                }
            )
        }

        if (showAddDialog) {
            AddCartBottomSheet(
                onDismiss = { showAddDialog = false },
                onConfirm = { name, address, imageUrl ->
                    onConfirmAdd(name, address, imageUrl)
                    showAddDialog = false
                }
            )
        }


        if (selectedCartForDelete != null) {
            RemoveCartDialog(
                cart = selectedCartForDelete!!,
                onDismiss = { selectedCartForDelete = null },
                onConfirm = {
                    onConfirmDelete(selectedCartForDelete!!.id)
                    selectedCartForDelete = null
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
        cartsList = emptyList(),
        onGetClick = {},
        onConfirmAdd = { _, _, _ -> },
        onConfirmEdit = { _, _, _, _, _, _ -> },
        onConfirmDelete = {},
        onDismissDialog = {},
        onAddCategoryClick = {},
        onEditCartClick = {},
        onViewOrdersClick = {},
        onConnectPaymentAccountClick = {}
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
        onConfirmEdit = { _, _, _, _, _, _ -> },
        onConfirmDelete = {},
        onDismissDialog = {},
        onAddCategoryClick = {},
        onEditCartClick = {},
        onViewOrdersClick = {},
        onConnectPaymentAccountClick = {}
    )
}
