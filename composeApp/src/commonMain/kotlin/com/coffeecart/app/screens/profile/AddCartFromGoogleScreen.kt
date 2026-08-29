package com.coffeecart.app.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.coffeecart.app.screens.profile.ui.components.CartDetailsForm
import com.coffeecart.app.theme.Spacing
import com.coffeecart.app.theme.dp
import com.coffeecart.shared.domain.CoffeeCartRepositoryInterface
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun AddCartFromGoogleScreen(
    repository: CoffeeCartRepositoryInterface = koinInject(),
    onSuccess: () -> Unit,
    onConfirmAdd: (name: String, address: String, imageUrl: String, placeId: String?) -> Unit,
) {
    var placeIdInput by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var openingHours by remember { mutableStateOf<List<String>>(emptyList()) }
    var isFetching by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isFetchedSuccess by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

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
                    onValueChange = { placeIdInput = it },
                    label = { Text("Google Place ID") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = {
                        if (placeIdInput.isNotBlank()) {
                            isFetching = true
                            statusMessage = null
                            coroutineScope.launch {
                                try {
                                    val details = repository.fetchPlaceDetails(placeIdInput)
                                    if (details != null) {
                                        details.name?.let { name = it }
                                        details.formattedAddress?.let { address = it }
                                        details.photoUrls.firstOrNull()?.let { imageUrl = it }
                                        openingHours = details.openingHours
                                        isFetchedSuccess = true
                                        statusMessage = "Successfully fetched Google Place info!"
                                    } else {
                                        statusMessage = "Failed to find details for Place ID."
                                    }
                                } catch (e: Exception) {
                                    statusMessage = "Error fetching details: ${e.message}"
                                } finally {
                                    isFetching = false
                                }
                            }
                        }
                    },
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

            if (openingHours.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(Spacing.Medium.dp)) {
                        Text(
                            text = "Fetched Opening Hours:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(Spacing.XXSmall.dp))
                        Text(
                            text = openingHours.joinToString("\n"),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.Small.dp))

            Text(
                text = "Cart Details (Modify if needed)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            CartDetailsForm(
                name = name,
                onNameChange = { name = it },
                address = address,
                onAddressChange = { address = it },
                imageUrl = imageUrl,
                onImageUrlChange = { imageUrl = it },
                placeId = placeIdInput,
                onPlaceIdChange = { placeIdInput = it }
            )

            Spacer(modifier = Modifier.height(Spacing.Large.dp))

            Button(
                onClick = {
                    if (name.isNotBlank() && address.isNotBlank()) {
                        onConfirmAdd(name.trim(), address.trim(), imageUrl.trim(), placeIdInput.trim().ifBlank { null })
                        onSuccess()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && address.isNotBlank()
            ) {
                Text("Save Coffee Cart")
            }
        }
    }
}

@Preview
@Composable
private fun AddCartFromGoogleScreenPreview() {
    AddCartFromGoogleScreen(
        onSuccess = {},
        onConfirmAdd = { _, _, _, _ -> }
    )
}



