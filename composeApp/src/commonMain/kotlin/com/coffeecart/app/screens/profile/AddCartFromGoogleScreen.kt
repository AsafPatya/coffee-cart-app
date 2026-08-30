package com.coffeecart.app.screens.profile

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import coil3.compose.AsyncImage
import com.coffeecart.app.screens.profile.ui.components.CartDetailsForm
import com.coffeecart.app.theme.Spacing
import com.coffeecart.app.theme.dp
import com.coffeecart.app.ui.location.CoffeeCartMap
import com.coffeecart.shared.contract.PlaceDetailsDto
import com.coffeecart.shared.feature.profile.AddCartFromGoogleViewModel
import org.koin.compose.koinInject

@Composable
fun AddCartFromGoogleScreen(
    viewModel: AddCartFromGoogleViewModel = koinInject(),
    onSuccess: () -> Unit,
) {
    val placeIdInput by viewModel.placeIdInput.collectAsState()
    val name by viewModel.name.collectAsState()
    val address by viewModel.address.collectAsState()
    val imageUrl by viewModel.imageUrl.collectAsState()
    val fetchedDetails by viewModel.fetchedDetails.collectAsState()
    val isFetching by viewModel.isFetching.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val isFetchedSuccess by viewModel.isFetchedSuccess.collectAsState()

    AddCartFromGoogleContent(
        placeIdInput = placeIdInput,
        onPlaceIdInputChange = { viewModel.updatePlaceIdInput(it) },
        name = name,
        onNameChange = { viewModel.updateName(it) },
        address = address,
        onAddressChange = { viewModel.updateAddress(it) },
        imageUrl = imageUrl,
        onImageUrlChange = { viewModel.updateImageUrl(it) },
        fetchedDetails = fetchedDetails,
        isFetching = isFetching,
        statusMessage = statusMessage,
        isFetchedSuccess = isFetchedSuccess,
        onFetchClick = { viewModel.fetchPlaceDetails() },
        onSaveClick = {
            viewModel.saveCoffeeCart(onSuccess)
        }
    )
}

@Composable
fun AddCartFromGoogleContent(
    placeIdInput: String,
    onPlaceIdInputChange: (String) -> Unit,
    name: String,
    onNameChange: (String) -> Unit,
    address: String,
    onAddressChange: (String) -> Unit,
    imageUrl: String,
    onImageUrlChange: (String) -> Unit,
    fetchedDetails: PlaceDetailsDto?,
    isFetching: Boolean,
    statusMessage: String?,
    isFetchedSuccess: Boolean,
    onFetchClick: () -> Unit,
    onSaveClick: () -> Unit,
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(Spacing.Large.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.Medium.dp)
        ) {
            Text(
                text = "Add Coffee Cart with Google Info",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = "Enter a Google Place ID to fetch details (Name, Address, Opening Hours, Location, Photos) automatically.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.Small.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = placeIdInput,
                    onValueChange = onPlaceIdInputChange,
                    label = { Text("Google Place ID") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = onFetchClick,
                    enabled = !isFetching && placeIdInput.isNotBlank()
                ) {
                    if (isFetching) {
                        CircularProgressIndicator(modifier = Modifier.padding(Spacing.XXSmall.dp))
                    } else {
                        Text("Fetch")
                    }
                }
            }

            statusMessage?.let { msg ->
                Text(
                    text = msg,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isFetchedSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }

            fetchedDetails?.let { details ->
                FetchedPlaceDetailsCard(details = details)
            }

            Spacer(modifier = Modifier.height(Spacing.Small.dp))

            Text(
                text = "Cart Details (Modify if needed)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            CartDetailsForm(
                name = name,
                onNameChange = onNameChange,
                address = address,
                onAddressChange = onAddressChange,
                imageUrl = imageUrl,
                onImageUrlChange = onImageUrlChange,
                placeId = placeIdInput,
                onPlaceIdChange = onPlaceIdInputChange
            )

            Spacer(modifier = Modifier.height(Spacing.Large.dp))

            Button(
                onClick = onSaveClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && address.isNotBlank()
            ) {
                Text("Save Coffee Cart")
            }
        }
    }
}

