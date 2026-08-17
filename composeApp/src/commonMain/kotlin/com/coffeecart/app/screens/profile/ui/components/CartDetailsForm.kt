package com.coffeecart.app.screens.profile.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.coffeecart.app.theme.Spacing
import com.coffeecart.app.theme.dp

/**
 * A highly reusable form offering Name/Address input fields and delegating
 * image picker/capturing device operations to CartMediaPicker.
 */
@Composable
fun CartDetailsForm(
    name: String,
    onNameChange: (String) -> Unit,
    address: String,
    onAddressChange: (String) -> Unit,
    imageUrl: String,
    onImageUrlChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.Small.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = address,
            onValueChange = onAddressChange,
            label = { Text("Address") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        CartMediaPicker(
            imageUrl = imageUrl,
            onImageUrlChange = onImageUrlChange
        )
    }
}

@Preview
@Composable
private fun CartDetailsFormPreview() {
    CartDetailsForm(
        name = "Riverside Coffee",
        onNameChange = {},
        address = "789 River Road",
        onAddressChange = {},
        imageUrl = "",
        onImageUrlChange = {}
    )
}
