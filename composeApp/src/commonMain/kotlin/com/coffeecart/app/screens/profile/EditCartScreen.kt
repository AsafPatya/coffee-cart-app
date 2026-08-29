package com.coffeecart.app.screens.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.coffeecart.app.screens.profile.ui.components.CartDetailsForm
import com.coffeecart.app.theme.Spacing
import com.coffeecart.app.theme.dp
import com.coffeecart.app.ui.location.UserLocation
import com.coffeecart.app.ui.location.rememberCurrentLocation
import com.coffeecart.app.ui.location.rememberLocationPicker
import com.coffeecart.shared.feature.cartdetails.CoffeeCartDetailsUiState
import com.coffeecart.shared.feature.cartdetails.CoffeeCartDetailsViewModel
import com.coffeecart.shared.model.CoffeeCart
import org.koin.compose.koinInject

@Composable
internal fun EditCartScreen(
    cartId: String,
    onDismiss: () -> Unit,
    onConfirm: (id: String, name: String, address: String, imageUrl: String, placeId: String?, latitude: Double?, longitude: Double?) -> Unit,
    detailsViewModel: CoffeeCartDetailsViewModel = koinInject(),
) {
    val uiState by detailsViewModel.uiState.collectAsState()

    LaunchedEffect(cartId) {
        detailsViewModel.loadCart(cartId)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        when (val state = uiState) {
            is CoffeeCartDetailsUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is CoffeeCartDetailsUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Error: ${state.message}",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            is CoffeeCartDetailsUiState.Success -> {
                EditCartScreenContent(
                    cart = state.cart,
                    onDismiss = onDismiss,
                    onConfirm = onConfirm,
                )
            }
        }
    }
}

@Composable
private fun EditCartScreenContent(
    cart: CoffeeCart,
    onDismiss: () -> Unit,
    onConfirm: (id: String, name: String, address: String, imageUrl: String, placeId: String?, latitude: Double?, longitude: Double?) -> Unit,
) {
    var nameInput by remember { mutableStateOf(cart.name) }
    var addressInput by remember { mutableStateOf(cart.address) }
    var imageUrlInput by remember { mutableStateOf(cart.imageUrl) }
    var placeIdInput by remember { mutableStateOf(cart.placeId.orEmpty()) }
    var latitude by remember { mutableStateOf(cart.latitude) }
    var longitude by remember { mutableStateOf(cart.longitude) }

    val cartLatitude = latitude
    val cartLongitude = longitude
    val initialLocation = if (cartLatitude != null && cartLongitude != null) {
        UserLocation(cartLatitude, cartLongitude)
    } else {
        rememberCurrentLocation()
    }
    val launchLocationPicker = rememberLocationPicker(
        initialLocation = initialLocation,
        onPicked = { lat, lng ->
            latitude = lat
            longitude = lng
        },
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.Large.dp),
        verticalArrangement = Arrangement.spacedBy(Spacing.Large.dp)
    ) {
        CartDetailsForm(
            name = nameInput,
            onNameChange = { nameInput = it },
            address = addressInput,
            onAddressChange = { addressInput = it },
            imageUrl = imageUrlInput,
            onImageUrlChange = { imageUrlInput = it },
            placeId = placeIdInput,
            onPlaceIdChange = { placeIdInput = it },
        )

        if (launchLocationPicker != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.8f)
                    .clickable { launchLocationPicker() },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(Spacing.Medium.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(Spacing.XXXXLarge.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(Spacing.Small.dp))
                        Text(
                            text = if (latitude != null && longitude != null) {
                                "Location: ${((latitude ?: 0.0) * 10000).toInt() / 10000.0}, ${((longitude ?: 0.0) * 10000).toInt() / 10000.0}"
                            } else {
                                "Location: not set"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(Spacing.XXSmall.dp))
                        Text(
                            text = "Tap to update location on map",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        Button(
            enabled = nameInput.trim().isNotEmpty() && addressInput.trim().isNotEmpty() && imageUrlInput.isNotEmpty(),
            onClick = {
                onConfirm(cart.id, nameInput, addressInput, imageUrlInput, placeIdInput.trim().ifEmpty { null }, latitude, longitude)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Changes")
        }
    }
}

@Preview
@Composable
private fun EditCartScreenPreview() {
    EditCartScreenContent(
        cart = CoffeeCart("1", "Downtown Espresso Cart", "123 Main St", ""),
        onDismiss = {},
        onConfirm = { _, _, _, _, _, _, _ -> }
    )
}