@Composable
fun FetchedPlaceDetailsCard(
    details: PlaceDetailsDto,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.Medium.dp),
            verticalArrangement = Arrangement.spacedBy(Spacing.Small.dp)
        ) {
            Text(
                text = "Fetched Google Place Details:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            details.name?.takeIf { it.isNotBlank() }?.let { name ->
                Text(
                    text = "Name: $name",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            details.formattedAddress?.takeIf { it.isNotBlank() }?.let { address ->
                Text(
                    text = "Address: $address",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            details.phoneNumber?.takeIf { it.isNotBlank() }?.let { phone ->
                Text(
                    text = "Phone: $phone",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (details.rating != null) {
                val totalRatingsText = details.userRatingsTotal?.let { " ($it ratings)" } ?: ""
                Text(
                    text = "Rating: ${details.rating} ⭐$totalRatingsText",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            details.website?.takeIf { it.isNotBlank() }?.let { website ->
                val fullUrl = if (website.startsWith("http://") || website.startsWith("https://")) {
                    website
                } else {
                    "https://$website"
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Website: ",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = website,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline
                        ),
                        modifier = Modifier.clickable {
                            try {
                                uriHandler.openUri(fullUrl)
                            } catch (_: Exception) {
                            }
                        }
                    )
                }
            }

            if (details.latitude != null && details.longitude != null) {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.XXSmall.dp)) {
                    Text(
                        text = "Location: Lat ${details.latitude}, Lng ${details.longitude}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    val mapShape = RoundedCornerShape(Spacing.Small.dp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Spacing.MapHeight.dp)
                            .clip(mapShape)
                    ) {
                        CoffeeCartMap(
                            latitude = details.latitude!!,
                            longitude = details.longitude!!,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            if (details.photoUrls.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.XXSmall.dp)) {
                    Text(
                        text = "Photos (${details.photoUrls.size}):",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.Small.dp)) {
                        details.photoUrls.chunked(2).forEach { rowPhotos ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(Spacing.Small.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                rowPhotos.forEach { photoUrl ->
                                    AsyncImage(
                                        model = photoUrl,
                                        contentDescription = "Place Photo",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(Spacing.CategoryCardHeight.dp)
                                            .clip(RoundedCornerShape(Spacing.Small.dp))
                                    )
                                }
                                if (rowPhotos.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            if (details.openingHours.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.XXSmall.dp)) {
                    Text(
                        text = "Opening Hours:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = details.openingHours.joinToString("\n"),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun AddCartFromGoogleScreenPreview() {
    AddCartFromGoogleContent(
        placeIdInput = "",
        onPlaceIdInputChange = {},
        name = "Central Coffee Cart",
        onNameChange = {},
        address = "123 Main St",
        onAddressChange = {},
        imageUrl = "https://example.com/image.jpg",
        onImageUrlChange = {},
        fetchedDetails = null,
        isFetching = false,
        statusMessage = null,
        isFetchedSuccess = false,
        onFetchClick = {},
        onSaveClick = {}
    )
}

@Preview
@Composable
private fun FetchedPlaceDetailsCardPreview() {
    FetchedPlaceDetailsCard(
        details = PlaceDetailsDto(
            name = "Central Coffee Cart",
            formattedAddress = "123 Main St, Springfield",
            phoneNumber = "+1 555-0199",
            latitude = 37.7749,
            longitude = -122.4194,
            rating = 4.8,
            userRatingsTotal = 156,
            website = "https://example.com/coffeecart",
            photoUrls = listOf("https://example.com/photo1.jpg", "https://example.com/photo2.jpg"),
            openingHours = listOf("Monday: 8:00 AM – 5:00 PM", "Tuesday: 8:00 AM – 5:00 PM")
        )
    )
}




